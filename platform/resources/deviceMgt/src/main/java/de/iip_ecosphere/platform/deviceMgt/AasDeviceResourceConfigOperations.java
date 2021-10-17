package de.iip_ecosphere.platform.deviceMgt;

import java.util.concurrent.ExecutionException;

public class AasDeviceResourceConfigOperations implements DeviceResourceConfigOperations {

    public static final String A_CONFIG_DOWNLOAD_URI = "A_CONFIG_DOWNLOAD_URI";
    public static final String A_LOCATION = "A_LOCATION";

    @Override
    public void setConfig(String id, String configPath) throws ExecutionException {
        DeviceManagementAas.notifySetConfig(id, A_CONFIG_DOWNLOAD_URI, A_LOCATION);
    }
}
