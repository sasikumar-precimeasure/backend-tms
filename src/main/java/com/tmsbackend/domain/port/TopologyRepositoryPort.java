package com.tmsbackend.domain.port;

import com.tmsbackend.domain.model.Device;
import com.tmsbackend.domain.model.Gateway;
import com.tmsbackend.domain.model.Transformer;
import java.util.List;
import java.util.Optional;

// Upserts the frontend's device topology (Transformer -> Gateway -> Device)
// so ingested readings and mail thresholds/recipients have something to
// reference - not a live command channel, purely bookkeeping metadata kept
// in sync by the 1-minute ingestion push.
public interface TopologyRepositoryPort {
    void upsertTransformer(Transformer transformer);

    void upsertGateway(Gateway gateway);

    void upsertDevice(Device device);

    Optional<Device> findDevice(String deviceId);

    List<Device> findAllDevices();

    // Backs the Data Log screen's Transformer/device picker - the backend's
    // own topology (kept in sync by the 1-minute ingestion push) rather than
    // the frontend's local Connection Settings state, since the Data Log
    // reflects what's actually been stored server-side.
    List<Transformer> findAllTransformers();

    List<Gateway> findAllGateways();
}
