package de.iip_ecosphere.platform.deviceMgt.storage;

import de.iip_ecosphere.platform.deviceMgt.Configuration;

public class StubStorageFactoryDescriptor implements StorageFactoryDescriptor {

    private static Storage runtimeStorage;
    private static Storage configStorage;

    @Override
    public Storage createRuntimeStorage(Configuration configuration) {
        if (runtimeStorage != null) {
            return runtimeStorage;
        }

        return null;
    }

    @Override
    public Storage createConfigStorage(Configuration configuration) {
        if (runtimeStorage != null) {
            return configStorage;
        }

        return null;
    }

    public static void setRuntimeStorage(Storage storage) {
        StubStorageFactoryDescriptor.runtimeStorage = storage;
    }
}
