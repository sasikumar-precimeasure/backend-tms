package com.tmsbackend.domain.model;

import java.time.Instant;
import java.time.LocalTime;

// Monthly report schedule - one app-wide row. The report for month M is sent
// on `dayOfMonth` of month M+1 at `sendTime`, both in `timezone`.
// lastSentPeriod ('YYYY-MM') / lastSentAt are written only by the scheduler.
public record ReportSettings(
        boolean enabled,
        int dayOfMonth,
        LocalTime sendTime,
        String timezone,
        String lastSentPeriod,
        Instant lastSentAt) {
}
