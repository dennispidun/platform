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

/**
 * POJO class for the Storage Setup
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public class StorageSetup {

    private String endpoint;
    private String accessKey;
    private String secretAccessKey;
    private String bucket;

    /**
     * Default constructor, used by SnakeYaml.
     */
    public StorageSetup() {
    }

    /**
     * Get the bucket.
     *
     * @return the bucket
     */
    public String getBucket() {
        return bucket;
    }

    /**
     * Set the bucket.
     *
     * @param bucket the bucket
     */
    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    /**
     * Get the endpoint
     *
     * @return the endpoint
     */
    public String getEndpoint() {
        return endpoint;
    }


    /**
     * Set the endpoint.
     *
     * @param endpoint the endpoint
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }


    /**
     * Get the accessKey
     *
     * @return the accessKey
     */
    public String getAccessKey() {
        return accessKey;
    }


    /**
     * Set the accessKey.
     *
     * @param accessKey the accessKey
     */
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    /**
     * Get the secretAccessKey
     *
     * @return the secretAccessKey
     */
    public String getSecretAccessKey() {
        return secretAccessKey;
    }


    /**
     * Set the secretAccessKey.
     *
     * @param secretAccessKey the secretAccessKey
     */
    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }
}
