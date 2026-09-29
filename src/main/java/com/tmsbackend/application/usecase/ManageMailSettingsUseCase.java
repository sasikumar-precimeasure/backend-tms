package com.tmsbackend.application.usecase;

import com.tmsbackend.application.audit.Auditable;
import com.tmsbackend.domain.model.AuditEventType;
import com.tmsbackend.domain.model.MailRecipient;
import com.tmsbackend.domain.model.MailSenderSettings;
import com.tmsbackend.domain.model.MailThresholds;
import com.tmsbackend.domain.port.MailSettingsRepositoryPort;
import com.tmsbackend.domain.port.TopologyRepositoryPort;
import java.util.List;
import org.springframework.stereotype.Component;

// @Auditable methods below have their AuditEvent row written by AuditAspect
// after the method returns - see that class and Auditable's javadoc.
@Component
public class ManageMailSettingsUseCase {
    public static class DeviceNotFoundException extends RuntimeException {
        public DeviceNotFoundException(String deviceId) {
            super("Device " + deviceId
                    + " is not known to the backend yet - it syncs from the frontend's periodic reading push, "
                    + "which may not have run yet for a device just added. Try again in a moment.");
        }
    }

    private final MailSettingsRepositoryPort mailSettingsRepository;
    private final TopologyRepositoryPort topologyRepository;

    public ManageMailSettingsUseCase(
            MailSettingsRepositoryPort mailSettingsRepository, TopologyRepositoryPort topologyRepository) {
        this.mailSettingsRepository = mailSettingsRepository;
        this.topologyRepository = topologyRepository;
    }

    public MailSenderSettings getSenderSettings() {
        return mailSettingsRepository.getSenderSettings();
    }

    @Auditable(type = AuditEventType.MAIL_CONFIG_CHANGE, description = "'Updated mail sender settings'")
    public void saveSenderSettings(Long actingUserId, MailSenderSettings settings) {
        mailSettingsRepository.saveSenderSettings(settings);
    }

    public List<MailRecipient> listRecipients() {
        return mailSettingsRepository.findAllRecipients();
    }

    @Auditable(
            type = AuditEventType.MAIL_CONFIG_CHANGE,
            description = "'Saved mail recipient ' + #recipient.email()")
    public MailRecipient saveRecipient(Long actingUserId, MailRecipient recipient) {
        return mailSettingsRepository.saveRecipient(recipient);
    }

    @Auditable(type = AuditEventType.MAIL_CONFIG_CHANGE, description = "'Removed mail recipient ' + #id")
    public void deleteRecipient(Long actingUserId, String id) {
        mailSettingsRepository.deleteRecipient(id);
    }

    public MailThresholds getThresholds(String deviceId) {
        return mailSettingsRepository.findThresholds(deviceId).orElse(null);
    }

    @Auditable(
            type = AuditEventType.MAIL_CONFIG_CHANGE,
            deviceId = "#thresholds.deviceId()",
            description = "'Updated mail thresholds'")
    public void saveThresholds(Long actingUserId, MailThresholds thresholds) {
        if (topologyRepository.findDevice(thresholds.deviceId()).isEmpty()) {
            throw new DeviceNotFoundException(thresholds.deviceId());
        }
        mailSettingsRepository.saveThresholds(thresholds);
    }
}
