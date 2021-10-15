package de.iip_ecosphere.platform.deviceMgt.registry;

import java.util.concurrent.ExecutionException;

public interface DeviceRegistryOperations {

    public void addDevice(String id, String ip) throws ExecutionException;

    public void removeDevice(String id) throws ExecutionException;

    public void imAlive(String id) throws ExecutionException;

    public void sendTelemetry(String id, String telemetryData) throws ExecutionException;
}
