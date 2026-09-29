package com.tmsbackend.domain.model;

import java.util.List;

public record Role(Long id, String name, boolean status, List<Permission> permissions) {
}
