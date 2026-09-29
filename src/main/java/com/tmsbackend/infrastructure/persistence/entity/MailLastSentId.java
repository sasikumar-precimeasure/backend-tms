package com.tmsbackend.infrastructure.persistence.entity;

import java.io.Serializable;
import java.util.Objects;

public class MailLastSentId implements Serializable {
    private String deviceId;
    private String conditionKey;

    public MailLastSentId() {
    }

    public MailLastSentId(String deviceId, String conditionKey) {
        this.deviceId = deviceId;
        this.conditionKey = conditionKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MailLastSentId that)) return false;
        return Objects.equals(deviceId, that.deviceId) && Objects.equals(conditionKey, that.conditionKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId, conditionKey);
    }
}
