package de.iip_ecosphere.platform.deviceMgt.storage;

import io.minio.MinioClient;

import java.util.Set;
import java.util.stream.Collectors;

public class S3RuntimeStorage extends S3Storage {

    public static final String PREFIX = "runtimes/";
    public static final String RUNTIME_YML_NAME = "runtime.yml";
    public static final String RUNTIME_IMAGE_NAME = "runtime-image.zip";

    public S3RuntimeStorage(MinioClient minioClient, String bucket) {
        super(PREFIX, minioClient, bucket);
    }

    @Override
    public Set<String> list() {
        return super.list().stream()
                .filter(key -> key.endsWith(RUNTIME_YML_NAME))
                .map(key -> key.replace("/"+RUNTIME_YML_NAME, ""))
                .collect(Collectors.toSet());
    }

    @Override
    public String generateDownloadUrl(String runtime) {
        String key = PREFIX + runtime + "/" + RUNTIME_IMAGE_NAME;
        return super.generateDownloadUrl(key);
    }
}
