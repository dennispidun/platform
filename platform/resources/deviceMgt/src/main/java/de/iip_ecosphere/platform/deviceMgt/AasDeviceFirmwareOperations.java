package de.iip_ecosphere.platform.deviceMgt;

import java.util.concurrent.ExecutionException;

public class AasDeviceFirmwareOperations implements DeviceFirmwareOperations {
    @Override
    public void updateRuntime(String id) throws ExecutionException {
        DeviceManagementAas.notifyUpdateRuntime(id);
    }
}
