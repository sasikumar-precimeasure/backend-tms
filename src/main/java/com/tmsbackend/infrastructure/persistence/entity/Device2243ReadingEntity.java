package com.tmsbackend.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "device2243_readings")
public class Device2243ReadingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(name = "oti_temperature")
    private Double otiTemperature;

    @Column(name = "wti_temperature")
    private Double wtiTemperature;

    @Column(name = "oti_alarm_setpoint")
    private Double otiAlarmSetpoint;

    @Column(name = "oti_alarm_diff")
    private Double otiAlarmDiff;

    @Column(name = "oti_trip_setpoint")
    private Double otiTripSetpoint;

    @Column(name = "oti_trip_diff")
    private Double otiTripDiff;

    @Column(name = "wti_alarm_setpoint")
    private Double wtiAlarmSetpoint;

    @Column(name = "wti_alarm_diff")
    private Double wtiAlarmDiff;

    @Column(name = "wti_trip_setpoint")
    private Double wtiTripSetpoint;

    @Column(name = "wti_trip_diff")
    private Double wtiTripDiff;

    @Column(name = "wti_fan1_setpoint")
    private Double wtiFan1Setpoint;

    @Column(name = "wti_fan1_diff")
    private Double wtiFan1Diff;

    @Column(name = "wti_fan2_setpoint")
    private Double wtiFan2Setpoint;

    @Column(name = "wti_fan2_diff")
    private Double wtiFan2Diff;

    @Column(name = "relay_delay")
    private Double relayDelay;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Instant recordedAt) {
        this.recordedAt = recordedAt;
    }

    public Double getOtiTemperature() {
        return otiTemperature;
    }

    public void setOtiTemperature(Double otiTemperature) {
        this.otiTemperature = otiTemperature;
    }

    public Double getWtiTemperature() {
        return wtiTemperature;
    }

    public void setWtiTemperature(Double wtiTemperature) {
        this.wtiTemperature = wtiTemperature;
    }

    public Double getOtiAlarmSetpoint() {
        return otiAlarmSetpoint;
    }

    public void setOtiAlarmSetpoint(Double otiAlarmSetpoint) {
        this.otiAlarmSetpoint = otiAlarmSetpoint;
    }

    public Double getOtiAlarmDiff() {
        return otiAlarmDiff;
    }

    public void setOtiAlarmDiff(Double otiAlarmDiff) {
        this.otiAlarmDiff = otiAlarmDiff;
    }

    public Double getOtiTripSetpoint() {
        return otiTripSetpoint;
    }

    public void setOtiTripSetpoint(Double otiTripSetpoint) {
        this.otiTripSetpoint = otiTripSetpoint;
    }

    public Double getOtiTripDiff() {
        return otiTripDiff;
    }

    public void setOtiTripDiff(Double otiTripDiff) {
        this.otiTripDiff = otiTripDiff;
    }

    public Double getWtiAlarmSetpoint() {
        return wtiAlarmSetpoint;
    }

    public void setWtiAlarmSetpoint(Double wtiAlarmSetpoint) {
        this.wtiAlarmSetpoint = wtiAlarmSetpoint;
    }

    public Double getWtiAlarmDiff() {
        return wtiAlarmDiff;
    }

    public void setWtiAlarmDiff(Double wtiAlarmDiff) {
        this.wtiAlarmDiff = wtiAlarmDiff;
    }

    public Double getWtiTripSetpoint() {
        return wtiTripSetpoint;
    }

    public void setWtiTripSetpoint(Double wtiTripSetpoint) {
        this.wtiTripSetpoint = wtiTripSetpoint;
    }

    public Double getWtiTripDiff() {
        return wtiTripDiff;
    }

    public void setWtiTripDiff(Double wtiTripDiff) {
        this.wtiTripDiff = wtiTripDiff;
    }

    public Double getWtiFan1Setpoint() {
        return wtiFan1Setpoint;
    }

    public void setWtiFan1Setpoint(Double wtiFan1Setpoint) {
        this.wtiFan1Setpoint = wtiFan1Setpoint;
    }

    public Double getWtiFan1Diff() {
        return wtiFan1Diff;
    }

    public void setWtiFan1Diff(Double wtiFan1Diff) {
        this.wtiFan1Diff = wtiFan1Diff;
    }

    public Double getWtiFan2Setpoint() {
        return wtiFan2Setpoint;
    }

    public void setWtiFan2Setpoint(Double wtiFan2Setpoint) {
        this.wtiFan2Setpoint = wtiFan2Setpoint;
    }

    public Double getWtiFan2Diff() {
        return wtiFan2Diff;
    }

    public void setWtiFan2Diff(Double wtiFan2Diff) {
        this.wtiFan2Diff = wtiFan2Diff;
    }

    public Double getRelayDelay() {
        return relayDelay;
    }

    public void setRelayDelay(Double relayDelay) {
        this.relayDelay = relayDelay;
    }
}
