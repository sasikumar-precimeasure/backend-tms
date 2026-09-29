package com.tmsbackend.application.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tmsbackend.domain.model.AuditEvent;
import com.tmsbackend.domain.model.AuditEventType;
import com.tmsbackend.domain.port.AuditEventRepositoryPort;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Lazy;

// Proves the specific failure mode Spring AOP proxies have that a full
// AspectJ compile-time weave wouldn't: a bean method calling another
// @Auditable method on itself (self-invocation) bypasses the proxy unless
// it's routed back through a Spring-managed reference to itself. This is
// exactly the shape EvaluateMailThresholdsUseCase.checkCondition ->
// recordBreach/recordMailSent has, so this test uses a minimal stand-in
// with the same self-injection pattern rather than the real use case, to
// isolate "does the AOP wiring work" from "does the mail-threshold business
// logic work" (that part is EvaluateMailThresholdsUseCaseTest's job).
class AuditAspectSelfInvocationTest {
    // Mirrors EvaluateMailThresholdsUseCase's own shape: a public method
    // that internally decides whether to call an @Auditable method, routing
    // that call through a self-injected (not `this`) reference.
    static class SelfInvokingComponent {
        private final SelfInvokingComponent self;
        private int directCallCount = 0;

        SelfInvokingComponent(@Lazy SelfInvokingComponent self) {
            this.self = self != null ? self : this;
        }

        void evaluate(String conditionKey, boolean breached) {
            if (breached) {
                self.recordBreach(conditionKey);
            }
        }

        // A getter, called through the proxy like any other method here -
        // reading the field directly off `bean` in the test would read the
        // CGLIB proxy's own field slot (a distinct object from the real
        // target the aspect's advice actually runs against), not the
        // target's value, and would always see 0 regardless of whether the
        // self-invocation fix works.
        int directCallCount() {
            return directCallCount;
        }

        @Auditable(type = AuditEventType.THRESHOLD_BREACH, fieldName = "#conditionKey", description = "'Breach: ' + #conditionKey")
        void recordBreach(String conditionKey) {
            directCallCount++;
        }
    }

    @Configuration
    @EnableAspectJAutoProxy
    static class TestConfig {
        @Bean
        AuditAspect auditAspect(AuditEventRepositoryPort auditEventRepository) {
            return new AuditAspect(auditEventRepository);
        }

        @Bean
        SelfInvokingComponent selfInvokingComponent(@Lazy SelfInvokingComponent self) {
            return new SelfInvokingComponent(self);
        }

        @Bean
        AuditEventRepositoryPort auditEventRepository() {
            return new InMemoryAuditEventRepository();
        }
    }

    static class InMemoryAuditEventRepository implements AuditEventRepositoryPort {
        final List<AuditEvent> saved = new ArrayList<>();

        @Override
        public AuditEvent save(AuditEvent event) {
            saved.add(event);
            return event;
        }

        @Override
        public List<AuditEvent> findRecent(int limit) {
            return saved;
        }

        @Override
        public List<AuditEvent> findByDevice(String deviceId, int limit) {
            return saved;
        }

        @Override
        public List<AuditEvent> findByUser(Long userId, int limit) {
            return saved;
        }
    }

    private AnnotationConfigApplicationContext context;

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void selfInjectedCallRoutesThroughTheProxyAndGetsAudited() {
        context = new AnnotationConfigApplicationContext(TestConfig.class);
        SelfInvokingComponent bean = context.getBean(SelfInvokingComponent.class);
        InMemoryAuditEventRepository auditRepo = (InMemoryAuditEventRepository) context.getBean(AuditEventRepositoryPort.class);

        bean.evaluate("OTI_HIGH", true);

        assertEquals(1, bean.directCallCount(), "the method itself should still have run");
        assertEquals(1, auditRepo.saved.size(), "self.recordBreach(...) must route through the proxy and get audited");
        AuditEvent event = auditRepo.saved.get(0);
        assertEquals(AuditEventType.THRESHOLD_BREACH, event.eventType());
        assertEquals("OTI_HIGH", event.fieldName());
        assertEquals("Breach: OTI_HIGH", event.description());
        assertTrue(event.occurredAt().isBefore(Instant.now().plusSeconds(1)));
    }

    @Test
    void noEventWhenNotBreached() {
        context = new AnnotationConfigApplicationContext(TestConfig.class);
        SelfInvokingComponent bean = context.getBean(SelfInvokingComponent.class);
        InMemoryAuditEventRepository auditRepo = (InMemoryAuditEventRepository) context.getBean(AuditEventRepositoryPort.class);

        bean.evaluate("OTI_HIGH", false);

        assertEquals(0, bean.directCallCount());
        assertEquals(0, auditRepo.saved.size());
    }
}
