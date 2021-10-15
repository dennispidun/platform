package de.iip_ecosphere.platform.deviceMgt;

import de.iip_ecosphere.platform.deviceMgt.ssh.SshRemoteManagementOperations;

import java.util.concurrent.ExecutionException;

public class DeviceManagementImpl implements DeviceManagement {

    private DeviceFirmwareOperations firmwareOperations;
    private DeviceRemoteManagementOperations remoteManagementOperations;
    private DeviceResourceConfigOperations resourceConfigOperations;

    public DeviceManagementImpl(DeviceFirmwareOperations firmwareOperations,
                                DeviceRemoteManagementOperations remoteManagementOperations,
                                DeviceResourceConfigOperations resourceConfigOperations) {
        this.firmwareOperations = firmwareOperations;
        this.remoteManagementOperations = remoteManagementOperations;
        this.resourceConfigOperations = resourceConfigOperations;
    }

    @Override
    public void updateRuntime(String id) throws ExecutionException {
        DeviceManagementAas.notifyUpdateRuntime(id);
    }

    @Override
    public SSHConnectionDetails createSSHServer(String id) throws ExecutionException {
        return SshRemoteManagementOperations.getInstance().createSSHServer(id);
    }
}
