package com.tmsbackend.infrastructure.web.dto;

import com.tmsbackend.domain.model.ReportSettings;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import java.time.LocalTime;

// sendTime is "HH:mm". lastSentPeriod/lastSentAt are read-only (ignored on save).
public record ReportSettingsDto(
        boolean enabled,
        @Min(1) @Max(28) int dayOfMonth,
        @NotBlank @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "must be HH:mm") String sendTime,
        @NotBlank String timezone,
        String lastSentPeriod,
        Instant lastSentAt) {
    public ReportSettings toDomain() {
        return new ReportSettings(enabled, dayOfMonth, LocalTime.parse(sendTime), timezone, null, null);
    }

    public static ReportSettingsDto from(ReportSettings s) {
        return new ReportSettingsDto(
                s.enabled(), s.dayOfMonth(), String.format("%02d:%02d", s.sendTime().getHour(), s.sendTime().getMinute()),
                s.timezone(), s.lastSentPeriod(), s.lastSentAt());
    }
}
