/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which is available
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.deviceMgt.storage;

import io.minio.MinioClient;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * A S3RuntimeStorage grants access to the runtime storage through s3.
 * For this purpose it uses MinioClient to communicate with the s3 storage.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public class S3RuntimeStorage extends S3Storage {

    public static final String PREFIX = "runtimes/";
    public static final String RUNTIME_YML_NAME = "runtime.yml";
    public static final String RUNTIME_IMAGE_NAME = "runtime-image.zip";

    /**
     * Creates a new S3RuntimeStorage
     *
     * @param minioClient the connected MinioClient
     * @param bucket the bucket
     */
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
