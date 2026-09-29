package com.tmsbackend.domain.port;

import com.tmsbackend.domain.model.MailRecipient;
import com.tmsbackend.domain.model.MailSenderSettings;
import com.tmsbackend.domain.model.MailThresholds;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MailSettingsRepositoryPort {
    MailSenderSettings getSenderSettings();

    void saveSenderSettings(MailSenderSettings settings);

    List<MailRecipient> findAllRecipients();

    MailRecipient saveRecipient(MailRecipient recipient);

    void deleteRecipient(String id);

    Optional<MailThresholds> findThresholds(String deviceId);

    void saveThresholds(MailThresholds thresholds);

    Optional<Instant> findLastSent(String deviceId, String conditionKey);

    void recordSent(String deviceId, String conditionKey, Instant when);
}
