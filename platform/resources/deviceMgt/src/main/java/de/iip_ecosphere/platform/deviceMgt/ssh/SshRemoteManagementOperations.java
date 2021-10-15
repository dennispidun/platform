package de.iip_ecosphere.platform.deviceMgt.ssh;

import de.iip_ecosphere.platform.deviceMgt.DeviceRemoteManagementOperations;

import java.util.concurrent.ExecutionException;

public class SshRemoteManagementOperations implements DeviceRemoteManagementOperations {

    public static DeviceRemoteManagementOperations getInstance() {
        return new SshRemoteManagementOperations();
    }

    @Override
    public SSHConnectionDetails createSSHServer(String id) throws ExecutionException {
        return null;
    }
}
