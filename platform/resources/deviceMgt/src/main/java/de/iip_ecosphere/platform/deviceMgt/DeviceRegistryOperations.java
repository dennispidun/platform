package de.iip_ecosphere.platform.deviceMgt;

import java.util.concurrent.ExecutionException;

public interface DeviceRegistryOperations {

    public void addDevice(String id) throws ExecutionException;

    public void removeDevice(String id) throws ExecutionException;

}
