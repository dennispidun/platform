package de.iip_ecosphere.platform.deviceMgt;

import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.mock;

public class StubDeviceManagement implements DeviceManagement {

    private static DeviceRemoteManagementOperations managementOperationsStub;
    private static DeviceFirmwareOperations firmwareOperationsStub;
    private static DeviceResourceConfigOperations resourceConfigOperationsStub;

    static DeviceFirmwareOperations mockFirmwareOperations() {
        if (firmwareOperationsStub == null) {
            firmwareOperationsStub = mock(DeviceFirmwareOperations.class);
        }
        return firmwareOperationsStub;
    }

    static DeviceResourceConfigOperations mockResourceConfigOperations() {
        if (resourceConfigOperationsStub == null) {
            resourceConfigOperationsStub = mock(DeviceResourceConfigOperations.class);
        }
        return resourceConfigOperationsStub;
    }

    static DeviceRemoteManagementOperations mockRemoteManagementOperations() {
        if (managementOperationsStub == null) {
            managementOperationsStub = mock(DeviceRemoteManagementOperations.class);
        }
        return managementOperationsStub;
    }

    @Override
    public void updateRuntime(String id) throws ExecutionException {
        if (firmwareOperationsStub != null) {
            firmwareOperationsStub.updateRuntime(id);
        }
    }

    @Override
    public SSHConnectionDetails establishSsh(String id) throws ExecutionException {
        if (managementOperationsStub != null) {
            return managementOperationsStub.establishSsh(id);
        }

        return null;
    }

    @Override
    public void setConfig(String id, String configPath) throws ExecutionException {
        if (resourceConfigOperationsStub != null) {
            resourceConfigOperationsStub.setConfig(id, configPath);
        }
    }
}
