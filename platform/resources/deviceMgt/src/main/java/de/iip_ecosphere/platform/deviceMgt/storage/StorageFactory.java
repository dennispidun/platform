package de.iip_ecosphere.platform.deviceMgt.storage;

import de.iip_ecosphere.platform.deviceMgt.Configuration;
import de.iip_ecosphere.platform.deviceMgt.DeviceManagementFactory;
import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.ServiceLoader;

public class StorageFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageFactory.class.getName());

    private Storage runtimeStorage;
    private Configuration configuration;

    public Storage createRuntimeStorage() {
        loadConfiguration();

        if (runtimeStorage == null && configuration != null) {
            Optional<StorageFactoryDescriptor> storageFactoryDescriptors =
                    ServiceLoaderUtils.findFirst(StorageFactoryDescriptor.class);
            if (storageFactoryDescriptors.isPresent()) {
                runtimeStorage = storageFactoryDescriptors.get().createRuntimeStorage(configuration);
            } else {
                runtimeStorage = new S3StorageFactoryDescriptor().createRuntimeStorage(configuration);
                LOGGER.info("No StorageFactoryDescriptor implementation available, \" +\n" +
                        "fall back to default implementation: S3StorageFactoryDescriptor");
            }
        }
        return runtimeStorage;
    }

    private void loadConfiguration() {
        if (configuration == null) {
            try {
                configuration = Configuration.readFromYaml(Configuration.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

}
