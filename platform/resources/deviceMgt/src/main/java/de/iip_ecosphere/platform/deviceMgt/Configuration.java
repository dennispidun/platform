package de.iip_ecosphere.platform.deviceMgt;

import de.iip_ecosphere.platform.deviceMgt.storage.StorageSetup;
import de.iip_ecosphere.platform.support.iip_aas.AasConfiguration;

public class Configuration extends AasConfiguration {

    private StorageSetup storage;

    public StorageSetup getStorage() {
        return storage;
    }

    public void setStorage(StorageSetup storage) {
        this.storage = storage;
    }
}
