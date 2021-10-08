package de.iip_ecosphere.platform.deviceMgt.registry;

import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;

import java.util.Set;

public interface DeviceRegistryClient extends DeviceRegistryOperations {

    public Set<SubmodelElementCollection> getDevices();

    public SubmodelElementCollection getDevice(String resourceId);

}
