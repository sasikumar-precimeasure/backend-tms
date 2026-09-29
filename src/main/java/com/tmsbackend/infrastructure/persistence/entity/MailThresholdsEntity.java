package com.tmsbackend.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "mail_thresholds")
public class MailThresholdsEntity {
    @Id
    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "oti_temp_high", nullable = false)
    private double otiTempHigh;

    @Column(name = "wti_temp_high", nullable = false)
    private double wtiTempHigh;

    @Column(name = "avr_high", nullable = false)
    private double avrHigh;

    @Column(name = "avr_low", nullable = false)
    private double avrLow;

    @Column(name = "tap_high", nullable = false)
    private double tapHigh;

    @Column(name = "tap_low", nullable = false)
    private double tapLow;

    @Column(name = "mail_time_minutes", nullable = false)
    private int mailTimeMinutes;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public double getOtiTempHigh() {
        return otiTempHigh;
    }

    public void setOtiTempHigh(double otiTempHigh) {
        this.otiTempHigh = otiTempHigh;
    }

    public double getWtiTempHigh() {
        return wtiTempHigh;
    }

    public void setWtiTempHigh(double wtiTempHigh) {
        this.wtiTempHigh = wtiTempHigh;
    }

    public double getAvrHigh() {
        return avrHigh;
    }

    public void setAvrHigh(double avrHigh) {
        this.avrHigh = avrHigh;
    }

    public double getAvrLow() {
        return avrLow;
    }

    public void setAvrLow(double avrLow) {
        this.avrLow = avrLow;
    }

    public double getTapHigh() {
        return tapHigh;
    }

    public void setTapHigh(double tapHigh) {
        this.tapHigh = tapHigh;
    }

    public double getTapLow() {
        return tapLow;
    }

    public void setTapLow(double tapLow) {
        this.tapLow = tapLow;
    }

    public int getMailTimeMinutes() {
        return mailTimeMinutes;
    }

    public void setMailTimeMinutes(int mailTimeMinutes) {
        this.mailTimeMinutes = mailTimeMinutes;
    }
}
