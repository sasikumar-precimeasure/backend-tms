package com.tmsbackend.infrastructure.report;

import com.tmsbackend.domain.model.DeviceType;
import com.tmsbackend.domain.model.MonthlyReport;
import com.tmsbackend.domain.port.ReportWorkbookPort;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Component;

// Summary sheet + one sheet per device. Device sheet columns follow the
// legacy VB report (Date, Time, OTI, WTI, Tap, PT Volt, Set Voltage / the
// 2243's setpoints), with each value being that 30-minute slot's average
// plus OTI/WTI peaks. Streaming workbook (SXSSF) since a month is ~1,440
// rows per device.
@Component
public class PoiReportWorkbookRenderer implements ReportWorkbookPort {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private record Column(String header, int width, Function<MonthlyReport.Slot, Double> value) {
    }

    private static final List<Column> IRTCC_COLUMNS = List.of(
            new Column("OTI Avg (°C)", 13, MonthlyReport.Slot::otiAvg),
            new Column("OTI Peak (°C)", 13, MonthlyReport.Slot::otiMax),
            new Column("WTI Avg (°C)", 13, MonthlyReport.Slot::wtiAvg),
            new Column("WTI Peak (°C)", 13, MonthlyReport.Slot::wtiMax),
            new Column("Tap Position", 12, MonthlyReport.Slot::tapPosition),
            new Column("PT Voltage Avg (V)", 17, MonthlyReport.Slot::ptVoltageAvg),
            new Column("Set Voltage (V)", 15, MonthlyReport.Slot::setVoltage));

    private static final List<Column> DEVICE_2243_COLUMNS = List.of(
            new Column("OTI Avg (°C)", 13, MonthlyReport.Slot::otiAvg),
            new Column("OTI Peak (°C)", 13, MonthlyReport.Slot::otiMax),
            new Column("WTI Avg (°C)", 13, MonthlyReport.Slot::wtiAvg),
            new Column("WTI Peak (°C)", 13, MonthlyReport.Slot::wtiMax),
            new Column("OTI Alarm SP", 13, MonthlyReport.Slot::otiAlarmSetpoint),
            new Column("OTI Trip SP", 12, MonthlyReport.Slot::otiTripSetpoint),
            new Column("WTI Alarm SP", 13, MonthlyReport.Slot::wtiAlarmSetpoint),
            new Column("WTI Trip SP", 12, MonthlyReport.Slot::wtiTripSetpoint));

    private static class Styles {
        final CellStyle title;
        final CellStyle subtitle;
        final CellStyle header;
        final CellStyle text;
        final CellStyle number;
        final CellStyle integer;

        Styles(SXSSFWorkbook wb) {
            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            title = wb.createCellStyle();
            title.setFont(titleFont);

            Font subtitleFont = wb.createFont();
            subtitleFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            subtitle = wb.createCellStyle();
            subtitle.setFont(subtitleFont);

            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            header = wb.createCellStyle();
            header.setFont(headerFont);
            header.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            header.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            header.setBorderBottom(BorderStyle.THIN);
            header.setWrapText(true);

            text = wb.createCellStyle();
            number = wb.createCellStyle();
            number.setDataFormat(wb.createDataFormat().getFormat("0.0"));
            integer = wb.createCellStyle();
            integer.setDataFormat(wb.createDataFormat().getFormat("0"));
        }
    }

    @Override
    public byte[] render(MonthlyReport report) {
        try (SXSSFWorkbook wb = new SXSSFWorkbook(200)) {
            Styles styles = new Styles(wb);
            String monthLabel = report.month().getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + report.month().getYear();
            String subtitle = "30-minute averages · times in " + report.zone();

            writeSummary(wb, styles, report, monthLabel, subtitle);
            Set<String> usedNames = new HashSet<>(Set.of("summary"));
            for (MonthlyReport.DeviceSection section : report.devices()) {
                writeDeviceSheet(wb, styles, section, uniqueSheetName(section, usedNames), monthLabel, subtitle);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to render monthly report workbook", e);
        }
    }

    private void writeSummary(SXSSFWorkbook wb, Styles styles, MonthlyReport report, String monthLabel, String subtitle) {
        Sheet sheet = wb.createSheet("Summary");
        setText(sheet.createRow(0), 0, "Monthly Transformer Report - " + monthLabel, styles.title);
        setText(sheet.createRow(1), 0, subtitle, styles.subtitle);

        String[] headers = {"Transformer", "Device", "Type", "OTI Avg (°C)", "OTI Peak (°C)", "WTI Avg (°C)", "WTI Peak (°C)", "Slots with data", "Coverage"};
        int[] widths = {22, 28, 10, 13, 13, 13, 13, 15, 11};
        Row headerRow = sheet.createRow(3);
        for (int i = 0; i < headers.length; i++) {
            setText(headerRow, i, headers[i], styles.header);
            sheet.setColumnWidth(i, widths[i] * 256);
        }

        int rowIndex = 4;
        for (MonthlyReport.DeviceSection section : report.devices()) {
            List<MonthlyReport.Slot> slots = section.slots();
            Row row = sheet.createRow(rowIndex++);
            setText(row, 0, section.transformerName(), styles.text);
            setText(row, 1, section.device().name(), styles.text);
            setText(row, 2, section.device().deviceType() == DeviceType.IRTCC ? "IRTCC" : "2243", styles.text);
            setNumber(row, 3, avg(slots, MonthlyReport.Slot::otiAvg), styles.number);
            setNumber(row, 4, max(slots, MonthlyReport.Slot::otiMax), styles.number);
            setNumber(row, 5, avg(slots, MonthlyReport.Slot::wtiAvg), styles.number);
            setNumber(row, 6, max(slots, MonthlyReport.Slot::wtiMax), styles.number);
            setText(row, 7, section.slotsWithData() + " / " + slots.size(), styles.text);
            setText(row, 8, slots.isEmpty() ? "-" : Math.round(100.0 * section.slotsWithData() / slots.size()) + "%", styles.text);
        }
        if (report.devices().isEmpty()) {
            setText(sheet.createRow(rowIndex), 0, "No devices are configured yet.", styles.subtitle);
        }
        sheet.createFreezePane(0, 4);
    }

    private void writeDeviceSheet(
            SXSSFWorkbook wb, Styles styles, MonthlyReport.DeviceSection section, String sheetName, String monthLabel, String subtitle) {
        List<Column> columns = section.device().deviceType() == DeviceType.IRTCC ? IRTCC_COLUMNS : DEVICE_2243_COLUMNS;
        Sheet sheet = wb.createSheet(sheetName);
        String title = (section.transformerName().isBlank() ? "" : section.transformerName() + " - ") + section.device().name();
        setText(sheet.createRow(0), 0, title + " - " + monthLabel, styles.title);
        setText(sheet.createRow(1), 0, subtitle + " · empty rows = no data received in that slot", styles.subtitle);

        Row headerRow = sheet.createRow(3);
        setText(headerRow, 0, "Date", styles.header);
        setText(headerRow, 1, "Time", styles.header);
        sheet.setColumnWidth(0, 12 * 256);
        sheet.setColumnWidth(1, 8 * 256);
        for (int i = 0; i < columns.size(); i++) {
            setText(headerRow, i + 2, columns.get(i).header(), styles.header);
            sheet.setColumnWidth(i + 2, columns.get(i).width() * 256);
        }
        int samplesCol = columns.size() + 2;
        setText(headerRow, samplesCol, "Samples", styles.header);
        sheet.setColumnWidth(samplesCol, 9 * 256);

        int rowIndex = 4;
        for (MonthlyReport.Slot slot : section.slots()) {
            Row row = sheet.createRow(rowIndex++);
            setText(row, 0, DATE.format(slot.start()), styles.text);
            setText(row, 1, TIME.format(slot.start()), styles.text);
            for (int i = 0; i < columns.size(); i++) {
                setNumber(row, i + 2, columns.get(i).value().apply(slot), styles.number);
            }
            setNumber(row, samplesCol, (double) slot.sampleCount(), styles.integer);
        }
        sheet.createFreezePane(2, 4);
    }

    private static String uniqueSheetName(MonthlyReport.DeviceSection section, Set<String> used) {
        String base = section.transformerName().isBlank()
                ? section.device().name()
                : section.device().name().startsWith(section.transformerName())
                        ? section.device().name()
                        : section.transformerName() + " " + section.device().name();
        // Excel: max 31 chars, no []:*?/\ and case-insensitively unique.
        String safe = WorkbookUtil.createSafeSheetName(base.isBlank() ? "Device" : base);
        if (safe.length() > 31) safe = safe.substring(0, 31);
        String candidate = safe;
        for (int n = 2; used.contains(candidate.toLowerCase(Locale.ROOT)); n++) {
            String suffix = " (" + n + ")";
            candidate = safe.substring(0, Math.min(safe.length(), 31 - suffix.length())) + suffix;
        }
        used.add(candidate.toLowerCase(Locale.ROOT));
        return candidate;
    }

    private static void setText(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellStyle(style);
        if (value != null) cell.setCellValue(value);
    }

    private static void setNumber(Row row, int col, Double value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellStyle(style);
        if (value != null) cell.setCellValue(value);
    }

    private static Double avg(List<MonthlyReport.Slot> slots, Function<MonthlyReport.Slot, Double> field) {
        return slots.stream().map(field).filter(Objects::nonNull).mapToDouble(Double::doubleValue).average().stream().boxed().findFirst().orElse(null);
    }

    private static Double max(List<MonthlyReport.Slot> slots, Function<MonthlyReport.Slot, Double> field) {
        return slots.stream().map(field).filter(Objects::nonNull).max(Double::compare).orElse(null);
    }
}
