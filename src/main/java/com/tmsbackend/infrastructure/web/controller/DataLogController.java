package com.tmsbackend.infrastructure.web.controller;

import com.tmsbackend.application.usecase.GetDataLogUseCase;
import com.tmsbackend.domain.model.Device2243Reading;
import com.tmsbackend.domain.model.IrtccReading;
import com.tmsbackend.domain.model.PagedResult;
import com.tmsbackend.infrastructure.web.CurrentUserResolver;
import com.tmsbackend.infrastructure.web.PermissionGuard;
import com.tmsbackend.infrastructure.web.dto.DataLogDto;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.List;
import java.util.function.BiFunction;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Settings > Data Log - a read-only browser/export over the historical
// readings the 1-minute ingestion push has already stored (see
// ReadingIngestController). Gated on its own "Data Log" permission menu
// (V3__data_log_permission.sql), independent of Dashboard access, same
// pattern as Users/Roles/Audit Log each getting their own menu.
@RestController
@RequestMapping("/tms/api/data-log")
public class DataLogController {
    private static final String MENU = "Data Log";
    private static final int MAX_EXPORT_ROWS = 50_000;

    private final GetDataLogUseCase getDataLogUseCase;
    private final CurrentUserResolver currentUserResolver;
    private final PermissionGuard permissionGuard;

    public DataLogController(
            GetDataLogUseCase getDataLogUseCase, CurrentUserResolver currentUserResolver, PermissionGuard permissionGuard) {
        this.getDataLogUseCase = getDataLogUseCase;
        this.currentUserResolver = currentUserResolver;
        this.permissionGuard = permissionGuard;
    }

    @GetMapping("/topology")
    public List<DataLogDto.TopologyDto> topology() {
        permissionGuard.requireRead(currentUserResolver.requireUserId(), MENU);
        return getDataLogUseCase.listTopology().stream().map(DataLogDto.TopologyDto::from).toList();
    }

    @GetMapping("/irtcc/{deviceId}")
    public DataLogDto.PagedDto<DataLogDto.IrtccReadingRowDto> irtcc(
            @PathVariable String deviceId,
            @RequestParam Instant from,
            @RequestParam Instant to,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize) {
        permissionGuard.requireRead(currentUserResolver.requireUserId(), MENU);
        PagedResult<IrtccReading> result = getDataLogUseCase.pageIrtcc(deviceId, from, to, page, pageSize);
        return DataLogDto.PagedDto.from(result, DataLogDto.IrtccReadingRowDto::from);
    }

    @GetMapping("/device2243/{deviceId}")
    public DataLogDto.PagedDto<DataLogDto.Device2243ReadingRowDto> device2243(
            @PathVariable String deviceId,
            @RequestParam Instant from,
            @RequestParam Instant to,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int pageSize) {
        permissionGuard.requireRead(currentUserResolver.requireUserId(), MENU);
        PagedResult<Device2243Reading> result = getDataLogUseCase.pageDevice2243(deviceId, from, to, page, pageSize);
        return DataLogDto.PagedDto.from(result, DataLogDto.Device2243ReadingRowDto::from);
    }

    // Exports the ENTIRE matching date range as one .xlsx workbook,
    // deliberately ignoring the screen's own pagination (see
    // GetDataLogUseCase's javadoc) - capped at MAX_EXPORT_ROWS purely as a
    // safety valve against an accidental multi-year range choking the
    // server's memory, not a feature limit the UI is expected to hit in
    // normal use.
    @GetMapping("/irtcc/{deviceId}/export")
    public ResponseEntity<byte[]> exportIrtcc(@PathVariable String deviceId, @RequestParam Instant from, @RequestParam Instant to) {
        permissionGuard.requireRead(currentUserResolver.requireUserId(), MENU);
        List<IrtccReading> rows = getDataLogUseCase.exportIrtcc(deviceId, from, to);
        String[] headers = {
            "Recorded At", "OTI Temp", "OTI Temp Max", "WTI Temp", "WTI Temp Max", "MOG", "Tap Position",
            "Tap Position Max", "Tap Count", "PT Voltage", "Actual PT Voltage", "Operation Mode",
            "LV Breaker Active", "HV Breaker Active", "OLTC Local", "PT Fail Active", "Hooter Active",
            "Mute Visible", "AVR Mode Is Auto", "Control Fail Active", "AFR Active", "Raise Relay Active",
            "Lower Relay Active", "Over Volt Active", "Under Volt Active", "AVR PT Ratio", "AVR Set Voltage",
            "AVR Raise Relay Voltage", "AVR Low Relay Voltage", "AVR HS Forward Voltage",
            "AVR HS Backward Voltage", "AVR Over Voltage", "AVR Under Voltage", "AVR PT Fail Setpoint",
            "AVR Initial Time", "AVR Sequential Time", "AVR High Fwd/Bwd Time", "AVR Control Fail Time",
            "AVR Relay Momentary Time",
        };
        return buildWorkbook("irtcc-data-log", headers, rows, (row, r) -> {
            int col = 0;
            setCell(row, col++, r.recordedAt().toString());
            setCell(row, col++, r.otiTemperature());
            setCell(row, col++, r.otiTemperatureMax());
            setCell(row, col++, r.wtiTemperature());
            setCell(row, col++, r.wtiTemperatureMax());
            setCell(row, col++, r.mog());
            setCell(row, col++, r.tapPosition());
            setCell(row, col++, r.tapPositionMax());
            setCell(row, col++, r.tapCount());
            setCell(row, col++, r.ptVoltage());
            setCell(row, col++, r.actualPtVoltage());
            setCell(row, col++, r.operationMode());
            setCell(row, col++, r.lvBreakerActive());
            setCell(row, col++, r.hvBreakerActive());
            setCell(row, col++, r.oltcLocal());
            setCell(row, col++, r.ptFailActive());
            setCell(row, col++, r.hooterActive());
            setCell(row, col++, r.muteVisible());
            setCell(row, col++, r.avrModeIsAuto());
            setCell(row, col++, r.controlFailActive());
            setCell(row, col++, r.afrActive());
            setCell(row, col++, r.raiseRelayActive());
            setCell(row, col++, r.lowerRelayActive());
            setCell(row, col++, r.overVoltActive());
            setCell(row, col++, r.underVoltActive());
            setCell(row, col++, r.avrPtRatio());
            setCell(row, col++, r.avrSetVoltage());
            setCell(row, col++, r.avrRaiseRelayVoltage());
            setCell(row, col++, r.avrLowRelayVoltage());
            setCell(row, col++, r.avrHsForwardVoltage());
            setCell(row, col++, r.avrHsBackwardVoltage());
            setCell(row, col++, r.avrOverVoltage());
            setCell(row, col++, r.avrUnderVoltage());
            setCell(row, col++, r.avrPtFailSetpoint());
            setCell(row, col++, r.avrInitialTime());
            setCell(row, col++, r.avrSequentialTime());
            setCell(row, col++, r.avrHighFwdBwdTime());
            setCell(row, col++, r.avrControlFailTime());
            setCell(row, col, r.avrRelayMomentaryTime());
            return null;
        });
    }

    @GetMapping("/device2243/{deviceId}/export")
    public ResponseEntity<byte[]> exportDevice2243(
            @PathVariable String deviceId, @RequestParam Instant from, @RequestParam Instant to) {
        permissionGuard.requireRead(currentUserResolver.requireUserId(), MENU);
        List<Device2243Reading> rows = getDataLogUseCase.exportDevice2243(deviceId, from, to);
        String[] headers = {
            "Recorded At", "OTI Temp", "WTI Temp", "OTI Alarm Setpoint", "OTI Alarm Diff", "OTI Trip Setpoint",
            "OTI Trip Diff", "WTI Alarm Setpoint", "WTI Alarm Diff", "WTI Trip Setpoint", "WTI Trip Diff",
            "WTI Fan1 Setpoint", "WTI Fan1 Diff", "WTI Fan2 Setpoint", "WTI Fan2 Diff", "Relay Delay",
        };
        return buildWorkbook("2243-data-log", headers, rows, (row, r) -> {
            int col = 0;
            setCell(row, col++, r.recordedAt().toString());
            setCell(row, col++, r.otiTemperature());
            setCell(row, col++, r.wtiTemperature());
            setCell(row, col++, r.otiAlarmSetpoint());
            setCell(row, col++, r.otiAlarmDiff());
            setCell(row, col++, r.otiTripSetpoint());
            setCell(row, col++, r.otiTripDiff());
            setCell(row, col++, r.wtiAlarmSetpoint());
            setCell(row, col++, r.wtiAlarmDiff());
            setCell(row, col++, r.wtiTripSetpoint());
            setCell(row, col++, r.wtiTripDiff());
            setCell(row, col++, r.wtiFan1Setpoint());
            setCell(row, col++, r.wtiFan1Diff());
            setCell(row, col++, r.wtiFan2Setpoint());
            setCell(row, col++, r.wtiFan2Diff());
            setCell(row, col, r.relayDelay());
            return null;
        });
    }

    private <T> ResponseEntity<byte[]> buildWorkbook(String fileBaseName, String[] headers, List<T> rows, BiFunction<Row, T, Void> rowWriter) {
        List<T> bounded = rows.size() > MAX_EXPORT_ROWS ? rows.subList(0, MAX_EXPORT_ROWS) : rows;
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Data Log");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            int rowIndex = 1;
            for (T r : bounded) {
                rowWriter.apply(sheet.createRow(rowIndex++), r);
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            responseHeaders.setContentDispositionFormData("attachment", fileBaseName + ".xlsx");
            return ResponseEntity.ok().headers(responseHeaders).body(out.toByteArray());
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to generate export workbook", e);
        }
    }

    private void setCell(Row row, int col, Double value) {
        Cell cell = row.createCell(col);
        if (value != null) cell.setCellValue(value);
    }

    private void setCell(Row row, int col, String value) {
        Cell cell = row.createCell(col);
        if (value != null) cell.setCellValue(value);
    }

    private void setCell(Row row, int col, Boolean value) {
        Cell cell = row.createCell(col);
        if (value != null) cell.setCellValue(value);
    }
}
