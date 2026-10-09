package com.tmsbackend.infrastructure.persistence.repository;

import com.tmsbackend.infrastructure.persistence.entity.DeviceEntity;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// last_seen_at is deliberately not mapped on DeviceEntity - it's written and
// read only by these two queries, so an ordinary upsert never resets it.
public interface DeviceJpaRepository extends JpaRepository<DeviceEntity, String> {
    @Modifying
    @Query(value = "update devices set last_seen_at = :at where id in (:ids)", nativeQuery = true)
    void markSeen(@Param("ids") Collection<String> ids, @Param("at") Instant at);

    // Devices included in pushes within `windowMinutes` of the newest push -
    // a window rather than "exactly the newest" so two browsers pushing a
    // few seconds apart both count.
    @Query(value = "select id from devices where last_seen_at >= "
            + "(select max(last_seen_at) from devices) - make_interval(mins => :windowMinutes)", nativeQuery = true)
    List<String> findRecentlySeenIds(@Param("windowMinutes") int windowMinutes);
}
