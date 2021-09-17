package de.iip_ecosphere.platform.deviceMgt;

import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;

public interface DeviceRegistryClient extends DeviceRegistryOperations {

    public SubmodelElementCollection getDevices();

    public SubmodelElementCollection getDevice(String resourceId);

}
