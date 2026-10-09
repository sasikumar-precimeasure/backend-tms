package com.tmsbackend.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tmsbackend.domain.model.Device;
import com.tmsbackend.domain.model.Device2243Reading;
import com.tmsbackend.domain.model.DeviceType;
import com.tmsbackend.domain.model.Gateway;
import com.tmsbackend.domain.model.IrtccReading;
import com.tmsbackend.domain.model.MailRecipient;
import com.tmsbackend.domain.model.MailSenderSettings;
import com.tmsbackend.domain.model.MailThresholds;
import com.tmsbackend.domain.model.PagedResult;
import com.tmsbackend.domain.model.Transformer;
import com.tmsbackend.domain.port.MailSenderPort;
import com.tmsbackend.domain.port.MailSettingsRepositoryPort;
import com.tmsbackend.domain.port.ReadingRepositoryPort;
import com.tmsbackend.domain.port.TopologyRepositoryPort;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// Fakes over mocks here - the port interfaces are small and the logic under
// test (threshold comparison + re-alert suppression) reads much more
// clearly against real in-memory state than a wall of when(...). This is a
// plain (non-Spring) unit test, so @Auditable/AuditAspect never fires here -
// there's no AOP proxy without a Spring context - which is fine, since what
// this test verifies is the threshold/suppression logic, not auditing.
// AopSelfInvocationTest separately verifies, through a real Spring context,
// that self.recordBreach(...)/self.recordMailSent(...) actually route
// through the proxy and get audited.
class EvaluateMailThresholdsUseCaseTest {
    private static final String DEVICE_ID = "device-1";

    private final AtomicReference<Instant> now = new AtomicReference<>(Instant.parse("2026-01-01T00:00:00Z"));
    private final Clock clock = Clock.fixed(now.get(), ZoneOffset.UTC);

    private final Map<String, IrtccReading> latestReadings = new HashMap<>();
    private final Map<String, Instant> lastSent = new HashMap<>();
    private final List<String> sentSubjects = new ArrayList<>();

    private MailThresholds thresholds;
    private MailSenderSettings senderSettings;
    private List<MailRecipient> recipients;

    private EvaluateMailThresholdsUseCase useCase;

    @BeforeEach
    void setUp() {
        thresholds = new MailThresholds(DEVICE_ID, 80, 85, 140, 95, 15, 1, 10);
        senderSettings = new MailSenderSettings("Alerts", "alerts@example.com", "smtp.example.com", 587, "pw", true);
        recipients = List.of(new MailRecipient("r1", "Ops", "ops@example.com", true, List.of(DEVICE_ID)));

        TopologyRepositoryPort topologyRepository = new TopologyRepositoryPort() {
            @Override
            public void upsertTransformer(com.tmsbackend.domain.model.Transformer transformer) {
            }

            @Override
            public void upsertGateway(com.tmsbackend.domain.model.Gateway gateway) {
            }

            @Override
            public void upsertDevice(Device device) {
            }

            @Override
            public Optional<Device> findDevice(String deviceId) {
                return Optional.of(new Device(DEVICE_ID, "gw-1", "TR1 IRTCC", 1, DeviceType.IRTCC, true));
            }

            @Override
            public List<Device> findAllDevices() {
                return List.of(new Device(DEVICE_ID, "gw-1", "TR1 IRTCC", 1, DeviceType.IRTCC, true));
            }

            @Override
            public List<Transformer> findAllTransformers() {
                return List.of();
            }

            @Override
            public List<Gateway> findAllGateways() {
                return List.of();
            }

            @Override
            public void markDevicesSeen(Collection<String> deviceIds, Instant at) {
            }

            @Override
            public Set<String> findCurrentDeviceIds() {
                return Set.of();
            }
        };

        ReadingRepositoryPort readingRepository = new ReadingRepositoryPort() {
            @Override
            public void saveIrtccReading(IrtccReading reading) {
                latestReadings.put(reading.deviceId(), reading);
            }

            @Override
            public void saveDevice2243Reading(Device2243Reading reading) {
            }

            @Override
            public Optional<IrtccReading> findLatestIrtcc(String deviceId) {
                return Optional.ofNullable(latestReadings.get(deviceId));
            }

            @Override
            public Optional<Device2243Reading> findLatestDevice2243(String deviceId) {
                return Optional.empty();
            }

            @Override
            public PagedResult<IrtccReading> findIrtccByDeviceAndDateRange(
                    String deviceId, Instant from, Instant to, int page, int pageSize) {
                return null;
            }

            @Override
            public PagedResult<Device2243Reading> findDevice2243ByDeviceAndDateRange(
                    String deviceId, Instant from, Instant to, int page, int pageSize) {
                return null;
            }

            @Override
            public List<IrtccReading> findAllIrtccByDeviceAndDateRange(String deviceId, Instant from, Instant to) {
                return List.of();
            }

            @Override
            public List<Device2243Reading> findAllDevice2243ByDeviceAndDateRange(String deviceId, Instant from, Instant to) {
                return List.of();
            }
        };

        MailSettingsRepositoryPort mailSettingsRepository = new MailSettingsRepositoryPort() {
            @Override
            public MailSenderSettings getSenderSettings() {
                return senderSettings;
            }

            @Override
            public void saveSenderSettings(MailSenderSettings settings) {
            }

            @Override
            public List<MailRecipient> findAllRecipients() {
                return recipients;
            }

            @Override
            public MailRecipient saveRecipient(MailRecipient recipient) {
                return recipient;
            }

            @Override
            public void deleteRecipient(String id) {
            }

            @Override
            public Optional<MailThresholds> findThresholds(String deviceId) {
                return Optional.of(thresholds);
            }

            @Override
            public void saveThresholds(MailThresholds thresholds) {
            }

            @Override
            public Optional<Instant> findLastSent(String deviceId, String conditionKey) {
                return Optional.ofNullable(lastSent.get(conditionKey));
            }

            @Override
            public void recordSent(String deviceId, String conditionKey, Instant when) {
                lastSent.put(conditionKey, when);
            }
        };

        MailSenderPort mailSender = (settings, toAddresses, subject, htmlBody) -> sentSubjects.add(subject);

        // Pass null for `self` - the production constructor falls back to
        // `this` when there's no Spring-proxied instance to inject (see its
        // own javadoc), which is exactly the plain-unit-test situation here.
        // That makes self.recordBreach(...) equivalent to a direct call
        // (un-audited, since there's no proxy either way - see the
        // class-level comment), which is fine: this test only checks
        // mail-sending and suppression behavior.
        useCase = new EvaluateMailThresholdsUseCase(
                topologyRepository, readingRepository, mailSettingsRepository, mailSender, clock, null);
    }

    private IrtccReading readingWithOti(double otiTemperature) {
        return new IrtccReading(
                DEVICE_ID, now.get(), otiTemperature, null, 50.0, null, null, 5.0, 16.0, 3.0, 100.0, 110.0,
                "Independent", false, false, true, false, false, false, true, false, false, false, false,
                false, false, List.of(), List.of(), 999.0, 120.0, 5.0, 5.0, 10.0, 10.0, 20.0, 20.0, 30.0, 30.0,
                15.0, 10.0, 300.0, 5.0);
    }

    @Test
    void sendsAlertWhenOtiExceedsThreshold() {
        latestReadings.put(DEVICE_ID, readingWithOti(95.0)); // threshold is 80

        useCase.execute();

        assertEquals(1, sentSubjects.size());
        assertTrue(sentSubjects.get(0).contains("Oil Temperature"));
    }

    @Test
    void doesNotSendWhenBelowThreshold() {
        latestReadings.put(DEVICE_ID, readingWithOti(60.0)); // below 80

        useCase.execute();

        assertEquals(0, sentSubjects.size());
    }

    @Test
    void suppressesResendWithinReAlertWindow() {
        latestReadings.put(DEVICE_ID, readingWithOti(95.0));

        useCase.execute(); // first breach - sends
        assertEquals(1, sentSubjects.size());

        useCase.execute(); // same minute, still breached - must NOT resend (mailTimeMinutes=10)
        assertEquals(1, sentSubjects.size());
    }

    @Test
    void resendsAfterReAlertWindowElapses() {
        latestReadings.put(DEVICE_ID, readingWithOti(95.0));
        useCase.execute();
        assertEquals(1, sentSubjects.size());

        // Simulate 11 minutes passing by directly advancing the recorded
        // last-sent timestamp into the past, since Clock.fixed can't tick -
        // this exercises the same Duration.between(...) comparison the real
        // scheduler would see after enough wall-clock time passed.
        lastSent.put("OTI_HIGH", now.get().minus(Duration.ofMinutes(11)));

        useCase.execute();
        assertEquals(2, sentSubjects.size());
    }
}
