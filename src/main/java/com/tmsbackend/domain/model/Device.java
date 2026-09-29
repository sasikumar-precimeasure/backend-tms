package com.tmsbackend.domain.model;

// Mirrors tms/src/domain/entities/ConnectionSettings.ts's SubDevice - id is
// the frontend-generated nanoid string, kept as the primary key here too so
// the 1-minute ingestion push can upsert by the same id the frontend uses.
public record Device(String id, String gatewayId, String name, int slaveId, DeviceType deviceType, boolean enabled) {
}
