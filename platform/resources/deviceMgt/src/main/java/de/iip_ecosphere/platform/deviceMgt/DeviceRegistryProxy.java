package de.iip_ecosphere.platform.deviceMgt;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class DeviceRegistryProxy extends AbstractDeviceRegistry {

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
    public Set<String> getIds() {
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
}
