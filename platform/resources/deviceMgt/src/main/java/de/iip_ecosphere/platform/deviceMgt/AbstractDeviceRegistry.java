package de.iip_ecosphere.platform.deviceMgt;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractDeviceRegistry implements DeviceRegistry {

    private static AtomicInteger id = new AtomicInteger(0);

    @Override
    public void addDevice(String id) throws ExecutionException {
        DeviceRegistryAas.notifyDeviceAdded("d_" + AbstractDeviceRegistry.id.incrementAndGet(), id);
    }

    @Override
    public void removeDevice(String id) throws ExecutionException {
        DeviceRegistryAas.notifyDeviceRemoved(id);
    }
}
