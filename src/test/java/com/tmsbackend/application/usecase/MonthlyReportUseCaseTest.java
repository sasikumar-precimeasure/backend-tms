package com.tmsbackend.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tmsbackend.domain.model.Device;
import com.tmsbackend.domain.model.Device2243Reading;
import com.tmsbackend.domain.model.DeviceType;
import com.tmsbackend.domain.model.Gateway;
import com.tmsbackend.domain.model.IrtccReading;
import com.tmsbackend.domain.model.MailAttachment;
import com.tmsbackend.domain.model.MailRecipient;
import com.tmsbackend.domain.model.MailSenderSettings;
import com.tmsbackend.domain.model.MailThresholds;
import com.tmsbackend.domain.model.MonthlyReport;
import com.tmsbackend.domain.model.PagedResult;
import com.tmsbackend.domain.model.ReportRecipient;
import com.tmsbackend.domain.model.ReportSettings;
import com.tmsbackend.domain.model.Transformer;
import com.tmsbackend.domain.port.MailSenderPort;
import com.tmsbackend.domain.port.MailSettingsRepositoryPort;
import com.tmsbackend.domain.port.ReadingRepositoryPort;
import com.tmsbackend.domain.port.ReportSettingsRepositoryPort;
import com.tmsbackend.domain.port.TopologyRepositoryPort;
import com.tmsbackend.infrastructure.report.PoiReportWorkbookRenderer;
import java.io.ByteArrayInputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

// Plain unit test over in-memory fakes, same approach as
// EvaluateMailThresholdsUseCaseTest - covers slot bucketing/averaging, the
// "is a report due" schedule check, and exactly-once scheduled sending.
class MonthlyReportUseCaseTest {
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final YearMonth SEPTEMBER = YearMonth.of(2026, 9);
    private static final Device IRTCC = new Device("dev-1", "gw-1", "TR1 IRTCC", 1, DeviceType.IRTCC, true);

    private final List<IrtccReading> irtccReadings = new ArrayList<>();
    private final List<ReportRecipient> recipients = new ArrayList<>();
    private final List<List<MailAttachment>> sentAttachments = new ArrayList<>();
    private ReportSettings settings = new ReportSettings(true, 1, LocalTime.of(6, 0), "Asia/Kolkata", null, null);

    private MonthlyReportUseCase useCaseAt(Instant now) {
        TopologyRepositoryPort topology = new TopologyRepositoryPort() {
            public void upsertTransformer(Transformer transformer) {}
            public void upsertGateway(Gateway gateway) {}
            public void upsertDevice(Device device) {}
            public Optional<Device> findDevice(String deviceId) { return Optional.of(IRTCC); }
            public List<Device> findAllDevices() { return List.of(IRTCC); }
            public List<Transformer> findAllTransformers() { return List.of(new Transformer("tr-1", "TR1")); }
            public List<Gateway> findAllGateways() { return List.of(new Gateway("gw-1", "tr-1", "Gateway 1", 1, "127.0.0.1", 502)); }
        };

        ReadingRepositoryPort readings = new ReadingRepositoryPort() {
            public void saveIrtccReading(IrtccReading reading) {}
            public void saveDevice2243Reading(Device2243Reading reading) {}
            public Optional<IrtccReading> findLatestIrtcc(String deviceId) { return Optional.empty(); }
            public Optional<Device2243Reading> findLatestDevice2243(String deviceId) { return Optional.empty(); }
            public PagedResult<IrtccReading> findIrtccByDeviceAndDateRange(String d, Instant f, Instant t, int p, int s) { return null; }
            public PagedResult<Device2243Reading> findDevice2243ByDeviceAndDateRange(String d, Instant f, Instant t, int p, int s) { return null; }

            // Newest first, inclusive range - same contract as the real adapter.
            public List<IrtccReading> findAllIrtccByDeviceAndDateRange(String deviceId, Instant from, Instant to) {
                return irtccReadings.stream()
                        .filter(r -> !r.recordedAt().isBefore(from) && !r.recordedAt().isAfter(to))
                        .sorted((a, b) -> b.recordedAt().compareTo(a.recordedAt()))
                        .toList();
            }

            public List<Device2243Reading> findAllDevice2243ByDeviceAndDateRange(String deviceId, Instant from, Instant to) {
                return List.of();
            }
        };

        ReportSettingsRepositoryPort reportSettings = new ReportSettingsRepositoryPort() {
            public ReportSettings getSettings() { return settings; }
            public void saveSettings(ReportSettings s) { settings = s; }
            public void markSent(String period, Instant when) {
                settings = new ReportSettings(settings.enabled(), settings.dayOfMonth(), settings.sendTime(), settings.timezone(), period, when);
            }
            public List<ReportRecipient> findAllRecipients() { return recipients; }
            public ReportRecipient saveRecipient(ReportRecipient r) { recipients.add(r); return r; }
            public void deleteRecipient(String id) {}
        };

        MailSettingsRepositoryPort mailSettings = new MailSettingsRepositoryPort() {
            public MailSenderSettings getSenderSettings() {
                return new MailSenderSettings("TMS", "tms@example.com", "smtp.example.com", 587, "secret", true);
            }
            public void saveSenderSettings(MailSenderSettings s) {}
            public List<MailRecipient> findAllRecipients() { return List.of(); }
            public MailRecipient saveRecipient(MailRecipient r) { return r; }
            public void deleteRecipient(String id) {}
            public Optional<MailThresholds> findThresholds(String deviceId) { return Optional.empty(); }
            public void saveThresholds(MailThresholds t) {}
            public Optional<Instant> findLastSent(String deviceId, String conditionKey) { return Optional.empty(); }
            public void recordSent(String deviceId, String conditionKey, Instant when) {}
        };

        MailSenderPort mailSender = new MailSenderPort() {
            public void send(MailSenderSettings s, List<String> to, String subject, String body) {
                send(s, to, subject, body, List.of());
            }

            @Override
            public void send(MailSenderSettings s, List<String> to, String subject, String body, List<MailAttachment> attachments) {
                sentAttachments.add(attachments);
            }
        };

        return new MonthlyReportUseCase(
                topology, readings, reportSettings, mailSettings, mailSender, new PoiReportWorkbookRenderer(),
                Clock.fixed(now, ZoneOffset.UTC), null);
    }

    private static IrtccReading irtcc(LocalDateTime istTime, double oti, double wti, double tap) {
        return new IrtccReading(
                IRTCC.id(), istTime.atZone(IST).toInstant(), oti, null, wti, null, null, tap, 16.0, 3.0, 100.0, 110.0,
                "Independent", false, false, true, false, false, false, true, false, false, false, false,
                false, false, List.of(), List.of(), 999.0, 120.0, 5.0, 5.0, 10.0, 10.0, 20.0, 20.0, 30.0, 30.0,
                15.0, 10.0, 300.0, 5.0);
    }

    private static Instant ist(String localDateTime) {
        return LocalDateTime.parse(localDateTime).atZone(IST).toInstant();
    }

    @Test
    void bucketsReadingsIntoThirtyMinuteSlotsInTheReportZone() {
        irtccReadings.add(irtcc(LocalDateTime.parse("2026-09-10T10:05"), 60, 50, 4));
        irtccReadings.add(irtcc(LocalDateTime.parse("2026-09-10T10:20"), 70, 54, 5));
        irtccReadings.add(irtcc(LocalDateTime.parse("2026-09-10T10:30"), 80, 60, 6));
        irtccReadings.add(irtcc(LocalDateTime.parse("2026-09-30T23:59"), 40, 30, 1)); // last slot of the month
        irtccReadings.add(irtcc(LocalDateTime.parse("2026-10-01T00:00"), 99, 99, 9)); // next month - excluded

        MonthlyReport report = useCaseAt(ist("2026-10-08T12:00")).buildReport(SEPTEMBER, IST);

        List<MonthlyReport.Slot> slots = report.devices().get(0).slots();
        assertEquals(30 * 48, slots.size());
        assertEquals("TR1", report.devices().get(0).transformerName());

        MonthlyReport.Slot tenAm = slots.stream().filter(s -> s.start().equals(LocalDateTime.parse("2026-09-10T10:00"))).findFirst().orElseThrow();
        assertEquals(2, tenAm.sampleCount());
        assertEquals(65.0, tenAm.otiAvg());
        assertEquals(70.0, tenAm.otiMax());
        assertEquals(52.0, tenAm.wtiAvg());
        assertEquals(54.0, tenAm.wtiMax());
        assertEquals(5.0, tenAm.tapPosition()); // last reading in the slot, not an average

        MonthlyReport.Slot tenThirty = slots.stream().filter(s -> s.start().equals(LocalDateTime.parse("2026-09-10T10:30"))).findFirst().orElseThrow();
        assertEquals(1, tenThirty.sampleCount());
        assertEquals(80.0, tenThirty.otiAvg());

        MonthlyReport.Slot last = slots.get(slots.size() - 1);
        assertEquals(LocalDateTime.parse("2026-09-30T23:30"), last.start());
        assertEquals(40.0, last.otiAvg());

        assertEquals(3, report.devices().get(0).slotsWithData());
    }

    @Test
    void reportIsDueOnlyAfterTheScheduledDayAndTime() {
        assertNull(MonthlyReportUseCase.dueReportMonth(settings, ist("2026-10-01T05:59")));
        assertEquals(SEPTEMBER, MonthlyReportUseCase.dueReportMonth(settings, ist("2026-10-01T06:00")));
        assertEquals(SEPTEMBER, MonthlyReportUseCase.dueReportMonth(settings, ist("2026-10-20T00:00")));
        assertEquals(YearMonth.of(2026, 12), MonthlyReportUseCase.dueReportMonth(settings, ist("2027-01-01T06:30")));
    }

    @Test
    void scheduledSendHappensExactlyOncePerMonth() {
        recipients.add(new ReportRecipient("r1", "Ops", "ops@example.com", true));
        irtccReadings.add(irtcc(LocalDateTime.parse("2026-09-10T10:05"), 60, 50, 4));

        useCaseAt(ist("2026-10-01T05:59")).runScheduled();
        assertEquals(0, sentAttachments.size(), "not due yet");

        useCaseAt(ist("2026-10-01T06:00")).runScheduled();
        assertEquals(1, sentAttachments.size());
        assertEquals("2026-09", settings.lastSentPeriod());
        assertEquals("transformer-report-2026-09.xlsx", sentAttachments.get(0).get(0).fileName());

        useCaseAt(ist("2026-10-01T06:01")).runScheduled();
        useCaseAt(ist("2026-10-15T09:00")).runScheduled();
        assertEquals(1, sentAttachments.size(), "same month must not be sent twice");
    }

    @Test
    void disabledScheduleNeverSends() {
        recipients.add(new ReportRecipient("r1", "Ops", "ops@example.com", true));
        settings = new ReportSettings(false, 1, LocalTime.of(6, 0), "Asia/Kolkata", null, null);

        useCaseAt(ist("2026-10-01T07:00")).runScheduled();

        assertEquals(0, sentAttachments.size());
    }

    @Test
    void noRecipientsMeansNotMarkedSentSoItRetriesLater() {
        recipients.add(new ReportRecipient("r1", "Ops", "ops@example.com", false)); // disabled

        useCaseAt(ist("2026-10-01T06:00")).runScheduled();

        assertEquals(0, sentAttachments.size());
        assertNull(settings.lastSentPeriod());
    }

    @Test
    void renderedWorkbookHasSummaryAndOneSheetPerDevice() throws Exception {
        irtccReadings.add(irtcc(LocalDateTime.parse("2026-09-10T10:05"), 60, 50, 4));

        byte[] bytes = useCaseAt(ist("2026-10-08T12:00")).renderReport(SEPTEMBER);

        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertEquals(2, wb.getNumberOfSheets());
            assertEquals("Summary", wb.getSheetName(0));
            assertTrue(wb.getSheetName(1).contains("TR1 IRTCC"));
            // title, subtitle, header + one row per 30-min slot
            assertEquals(3 + 30 * 48, wb.getSheetAt(1).getPhysicalNumberOfRows());
            assertEquals("TR1", wb.getSheetAt(0).getRow(4).getCell(0).getStringCellValue());
            assertEquals(60.0, wb.getSheetAt(0).getRow(4).getCell(3).getNumericCellValue());
        }
    }
}
