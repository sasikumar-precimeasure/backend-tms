package com.tmsbackend.application.usecase;

import com.tmsbackend.application.audit.Auditable;
import com.tmsbackend.domain.model.AuditEventType;
import com.tmsbackend.domain.model.Device;
import com.tmsbackend.domain.model.Device2243Reading;
import com.tmsbackend.domain.model.DeviceType;
import com.tmsbackend.domain.model.IrtccReading;
import com.tmsbackend.domain.model.MailRecipient;
import com.tmsbackend.domain.model.MailSenderSettings;
import com.tmsbackend.domain.model.MailThresholds;
import com.tmsbackend.domain.port.MailSenderPort;
import com.tmsbackend.domain.port.MailSettingsRepositoryPort;
import com.tmsbackend.domain.port.ReadingRepositoryPort;
import com.tmsbackend.domain.port.TopologyRepositoryPort;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

// Runs every minute (see config.SchedulingConfig): for each device, compares
// its latest reading against its own mail_thresholds row (same OTI/WTI/AVR/
// tap comparisons Form1.txt's MailTimer_Tick made) and, on a
// breach not already re-alerted within mailTimeMinutes, sends one email per
// enabled recipient opted into that device and records both a THRESHOLD_
// BREACH and a MAIL_SENT audit event. `Clock` is injected (not
// Instant.now() directly) so this is unit-testable with a fixed time
// without needing a real scheduler tick.
//
// A 2243 device is checked for OTI High / WTI High only - it has no AVR or
// tap position, so those thresholds don't apply to it (and aren't shown for
// it in Mail Configuration).
@Component
public class EvaluateMailThresholdsUseCase {
    private final TopologyRepositoryPort topologyRepository;
    private final ReadingRepositoryPort readingRepository;
    private final MailSettingsRepositoryPort mailSettingsRepository;
    private final MailSenderPort mailSender;
    private final Clock clock;
    // Self-injected via the Spring-managed (AOP-proxied) bean, not `this` -
    // Spring AOP proxies only intercept calls arriving from outside the
    // bean, so calling this.recordBreach(...)/this.recordMailSent(...)
    // directly would bypass AuditAspect entirely and silently drop those
    // audit events. Routing the call through `self` sends it back in via
    // the proxy, the standard fix for this well-known Spring AOP
    // self-invocation limitation. @Lazy breaks the circular-bean-creation
    // dependency this would otherwise cause at startup.
    //
    // A null `self` (only possible outside the Spring container, e.g. a
    // plain unit test) falls back to `this` - there's no proxy to route
    // through in that case anyway, so a direct call is equivalent, just
    // un-audited.
    private final EvaluateMailThresholdsUseCase self;

    public EvaluateMailThresholdsUseCase(
            TopologyRepositoryPort topologyRepository,
            ReadingRepositoryPort readingRepository,
            MailSettingsRepositoryPort mailSettingsRepository,
            MailSenderPort mailSender,
            Clock clock,
            @Lazy EvaluateMailThresholdsUseCase self) {
        this.topologyRepository = topologyRepository;
        this.readingRepository = readingRepository;
        this.mailSettingsRepository = mailSettingsRepository;
        this.mailSender = mailSender;
        this.clock = clock;
        this.self = self != null ? self : this;
    }

    public void execute() {
        MailSenderSettings senderSettings = mailSettingsRepository.getSenderSettings();
        if (senderSettings.smtpHost().isBlank()) {
            return; // Not configured yet for this install.
        }

        List<MailRecipient> allRecipients = mailSettingsRepository.findAllRecipients();

        for (Device device : topologyRepository.findAllDevices()) {
            if (!device.enabled()) {
                continue;
            }
            if (device.deviceType() == DeviceType.IRTCC) {
                evaluateDevice(device, senderSettings, allRecipients);
            } else if (device.deviceType() == DeviceType.DEVICE_2243) {
                evaluate2243Device(device, senderSettings, allRecipients);
            }
        }
    }

    private void evaluateDevice(Device device, MailSenderSettings senderSettings, List<MailRecipient> allRecipients) {
        Optional<MailThresholds> thresholdsOpt = mailSettingsRepository.findThresholds(device.id());
        Optional<IrtccReading> readingOpt = readingRepository.findLatestIrtcc(device.id());
        if (thresholdsOpt.isEmpty() || readingOpt.isEmpty()) {
            return;
        }
        MailThresholds thresholds = thresholdsOpt.get();
        IrtccReading reading = readingOpt.get();

        checkTemperatures(device, thresholds, senderSettings, allRecipients, reading.otiTemperature(), reading.wtiTemperature());

        boolean avrBreach = reading.actualPtVoltage() != null
                && (reading.actualPtVoltage() > thresholds.avrHigh() || reading.actualPtVoltage() < thresholds.avrLow());
        checkCondition(device, thresholds, senderSettings, allRecipients, "AVR_VOLTAGE",
                avrBreach, "Voltage threshold exceeded",
                "PT Voltage: " + reading.actualPtVoltage()
                        + " (range " + thresholds.avrLow() + "-" + thresholds.avrHigh() + ")");

        boolean tapBreach = reading.tapPosition() != null
                && (reading.tapPosition() > thresholds.tapHigh() || reading.tapPosition() < thresholds.tapLow());
        checkCondition(device, thresholds, senderSettings, allRecipients, "TAP_POSITION",
                tapBreach, "Tap position threshold exceeded",
                "Tap Position: " + reading.tapPosition()
                        + " (range " + thresholds.tapLow() + "-" + thresholds.tapHigh() + ")");
    }

    private void evaluate2243Device(Device device, MailSenderSettings senderSettings, List<MailRecipient> allRecipients) {
        Optional<MailThresholds> thresholdsOpt = mailSettingsRepository.findThresholds(device.id());
        Optional<Device2243Reading> readingOpt = readingRepository.findLatestDevice2243(device.id());
        if (thresholdsOpt.isEmpty() || readingOpt.isEmpty()) {
            return;
        }
        Device2243Reading reading = readingOpt.get();
        checkTemperatures(device, thresholdsOpt.get(), senderSettings, allRecipients, reading.otiTemperature(), reading.wtiTemperature());
    }

    private void checkTemperatures(
            Device device,
            MailThresholds thresholds,
            MailSenderSettings senderSettings,
            List<MailRecipient> allRecipients,
            Double otiTemperature,
            Double wtiTemperature) {
        checkCondition(device, thresholds, senderSettings, allRecipients, "OTI_HIGH",
                otiTemperature != null && otiTemperature > thresholds.otiTempHigh(),
                "Oil Temperature threshold exceeded",
                "OTI Temperature: " + otiTemperature + " (threshold " + thresholds.otiTempHigh() + ")");

        checkCondition(device, thresholds, senderSettings, allRecipients, "WTI_HIGH",
                wtiTemperature != null && wtiTemperature > thresholds.wtiTempHigh(),
                "Winding Temperature threshold exceeded",
                "WTI Temperature: " + wtiTemperature + " (threshold " + thresholds.wtiTempHigh() + ")");
    }

    private void checkCondition(
            Device device,
            MailThresholds thresholds,
            MailSenderSettings senderSettings,
            List<MailRecipient> allRecipients,
            String conditionKey,
            boolean breached,
            String subjectSuffix,
            String bodyDetail) {
        if (!breached) {
            return;
        }

        Instant now = Instant.now(clock);
        Optional<Instant> lastSent = mailSettingsRepository.findLastSent(device.id(), conditionKey);
        if (lastSent.isPresent()
                && Duration.between(lastSent.get(), now).toMinutes() < thresholds.mailTimeMinutes()) {
            return; // Still within the re-alert suppression window.
        }

        List<String> recipientEmails = allRecipients.stream()
                .filter(MailRecipient::enabled)
                .filter(r -> r.deviceIds().contains(device.id()))
                .map(MailRecipient::email)
                .toList();

        self.recordBreach(device, conditionKey, subjectSuffix);

        if (recipientEmails.isEmpty()) {
            return; // Breach recorded, but nobody is subscribed to hear about it.
        }

        String subject = device.name() + " " + subjectSuffix;
        String body = "<html><body><b>Dear Sir/Madam,</b><br/><p>" + bodyDetail + "</p></body></html>";
        mailSender.send(senderSettings, recipientEmails, subject, body);

        mailSettingsRepository.recordSent(device.id(), conditionKey, now);
        self.recordMailSent(device, conditionKey, subjectSuffix);
    }

    // Split out of checkCondition so each is its own always-exactly-one-event
    // @Auditable method - checkCondition's own branching (suppression
    // window, no recipients subscribed) decides whether these get called at
    // all, but once called each unconditionally logs its one event, which is
    // what the annotation model requires. No actingUserId/actingAdminId
    // parameter on either - AuditAspect logs a null (system) actor for a
    // scheduled job with no human caller, exactly like the removed manual
    // auditEventRepository.save(..., null, ...) calls did. Package-private
    // (not private) since `self` calls these through the proxy, which
    // requires them to be visible outside strict `this`-only invocation.
    @Auditable(
            type = AuditEventType.THRESHOLD_BREACH,
            fieldName = "#conditionKey",
            deviceId = "#device.id()",
            description = "#device.name() + ' - ' + #subjectSuffix")
    void recordBreach(Device device, String conditionKey, String subjectSuffix) {
        // No body - this method exists only to be the @Auditable join point.
    }

    @Auditable(
            type = AuditEventType.MAIL_SENT,
            fieldName = "#conditionKey",
            deviceId = "#device.id()",
            description = "'Alert email sent for ' + #device.name() + ' (' + #subjectSuffix + ')'")
    void recordMailSent(Device device, String conditionKey, String subjectSuffix) {
        // No body - this method exists only to be the @Auditable join point.
    }
}
