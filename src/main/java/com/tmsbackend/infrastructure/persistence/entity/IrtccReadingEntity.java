package com.tmsbackend.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "irtcc_readings")
public class IrtccReadingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    @Column(name = "oti_temperature")
    private Double otiTemperature;

    @Column(name = "oti_temperature_max")
    private Double otiTemperatureMax;

    @Column(name = "wti_temperature")
    private Double wtiTemperature;

    @Column(name = "wti_temperature_max")
    private Double wtiTemperatureMax;

    private Double mog;

    @Column(name = "tap_position")
    private Double tapPosition;

    @Column(name = "tap_position_max")
    private Double tapPositionMax;

    @Column(name = "tap_count")
    private Double tapCount;

    @Column(name = "pt_voltage")
    private Double ptVoltage;

    @Column(name = "actual_pt_voltage")
    private Double actualPtVoltage;

    @Column(name = "operation_mode")
    private String operationMode;

    @Column(name = "lv_breaker_active")
    private Boolean lvBreakerActive;

    @Column(name = "hv_breaker_active")
    private Boolean hvBreakerActive;

    @Column(name = "oltc_local")
    private Boolean oltcLocal;

    @Column(name = "pt_fail_active")
    private Boolean ptFailActive;

    @Column(name = "hooter_active")
    private Boolean hooterActive;

    @Column(name = "mute_visible")
    private Boolean muteVisible;

    @Column(name = "avr_mode_is_auto")
    private Boolean avrModeIsAuto;

    @Column(name = "control_fail_active")
    private Boolean controlFailActive;

    @Column(name = "afr_active")
    private Boolean afrActive;

    @Column(name = "raise_relay_active")
    private Boolean raiseRelayActive;

    @Column(name = "lower_relay_active")
    private Boolean lowerRelayActive;

    @Column(name = "over_volt_active")
    private Boolean overVoltActive;

    @Column(name = "under_volt_active")
    private Boolean underVoltActive;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<Boolean> annunciation;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "annunciation_ack")
    private List<Boolean> annunciationAck;

    @Column(name = "avr_pt_ratio")
    private Double avrPtRatio;

    @Column(name = "avr_set_voltage")
    private Double avrSetVoltage;

    @Column(name = "avr_raise_relay_voltage")
    private Double avrRaiseRelayVoltage;

    @Column(name = "avr_low_relay_voltage")
    private Double avrLowRelayVoltage;

    @Column(name = "avr_hs_forward_voltage")
    private Double avrHsForwardVoltage;

    @Column(name = "avr_hs_backward_voltage")
    private Double avrHsBackwardVoltage;

    @Column(name = "avr_over_voltage")
    private Double avrOverVoltage;

    @Column(name = "avr_under_voltage")
    private Double avrUnderVoltage;

    @Column(name = "avr_pt_fail_setpoint")
    private Double avrPtFailSetpoint;

    @Column(name = "avr_initial_time")
    private Double avrInitialTime;

    @Column(name = "avr_sequential_time")
    private Double avrSequentialTime;

    @Column(name = "avr_high_fwd_bwd_time")
    private Double avrHighFwdBwdTime;

    @Column(name = "avr_control_fail_time")
    private Double avrControlFailTime;

    @Column(name = "avr_relay_momentary_time")
    private Double avrRelayMomentaryTime;

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

    public Double getOtiTemperatureMax() {
        return otiTemperatureMax;
    }

    public void setOtiTemperatureMax(Double otiTemperatureMax) {
        this.otiTemperatureMax = otiTemperatureMax;
    }

    public Double getWtiTemperature() {
        return wtiTemperature;
    }

    public void setWtiTemperature(Double wtiTemperature) {
        this.wtiTemperature = wtiTemperature;
    }

    public Double getWtiTemperatureMax() {
        return wtiTemperatureMax;
    }

    public void setWtiTemperatureMax(Double wtiTemperatureMax) {
        this.wtiTemperatureMax = wtiTemperatureMax;
    }

    public Double getMog() {
        return mog;
    }

    public void setMog(Double mog) {
        this.mog = mog;
    }

    public Double getTapPosition() {
        return tapPosition;
    }

    public void setTapPosition(Double tapPosition) {
        this.tapPosition = tapPosition;
    }

    public Double getTapPositionMax() {
        return tapPositionMax;
    }

    public void setTapPositionMax(Double tapPositionMax) {
        this.tapPositionMax = tapPositionMax;
    }

    public Double getTapCount() {
        return tapCount;
    }

    public void setTapCount(Double tapCount) {
        this.tapCount = tapCount;
    }

    public Double getPtVoltage() {
        return ptVoltage;
    }

    public void setPtVoltage(Double ptVoltage) {
        this.ptVoltage = ptVoltage;
    }

    public Double getActualPtVoltage() {
        return actualPtVoltage;
    }

    public void setActualPtVoltage(Double actualPtVoltage) {
        this.actualPtVoltage = actualPtVoltage;
    }

    public String getOperationMode() {
        return operationMode;
    }

    public void setOperationMode(String operationMode) {
        this.operationMode = operationMode;
    }

    public Boolean getLvBreakerActive() {
        return lvBreakerActive;
    }

    public void setLvBreakerActive(Boolean lvBreakerActive) {
        this.lvBreakerActive = lvBreakerActive;
    }

    public Boolean getHvBreakerActive() {
        return hvBreakerActive;
    }

    public void setHvBreakerActive(Boolean hvBreakerActive) {
        this.hvBreakerActive = hvBreakerActive;
    }

    public Boolean getOltcLocal() {
        return oltcLocal;
    }

    public void setOltcLocal(Boolean oltcLocal) {
        this.oltcLocal = oltcLocal;
    }

    public Boolean getPtFailActive() {
        return ptFailActive;
    }

    public void setPtFailActive(Boolean ptFailActive) {
        this.ptFailActive = ptFailActive;
    }

    public Boolean getHooterActive() {
        return hooterActive;
    }

    public void setHooterActive(Boolean hooterActive) {
        this.hooterActive = hooterActive;
    }

    public Boolean getMuteVisible() {
        return muteVisible;
    }

    public void setMuteVisible(Boolean muteVisible) {
        this.muteVisible = muteVisible;
    }

    public Boolean getAvrModeIsAuto() {
        return avrModeIsAuto;
    }

    public void setAvrModeIsAuto(Boolean avrModeIsAuto) {
        this.avrModeIsAuto = avrModeIsAuto;
    }

    public Boolean getControlFailActive() {
        return controlFailActive;
    }

    public void setControlFailActive(Boolean controlFailActive) {
        this.controlFailActive = controlFailActive;
    }

    public Boolean getAfrActive() {
        return afrActive;
    }

    public void setAfrActive(Boolean afrActive) {
        this.afrActive = afrActive;
    }

    public Boolean getRaiseRelayActive() {
        return raiseRelayActive;
    }

    public void setRaiseRelayActive(Boolean raiseRelayActive) {
        this.raiseRelayActive = raiseRelayActive;
    }

    public Boolean getLowerRelayActive() {
        return lowerRelayActive;
    }

    public void setLowerRelayActive(Boolean lowerRelayActive) {
        this.lowerRelayActive = lowerRelayActive;
    }

    public Boolean getOverVoltActive() {
        return overVoltActive;
    }

    public void setOverVoltActive(Boolean overVoltActive) {
        this.overVoltActive = overVoltActive;
    }

    public Boolean getUnderVoltActive() {
        return underVoltActive;
    }

    public void setUnderVoltActive(Boolean underVoltActive) {
        this.underVoltActive = underVoltActive;
    }

    public List<Boolean> getAnnunciation() {
        return annunciation;
    }

    public void setAnnunciation(List<Boolean> annunciation) {
        this.annunciation = annunciation;
    }

    public List<Boolean> getAnnunciationAck() {
        return annunciationAck;
    }

    public void setAnnunciationAck(List<Boolean> annunciationAck) {
        this.annunciationAck = annunciationAck;
    }

    public Double getAvrPtRatio() {
        return avrPtRatio;
    }

    public void setAvrPtRatio(Double avrPtRatio) {
        this.avrPtRatio = avrPtRatio;
    }

    public Double getAvrSetVoltage() {
        return avrSetVoltage;
    }

    public void setAvrSetVoltage(Double avrSetVoltage) {
        this.avrSetVoltage = avrSetVoltage;
    }

    public Double getAvrRaiseRelayVoltage() {
        return avrRaiseRelayVoltage;
    }

    public void setAvrRaiseRelayVoltage(Double avrRaiseRelayVoltage) {
        this.avrRaiseRelayVoltage = avrRaiseRelayVoltage;
    }

    public Double getAvrLowRelayVoltage() {
        return avrLowRelayVoltage;
    }

    public void setAvrLowRelayVoltage(Double avrLowRelayVoltage) {
        this.avrLowRelayVoltage = avrLowRelayVoltage;
    }

    public Double getAvrHsForwardVoltage() {
        return avrHsForwardVoltage;
    }

    public void setAvrHsForwardVoltage(Double avrHsForwardVoltage) {
        this.avrHsForwardVoltage = avrHsForwardVoltage;
    }

    public Double getAvrHsBackwardVoltage() {
        return avrHsBackwardVoltage;
    }

    public void setAvrHsBackwardVoltage(Double avrHsBackwardVoltage) {
        this.avrHsBackwardVoltage = avrHsBackwardVoltage;
    }

    public Double getAvrOverVoltage() {
        return avrOverVoltage;
    }

    public void setAvrOverVoltage(Double avrOverVoltage) {
        this.avrOverVoltage = avrOverVoltage;
    }

    public Double getAvrUnderVoltage() {
        return avrUnderVoltage;
    }

    public void setAvrUnderVoltage(Double avrUnderVoltage) {
        this.avrUnderVoltage = avrUnderVoltage;
    }

    public Double getAvrPtFailSetpoint() {
        return avrPtFailSetpoint;
    }

    public void setAvrPtFailSetpoint(Double avrPtFailSetpoint) {
        this.avrPtFailSetpoint = avrPtFailSetpoint;
    }

    public Double getAvrInitialTime() {
        return avrInitialTime;
    }

    public void setAvrInitialTime(Double avrInitialTime) {
        this.avrInitialTime = avrInitialTime;
    }

    public Double getAvrSequentialTime() {
        return avrSequentialTime;
    }

    public void setAvrSequentialTime(Double avrSequentialTime) {
        this.avrSequentialTime = avrSequentialTime;
    }

    public Double getAvrHighFwdBwdTime() {
        return avrHighFwdBwdTime;
    }

    public void setAvrHighFwdBwdTime(Double avrHighFwdBwdTime) {
        this.avrHighFwdBwdTime = avrHighFwdBwdTime;
    }

    public Double getAvrControlFailTime() {
        return avrControlFailTime;
    }

    public void setAvrControlFailTime(Double avrControlFailTime) {
        this.avrControlFailTime = avrControlFailTime;
    }

    public Double getAvrRelayMomentaryTime() {
        return avrRelayMomentaryTime;
    }

    public void setAvrRelayMomentaryTime(Double avrRelayMomentaryTime) {
        this.avrRelayMomentaryTime = avrRelayMomentaryTime;
    }
}
