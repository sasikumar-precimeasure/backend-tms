package com.tmsbackend.config;

import com.tmsbackend.application.usecase.EvaluateMailThresholdsUseCase;
import com.tmsbackend.application.usecase.MonthlyReportUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class SchedulingConfig {
    private static final Logger log = LoggerFactory.getLogger(SchedulingConfig.class);

    private final EvaluateMailThresholdsUseCase evaluateMailThresholdsUseCase;
    private final MonthlyReportUseCase monthlyReportUseCase;

    public SchedulingConfig(
            EvaluateMailThresholdsUseCase evaluateMailThresholdsUseCase, MonthlyReportUseCase monthlyReportUseCase) {
        this.evaluateMailThresholdsUseCase = evaluateMailThresholdsUseCase;
        this.monthlyReportUseCase = monthlyReportUseCase;
    }

    // Every minute: compare each device's latest reading against its own
    // mail thresholds and send alert emails where a breach isn't already
    // suppressed by its re-alert interval - see EvaluateMailThresholdsUseCase.
    @Scheduled(fixedRate = 60_000)
    public void evaluateMailThresholds() {
        try {
            evaluateMailThresholdsUseCase.execute();
        } catch (Exception e) {
            // A single bad tick (e.g. SMTP momentarily unreachable) must
            // never kill the scheduler - log and try again next minute.
            log.error("Mail threshold evaluation failed", e);
        }
    }

    // Every minute: send last month's report once its scheduled day/time
    // has passed - exactly-once per month, see MonthlyReportUseCase.
    @Scheduled(fixedRate = 60_000, initialDelay = 30_000)
    public void sendMonthlyReport() {
        try {
            monthlyReportUseCase.runScheduled();
        } catch (Exception e) {
            log.error("Monthly report check failed", e);
        }
    }
}
