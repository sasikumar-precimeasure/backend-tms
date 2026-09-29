package com.tmsbackend.application.audit;

import com.tmsbackend.domain.model.AuditEvent;
import com.tmsbackend.domain.port.AuditEventRepositoryPort;
import java.lang.reflect.Method;
import java.time.Instant;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

// Writes the single AuditEvent every @Auditable use-case method declares,
// once that method has already returned successfully - @AfterReturning
// never fires on a thrown exception, so a failed operation (not-found,
// validation, etc.) still logs nothing, exactly like the manual
// auditEventRepository.save(...) call sites this replaces (each of those
// only ran after every prior line in its method had already succeeded).
//
// This is the one place in the codebase that both (a) resolves an
// @Auditable method's SpEL attributes and (b) decides the acting-user
// convention (a Long parameter literally named actingUserId/actingAdminId,
// or a null/system actor if neither parameter exists) - see Auditable's own
// javadoc for why that split exists.
@Aspect
@Component
public class AuditAspect {
    private static final ExpressionParser PARSER = new SpelExpressionParser();
    private static final ParameterNameDiscoverer PARAM_NAMES = new DefaultParameterNameDiscoverer();

    private final AuditEventRepositoryPort auditEventRepository;

    public AuditAspect(AuditEventRepositoryPort auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void recordAuditEvent(JoinPoint joinPoint, Auditable auditable, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        StandardEvaluationContext context = new StandardEvaluationContext();
        String[] paramNames = PARAM_NAMES.getParameterNames(method);
        Object[] args = joinPoint.getArgs();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
        }
        context.setVariable("result", result);

        Long actorId = resolveActorId(paramNames, args);

        auditEventRepository.save(new AuditEvent(
                null,
                Instant.now(),
                actorId,
                blankToNull(evaluate(auditable.deviceId(), context)),
                auditable.type(),
                blankToNull(evaluate(auditable.fieldName(), context)),
                blankToNull(evaluate(auditable.oldValue(), context)),
                blankToNull(evaluate(auditable.newValue(), context)),
                evaluate(auditable.description(), context),
                null));
    }

    private Long resolveActorId(String[] paramNames, Object[] args) {
        if (paramNames == null) {
            return null;
        }
        for (int i = 0; i < paramNames.length; i++) {
            if (("actingUserId".equals(paramNames[i]) || "actingAdminId".equals(paramNames[i]))
                    && args[i] instanceof Long actorId) {
                return actorId;
            }
        }
        return null; // System-initiated (e.g. the scheduled mail-threshold job) - no human actor.
    }

    private String evaluate(String spel, StandardEvaluationContext context) {
        if (spel.isEmpty()) {
            return null;
        }
        Expression expression = PARSER.parseExpression(spel);
        Object value = expression.getValue(context);
        return value == null ? null : String.valueOf(value);
    }

    private String blankToNull(String value) {
        return (value == null || value.isEmpty()) ? null : value;
    }
}
