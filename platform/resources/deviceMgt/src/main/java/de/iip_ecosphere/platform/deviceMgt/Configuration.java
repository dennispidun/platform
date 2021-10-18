package de.iip_ecosphere.platform.deviceMgt;

import de.iip_ecosphere.platform.deviceMgt.storage.StorageSetup;
import de.iip_ecosphere.platform.support.iip_aas.AasConfiguration;

/**
 * Basic Configuration
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public class Configuration extends AasConfiguration {

    private StorageSetup storage;

    /**
     * Get the StorageSetup
     *
     * @return the storageSetup
     */
    public StorageSetup getStorage() {
        return storage;
    }

    /**
     * Set the StorageSetup
     *
     * @param storage the StorageSetup
     */
    public void setStorage(StorageSetup storage) {
        this.storage = storage;
    }
}
