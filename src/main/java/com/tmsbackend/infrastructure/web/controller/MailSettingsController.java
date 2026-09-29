package com.tmsbackend.infrastructure.web.controller;

import com.tmsbackend.application.usecase.ManageMailSettingsUseCase;
import com.tmsbackend.infrastructure.web.CurrentUserResolver;
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

// Backs the frontend's Mail Configuration screen (currently persisting only
// to localStorage) - once the frontend's mailSettings/slice.ts also calls
// these endpoints on save, this becomes the source of truth the scheduled
// EvaluateMailThresholdsUseCase job reads from.
@RestController
@RequestMapping("/tms/api/mail-settings")
public class MailSettingsController {
    private final ManageMailSettingsUseCase manageMailSettingsUseCase;
    private final CurrentUserResolver currentUserResolver;

    public MailSettingsController(ManageMailSettingsUseCase manageMailSettingsUseCase, CurrentUserResolver currentUserResolver) {
        this.manageMailSettingsUseCase = manageMailSettingsUseCase;
        this.currentUserResolver = currentUserResolver;
    }

    @GetMapping("/sender")
    public MailSenderSettingsDto getSender() {
        return MailSenderSettingsDto.from(manageMailSettingsUseCase.getSenderSettings());
    }

    @PutMapping("/sender")
    public void saveSender(@Valid @RequestBody MailSenderSettingsDto request) {
        manageMailSettingsUseCase.saveSenderSettings(currentUserResolver.requireUserId(), request.toDomain());
    }

    @GetMapping("/recipients")
    public List<MailRecipientDto> listRecipients() {
        return manageMailSettingsUseCase.listRecipients().stream().map(MailRecipientDto::from).toList();
    }

    @PostMapping("/recipients")
    public MailRecipientDto saveRecipient(@Valid @RequestBody MailRecipientDto request) {
        return MailRecipientDto.from(manageMailSettingsUseCase.saveRecipient(currentUserResolver.requireUserId(), request.toDomain()));
    }

    @DeleteMapping("/recipients/{id}")
    public void deleteRecipient(@PathVariable String id) {
        manageMailSettingsUseCase.deleteRecipient(currentUserResolver.requireUserId(), id);
    }

    @GetMapping("/thresholds/{deviceId}")
    public MailThresholdsDto getThresholds(@PathVariable String deviceId) {
        var thresholds = manageMailSettingsUseCase.getThresholds(deviceId);
        return thresholds != null ? MailThresholdsDto.from(thresholds) : null;
    }

    @PutMapping("/thresholds")
    public void saveThresholds(@Valid @RequestBody MailThresholdsDto request) {
        manageMailSettingsUseCase.saveThresholds(currentUserResolver.requireUserId(), request.toDomain());
    }
}
