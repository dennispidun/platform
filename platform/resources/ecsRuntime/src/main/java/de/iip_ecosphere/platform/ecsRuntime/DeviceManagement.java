package de.iip_ecosphere.platform.ecsRuntime;

import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryAasClient;
import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryClient;
import de.iip_ecosphere.platform.ecsRuntime.ssh.RemoteAccessServer;
import de.iip_ecosphere.platform.ecsRuntime.ssh.RemoteAccessServerFactory;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.iip_aas.Id;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class DeviceManagement {

    private static DeviceRegistryAasClient deviceRegistryAasClient;
    private static RemoteAccessServer remoteAccessServer;

    public static DeviceRegistryClient getRegistryClient() throws IOException {
        if (null == deviceRegistryAasClient) {
            deviceRegistryAasClient = new DeviceRegistryAasClient();
        }
        return deviceRegistryAasClient;
    }

    public static RemoteAccessServer getRemoteAccessServer() {
        if (null == remoteAccessServer) {
            remoteAccessServer = RemoteAccessServerFactory.create();
        }
        return remoteAccessServer;
    }

    public static void initializeDevice() {
        try {
            DeviceRegistryClient registryClient = getRegistryClient();
            SubmodelElementCollection device = registryClient.getDevice(Id.getDeviceIdAas());

            if (null == device) {
                registryClient.addDevice(Id.getDeviceIdAas());
            }

            RemoteAccessServer remoteAccessServer = getRemoteAccessServer();
            remoteAccessServer.start();
        } catch (IOException | ExecutionException e) {
            e.printStackTrace();
        }

    }

    public static void removeDevice() {
        try {
            DeviceRegistryClient registryClient = getRegistryClient();
            SubmodelElementCollection device = registryClient.getDevice(Id.getDeviceIdAas());
            if (null != device) {
                registryClient.removeDevice(Id.getDeviceIdAas());
            }
        } catch (IOException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
