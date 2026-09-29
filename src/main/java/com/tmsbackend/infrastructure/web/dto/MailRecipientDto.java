package com.tmsbackend.infrastructure.web.dto;

import com.tmsbackend.domain.model.MailRecipient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record MailRecipientDto(String id, @NotBlank String name, @NotBlank @Email String email, boolean enabled, List<String> deviceIds) {
    public MailRecipient toDomain() {
        return new MailRecipient(id, name, email, enabled, deviceIds != null ? deviceIds : List.of());
    }

    public static MailRecipientDto from(MailRecipient recipient) {
        return new MailRecipientDto(recipient.id(), recipient.name(), recipient.email(), recipient.enabled(), recipient.deviceIds());
    }
}
