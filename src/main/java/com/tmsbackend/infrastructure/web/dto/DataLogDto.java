package com.tmsbackend.infrastructure.web.dto;

import com.tmsbackend.application.usecase.GetDataLogUseCase;
import com.tmsbackend.domain.model.Device;
import com.tmsbackend.domain.model.Device2243Reading;
import com.tmsbackend.domain.model.IrtccReading;
import com.tmsbackend.domain.model.PagedResult;
import java.time.Instant;
import java.util.List;

// JSON shapes for the Data Log screen's read endpoints - field names mirror
// ReadingBatchRequestDto's IrtccReadingDto/Device2243ReadingDto 1:1 (both
// ultimately describe the same domain records), so the frontend can reuse
// the exact same TS field names it already has for DashboardReadings/
// Device2243Readings.
public class DataLogDto {
    public record TopologyDto(String id, String name, List<GatewayDto> gateways) {
        public static TopologyDto from(GetDataLogUseCase.TransformerTopology t) {
            return new TopologyDto(
                    t.transformer().id(), t.transformer().name(), t.gateways().stream().map(GatewayDto::from).toList());
        }
    }

    public record GatewayDto(String id, String name, List<DeviceDto> devices) {
        public static GatewayDto from(GetDataLogUseCase.GatewayTopology g) {
            return new GatewayDto(g.gateway().id(), g.gateway().name(), g.devices().stream().map(DeviceDto::from).toList());
        }
    }

    public record DeviceDto(String id, String name, String deviceType, Instant lastReadingAt) {
        public static DeviceDto from(GetDataLogUseCase.DeviceTopology t) {
            Device d = t.device();
            return new DeviceDto(d.id(), d.name(), d.deviceType().name(), t.lastReadingAt());
        }
    }

    public record IrtccReadingRowDto(
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
        public static IrtccReadingRowDto from(IrtccReading r) {
            return new IrtccReadingRowDto(
                    r.recordedAt(), r.otiTemperature(), r.otiTemperatureMax(), r.wtiTemperature(), r.wtiTemperatureMax(),
                    r.mog(), r.tapPosition(), r.tapPositionMax(), r.tapCount(), r.ptVoltage(), r.actualPtVoltage(),
                    r.operationMode(), r.lvBreakerActive(), r.hvBreakerActive(), r.oltcLocal(), r.ptFailActive(),
                    r.hooterActive(), r.muteVisible(), r.avrModeIsAuto(), r.controlFailActive(), r.afrActive(),
                    r.raiseRelayActive(), r.lowerRelayActive(), r.overVoltActive(), r.underVoltActive(),
                    r.avrPtRatio(), r.avrSetVoltage(), r.avrRaiseRelayVoltage(), r.avrLowRelayVoltage(),
                    r.avrHsForwardVoltage(), r.avrHsBackwardVoltage(), r.avrOverVoltage(), r.avrUnderVoltage(),
                    r.avrPtFailSetpoint(), r.avrInitialTime(), r.avrSequentialTime(), r.avrHighFwdBwdTime(),
                    r.avrControlFailTime(), r.avrRelayMomentaryTime());
        }
    }

    public record Device2243ReadingRowDto(
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
        public static Device2243ReadingRowDto from(Device2243Reading r) {
            return new Device2243ReadingRowDto(
                    r.recordedAt(), r.otiTemperature(), r.wtiTemperature(), r.otiAlarmSetpoint(), r.otiAlarmDiff(),
                    r.otiTripSetpoint(), r.otiTripDiff(), r.wtiAlarmSetpoint(), r.wtiAlarmDiff(), r.wtiTripSetpoint(),
                    r.wtiTripDiff(), r.wtiFan1Setpoint(), r.wtiFan1Diff(), r.wtiFan2Setpoint(), r.wtiFan2Diff(),
                    r.relayDelay());
        }
    }

    public record PagedDto<T>(List<T> items, long totalItems, int totalPages, int page, int pageSize) {
        public static <D, T> PagedDto<D> from(PagedResult<T> result, java.util.function.Function<T, D> mapper) {
            return new PagedDto<>(
                    result.items().stream().map(mapper).toList(), result.totalItems(), result.totalPages(),
                    result.page(), result.pageSize());
        }
    }
}
