package de.iip_ecosphere.platform.deviceMgt;

import java.util.concurrent.ExecutionException;

public interface DeviceFirmwareOperations {

    public void updateRuntime(String id) throws ExecutionException;

}
