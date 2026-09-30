package com.tmsbackend.domain.port;

import com.tmsbackend.domain.model.Device2243Reading;
import com.tmsbackend.domain.model.IrtccReading;
import com.tmsbackend.domain.model.PagedResult;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ReadingRepositoryPort {
    // Idempotent on (deviceId, recordedAt) - a retried push after a client
    // timeout must not double-insert.
    void saveIrtccReading(IrtccReading reading);

    void saveDevice2243Reading(Device2243Reading reading);

    Optional<IrtccReading> findLatestIrtcc(String deviceId);

    Optional<Device2243Reading> findLatestDevice2243(String deviceId);

    // Backs the Data Log screen - newest-first, bounded by an inclusive
    // [from, to] instant range. `page` is 1-based to match the screen's own
    // page-number display; `pageSize` <= 0 is never passed by callers here.
    PagedResult<IrtccReading> findIrtccByDeviceAndDateRange(String deviceId, Instant from, Instant to, int page, int pageSize);

    PagedResult<Device2243Reading> findDevice2243ByDeviceAndDateRange(String deviceId, Instant from, Instant to, int page, int pageSize);

    // Export ignores pagination entirely and returns every matching row for
    // the range - see DataLogController's export endpoints.
    List<IrtccReading> findAllIrtccByDeviceAndDateRange(String deviceId, Instant from, Instant to);

    List<Device2243Reading> findAllDevice2243ByDeviceAndDateRange(String deviceId, Instant from, Instant to);
}
