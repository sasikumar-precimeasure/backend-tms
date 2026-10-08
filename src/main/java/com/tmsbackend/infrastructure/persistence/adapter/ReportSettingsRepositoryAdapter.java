package com.tmsbackend.infrastructure.persistence.adapter;

import com.tmsbackend.domain.model.ReportRecipient;
import com.tmsbackend.domain.model.ReportSettings;
import com.tmsbackend.domain.port.ReportSettingsRepositoryPort;
import com.tmsbackend.infrastructure.persistence.entity.ReportRecipientEntity;
import com.tmsbackend.infrastructure.persistence.entity.ReportSettingsEntity;
import com.tmsbackend.infrastructure.persistence.repository.ReportRecipientJpaRepository;
import com.tmsbackend.infrastructure.persistence.repository.ReportSettingsJpaRepository;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ReportSettingsRepositoryAdapter implements ReportSettingsRepositoryPort {
    private static final int SETTINGS_ID = 1;

    private final ReportSettingsJpaRepository settingsJpaRepository;
    private final ReportRecipientJpaRepository recipientJpaRepository;

    public ReportSettingsRepositoryAdapter(
            ReportSettingsJpaRepository settingsJpaRepository, ReportRecipientJpaRepository recipientJpaRepository) {
        this.settingsJpaRepository = settingsJpaRepository;
        this.recipientJpaRepository = recipientJpaRepository;
    }

    private ReportSettingsEntity loadEntity() {
        return settingsJpaRepository.findById(SETTINGS_ID).orElseGet(() -> {
            ReportSettingsEntity fresh = new ReportSettingsEntity();
            fresh.setId(SETTINGS_ID);
            return fresh;
        });
    }

    @Override
    public ReportSettings getSettings() {
        ReportSettingsEntity e = loadEntity();
        return new ReportSettings(
                e.isEnabled(), e.getDayOfMonth(), e.getSendTime(), e.getTimezone(), e.getLastSentPeriod(), e.getLastSentAt());
    }

    @Override
    public void saveSettings(ReportSettings settings) {
        ReportSettingsEntity e = loadEntity();
        e.setEnabled(settings.enabled());
        e.setDayOfMonth(settings.dayOfMonth());
        e.setSendTime(settings.sendTime());
        e.setTimezone(settings.timezone());
        settingsJpaRepository.save(e);
    }

    @Override
    public void markSent(String period, Instant when) {
        ReportSettingsEntity e = loadEntity();
        e.setLastSentPeriod(period);
        e.setLastSentAt(when);
        settingsJpaRepository.save(e);
    }

    @Override
    public List<ReportRecipient> findAllRecipients() {
        return recipientJpaRepository.findAll().stream()
                .sorted(Comparator.comparing(ReportRecipientEntity::getName, String.CASE_INSENSITIVE_ORDER))
                .map(e -> new ReportRecipient(e.getId(), e.getName(), e.getEmail(), e.isEnabled()))
                .toList();
    }

    @Override
    public ReportRecipient saveRecipient(ReportRecipient recipient) {
        String id = recipient.id() != null && !recipient.id().isBlank() ? recipient.id() : UUID.randomUUID().toString();
        ReportRecipientEntity e = recipientJpaRepository.findById(id).orElseGet(ReportRecipientEntity::new);
        e.setId(id);
        e.setName(recipient.name());
        e.setEmail(recipient.email());
        e.setEnabled(recipient.enabled());
        recipientJpaRepository.save(e);
        return new ReportRecipient(id, e.getName(), e.getEmail(), e.isEnabled());
    }

    @Override
    public void deleteRecipient(String id) {
        recipientJpaRepository.deleteById(id);
    }
}
