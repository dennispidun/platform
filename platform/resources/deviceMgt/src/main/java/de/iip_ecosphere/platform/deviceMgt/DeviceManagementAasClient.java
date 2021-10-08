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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryAas;
import de.iip_ecosphere.platform.support.iip_aas.SubmodelElementsCollectionClient;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper.fromJson;


public class DeviceManagementAasClient extends SubmodelElementsCollectionClient implements DeviceFirmwareOperations, DeviceRemoteManagementOperations {

    public DeviceManagementAasClient() throws IOException {
        super(DeviceRegistryAas.NAME_SUBMODEL, DeviceManagementAas.NAME_COLL_DEVICE_MANAGER);
    }

    @Override
    public void updateRuntime(String id) throws ExecutionException {
        getOperation(DeviceManagementAas.NAME_OP_UPDATE_RUNTIME).invoke(id);
    }

    @Override
    public SSHConnectionDetails createSSHServer(String id) throws ExecutionException {
        Object operationResult = getOperation(DeviceManagementAas.NAME_OP_ESTABLISH_SSH).invoke(id);
        String result = fromJson(operationResult);
        ObjectMapper mapper = new ObjectMapper();
        try {
            SSHConnectionDetails connectionDetails = mapper.readValue(result, SSHConnectionDetails.class);
            System.out.println(connectionDetails);
            return connectionDetails;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
