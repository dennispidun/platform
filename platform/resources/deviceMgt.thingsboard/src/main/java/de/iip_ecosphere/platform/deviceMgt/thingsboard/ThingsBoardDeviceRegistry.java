package de.iip_ecosphere.platform.deviceMgt.thingsboard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.iip_ecosphere.platform.deviceMgt.DeviceDescriptor;
import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistry;
import org.thingsboard.rest.client.RestClient;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.device.DeviceSearchQuery;
import org.thingsboard.server.common.data.id.DeviceId;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class ThingsBoardDeviceRegistry implements DeviceRegistry {

    private RestClient restClient;

    public ThingsBoardDeviceRegistry(RestClient restClient) {
        this.restClient = restClient;
    }

    public RestClient getRestClient() {
        return restClient;
    }

    @Override
    public Set<String> getIds() {
        return restClient.findByQuery(new DeviceSearchQuery())
                .stream()
                .map(Device::getName)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> getManagedIds() {
        return restClient.findByQuery(new DeviceSearchQuery())
                .stream()
                .map(d -> d.getId().toString())
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<? extends DeviceDescriptor> getDevices() {
        return restClient.findByQuery(new DeviceSearchQuery())
                .stream()
                .map(d -> new ThingsBoardDeviceDescriptor(d, this.restClient))
                .collect(Collectors.toSet());
    }

    @Override
    public DeviceDescriptor getDevice(String id) {
        return this.restClient.getTenantDevice(id)
                .map(d -> new ThingsBoardDeviceDescriptor(d, this.restClient))
                .orElse(null);

    }

    @Override
    public DeviceDescriptor getDeviceByManagedId(String id) {
        return this.restClient.getDeviceById(new DeviceId(UUID.fromString(id)))
                .map(tbDevice -> new ThingsBoardDeviceDescriptor(tbDevice, this.restClient))
                .orElse(null);
    }

    @Override
    public void addDevice(String id, String ip) {
        if (id == null || id.isEmpty() || ip == null || ip.isEmpty()) {
            return;
        }

        Device tbDevice = this.restClient.getTenantDevice(id).orElse(null);
        if (tbDevice == null) {
            Device device = new Device();
            device.setName(id);
            tbDevice = this.restClient.saveDevice(device);
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode attribute = mapper.readTree("{\"ip\": \"" + ip + "\"}");
            this.restClient.saveDeviceAttributes(tbDevice.getId(), "SERVER_SCOPE", attribute);
        } catch (JsonProcessingException ignore) {
        }

    }

    @Override
    public void removeDevice(String id) {
        if (id == null || id.isEmpty()) {
            return;
        }

        this.restClient.getTenantDevice(id)
                .ifPresent(tbDevice -> this.restClient.deleteDevice(tbDevice.getId()));
    }

    @Override
    public void imAlive(String id) throws ExecutionException {
        sendTelemetry(id, "{\"active\": true}");
    }

    @Override
    public void sendTelemetry(String id, String telemetryData) throws ExecutionException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode telemetry = mapper.readTree(telemetryData);
            this.restClient.getTenantDevice(id)
                    .ifPresent(tbDevice ->
                            this.restClient.saveEntityTelemetry(tbDevice.getId(), "SERVER_SCOPE", telemetry));
        } catch (JsonProcessingException e) {
            throw new ExecutionException("TelemetryData is not json: ", e);
        }
    }
}
