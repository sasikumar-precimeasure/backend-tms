package com.tmsbackend.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// A real system clock in production; injectable so
// EvaluateMailThresholdsUseCase is unit-testable with a fixed time without
// needing a real scheduler tick.
@Configuration
public class ClockConfig {
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
