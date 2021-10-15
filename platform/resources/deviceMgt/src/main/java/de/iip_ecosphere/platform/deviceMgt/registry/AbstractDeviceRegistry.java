package de.iip_ecosphere.platform.deviceMgt.registry;

import java.util.concurrent.ExecutionException;

public abstract class AbstractDeviceRegistry implements DeviceRegistry {

    @Override
    public void addDevice(String id, String ip) throws ExecutionException {
        DeviceRegistryAas.notifyDeviceAdded(id, id, ip);
    }

    @Override
    public void removeDevice(String id) throws ExecutionException {
        DeviceRegistryAas.notifyDeviceRemoved(id);
    }
}
