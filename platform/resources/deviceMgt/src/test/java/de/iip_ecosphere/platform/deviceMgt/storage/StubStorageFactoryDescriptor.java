package de.iip_ecosphere.platform.deviceMgt.storage;

import de.iip_ecosphere.platform.deviceMgt.Configuration;

public class StubStorageFactoryDescriptor implements StorageFactoryDescriptor {

    private static Storage storage;

    @Override
    public Storage createRuntimeStorage(Configuration configuration) {
        if (storage != null) {
            return storage;
        }

        return null;
    }

    public static void setStorage(Storage storage) {
        StubStorageFactoryDescriptor.storage = storage;
    }
}
