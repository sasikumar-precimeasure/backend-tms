package com.tmsbackend.application.dto;

import com.tmsbackend.domain.model.Device2243Reading;
import com.tmsbackend.domain.model.DeviceType;
import com.tmsbackend.domain.model.IrtccReading;
import java.util.List;

// Framework-agnostic shape for the 1-minute ingestion push - the web
// layer's DTO maps request JSON onto this. Mirrors
// ConnectionSettings.ts's Transformer -> Gateway -> SubDevice nesting so
// the topology upsert and the reading itself travel together in one call.
public record ReadingBatchRequest(List<TransformerEntry> transformers) {

    public record TransformerEntry(String id, String name, List<GatewayEntry> gateways) {
    }

    public record GatewayEntry(String id, String name, int clientId, String ipAddress, int port, List<DeviceEntry> devices) {
    }

    public record DeviceEntry(
            String id,
            String name,
            int slaveId,
            DeviceType deviceType,
            boolean enabled,
            IrtccReading irtccReading,
            Device2243Reading device2243Reading) {
    }
}
