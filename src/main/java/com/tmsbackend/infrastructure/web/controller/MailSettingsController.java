package com.tmsbackend.infrastructure.web.controller;

import com.tmsbackend.application.usecase.ManageMailSettingsUseCase;
import com.tmsbackend.infrastructure.web.CurrentUserResolver;
import com.tmsbackend.infrastructure.web.PermissionGuard;
import com.tmsbackend.infrastructure.web.dto.MailRecipientDto;
import com.tmsbackend.infrastructure.web.dto.MailSenderSettingsDto;
import com.tmsbackend.infrastructure.web.dto.MailThresholdsDto;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Backs the frontend's Mail Configuration screen - the source of truth the
// scheduled EvaluateMailThresholdsUseCase job reads from.
@RestController
@RequestMapping("/tms/api/mail-settings")
public class MailSettingsController {
    // Viewing needs read, every change needs write on this menu - it had no
    // checks at all, so a read-only role could change sender, recipients and
    // alert thresholds.
    private static final String MENU = "Mail Configuration";

    private final ManageMailSettingsUseCase manageMailSettingsUseCase;
    private final CurrentUserResolver currentUserResolver;
    private final PermissionGuard permissionGuard;

    public MailSettingsController(
            ManageMailSettingsUseCase manageMailSettingsUseCase, CurrentUserResolver currentUserResolver, PermissionGuard permissionGuard) {
        this.manageMailSettingsUseCase = manageMailSettingsUseCase;
        this.currentUserResolver = currentUserResolver;
        this.permissionGuard = permissionGuard;
    }

    private Long requireRead() {
        Long userId = currentUserResolver.requireUserId();
        permissionGuard.requireRead(userId, MENU);
        return userId;
    }

    private Long requireWrite() {
        Long userId = currentUserResolver.requireUserId();
        permissionGuard.requireWrite(userId, MENU);
        return userId;
    }

    @GetMapping("/sender")
    public MailSenderSettingsDto getSender() {
        requireRead();
        return MailSenderSettingsDto.from(manageMailSettingsUseCase.getSenderSettings());
    }

    @PutMapping("/sender")
    public void saveSender(@Valid @RequestBody MailSenderSettingsDto request) {
        manageMailSettingsUseCase.saveSenderSettings(requireWrite(), request.toDomain());
    }

    @GetMapping("/recipients")
    public List<MailRecipientDto> listRecipients() {
        requireRead();
        return manageMailSettingsUseCase.listRecipients().stream().map(MailRecipientDto::from).toList();
    }

    @PostMapping("/recipients")
    public MailRecipientDto saveRecipient(@Valid @RequestBody MailRecipientDto request) {
        return MailRecipientDto.from(manageMailSettingsUseCase.saveRecipient(requireWrite(), request.toDomain()));
    }

    @DeleteMapping("/recipients/{id}")
    public void deleteRecipient(@PathVariable String id) {
        manageMailSettingsUseCase.deleteRecipient(requireWrite(), id);
    }

    @GetMapping("/thresholds/{deviceId}")
    public MailThresholdsDto getThresholds(@PathVariable String deviceId) {
        requireRead();
        var thresholds = manageMailSettingsUseCase.getThresholds(deviceId);
        return thresholds != null ? MailThresholdsDto.from(thresholds) : null;
    }

    @PutMapping("/thresholds")
    public void saveThresholds(@Valid @RequestBody MailThresholdsDto request) {
        manageMailSettingsUseCase.saveThresholds(requireWrite(), request.toDomain());
    }
}
