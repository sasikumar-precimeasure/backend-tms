package com.tmsbackend.infrastructure.persistence.adapter;

import com.tmsbackend.domain.model.AuditEvent;
import com.tmsbackend.domain.port.AuditEventRepositoryPort;
import com.tmsbackend.infrastructure.persistence.entity.AuditEventEntity;
import com.tmsbackend.infrastructure.persistence.repository.AuditEventJpaRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class AuditEventRepositoryAdapter implements AuditEventRepositoryPort {
    private final AuditEventJpaRepository jpaRepository;

    public AuditEventRepositoryAdapter(AuditEventJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public AuditEvent save(AuditEvent event) {
        AuditEventEntity entity = new AuditEventEntity();
        entity.setOccurredAt(event.occurredAt());
        entity.setUserId(event.userId());
        entity.setDeviceId(event.deviceId());
        entity.setEventType(event.eventType());
        entity.setFieldName(event.fieldName());
        entity.setOldValue(event.oldValue());
        entity.setNewValue(event.newValue());
        entity.setDescription(event.description());
        entity.setRawPayload(event.rawPayloadJson());
        AuditEventEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<AuditEvent> findRecent(int limit) {
        return jpaRepository.findAllByOrderByOccurredAtDesc(PageRequest.of(0, limit)).stream().map(this::toDomain).toList();
    }

    @Override
    public List<AuditEvent> findByDevice(String deviceId, int limit) {
        return jpaRepository.findByDeviceIdOrderByOccurredAtDesc(deviceId, PageRequest.of(0, limit)).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<AuditEvent> findByUser(Long userId, int limit) {
        return jpaRepository.findByUserIdOrderByOccurredAtDesc(userId, PageRequest.of(0, limit)).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<AuditEvent> findByDateRange(String deviceId, Instant from, Instant to, int limit) {
        PageRequest page = PageRequest.of(0, limit);
        List<AuditEventEntity> entities = deviceId != null
                ? jpaRepository.findByDeviceIdAndOccurredAtBetweenOrderByOccurredAtDesc(deviceId, from, to, page)
                : jpaRepository.findByOccurredAtBetweenOrderByOccurredAtDesc(from, to, page);
        return entities.stream().map(this::toDomain).toList();
    }

    private AuditEvent toDomain(AuditEventEntity e) {
        return new AuditEvent(
                e.getId(), e.getOccurredAt(), e.getUserId(), e.getDeviceId(), e.getEventType(),
                e.getFieldName(), e.getOldValue(), e.getNewValue(), e.getDescription(), e.getRawPayload());
    }
}
