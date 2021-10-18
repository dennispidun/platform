package de.iip_ecosphere.platform.deviceMgt.storage;

import de.iip_ecosphere.platform.deviceMgt.Configuration;

public interface StorageFactoryDescriptor {

    Storage createRuntimeStorage(Configuration configuration);

}
