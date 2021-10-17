package de.iip_ecosphere.platform.deviceMgt;

import java.util.concurrent.ExecutionException;

public interface DeviceResourceConfigOperations {

    void setConfig(String id, String configPath) throws ExecutionException;
}
