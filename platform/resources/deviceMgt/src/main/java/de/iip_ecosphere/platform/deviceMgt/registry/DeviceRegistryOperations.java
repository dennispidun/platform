package de.iip_ecosphere.platform.deviceMgt.registry;

import java.util.concurrent.ExecutionException;

public interface DeviceRegistryOperations {

    void addDevice(String id, String ip) throws ExecutionException;

    void removeDevice(String id) throws ExecutionException;

    void imAlive(String id) throws ExecutionException;

    void sendTelemetry(String id, String telemetryData) throws ExecutionException;
}
