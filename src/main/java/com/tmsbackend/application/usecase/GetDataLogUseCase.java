package com.tmsbackend.application.usecase;

import com.tmsbackend.domain.model.Device;
import com.tmsbackend.domain.model.Device2243Reading;
import com.tmsbackend.domain.model.DeviceType;
import com.tmsbackend.domain.model.Gateway;
import com.tmsbackend.domain.model.IrtccReading;
import com.tmsbackend.domain.model.PagedResult;
import com.tmsbackend.domain.model.Transformer;
import com.tmsbackend.domain.port.ReadingRepositoryPort;
import com.tmsbackend.domain.port.TopologyRepositoryPort;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

// Backs the Data Log screen (Settings > Data Log): browsing/exporting the
// historical readings the 1-minute ingestion push has stored, per device,
// over a date range - a pure read layer over ReadingRepositoryPort, with no
// mutation of its own.
@Component
public class GetDataLogUseCase {
    public static class DeviceNotFoundException extends RuntimeException {
        public DeviceNotFoundException(String deviceId) {
            super("Device " + deviceId + " was not found");
        }
    }

    // A record listing every transformer with its gateways/devices nested -
    // exactly what the frontend's Select Transformer -> device dropdown
    // needs in one call, rather than three separate list endpoints.
    public record TransformerTopology(Transformer transformer, List<GatewayTopology> gateways) {
    }

    public record GatewayTopology(Gateway gateway, List<Device> devices) {
    }

    private final TopologyRepositoryPort topologyRepository;
    private final ReadingRepositoryPort readingRepository;

    public GetDataLogUseCase(TopologyRepositoryPort topologyRepository, ReadingRepositoryPort readingRepository) {
        this.topologyRepository = topologyRepository;
        this.readingRepository = readingRepository;
    }

    public List<TransformerTopology> listTopology() {
        List<Transformer> transformers = topologyRepository.findAllTransformers();
        List<Gateway> gateways = topologyRepository.findAllGateways();
        List<Device> devices = topologyRepository.findAllDevices();

        return transformers.stream()
                .map(transformer -> {
                    List<GatewayTopology> gatewayTopologies = gateways.stream()
                            .filter(gw -> gw.transformerId().equals(transformer.id()))
                            .map(gw -> new GatewayTopology(
                                    gw, devices.stream().filter(d -> d.gatewayId().equals(gw.id())).toList()))
                            .toList();
                    return new TransformerTopology(transformer, gatewayTopologies);
                })
                .toList();
    }

    private Device requireDevice(String deviceId) {
        return topologyRepository.findDevice(deviceId).orElseThrow(() -> new DeviceNotFoundException(deviceId));
    }

    public PagedResult<IrtccReading> pageIrtcc(String deviceId, Instant from, Instant to, int page, int pageSize) {
        Device device = requireDevice(deviceId);
        if (device.deviceType() != DeviceType.IRTCC) {
            throw new DeviceNotFoundException(deviceId);
        }
        return readingRepository.findIrtccByDeviceAndDateRange(deviceId, from, to, page, pageSize);
    }

    public PagedResult<Device2243Reading> pageDevice2243(String deviceId, Instant from, Instant to, int page, int pageSize) {
        Device device = requireDevice(deviceId);
        if (device.deviceType() != DeviceType.DEVICE_2243) {
            throw new DeviceNotFoundException(deviceId);
        }
        return readingRepository.findDevice2243ByDeviceAndDateRange(deviceId, from, to, page, pageSize);
    }

    public List<IrtccReading> exportIrtcc(String deviceId, Instant from, Instant to) {
        Device device = requireDevice(deviceId);
        if (device.deviceType() != DeviceType.IRTCC) {
            throw new DeviceNotFoundException(deviceId);
        }
        return readingRepository.findAllIrtccByDeviceAndDateRange(deviceId, from, to);
    }

    public List<Device2243Reading> exportDevice2243(String deviceId, Instant from, Instant to) {
        Device device = requireDevice(deviceId);
        if (device.deviceType() != DeviceType.DEVICE_2243) {
            throw new DeviceNotFoundException(deviceId);
        }
        return readingRepository.findAllDevice2243ByDeviceAndDateRange(deviceId, from, to);
    }
}
