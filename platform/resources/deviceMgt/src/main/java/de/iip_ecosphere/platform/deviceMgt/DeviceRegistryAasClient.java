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

package de.iip_ecosphere.platform.deviceMgt;

import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.iip_aas.SubmodelElementsCollectionClient;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.StreamSupport;


public class DeviceRegistryAasClient extends SubmodelElementsCollectionClient implements DeviceRegistryClient {

    public DeviceRegistryAasClient() throws IOException {
        super(DeviceRegistryAas.NAME_SUBMODEL, DeviceRegistryAas.NAME_COLL_DEVICE_REGISTRY);
    }

    @Override
    public SubmodelElementCollection getDevices() {
        return this.getSubmodel()
                .getSubmodelElementCollection(DeviceRegistryAas.NAME_COLL_DEVICE_REGISTRY)
                .getSubmodelElementCollection(DeviceRegistryAas.NAME_COLL_MANAGED_DEVICES);
    }

    @Override
    public SubmodelElementCollection getDevice(String resourceId) {
        Optional<SubmodelElementCollection> device = StreamSupport.stream(this.getDevices().elements().spliterator(), false)
                .map(e -> this.getDevices().getSubmodelElementCollection(e.getIdShort()))
                .filter(e -> {
                    try {
                        return resourceId.equals(e.getProperty(
                                DeviceRegistryAas.NAME_PROP_DEVICE_RESOURCE_ID
                        ).getValue());
                    } catch (ExecutionException ex) {
                        return false;
                    }
                })
                .findFirst();
        return device.orElse(null);
    }

    @Override
    public void addDevice(String id) throws ExecutionException {
        getOperation(DeviceRegistryAas.NAME_OP_DEVICE_ADD).invoke(id);
    }

    @Override
    public void removeDevice(String id) throws ExecutionException {
        getOperation(DeviceRegistryAas.NAME_OP_DEVICE_REMOVE).invoke(id);
    }
}
