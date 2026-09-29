package com.tmsbackend.domain.model;

// Menu+function-level permission - matches the frontend's Permission type
// (tms/src/domain/entities/User.ts) exactly: no single "isSuperAdmin" flag,
// a super admin is just a role whose permissions cover every menu.
public record Permission(String menu, String function, boolean read, boolean write) {
}
