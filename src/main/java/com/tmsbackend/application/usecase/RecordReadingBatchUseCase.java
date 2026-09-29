package com.tmsbackend.application.usecase;

import com.tmsbackend.application.dto.ReadingBatchRequest;
import com.tmsbackend.domain.model.Device;
import com.tmsbackend.domain.model.Gateway;
import com.tmsbackend.domain.model.Transformer;
import com.tmsbackend.domain.port.ReadingRepositoryPort;
import com.tmsbackend.domain.port.TopologyRepositoryPort;
import org.springframework.stereotype.Component;

// One call per 60s tick from the frontend, covering every currently
// enabled/visible device - upserts the topology metadata (so the backend's
// device list stays in sync with whatever's configured in Settings,
// without a separate sync endpoint) and inserts one reading row per device.
// Idempotent on (deviceId, recordedAt) at the persistence layer, so a
// retried push after a client-side timeout never double-inserts.
@Component
public class RecordReadingBatchUseCase {
    private final TopologyRepositoryPort topologyRepository;
    private final ReadingRepositoryPort readingRepository;

    public RecordReadingBatchUseCase(TopologyRepositoryPort topologyRepository, ReadingRepositoryPort readingRepository) {
        this.topologyRepository = topologyRepository;
        this.readingRepository = readingRepository;
    }

    public void execute(ReadingBatchRequest request) {
        for (ReadingBatchRequest.TransformerEntry tr : request.transformers()) {
            topologyRepository.upsertTransformer(new Transformer(tr.id(), tr.name()));

            for (ReadingBatchRequest.GatewayEntry gw : tr.gateways()) {
                topologyRepository.upsertGateway(new Gateway(gw.id(), tr.id(), gw.name(), gw.clientId(), gw.ipAddress(), gw.port()));

                for (ReadingBatchRequest.DeviceEntry device : gw.devices()) {
                    topologyRepository.upsertDevice(
                            new Device(device.id(), gw.id(), device.name(), device.slaveId(), device.deviceType(), device.enabled()));

                    if (device.irtccReading() != null) {
                        readingRepository.saveIrtccReading(device.irtccReading());
                    }
                    if (device.device2243Reading() != null) {
                        readingRepository.saveDevice2243Reading(device.device2243Reading());
                    }
                }
            }
        }
    }
}
