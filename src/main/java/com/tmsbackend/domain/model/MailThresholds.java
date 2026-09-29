package com.tmsbackend.domain.model;

// Mirrors tms/src/domain/entities/MailSettings.ts's MailThresholds exactly -
// one row per device. mailTimeMinutes is the re-alert-interval suppression
// window (Form1.txt's Mail_Coun, now wall-clock-driven instead of
// poll-tick-driven since this is evaluated by a scheduled job, not a UI
// timer tick).
public record MailThresholds(
        String deviceId,
        double otiTempHigh,
        double wtiTempHigh,
        double avrHigh,
        double avrLow,
        double tapHigh,
        double tapLow,
        int mailTimeMinutes) {
}
