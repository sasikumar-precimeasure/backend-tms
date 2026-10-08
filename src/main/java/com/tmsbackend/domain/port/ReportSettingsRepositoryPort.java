package com.tmsbackend.domain.port;

import com.tmsbackend.domain.model.ReportRecipient;
import com.tmsbackend.domain.model.ReportSettings;
import java.time.Instant;
import java.util.List;

public interface ReportSettingsRepositoryPort {
    ReportSettings getSettings();

    // Saves the schedule fields only - lastSentPeriod/lastSentAt are left
    // untouched (see markSent).
    void saveSettings(ReportSettings settings);

    void markSent(String period, Instant when);

    List<ReportRecipient> findAllRecipients();

    ReportRecipient saveRecipient(ReportRecipient recipient);

    void deleteRecipient(String id);
}
