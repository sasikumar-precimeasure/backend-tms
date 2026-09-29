package com.tmsbackend.domain.model;

import java.time.Instant;
import java.util.List;

// Mirrors tms/src/domain/entities/TransformerRegisterMap.ts's
// DashboardReadings exactly - one snapshot pushed every 60s per IRTCC
// device. All numeric fields are nullable (a field can be genuinely
// unavailable - "Open"/disconnected sensor, read error, etc. - the
// frontend already collapses those to null before this reaches here).
// annunciation/annunciationAck are index-aligned booleans against the
// fixed 12-tile order (ANNUNCIATION_TILES on the frontend).
public record IrtccReading(
        String deviceId,
        Instant recordedAt,
        Double otiTemperature,
        Double otiTemperatureMax,
        Double wtiTemperature,
        Double wtiTemperatureMax,
        Double mog,
        Double tapPosition,
        Double tapPositionMax,
        Double tapCount,
        Double ptVoltage,
        Double actualPtVoltage,
        String operationMode,
        Boolean lvBreakerActive,
        Boolean hvBreakerActive,
        Boolean oltcLocal,
        Boolean ptFailActive,
        Boolean hooterActive,
        Boolean muteVisible,
        Boolean avrModeIsAuto,
        Boolean controlFailActive,
        Boolean afrActive,
        Boolean raiseRelayActive,
        Boolean lowerRelayActive,
        Boolean overVoltActive,
        Boolean underVoltActive,
        List<Boolean> annunciation,
        List<Boolean> annunciationAck,
        Double avrPtRatio,
        Double avrSetVoltage,
        Double avrRaiseRelayVoltage,
        Double avrLowRelayVoltage,
        Double avrHsForwardVoltage,
        Double avrHsBackwardVoltage,
        Double avrOverVoltage,
        Double avrUnderVoltage,
        Double avrPtFailSetpoint,
        Double avrInitialTime,
        Double avrSequentialTime,
        Double avrHighFwdBwdTime,
        Double avrControlFailTime,
        Double avrRelayMomentaryTime) {
}
