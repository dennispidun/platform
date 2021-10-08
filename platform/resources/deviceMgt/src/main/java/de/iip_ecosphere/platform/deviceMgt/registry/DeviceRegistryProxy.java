package de.iip_ecosphere.platform.deviceMgt.registry;

import de.iip_ecosphere.platform.deviceMgt.DeviceDescriptor;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutionException;

class DeviceRegistryProxy extends AbstractDeviceRegistry {

    private DeviceRegistry sink;

    public DeviceRegistryProxy(DeviceRegistry sink) {
        this.sink = sink;
    }

    @Override
    public void addDevice(String id) throws ExecutionException {
        super.addDevice(id);
        sink.addDevice(id);
    }

    @Override
    public void removeDevice(String id) throws ExecutionException {
        super.removeDevice(id);
        sink.removeDevice(id);
    }

    @Override
    public void imAlive(String id) throws ExecutionException {
        sink.imAlive(id);
    }

    @Override
    public void sendTelemetry(String id, String telemetryData) throws ExecutionException {
        sink.sendTelemetry(id, telemetryData);
    }

    @Override
    public Set<String> getIds() {
        return sink.getIds();
    }

    @Override
    public Set<String> getManagedIds() {
        return sink.getIds();
    }

    @Override
    public Collection<? extends DeviceDescriptor> getDevices() {
        return sink.getDevices();
    }

    @Override
    public DeviceDescriptor getDevice(String id) {
        return sink.getDevice(id);
    }

    @Override
    public DeviceDescriptor getDeviceByManagedId(String id) {
        return null;
    }
}
