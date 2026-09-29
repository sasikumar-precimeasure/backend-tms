package com.tmsbackend.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "mail_last_sent")
@IdClass(MailLastSentId.class)
public class MailLastSentEntity {
    @Id
    @Column(name = "device_id")
    private String deviceId;

    @Id
    @Column(name = "condition_key")
    private String conditionKey;

    @Column(name = "last_sent_at", nullable = false)
    private Instant lastSentAt;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getConditionKey() {
        return conditionKey;
    }

    public void setConditionKey(String conditionKey) {
        this.conditionKey = conditionKey;
    }

    public Instant getLastSentAt() {
        return lastSentAt;
    }

    public void setLastSentAt(Instant lastSentAt) {
        this.lastSentAt = lastSentAt;
    }
}
