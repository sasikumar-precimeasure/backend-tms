package com.tmsbackend.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalTime;

@Entity
@Table(name = "report_settings")
public class ReportSettingsEntity {
    @Id
    private Integer id;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "day_of_month", nullable = false)
    private int dayOfMonth = 1;

    @Column(name = "send_time", nullable = false)
    private LocalTime sendTime = LocalTime.of(6, 0);

    @Column(nullable = false)
    private String timezone = "Asia/Kolkata";

    @Column(name = "last_sent_period")
    private String lastSentPeriod;

    @Column(name = "last_sent_at")
    private Instant lastSentAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public LocalTime getSendTime() {
        return sendTime;
    }

    public void setSendTime(LocalTime sendTime) {
        this.sendTime = sendTime;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getLastSentPeriod() {
        return lastSentPeriod;
    }

    public void setLastSentPeriod(String lastSentPeriod) {
        this.lastSentPeriod = lastSentPeriod;
    }

    public Instant getLastSentAt() {
        return lastSentAt;
    }

    public void setLastSentAt(Instant lastSentAt) {
        this.lastSentAt = lastSentAt;
    }
}
