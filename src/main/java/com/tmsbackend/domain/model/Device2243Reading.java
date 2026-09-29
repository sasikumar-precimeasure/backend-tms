package com.tmsbackend.domain.model;

import java.time.Instant;

// Mirrors tms/src/domain/entities/Device2243RegisterMap.ts's
// Device2243Readings exactly - one snapshot pushed every 60s per 2243
// device.
public record Device2243Reading(
        String deviceId,
        Instant recordedAt,
        Double otiTemperature,
        Double wtiTemperature,
        Double otiAlarmSetpoint,
        Double otiAlarmDiff,
        Double otiTripSetpoint,
        Double otiTripDiff,
        Double wtiAlarmSetpoint,
        Double wtiAlarmDiff,
        Double wtiTripSetpoint,
        Double wtiTripDiff,
        Double wtiFan1Setpoint,
        Double wtiFan1Diff,
        Double wtiFan2Setpoint,
        Double wtiFan2Diff,
        Double relayDelay) {
}
