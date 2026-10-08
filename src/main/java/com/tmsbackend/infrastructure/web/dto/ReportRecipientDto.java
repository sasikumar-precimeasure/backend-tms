package com.tmsbackend.infrastructure.web.dto;

import com.tmsbackend.domain.model.ReportRecipient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ReportRecipientDto(String id, @NotBlank String name, @NotBlank @Email String email, boolean enabled) {
    public ReportRecipient toDomain() {
        return new ReportRecipient(id, name, email, enabled);
    }

    public static ReportRecipientDto from(ReportRecipient r) {
        return new ReportRecipientDto(r.id(), r.name(), r.email(), r.enabled());
    }
}
