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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.deviceMgt.DeviceManagementAasClient;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.iip_ecosphere.platform.support.aas.*;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper;
import de.iip_ecosphere.platform.deviceMgt.DeviceManagementAas;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.MockitoJUnitRunner;
/**
 * Tests the {@link DeviceManagementAas}.
 * 
 * @author Dennis Pidun, University of Hildesheim
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceManagementAasTest {

    public static final String A_DEVICE = "A_DEVICE";
    private static Aas aas;
    private static Server implServer;
    private static Server aasServer;

    @Captor
    ArgumentCaptor<JsonResultWrapper.ExceptionFunction> exceptionFunctionArgumentCaptor;

    /**
     * Initializes the test.
     */
    @BeforeClass
    public static void startup() throws IOException {
        AasPartRegistry.AasBuildResult res = AasPartRegistry.build(); //c -> c instanceof DeviceManagementAas
        AasPartRegistry.setAasSetup(AasSetup.createLocalEphemeralSetup());
        implServer = res.getProtocolServerBuilder().build();
        implServer.start();
        aasServer = AasPartRegistry.deploy(res.getAas());
        aasServer.start();
        aas = AasPartRegistry.retrieveIipAas();
    }
    
    /**
     * Shuts down the test.
     */
    @AfterClass
    public static void shutdown() {
        implServer.stop(false);
        aasServer.stop(false);
    }
    
    @Test
    public void init_contributorClassLoads() {
        Assert.assertTrue(AasPartRegistry.contributorClasses().contains(DeviceManagementAas.class));
    }

    @Test
    public void init_contributedAasIsDeployed() {
        aas.accept(new AasPrintVisitor());

        Submodel resourcesSubmodel = aas.getSubmodel(AasPartRegistry.NAME_SUBMODEL_RESOURCES);
        Assert.assertNotNull(resourcesSubmodel);
        SubmodelElementCollection deviceManager = resourcesSubmodel
                .getSubmodelElementCollection("deviceManager");
        Assert.assertNotNull(deviceManager);
    }

//    ECS AAS lässt sich nicht so einfach "wegmocken": TODO: klären, ob dies sinnvoll beim Testen ist.
//    @Test
//    public void op_updateRuntime_shouldTriggerUpdate() throws IOException, URISyntaxException, ExecutionException {
//        DeviceManagementAasClient client = new DeviceManagementAasClient();
//
//        URI expectedUri = new URI(DeviceManagementAas.ECS_UPDATE_URI);
//        Capture capture = new Capture();
//        mockDeviceResourceWithUpdateOperation(A_DEVICE, capture);
//
//        client.updateRuntime(A_DEVICE);
//        AasPartRegistry.retrieveIipAas().accept(new AasPrintVisitor());
//        Assert.assertEquals(expectedUri, capture.getValue());
//    }
//
//    public static void mockDeviceResourceWithUpdateOperation(String aDeviceId,
//                                                             Capture capture)
//            throws IOException {
//
//        AasPartRegistry.retrieveIipAas().getSubmodel(AasPartRegistry.NAME_SUBMODEL_RESOURCES)
//            .createSubmodelElementCollectionBuilder(aDeviceId, false, false)
//                .createOperationBuilder("updateRuntime")
//                .addInputVariable("location", Type.ANY_URI)
//                .setInvocable((objects -> {
//                    System.out.println("Hello");
//                    return null;
//                }))
//                .build();
//
//    }
//
//    private static final class Capture {
//        private Object value;
//
//        public Object getValue() {
//            return value;
//        }
//
//        public void setValue(Object value) {
//            this.value = value;
//        }
//    }

}
