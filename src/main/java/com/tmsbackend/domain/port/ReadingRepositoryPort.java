package com.tmsbackend.domain.port;

import com.tmsbackend.domain.model.Device2243Reading;
import com.tmsbackend.domain.model.IrtccReading;
import java.util.Optional;

public interface ReadingRepositoryPort {
    // Idempotent on (deviceId, recordedAt) - a retried push after a client
    // timeout must not double-insert.
    void saveIrtccReading(IrtccReading reading);

    void saveDevice2243Reading(Device2243Reading reading);

    Optional<IrtccReading> findLatestIrtcc(String deviceId);

    Optional<Device2243Reading> findLatestDevice2243(String deviceId);
}
