package com.tmsbackend.application.audit;

import com.tmsbackend.domain.model.AuditEventType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Declares that a use-case method, once it returns successfully, should
// write exactly one AuditEvent row - AuditAspect does the actual write. A
// thrown exception (validation failure, not-found, etc.) means no event is
// recorded, matching the old hand-written auditEventRepository.save(...)
// call sites this replaces, which only ever ran after every prior line in
// the method had already succeeded.
//
// Every string attribute is a SpEL expression evaluated against the
// method's own parameters (by name, e.g. "#userName") and, only in
// `description`/`fieldName`/`oldValue`/`newValue`/`deviceId`, the method's
// return value as `#result`. Leave an attribute as "" for "this event has
// no such field" (stored as a null column) rather than a literal empty
// string.
//
// The actor is NOT an attribute here: AuditAspect resolves it by convention,
// looking for a Long-typed method parameter named "actingUserId" or
// "actingAdminId" and falling back to a null (system) actor for methods
// with neither (e.g. the scheduled mail-threshold job, which has no human
// caller). This keeps every annotation focused on "what changed", not
// "who's asking", the same split the removed manual call sites already had.
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Auditable {
    AuditEventType type();

    // SpEL - required, since every audit row needs a human-readable summary.
    String description();

    // SpEL - "" (default) means no field name for this event.
    String fieldName() default "";

    // SpEL - "" (default) means no old value for this event.
    String oldValue() default "";

    // SpEL - "" (default) means no new value for this event.
    String newValue() default "";

    // SpEL - "" (default) means no device id for this event.
    String deviceId() default "";
}
