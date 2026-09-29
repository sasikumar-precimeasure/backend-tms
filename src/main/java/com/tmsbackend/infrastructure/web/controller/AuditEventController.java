package com.tmsbackend.infrastructure.web.controller;

import com.tmsbackend.application.usecase.RecordAuditEventUseCase;
import com.tmsbackend.domain.port.AuditEventRepositoryPort;
import com.tmsbackend.infrastructure.web.CurrentUserResolver;
import com.tmsbackend.infrastructure.web.PermissionGuard;
import com.tmsbackend.infrastructure.web.dto.AuditEventDto;
import com.tmsbackend.infrastructure.web.dto.AuditEventRequestDto;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tms/api/audit-events")
public class AuditEventController {
    private static final String MENU = "Audit Log";

    private final RecordAuditEventUseCase recordAuditEventUseCase;
    private final AuditEventRepositoryPort auditEventRepository;
    private final CurrentUserResolver currentUserResolver;
    private final PermissionGuard permissionGuard;

    public AuditEventController(
            RecordAuditEventUseCase recordAuditEventUseCase,
            AuditEventRepositoryPort auditEventRepository,
            CurrentUserResolver currentUserResolver,
            PermissionGuard permissionGuard) {
        this.recordAuditEventUseCase = recordAuditEventUseCase;
        this.auditEventRepository = auditEventRepository;
        this.currentUserResolver = currentUserResolver;
        this.permissionGuard = permissionGuard;
    }

    // Intentionally ungated beyond "is logged in" - every user's own normal
    // actions (annunciation ack, AVR changes, ...) call this to record
    // themselves, and gating it on the Audit Log permission would silently
    // break the whole audit trail for everyone except whoever can view it.
    @PostMapping
    public void record(@Valid @RequestBody AuditEventRequestDto request) {
        recordAuditEventUseCase.execute(currentUserResolver.requireUserId(), request.toDomain());
    }

    // `from`/`to` are both optional but only meaningful together - if either
    // is omitted this falls back to the existing "most recent N" behavior
    // (findRecent) rather than a date-bounded query, since a from-only or
    // to-only range is ambiguous (open-ended into the future/past) and the
    // frontend's date-range filter always supplies both once the user picks
    // one end.
    @GetMapping
    public List<AuditEventDto> recent(
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to) {
        permissionGuard.requireRead(currentUserResolver.requireUserId(), MENU);
        if (from != null && to != null) {
            return auditEventRepository.findByDateRange(null, from, to, limit).stream().map(AuditEventDto::from).toList();
        }
        return auditEventRepository.findRecent(limit).stream().map(AuditEventDto::from).toList();
    }

    @GetMapping("/device/{deviceId}")
    public List<AuditEventDto> byDevice(
            @PathVariable String deviceId,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to) {
        permissionGuard.requireRead(currentUserResolver.requireUserId(), MENU);
        if (from != null && to != null) {
            return auditEventRepository.findByDateRange(deviceId, from, to, limit).stream().map(AuditEventDto::from).toList();
        }
        return auditEventRepository.findByDevice(deviceId, limit).stream().map(AuditEventDto::from).toList();
    }
}
