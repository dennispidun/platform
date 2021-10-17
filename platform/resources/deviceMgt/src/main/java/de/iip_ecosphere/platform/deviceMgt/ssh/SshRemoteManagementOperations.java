package de.iip_ecosphere.platform.deviceMgt.ssh;

import de.iip_ecosphere.platform.deviceMgt.DeviceRemoteManagementOperations;
import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryAas;
import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryAasClient;
import de.iip_ecosphere.platform.ecsRuntime.EcsAasClient;
import de.iip_ecosphere.platform.ecsRuntime.ssh.RemoteAccessServer;
import de.iip_ecosphere.platform.support.aas.Property;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class SshRemoteManagementOperations implements DeviceRemoteManagementOperations {

    @Override
    public SSHConnectionDetails establishSsh(String id) throws ExecutionException {
        RemoteAccessServer.Credentials credentials = null;
        String deviceIp = null;
        try {
            credentials = new EcsAasClient(id).createRemoteConnectionCredentials();
            Property ipProp = new DeviceRegistryAasClient().getDevice(id).getProperty(DeviceRegistryAas.NAME_PROP_DEVICE_IP);
            deviceIp = (String) ipProp.getValue();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (credentials == null) {
            return null;
        }

        return new SSHConnectionDetails(deviceIp, 5555, credentials.getKey(), credentials.getSecret());
    }
}
