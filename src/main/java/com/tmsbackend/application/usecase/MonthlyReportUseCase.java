package com.tmsbackend.application.usecase;

import com.tmsbackend.application.audit.Auditable;
import com.tmsbackend.domain.model.AuditEventType;
import com.tmsbackend.domain.model.Device;
import com.tmsbackend.domain.model.Device2243Reading;
import com.tmsbackend.domain.model.DeviceType;
import com.tmsbackend.domain.model.Gateway;
import com.tmsbackend.domain.model.IrtccReading;
import com.tmsbackend.domain.model.MailAttachment;
import com.tmsbackend.domain.model.MailSenderSettings;
import com.tmsbackend.domain.model.MonthlyReport;
import com.tmsbackend.domain.model.ReportRecipient;
import com.tmsbackend.domain.model.ReportSettings;
import com.tmsbackend.domain.port.MailSenderPort;
import com.tmsbackend.domain.port.MailSettingsRepositoryPort;
import com.tmsbackend.domain.port.ReadingRepositoryPort;
import com.tmsbackend.domain.port.ReportSettingsRepositoryPort;
import com.tmsbackend.domain.port.ReportWorkbookPort;
import com.tmsbackend.domain.port.TopologyRepositoryPort;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

// Monthly report: every enabled device's readings for one calendar month,
// bucketed into 30-minute slots (averages + peaks), rendered to .xlsx and
// emailed to the report recipient list. Sent automatically by the scheduler
// (runScheduled, ticked every minute from SchedulingConfig) and on demand
// from Settings > Mail Configuration (sendNow / renderReport).
@Component
public class MonthlyReportUseCase {
    public static class InvalidReportSettingsException extends RuntimeException {
        public InvalidReportSettingsException(String message) {
            super(message);
        }
    }

    public static class ReportNotSendableException extends RuntimeException {
        public ReportNotSendableException(String message) {
            super(message);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(MonthlyReportUseCase.class);
    private static final int SLOT_MINUTES = 30;
    // After a failed scheduled send (SMTP down, no recipients yet) wait this
    // long before trying again, instead of retrying - and logging - every
    // minute until someone fixes it.
    private static final Duration RETRY_AFTER_FAILURE = Duration.ofMinutes(15);
    private static final String XLSX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final TopologyRepositoryPort topologyRepository;
    private final ReadingRepositoryPort readingRepository;
    private final ReportSettingsRepositoryPort reportSettingsRepository;
    private final MailSettingsRepositoryPort mailSettingsRepository;
    private final MailSenderPort mailSender;
    private final ReportWorkbookPort workbookRenderer;
    private final Clock clock;
    // Routed through the Spring proxy so @Auditable fires - see the same
    // pattern (and its rationale) in EvaluateMailThresholdsUseCase.
    private final MonthlyReportUseCase self;

    private volatile Instant lastFailedAttemptAt;

    public MonthlyReportUseCase(
            TopologyRepositoryPort topologyRepository,
            ReadingRepositoryPort readingRepository,
            ReportSettingsRepositoryPort reportSettingsRepository,
            MailSettingsRepositoryPort mailSettingsRepository,
            MailSenderPort mailSender,
            ReportWorkbookPort workbookRenderer,
            Clock clock,
            @Lazy MonthlyReportUseCase self) {
        this.topologyRepository = topologyRepository;
        this.readingRepository = readingRepository;
        this.reportSettingsRepository = reportSettingsRepository;
        this.mailSettingsRepository = mailSettingsRepository;
        this.mailSender = mailSender;
        this.workbookRenderer = workbookRenderer;
        this.clock = clock;
        this.self = self != null ? self : this;
    }

    // -- Settings / recipients --

    public ReportSettings getSettings() {
        return reportSettingsRepository.getSettings();
    }

    @Auditable(type = AuditEventType.MAIL_CONFIG_CHANGE, description = "'Updated monthly report schedule'")
    public void saveSettings(Long actingUserId, ReportSettings settings) {
        if (settings.dayOfMonth() < 1 || settings.dayOfMonth() > 28) {
            throw new InvalidReportSettingsException("Day of month must be between 1 and 28");
        }
        if (settings.sendTime() == null) {
            throw new InvalidReportSettingsException("Send time is required");
        }
        parseZone(settings.timezone());
        reportSettingsRepository.saveSettings(settings);
    }

    public List<ReportRecipient> listRecipients() {
        return reportSettingsRepository.findAllRecipients();
    }

    @Auditable(type = AuditEventType.MAIL_CONFIG_CHANGE, description = "'Saved monthly report recipient ' + #recipient.email()")
    public ReportRecipient saveRecipient(Long actingUserId, ReportRecipient recipient) {
        return reportSettingsRepository.saveRecipient(recipient);
    }

    @Auditable(type = AuditEventType.MAIL_CONFIG_CHANGE, description = "'Removed monthly report recipient ' + #id")
    public void deleteRecipient(Long actingUserId, String id) {
        reportSettingsRepository.deleteRecipient(id);
    }

    // -- Report generation --

    public byte[] renderReport(YearMonth month) {
        return workbookRenderer.render(buildReport(month, parseZone(getSettings().timezone())));
    }

    public static String fileName(YearMonth month) {
        return "transformer-report-" + month + ".xlsx";
    }

    public MonthlyReport buildReport(YearMonth month, ZoneId zone) {
        Instant from = month.atDay(1).atStartOfDay(zone).toInstant();
        Instant toExclusive = month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant();
        // The repository's range is inclusive on both ends.
        Instant to = toExclusive.minusMillis(1);

        Map<String, Gateway> gatewaysById = new HashMap<>();
        topologyRepository.findAllGateways().forEach(g -> gatewaysById.put(g.id(), g));
        Map<String, String> transformerNames = new HashMap<>();
        topologyRepository.findAllTransformers().forEach(t -> transformerNames.put(t.id(), t.name()));

        List<MonthlyReport.DeviceSection> sections = new ArrayList<>();
        for (Device device : topologyRepository.findAllDevices()) {
            if (!device.enabled()) continue;
            Gateway gateway = gatewaysById.get(device.gatewayId());
            String transformerName = gateway != null ? transformerNames.getOrDefault(gateway.transformerId(), "") : "";
            List<MonthlyReport.Slot> slots = device.deviceType() == DeviceType.IRTCC
                    ? irtccSlots(readingRepository.findAllIrtccByDeviceAndDateRange(device.id(), from, to), month, zone)
                    : device2243Slots(readingRepository.findAllDevice2243ByDeviceAndDateRange(device.id(), from, to), month, zone);
            sections.add(new MonthlyReport.DeviceSection(transformerName, device, slots));
        }
        sections.sort(Comparator.comparing(MonthlyReport.DeviceSection::transformerName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(s -> s.device().name(), String.CASE_INSENSITIVE_ORDER));
        return new MonthlyReport(month, zone, sections);
    }

    private static List<MonthlyReport.Slot> irtccSlots(List<IrtccReading> readings, YearMonth month, ZoneId zone) {
        Map<LocalDateTime, List<IrtccReading>> bySlot = groupBySlot(readings, IrtccReading::recordedAt, zone);
        return everySlot(month).stream().map(start -> {
            List<IrtccReading> rs = bySlot.getOrDefault(start, List.of());
            IrtccReading last = rs.isEmpty() ? null : rs.get(rs.size() - 1);
            return new MonthlyReport.Slot(
                    start, rs.size(),
                    avg(rs, IrtccReading::otiTemperature), max(rs, IrtccReading::otiTemperature),
                    avg(rs, IrtccReading::wtiTemperature), max(rs, IrtccReading::wtiTemperature),
                    last != null ? last.tapPosition() : null,
                    avg(rs, IrtccReading::actualPtVoltage),
                    last != null ? last.avrSetVoltage() : null,
                    null, null, null, null);
        }).toList();
    }

    private static List<MonthlyReport.Slot> device2243Slots(List<Device2243Reading> readings, YearMonth month, ZoneId zone) {
        Map<LocalDateTime, List<Device2243Reading>> bySlot = groupBySlot(readings, Device2243Reading::recordedAt, zone);
        return everySlot(month).stream().map(start -> {
            List<Device2243Reading> rs = bySlot.getOrDefault(start, List.of());
            Device2243Reading last = rs.isEmpty() ? null : rs.get(rs.size() - 1);
            return new MonthlyReport.Slot(
                    start, rs.size(),
                    avg(rs, Device2243Reading::otiTemperature), max(rs, Device2243Reading::otiTemperature),
                    avg(rs, Device2243Reading::wtiTemperature), max(rs, Device2243Reading::wtiTemperature),
                    null, null, null,
                    last != null ? last.otiAlarmSetpoint() : null,
                    last != null ? last.otiTripSetpoint() : null,
                    last != null ? last.wtiAlarmSetpoint() : null,
                    last != null ? last.wtiTripSetpoint() : null);
        }).toList();
    }

    // Each list is in ascending time order, so the last element is the
    // slot's most recent reading.
    private static <T> Map<LocalDateTime, List<T>> groupBySlot(List<T> readings, Function<T, Instant> time, ZoneId zone) {
        Map<LocalDateTime, List<T>> bySlot = new HashMap<>();
        readings.stream()
                .sorted(Comparator.comparing(time))
                .forEach(r -> bySlot.computeIfAbsent(slotStart(time.apply(r), zone), k -> new ArrayList<>()).add(r));
        return bySlot;
    }

    static LocalDateTime slotStart(Instant instant, ZoneId zone) {
        LocalDateTime local = LocalDateTime.ofInstant(instant, zone).truncatedTo(ChronoUnit.MINUTES);
        return local.withMinute(local.getMinute() / SLOT_MINUTES * SLOT_MINUTES);
    }

    private static List<LocalDateTime> everySlot(YearMonth month) {
        List<LocalDateTime> slots = new ArrayList<>();
        LocalDateTime end = month.plusMonths(1).atDay(1).atStartOfDay();
        for (LocalDateTime t = month.atDay(1).atStartOfDay(); t.isBefore(end); t = t.plusMinutes(SLOT_MINUTES)) {
            slots.add(t);
        }
        return slots;
    }

    private static <T> Double avg(List<T> rows, Function<T, Double> field) {
        double sum = 0;
        int n = 0;
        for (T row : rows) {
            Double v = field.apply(row);
            if (v != null) {
                sum += v;
                n++;
            }
        }
        return n == 0 ? null : sum / n;
    }

    private static <T> Double max(List<T> rows, Function<T, Double> field) {
        return rows.stream().map(field).filter(Objects::nonNull).max(Double::compare).orElse(null);
    }

    // -- Sending --

    // Ticked every minute. The report for the previous month is due once
    // "now" (in the report's zone) passes dayOfMonth + sendTime of the
    // current month; lastSentPeriod makes it exactly-once, and because the
    // check is ">= scheduled time" rather than "== scheduled minute", a
    // backend that was down at the scheduled time still sends once it's
    // back up.
    public void runScheduled() {
        ReportSettings settings = getSettings();
        if (!settings.enabled()) return;

        Instant now = Instant.now(clock);
        YearMonth due = dueReportMonth(settings, now);
        if (due == null || due.toString().equals(settings.lastSentPeriod())) return;
        if (lastFailedAttemptAt != null && Duration.between(lastFailedAttemptAt, now).compareTo(RETRY_AFTER_FAILURE) < 0) {
            return;
        }

        try {
            int recipientCount = send(due);
            reportSettingsRepository.markSent(due.toString(), now);
            lastFailedAttemptAt = null;
            self.recordReportSent(due.toString(), recipientCount, "scheduled");
            log.info("Monthly report for {} sent to {} recipient(s)", due, recipientCount);
        } catch (RuntimeException e) {
            lastFailedAttemptAt = now;
            log.error("Monthly report for {} could not be sent - retrying in {} min: {}",
                    due, RETRY_AFTER_FAILURE.toMinutes(), e.getMessage());
        }
    }

    static YearMonth dueReportMonth(ReportSettings settings, Instant now) {
        ZonedDateTime localNow = now.atZone(parseZone(settings.timezone()));
        ZonedDateTime scheduled = localNow.toLocalDate()
                .withDayOfMonth(settings.dayOfMonth())
                .atTime(settings.sendTime())
                .atZone(localNow.getZone());
        if (localNow.isBefore(scheduled)) {
            return null;
        }
        return YearMonth.from(localNow).minusMonths(1);
    }

    // Manual "Send now" from Settings - goes to the same recipients but does
    // not touch lastSentPeriod, so it never suppresses the scheduled send.
    public int sendNow(Long actingUserId, YearMonth month) {
        int recipientCount = send(month);
        self.recordReportSent(month.toString(), recipientCount, "manual");
        return recipientCount;
    }

    private int send(YearMonth month) {
        MailSenderSettings sender = mailSettingsRepository.getSenderSettings();
        if (sender.smtpHost() == null || sender.smtpHost().isBlank()) {
            throw new ReportNotSendableException("Mail sender (SMTP) is not configured yet");
        }
        List<String> to = listRecipients().stream()
                .filter(ReportRecipient::enabled)
                .map(ReportRecipient::email)
                .filter(email -> email != null && !email.isBlank())
                .toList();
        if (to.isEmpty()) {
            throw new ReportNotSendableException("No enabled monthly report recipients");
        }

        MonthlyReport report = buildReport(month, parseZone(getSettings().timezone()));
        byte[] workbook = workbookRenderer.render(report);
        String monthLabel = monthLabel(month);
        try {
            mailSender.send(
                    sender, to, "Monthly Transformer Report - " + monthLabel, emailBody(report, monthLabel),
                    List.of(new MailAttachment(fileName(month), XLSX_CONTENT_TYPE, workbook)));
        } catch (RuntimeException e) {
            throw new ReportNotSendableException(e.getMessage());
        }
        return to.size();
    }

    @Auditable(
            type = AuditEventType.MAIL_SENT,
            fieldName = "'MONTHLY_REPORT'",
            description = "'Monthly report for ' + #period + ' sent to ' + #recipientCount + ' recipient(s) (' + #trigger + ')'")
    void recordReportSent(String period, int recipientCount, String trigger) {
        // No body - this method exists only to be the @Auditable join point.
    }

    private static String monthLabel(YearMonth month) {
        return month.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + month.getYear();
    }

    private static String emailBody(MonthlyReport report, String monthLabel) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body style=\"font-family:Arial,sans-serif;font-size:13px\">")
                .append("<b>Dear Sir/Madam,</b>")
                .append("<p>Please find attached the transformer report for <b>").append(monthLabel)
                .append("</b> (30-minute averages, times in ").append(report.zone()).append(").</p>")
                .append("<table cellpadding=\"6\" cellspacing=\"0\" border=\"1\" style=\"border-collapse:collapse;font-size:12px\">")
                .append("<tr style=\"background:#f1f5f9\"><th>Transformer</th><th>Device</th>")
                .append("<th>OTI Avg</th><th>OTI Peak</th><th>WTI Avg</th><th>WTI Peak</th><th>Data coverage</th></tr>");
        for (MonthlyReport.DeviceSection section : report.devices()) {
            List<MonthlyReport.Slot> slots = section.slots();
            html.append("<tr><td>").append(escape(section.transformerName())).append("</td><td>")
                    .append(escape(section.device().name())).append("</td><td>")
                    .append(fmt(avgOfSlots(slots, MonthlyReport.Slot::otiAvg))).append("</td><td>")
                    .append(fmt(maxOfSlots(slots, MonthlyReport.Slot::otiMax))).append("</td><td>")
                    .append(fmt(avgOfSlots(slots, MonthlyReport.Slot::wtiAvg))).append("</td><td>")
                    .append(fmt(maxOfSlots(slots, MonthlyReport.Slot::wtiMax))).append("</td><td>")
                    .append(slots.isEmpty() ? "-" : Math.round(100.0 * section.slotsWithData() / slots.size()) + "%")
                    .append("</td></tr>");
        }
        html.append("</table><p style=\"color:#64748b\">Generated automatically by TMS on ")
                .append(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm").format(ZonedDateTime.now(report.zone())))
                .append(".</p></body></html>");
        return html.toString();
    }

    static Double avgOfSlots(List<MonthlyReport.Slot> slots, Function<MonthlyReport.Slot, Double> field) {
        return avg(slots, field);
    }

    static Double maxOfSlots(List<MonthlyReport.Slot> slots, Function<MonthlyReport.Slot, Double> field) {
        return max(slots, field);
    }

    private static String fmt(Double value) {
        return value == null ? "-" : String.format(Locale.ENGLISH, "%.1f", value);
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static ZoneId parseZone(String timezone) {
        try {
            return ZoneId.of(timezone);
        } catch (DateTimeException | NullPointerException e) {
            throw new InvalidReportSettingsException("Unknown time zone: " + timezone);
        }
    }
}
