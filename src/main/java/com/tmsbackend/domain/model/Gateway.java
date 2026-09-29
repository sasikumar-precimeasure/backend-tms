package com.tmsbackend.domain.model;

// Mirrors tms/src/domain/entities/ConnectionSettings.ts's Gateway - only the
// bookkeeping fields (not live connection status, which never leaves the
// frontend/Modbus gateway service and has no business being persisted here).
public record Gateway(String id, String transformerId, String name, int clientId, String ipAddress, int port) {
}
