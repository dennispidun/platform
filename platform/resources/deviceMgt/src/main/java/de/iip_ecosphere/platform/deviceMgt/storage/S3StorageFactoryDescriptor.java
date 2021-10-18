package de.iip_ecosphere.platform.deviceMgt.storage;

import de.iip_ecosphere.platform.deviceMgt.Configuration;
import io.minio.MinioClient;

public class S3StorageFactoryDescriptor implements StorageFactoryDescriptor {

    public Storage createRuntimeStorage(Configuration configuration) {
        if (null == configuration || null == configuration.getStorage()) {
            return null;
        }

        StorageSetup storageSetup = configuration.getStorage();
        MinioClient minioClient = MinioClient.builder()
                .endpoint(storageSetup.getEndpoint())
                .credentials(storageSetup.getAccessKey(), storageSetup.getSecretAccessKey())
                .build();
        return new S3RuntimeStorage(minioClient, storageSetup.getBucket());
    }
}
