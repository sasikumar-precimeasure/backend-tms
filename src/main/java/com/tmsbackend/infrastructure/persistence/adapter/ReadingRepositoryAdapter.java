package com.tmsbackend.infrastructure.persistence.adapter;

import com.tmsbackend.domain.model.Device2243Reading;
import com.tmsbackend.domain.model.IrtccReading;
import com.tmsbackend.domain.port.ReadingRepositoryPort;
import com.tmsbackend.infrastructure.persistence.entity.Device2243ReadingEntity;
import com.tmsbackend.infrastructure.persistence.entity.IrtccReadingEntity;
import com.tmsbackend.infrastructure.persistence.repository.Device2243ReadingJpaRepository;
import com.tmsbackend.infrastructure.persistence.repository.IrtccReadingJpaRepository;
import java.util.Optional;
import org.springframework.stereotype.Component;

// Idempotent on (deviceId, recordedAt): a retried push after a client
// timeout looks up the existing row first (via the unique DB constraint's
// matching finder) and updates it in place instead of inserting a
// duplicate.
@Component
public class ReadingRepositoryAdapter implements ReadingRepositoryPort {
    private final IrtccReadingJpaRepository irtccReadingJpaRepository;
    private final Device2243ReadingJpaRepository device2243ReadingJpaRepository;

    public ReadingRepositoryAdapter(
            IrtccReadingJpaRepository irtccReadingJpaRepository,
            Device2243ReadingJpaRepository device2243ReadingJpaRepository) {
        this.irtccReadingJpaRepository = irtccReadingJpaRepository;
        this.device2243ReadingJpaRepository = device2243ReadingJpaRepository;
    }

    @Override
    public void saveIrtccReading(IrtccReading reading) {
        IrtccReadingEntity entity = irtccReadingJpaRepository
                .findByDeviceIdAndRecordedAt(reading.deviceId(), reading.recordedAt())
                .orElseGet(IrtccReadingEntity::new);

        entity.setDeviceId(reading.deviceId());
        entity.setRecordedAt(reading.recordedAt());
        entity.setOtiTemperature(reading.otiTemperature());
        entity.setOtiTemperatureMax(reading.otiTemperatureMax());
        entity.setWtiTemperature(reading.wtiTemperature());
        entity.setWtiTemperatureMax(reading.wtiTemperatureMax());
        entity.setMog(reading.mog());
        entity.setTapPosition(reading.tapPosition());
        entity.setTapPositionMax(reading.tapPositionMax());
        entity.setTapCount(reading.tapCount());
        entity.setPtVoltage(reading.ptVoltage());
        entity.setActualPtVoltage(reading.actualPtVoltage());
        entity.setOperationMode(reading.operationMode());
        entity.setLvBreakerActive(reading.lvBreakerActive());
        entity.setHvBreakerActive(reading.hvBreakerActive());
        entity.setOltcLocal(reading.oltcLocal());
        entity.setPtFailActive(reading.ptFailActive());
        entity.setHooterActive(reading.hooterActive());
        entity.setMuteVisible(reading.muteVisible());
        entity.setAvrModeIsAuto(reading.avrModeIsAuto());
        entity.setControlFailActive(reading.controlFailActive());
        entity.setAfrActive(reading.afrActive());
        entity.setRaiseRelayActive(reading.raiseRelayActive());
        entity.setLowerRelayActive(reading.lowerRelayActive());
        entity.setOverVoltActive(reading.overVoltActive());
        entity.setUnderVoltActive(reading.underVoltActive());
        entity.setAnnunciation(reading.annunciation());
        entity.setAnnunciationAck(reading.annunciationAck());
        entity.setAvrPtRatio(reading.avrPtRatio());
        entity.setAvrSetVoltage(reading.avrSetVoltage());
        entity.setAvrRaiseRelayVoltage(reading.avrRaiseRelayVoltage());
        entity.setAvrLowRelayVoltage(reading.avrLowRelayVoltage());
        entity.setAvrHsForwardVoltage(reading.avrHsForwardVoltage());
        entity.setAvrHsBackwardVoltage(reading.avrHsBackwardVoltage());
        entity.setAvrOverVoltage(reading.avrOverVoltage());
        entity.setAvrUnderVoltage(reading.avrUnderVoltage());
        entity.setAvrPtFailSetpoint(reading.avrPtFailSetpoint());
        entity.setAvrInitialTime(reading.avrInitialTime());
        entity.setAvrSequentialTime(reading.avrSequentialTime());
        entity.setAvrHighFwdBwdTime(reading.avrHighFwdBwdTime());
        entity.setAvrControlFailTime(reading.avrControlFailTime());
        entity.setAvrRelayMomentaryTime(reading.avrRelayMomentaryTime());

        irtccReadingJpaRepository.save(entity);
    }

    @Override
    public void saveDevice2243Reading(Device2243Reading reading) {
        Device2243ReadingEntity entity = device2243ReadingJpaRepository
                .findByDeviceIdAndRecordedAt(reading.deviceId(), reading.recordedAt())
                .orElseGet(Device2243ReadingEntity::new);

        entity.setDeviceId(reading.deviceId());
        entity.setRecordedAt(reading.recordedAt());
        entity.setOtiTemperature(reading.otiTemperature());
        entity.setWtiTemperature(reading.wtiTemperature());
        entity.setOtiAlarmSetpoint(reading.otiAlarmSetpoint());
        entity.setOtiAlarmDiff(reading.otiAlarmDiff());
        entity.setOtiTripSetpoint(reading.otiTripSetpoint());
        entity.setOtiTripDiff(reading.otiTripDiff());
        entity.setWtiAlarmSetpoint(reading.wtiAlarmSetpoint());
        entity.setWtiAlarmDiff(reading.wtiAlarmDiff());
        entity.setWtiTripSetpoint(reading.wtiTripSetpoint());
        entity.setWtiTripDiff(reading.wtiTripDiff());
        entity.setWtiFan1Setpoint(reading.wtiFan1Setpoint());
        entity.setWtiFan1Diff(reading.wtiFan1Diff());
        entity.setWtiFan2Setpoint(reading.wtiFan2Setpoint());
        entity.setWtiFan2Diff(reading.wtiFan2Diff());
        entity.setRelayDelay(reading.relayDelay());

        device2243ReadingJpaRepository.save(entity);
    }

    @Override
    public Optional<IrtccReading> findLatestIrtcc(String deviceId) {
        return irtccReadingJpaRepository.findFirstByDeviceIdOrderByRecordedAtDesc(deviceId).map(this::toDomain);
    }

    @Override
    public Optional<Device2243Reading> findLatestDevice2243(String deviceId) {
        return device2243ReadingJpaRepository.findFirstByDeviceIdOrderByRecordedAtDesc(deviceId).map(this::toDomain);
    }

    private IrtccReading toDomain(IrtccReadingEntity e) {
        return new IrtccReading(
                e.getDeviceId(), e.getRecordedAt(), e.getOtiTemperature(), e.getOtiTemperatureMax(),
                e.getWtiTemperature(), e.getWtiTemperatureMax(), e.getMog(), e.getTapPosition(),
                e.getTapPositionMax(), e.getTapCount(), e.getPtVoltage(), e.getActualPtVoltage(),
                e.getOperationMode(), e.getLvBreakerActive(), e.getHvBreakerActive(), e.getOltcLocal(),
                e.getPtFailActive(), e.getHooterActive(), e.getMuteVisible(), e.getAvrModeIsAuto(),
                e.getControlFailActive(), e.getAfrActive(), e.getRaiseRelayActive(), e.getLowerRelayActive(),
                e.getOverVoltActive(), e.getUnderVoltActive(), e.getAnnunciation(), e.getAnnunciationAck(),
                e.getAvrPtRatio(), e.getAvrSetVoltage(), e.getAvrRaiseRelayVoltage(), e.getAvrLowRelayVoltage(),
                e.getAvrHsForwardVoltage(), e.getAvrHsBackwardVoltage(), e.getAvrOverVoltage(),
                e.getAvrUnderVoltage(), e.getAvrPtFailSetpoint(), e.getAvrInitialTime(), e.getAvrSequentialTime(),
                e.getAvrHighFwdBwdTime(), e.getAvrControlFailTime(), e.getAvrRelayMomentaryTime());
    }

    private Device2243Reading toDomain(Device2243ReadingEntity e) {
        return new Device2243Reading(
                e.getDeviceId(), e.getRecordedAt(), e.getOtiTemperature(), e.getWtiTemperature(),
                e.getOtiAlarmSetpoint(), e.getOtiAlarmDiff(), e.getOtiTripSetpoint(), e.getOtiTripDiff(),
                e.getWtiAlarmSetpoint(), e.getWtiAlarmDiff(), e.getWtiTripSetpoint(), e.getWtiTripDiff(),
                e.getWtiFan1Setpoint(), e.getWtiFan1Diff(), e.getWtiFan2Setpoint(), e.getWtiFan2Diff(),
                e.getRelayDelay());
    }
}
