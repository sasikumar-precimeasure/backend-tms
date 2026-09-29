package com.tmsbackend.infrastructure.web.dto;

import com.tmsbackend.application.dto.ReadingBatchRequest;
import com.tmsbackend.domain.model.Device2243Reading;
import com.tmsbackend.domain.model.DeviceType;
import com.tmsbackend.domain.model.IrtccReading;
import java.time.Instant;
import java.util.List;

// JSON shape for POST /tms/api/readings/batch - camelCase throughout,
// matching the frontend's own DashboardReadings/Device2243Readings field
// names 1:1 so the frontend's push thunk can serialize its Redux state
// with minimal reshaping.
public record ReadingBatchRequestDto(List<TransformerEntryDto> transformers) {

    public record TransformerEntryDto(String id, String name, List<GatewayEntryDto> gateways) {
    }

    public record GatewayEntryDto(String id, String name, int clientId, String ipAddress, int port, List<DeviceEntryDto> devices) {
    }

    public record DeviceEntryDto(
            String id,
            String name,
            int slaveId,
            DeviceType deviceType,
            boolean enabled,
            IrtccReadingDto irtccReading,
            Device2243ReadingDto device2243Reading) {
    }

    public record IrtccReadingDto(
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

        IrtccReading toDomain(String deviceId) {
            return new IrtccReading(
                    deviceId, recordedAt, otiTemperature, otiTemperatureMax, wtiTemperature, wtiTemperatureMax,
                    mog, tapPosition, tapPositionMax, tapCount, ptVoltage, actualPtVoltage, operationMode,
                    lvBreakerActive, hvBreakerActive, oltcLocal, ptFailActive, hooterActive, muteVisible,
                    avrModeIsAuto, controlFailActive, afrActive, raiseRelayActive, lowerRelayActive,
                    overVoltActive, underVoltActive, annunciation, annunciationAck, avrPtRatio, avrSetVoltage,
                    avrRaiseRelayVoltage, avrLowRelayVoltage, avrHsForwardVoltage, avrHsBackwardVoltage,
                    avrOverVoltage, avrUnderVoltage, avrPtFailSetpoint, avrInitialTime, avrSequentialTime,
                    avrHighFwdBwdTime, avrControlFailTime, avrRelayMomentaryTime);
        }
    }

    public record Device2243ReadingDto(
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

        Device2243Reading toDomain(String deviceId) {
            return new Device2243Reading(
                    deviceId, recordedAt, otiTemperature, wtiTemperature, otiAlarmSetpoint, otiAlarmDiff,
                    otiTripSetpoint, otiTripDiff, wtiAlarmSetpoint, wtiAlarmDiff, wtiTripSetpoint, wtiTripDiff,
                    wtiFan1Setpoint, wtiFan1Diff, wtiFan2Setpoint, wtiFan2Diff, relayDelay);
        }
    }

    public ReadingBatchRequest toDomain() {
        List<ReadingBatchRequest.TransformerEntry> trEntries = transformers.stream()
                .map(tr -> new ReadingBatchRequest.TransformerEntry(
                        tr.id(),
                        tr.name(),
                        tr.gateways().stream()
                                .map(gw -> new ReadingBatchRequest.GatewayEntry(
                                        gw.id(),
                                        gw.name(),
                                        gw.clientId(),
                                        gw.ipAddress(),
                                        gw.port(),
                                        gw.devices().stream()
                                                .map(d -> new ReadingBatchRequest.DeviceEntry(
                                                        d.id(),
                                                        d.name(),
                                                        d.slaveId(),
                                                        d.deviceType(),
                                                        d.enabled(),
                                                        d.irtccReading() != null ? d.irtccReading().toDomain(d.id()) : null,
                                                        d.device2243Reading() != null ? d.device2243Reading().toDomain(d.id()) : null))
                                                .toList()))
                                .toList()))
                .toList();
        return new ReadingBatchRequest(trEntries);
    }
}
