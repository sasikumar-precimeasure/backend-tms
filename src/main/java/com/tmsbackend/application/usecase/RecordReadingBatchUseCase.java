package com.tmsbackend.application.usecase;

import com.tmsbackend.application.dto.ReadingBatchRequest;
import com.tmsbackend.domain.model.Device;
import com.tmsbackend.domain.model.Gateway;
import com.tmsbackend.domain.model.Transformer;
import com.tmsbackend.domain.port.ReadingRepositoryPort;
import com.tmsbackend.domain.port.TopologyRepositoryPort;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// One call per 60s tick from the frontend, covering every currently
// enabled/visible device - upserts the topology metadata (so the backend's
// device list stays in sync with whatever's configured in Settings,
// without a separate sync endpoint) and inserts one reading row per device.
// Idempotent on (deviceId, recordedAt) at the persistence layer, so a
// retried push after a client-side timeout never double-inserts.
@Component
public class RecordReadingBatchUseCase {
    private static final Logger log = LoggerFactory.getLogger(RecordReadingBatchUseCase.class);

    private final TopologyRepositoryPort topologyRepository;
    private final ReadingRepositoryPort readingRepository;
    private final Clock clock;

    public RecordReadingBatchUseCase(TopologyRepositoryPort topologyRepository, ReadingRepositoryPort readingRepository, Clock clock) {
        this.topologyRepository = topologyRepository;
        this.readingRepository = readingRepository;
        this.clock = clock;
    }

    public void execute(ReadingBatchRequest request) {
        List<String> pushedDeviceIds = new ArrayList<>();
        for (ReadingBatchRequest.TransformerEntry tr : request.transformers()) {
            topologyRepository.upsertTransformer(new Transformer(tr.id(), tr.name()));

            for (ReadingBatchRequest.GatewayEntry gw : tr.gateways()) {
                topologyRepository.upsertGateway(new Gateway(gw.id(), tr.id(), gw.name(), gw.clientId(), gw.ipAddress(), gw.port()));

                for (ReadingBatchRequest.DeviceEntry device : gw.devices()) {
                    topologyRepository.upsertDevice(
                            new Device(device.id(), gw.id(), device.name(), device.slaveId(), device.deviceType(), device.enabled()));
                    pushedDeviceIds.add(device.id());

                    // Saved per device: one device's bad row must not cost every
                    // other transformer its reading for this minute.
                    try {
                        if (device.irtccReading() != null) {
                            readingRepository.saveIrtccReading(device.irtccReading());
                        }
                        if (device.device2243Reading() != null) {
                            readingRepository.saveDevice2243Reading(device.device2243Reading());
                        }
                    } catch (RuntimeException e) {
                        log.warn("Could not save reading for device {} ({}): {}", device.id(), device.name(), e.getMessage());
                    }
                }
            }
        }
        // This push is the browser's full current device list - see
        // TopologyRepositoryPort.findCurrentDeviceIds.
        topologyRepository.markDevicesSeen(pushedDeviceIds, Instant.now(clock));
    }
}
