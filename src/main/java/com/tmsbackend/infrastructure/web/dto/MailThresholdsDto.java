package com.tmsbackend.infrastructure.web.dto;

import com.tmsbackend.domain.model.MailThresholds;

public record MailThresholdsDto(
        String deviceId, double otiTempHigh, double wtiTempHigh, double avrHigh, double avrLow, double tapHigh,
        double tapLow, int mailTimeMinutes) {

    public MailThresholds toDomain() {
        return new MailThresholds(deviceId, otiTempHigh, wtiTempHigh, avrHigh, avrLow, tapHigh, tapLow, mailTimeMinutes);
    }

    public static MailThresholdsDto from(MailThresholds thresholds) {
        return new MailThresholdsDto(
                thresholds.deviceId(), thresholds.otiTempHigh(), thresholds.wtiTempHigh(), thresholds.avrHigh(),
                thresholds.avrLow(), thresholds.tapHigh(), thresholds.tapLow(), thresholds.mailTimeMinutes());
    }
}
