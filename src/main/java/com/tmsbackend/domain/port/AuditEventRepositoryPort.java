package com.tmsbackend.domain.port;

import com.tmsbackend.domain.model.AuditEvent;
import java.time.Instant;
import java.util.List;

public interface AuditEventRepositoryPort {
    AuditEvent save(AuditEvent event);

    List<AuditEvent> findRecent(int limit);

    List<AuditEvent> findByDevice(String deviceId, int limit);

    List<AuditEvent> findByUser(Long userId, int limit);

    // deviceId is optional (null = every device) - from/to are always
    // required, since this is only ever called once a caller has picked a
    // date range (see AuditEventController.recent's occurredAt-based
    // overload).
    List<AuditEvent> findByDateRange(String deviceId, Instant from, Instant to, int limit);
}
