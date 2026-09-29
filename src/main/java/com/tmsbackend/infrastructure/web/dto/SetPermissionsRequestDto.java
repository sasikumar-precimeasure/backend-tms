package com.tmsbackend.infrastructure.web.dto;

import com.tmsbackend.domain.model.Permission;
import java.util.List;

public record SetPermissionsRequestDto(List<Permission> permissions) {
}
