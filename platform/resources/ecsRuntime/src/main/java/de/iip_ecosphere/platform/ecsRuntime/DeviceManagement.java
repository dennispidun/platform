package de.iip_ecosphere.platform.ecsRuntime;

import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryAasClient;
import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryClient;
import de.iip_ecosphere.platform.ecsRuntime.ssh.RemoteAccessServer;
import de.iip_ecosphere.platform.ecsRuntime.ssh.RemoteAccessServerFactory;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.iip_aas.Id;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutionException;

public class DeviceManagement {

    private static RemoteAccessServer remoteAccessServer;

    public static DeviceRegistryClient getRegistryClient() throws IOException {
        return new DeviceRegistryAasClient();
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
                String ip = "";

                // see https://stackoverflow.com/a/38342964
                try(final DatagramSocket socket = new DatagramSocket()){
                    socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                    ip = socket.getLocalAddress().getHostAddress();
                }
                registryClient.addDevice(Id.getDeviceIdAas(), ip);
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
