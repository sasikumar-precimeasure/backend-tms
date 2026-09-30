package com.tmsbackend.infrastructure.persistence.adapter;

import com.tmsbackend.domain.model.Device;
import com.tmsbackend.domain.model.Gateway;
import com.tmsbackend.domain.model.Transformer;
import com.tmsbackend.domain.port.TopologyRepositoryPort;
import com.tmsbackend.infrastructure.persistence.entity.DeviceEntity;
import com.tmsbackend.infrastructure.persistence.entity.GatewayEntity;
import com.tmsbackend.infrastructure.persistence.entity.TransformerEntity;
import com.tmsbackend.infrastructure.persistence.repository.DeviceJpaRepository;
import com.tmsbackend.infrastructure.persistence.repository.GatewayJpaRepository;
import com.tmsbackend.infrastructure.persistence.repository.TransformerJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class TopologyRepositoryAdapter implements TopologyRepositoryPort {
    private final TransformerJpaRepository transformerJpaRepository;
    private final GatewayJpaRepository gatewayJpaRepository;
    private final DeviceJpaRepository deviceJpaRepository;

    public TopologyRepositoryAdapter(
            TransformerJpaRepository transformerJpaRepository,
            GatewayJpaRepository gatewayJpaRepository,
            DeviceJpaRepository deviceJpaRepository) {
        this.transformerJpaRepository = transformerJpaRepository;
        this.gatewayJpaRepository = gatewayJpaRepository;
        this.deviceJpaRepository = deviceJpaRepository;
    }

    @Override
    public void upsertTransformer(Transformer transformer) {
        TransformerEntity entity = transformerJpaRepository.findById(transformer.id()).orElseGet(TransformerEntity::new);
        entity.setId(transformer.id());
        entity.setName(transformer.name());
        transformerJpaRepository.save(entity);
    }

    @Override
    public void upsertGateway(Gateway gateway) {
        GatewayEntity entity = gatewayJpaRepository.findById(gateway.id()).orElseGet(GatewayEntity::new);
        entity.setId(gateway.id());
        entity.setTransformerId(gateway.transformerId());
        entity.setName(gateway.name());
        entity.setClientId(gateway.clientId());
        entity.setIpAddress(gateway.ipAddress());
        entity.setPort(gateway.port());
        gatewayJpaRepository.save(entity);
    }

    @Override
    public void upsertDevice(Device device) {
        DeviceEntity entity = deviceJpaRepository.findById(device.id()).orElseGet(DeviceEntity::new);
        entity.setId(device.id());
        entity.setGatewayId(device.gatewayId());
        entity.setName(device.name());
        entity.setSlaveId(device.slaveId());
        entity.setDeviceType(device.deviceType());
        entity.setEnabled(device.enabled());
        deviceJpaRepository.save(entity);
    }

    @Override
    public Optional<Device> findDevice(String deviceId) {
        return deviceJpaRepository.findById(deviceId).map(this::toDomain);
    }

    @Override
    public List<Device> findAllDevices() {
        return deviceJpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Transformer> findAllTransformers() {
        return transformerJpaRepository.findAll().stream()
                .map(e -> new Transformer(e.getId(), e.getName()))
                .toList();
    }

    @Override
    public List<Gateway> findAllGateways() {
        return gatewayJpaRepository.findAll().stream()
                .map(e -> new Gateway(e.getId(), e.getTransformerId(), e.getName(), e.getClientId(), e.getIpAddress(), e.getPort()))
                .toList();
    }

    private Device toDomain(DeviceEntity entity) {
        return new Device(entity.getId(), entity.getGatewayId(), entity.getName(), entity.getSlaveId(), entity.getDeviceType(), entity.isEnabled());
    }
}
