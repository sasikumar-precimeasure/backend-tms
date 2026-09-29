package com.tmsbackend.infrastructure.persistence.adapter;

import com.tmsbackend.domain.model.MailRecipient;
import com.tmsbackend.domain.model.MailSenderSettings;
import com.tmsbackend.domain.model.MailThresholds;
import com.tmsbackend.domain.port.MailSettingsRepositoryPort;
import com.tmsbackend.infrastructure.mail.MailPasswordEncryptor;
import com.tmsbackend.infrastructure.persistence.entity.MailLastSentEntity;
import com.tmsbackend.infrastructure.persistence.entity.MailLastSentId;
import com.tmsbackend.infrastructure.persistence.entity.MailRecipientEntity;
import com.tmsbackend.infrastructure.persistence.entity.MailSenderSettingsEntity;
import com.tmsbackend.infrastructure.persistence.entity.MailThresholdsEntity;
import com.tmsbackend.infrastructure.persistence.repository.MailLastSentJpaRepository;
import com.tmsbackend.infrastructure.persistence.repository.MailRecipientJpaRepository;
import com.tmsbackend.infrastructure.persistence.repository.MailSenderSettingsJpaRepository;
import com.tmsbackend.infrastructure.persistence.repository.MailThresholdsJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class MailSettingsRepositoryAdapter implements MailSettingsRepositoryPort {
    private static final int SENDER_SETTINGS_ID = 1;

    private final MailSenderSettingsJpaRepository senderSettingsJpaRepository;
    private final MailRecipientJpaRepository recipientJpaRepository;
    private final MailThresholdsJpaRepository thresholdsJpaRepository;
    private final MailLastSentJpaRepository lastSentJpaRepository;
    private final MailPasswordEncryptor passwordEncryptor;

    public MailSettingsRepositoryAdapter(
            MailSenderSettingsJpaRepository senderSettingsJpaRepository,
            MailRecipientJpaRepository recipientJpaRepository,
            MailThresholdsJpaRepository thresholdsJpaRepository,
            MailLastSentJpaRepository lastSentJpaRepository,
            MailPasswordEncryptor passwordEncryptor) {
        this.senderSettingsJpaRepository = senderSettingsJpaRepository;
        this.recipientJpaRepository = recipientJpaRepository;
        this.thresholdsJpaRepository = thresholdsJpaRepository;
        this.lastSentJpaRepository = lastSentJpaRepository;
        this.passwordEncryptor = passwordEncryptor;
    }

    @Override
    public MailSenderSettings getSenderSettings() {
        MailSenderSettingsEntity entity = senderSettingsJpaRepository.findById(SENDER_SETTINGS_ID)
                .orElseGet(() -> {
                    MailSenderSettingsEntity fresh = new MailSenderSettingsEntity();
                    fresh.setId(SENDER_SETTINGS_ID);
                    return fresh;
                });
        return new MailSenderSettings(
                entity.getSenderName(), entity.getSenderEmail(), entity.getSmtpHost(), entity.getSmtpPort(),
                passwordEncryptor.decrypt(entity.getPasswordEncrypted()), entity.isEnableSsl());
    }

    @Override
    public void saveSenderSettings(MailSenderSettings settings) {
        MailSenderSettingsEntity entity = senderSettingsJpaRepository.findById(SENDER_SETTINGS_ID)
                .orElseGet(() -> {
                    MailSenderSettingsEntity fresh = new MailSenderSettingsEntity();
                    fresh.setId(SENDER_SETTINGS_ID);
                    return fresh;
                });
        entity.setSenderName(settings.senderName());
        entity.setSenderEmail(settings.senderEmail());
        entity.setSmtpHost(settings.smtpHost());
        entity.setSmtpPort(settings.smtpPort());
        entity.setEnableSsl(settings.enableSsl());
        // Only re-encrypt/overwrite if a new password was actually
        // provided - an empty string means "keep the existing one" (the
        // frontend never displays the real password back, so it can't
        // round-trip it unchanged otherwise).
        if (settings.password() != null && !settings.password().isEmpty()) {
            entity.setPasswordEncrypted(passwordEncryptor.encrypt(settings.password()));
        }
        senderSettingsJpaRepository.save(entity);
    }

    @Override
    public List<MailRecipient> findAllRecipients() {
        return recipientJpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public MailRecipient saveRecipient(MailRecipient recipient) {
        String id = recipient.id() != null && !recipient.id().isBlank() ? recipient.id() : UUID.randomUUID().toString();
        MailRecipientEntity entity = recipientJpaRepository.findById(id).orElseGet(MailRecipientEntity::new);
        entity.setId(id);
        entity.setName(recipient.name());
        entity.setEmail(recipient.email());
        entity.setEnabled(recipient.enabled());
        entity.setDeviceIds(recipient.deviceIds());
        MailRecipientEntity saved = recipientJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteRecipient(String id) {
        recipientJpaRepository.deleteById(id);
    }

    @Override
    public Optional<MailThresholds> findThresholds(String deviceId) {
        return thresholdsJpaRepository.findById(deviceId).map(this::toDomain);
    }

    @Override
    public void saveThresholds(MailThresholds thresholds) {
        MailThresholdsEntity entity = thresholdsJpaRepository.findById(thresholds.deviceId()).orElseGet(MailThresholdsEntity::new);
        entity.setDeviceId(thresholds.deviceId());
        entity.setOtiTempHigh(thresholds.otiTempHigh());
        entity.setWtiTempHigh(thresholds.wtiTempHigh());
        entity.setAvrHigh(thresholds.avrHigh());
        entity.setAvrLow(thresholds.avrLow());
        entity.setTapHigh(thresholds.tapHigh());
        entity.setTapLow(thresholds.tapLow());
        entity.setMailTimeMinutes(thresholds.mailTimeMinutes());
        thresholdsJpaRepository.save(entity);
    }

    @Override
    public Optional<Instant> findLastSent(String deviceId, String conditionKey) {
        return lastSentJpaRepository.findById(new MailLastSentId(deviceId, conditionKey)).map(MailLastSentEntity::getLastSentAt);
    }

    @Override
    public void recordSent(String deviceId, String conditionKey, Instant when) {
        MailLastSentId id = new MailLastSentId(deviceId, conditionKey);
        MailLastSentEntity entity = lastSentJpaRepository.findById(id).orElseGet(MailLastSentEntity::new);
        entity.setDeviceId(deviceId);
        entity.setConditionKey(conditionKey);
        entity.setLastSentAt(when);
        lastSentJpaRepository.save(entity);
    }

    private MailRecipient toDomain(MailRecipientEntity entity) {
        return new MailRecipient(entity.getId(), entity.getName(), entity.getEmail(), entity.isEnabled(), entity.getDeviceIds());
    }

    private MailThresholds toDomain(MailThresholdsEntity entity) {
        return new MailThresholds(
                entity.getDeviceId(), entity.getOtiTempHigh(), entity.getWtiTempHigh(), entity.getAvrHigh(),
                entity.getAvrLow(), entity.getTapHigh(), entity.getTapLow(), entity.getMailTimeMinutes());
    }
}
