package de.iip_ecosphere.platform.deviceMgt;

import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistry;
import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryFactory;

import java.util.concurrent.ExecutionException;

public class DeviceManagementImpl implements DeviceManagement {

    private DeviceFirmwareOperations firmwareOperations;
    private DeviceRemoteManagementOperations remoteManagementOperations;
    private DeviceResourceConfigOperations resourceConfigOperations;

    private final DeviceRegistry deviceRegistry;

    public DeviceManagementImpl(DeviceFirmwareOperations firmwareOperations,
                                DeviceRemoteManagementOperations remoteManagementOperations,
                                DeviceResourceConfigOperations resourceConfigOperations) {
        this.firmwareOperations = firmwareOperations;
        this.remoteManagementOperations = remoteManagementOperations;
        this.resourceConfigOperations = resourceConfigOperations;
        this.deviceRegistry = DeviceRegistryFactory.getDeviceRegistry();
    }

    @Override
    public void updateRuntime(String id) throws ExecutionException {
        if (deviceRegistry.getDevice(id) == null) {
            return;
        }
        this.firmwareOperations.updateRuntime(id);
    }

    @Override
    public SSHConnectionDetails establishSsh(String id) throws ExecutionException {
        return remoteManagementOperations.establishSsh(id);
    }

    @Override
    public void setConfig(String id, String configPath) throws ExecutionException {
        if (deviceRegistry.getDevice(id) == null) {
            return;
        }
        resourceConfigOperations.setConfig(id, configPath);
    }
}
