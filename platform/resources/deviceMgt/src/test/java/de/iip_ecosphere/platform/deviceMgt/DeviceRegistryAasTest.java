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

import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistry;
import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryAas;
import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryAasClient;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.*;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import org.junit.*;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.*;
import static de.iip_ecosphere.platform.deviceMgt.StubDeviceRegistryFactoryDescriptor.mockDeviceRegistry;

/**
 * Tests the {@link DeviceRegistryAas}.
 * 
 * @author Dennis Pidun, University of Hildesheim
 */
public class DeviceRegistryAasTest {

    private final static Class contributorClass = DeviceRegistryAas.class;

    public static final String A_VALID_DEVICE = "A_VALID_DEVICE";
    public static final String AN_INVALID_DEVICE = "AN_INVALID_DEVICE";
    public static final String SOME_TELEMETRY_DATA = "{\"someField\": \"someData\"}";

    private Aas aas;
    private Server implServer;
    private Server aasServer;
    private Submodel resourcesSubmodel;
    private SubmodelElementCollection deviceRegistry;

    @Before
    public void setUp() throws Exception {
        ActiveAasBase.setNotificationMode(ActiveAasBase.NotificationMode.SYNCHRONOUS);

        AasPartRegistry.setAasSetup(AasSetup.createLocalEphemeralSetup());
        AasPartRegistry.AasBuildResult res = AasPartRegistry.build(contributorClass::isInstance);

        implServer = res.getProtocolServerBuilder().build();
        implServer.start();
        aasServer = AasPartRegistry.deploy(res.getAas());
        aasServer.start();

        aas = AasPartRegistry.retrieveIipAas();
        aas.accept(new AasPrintVisitor());

        resourcesSubmodel = aas.getSubmodel(AasPartRegistry.NAME_SUBMODEL_RESOURCES);
        deviceRegistry = resourcesSubmodel
                .getSubmodelElementCollection(DeviceRegistryAas.NAME_COLL_DEVICE_REGISTRY);
    }

    @After
    public void tearDown() {
        implServer.stop(true);
        aasServer.stop(true);

        // reset Mocks, as Implementation build DeviceRegistry only once
        // otherwise old invocations-counter won't reset
        Mockito.reset(mockDeviceRegistry());
    }
    
    @Test
    public void init_contributorClassLoads() {
        Assert.assertTrue(AasPartRegistry.contributorClasses().contains(contributorClass));
    }

    @Test
    public void init_contributedAasIsDeployed() {
        Assert.assertNotNull(resourcesSubmodel);
        Assert.assertNotNull(deviceRegistry);
    }

    @Test
    public void op_addDevice_withValidDeviceIdentifier_addsDevice() throws ExecutionException, IOException {
        mockDeviceResource(A_VALID_DEVICE);
        DeviceRegistryAasClient client = new DeviceRegistryAasClient();
        client.addDevice(A_VALID_DEVICE);

        Set<SubmodelElementCollection> devices = client.getDevices();
        Assert.assertNotNull(devices);

        int elementsCount = devices.size();
        Assert.assertEquals(1, elementsCount);
        AasPartRegistry.retrieveIipAas().accept(new AasPrintVisitor());
    }

    @Test
    public void op_removeDevice_withValidDeviceIdentifier_removesDevice() throws ExecutionException, IOException {
        mockDeviceResource(A_VALID_DEVICE);
        DeviceRegistryAasClient client = new DeviceRegistryAasClient();
        int beforeCount = client.getDevices().size();

        client.addDevice(A_VALID_DEVICE);

        // refresh client, as its underlying submodel is outdated
        client = new DeviceRegistryAasClient();
        int addCount = client.getDevices().size();
        Assert.assertEquals(1, addCount);

        client.removeDevice(A_VALID_DEVICE);

        client = new DeviceRegistryAasClient();
        int afterCount = client.getDevices().size();

        Assert.assertEquals(beforeCount, afterCount);
    }

    @Test
    public void op_imAlive_isCallable() throws IOException, ExecutionException {
        Operation operation = getOperation(DeviceRegistryAas.NAME_OP_IM_ALIVE);

        Assert.assertNotNull(operation);

        Object invoke = operation.invoke(A_VALID_DEVICE);
        Assert.assertNotNull(invoke);
    }

    private Operation getOperation(String operationName) {
        Operation operation = aas.getSubmodel(AasPartRegistry.NAME_SUBMODEL_RESOURCES)
                .getSubmodelElementCollection(DeviceRegistryAas.NAME_COLL_DEVICE_REGISTRY)
                .getOperation(operationName);
        return operation;
    }

    @Test
    public void op_imAlive_callsImAliveFromRegistry() throws ExecutionException {
        // create mockRegistry to check if it get Calls
        DeviceRegistry mockRegistry = mockDeviceRegistry();

        // invoke our operation under test
        getOperation(DeviceRegistryAas.NAME_OP_IM_ALIVE).invoke(A_VALID_DEVICE);

        // verify that the actual registry is getting called
        verify(mockRegistry).imAlive(eq(A_VALID_DEVICE));
    }

    @Test
    public void op_sendTelemetry_isCallable() throws ExecutionException {
        Operation operation = getOperation(DeviceRegistryAas.NAME_OP_SEND_TELEMETRY);

        Assert.assertNotNull(operation);

        Object invoke = operation.invoke(A_VALID_DEVICE, SOME_TELEMETRY_DATA);
        Assert.assertNotNull(invoke);
    }

    @Test
    public void op_sendTelemetry_deliversTelemetryToRegistry() throws ExecutionException {
        DeviceRegistry mockRegistry = mockDeviceRegistry();

        getOperation(DeviceRegistryAas.NAME_OP_SEND_TELEMETRY).invoke(A_VALID_DEVICE, SOME_TELEMETRY_DATA);

        verify(mockRegistry).sendTelemetry(eq(A_VALID_DEVICE), eq(SOME_TELEMETRY_DATA));
    }

    @Test
    public void op_removeDevice_withInvalidDeviceIdentifier_doesNotremoveDevice() throws ExecutionException, IOException {
        DeviceRegistryAasClient client = new DeviceRegistryAasClient();
        int beforeCount = client.getDevices().size();

        client.removeDevice(AN_INVALID_DEVICE);
        client = new DeviceRegistryAasClient();
        int afterCount = client.getDevices().size();

        Assert.assertEquals(beforeCount, afterCount);
    }


    public static void mockDeviceResource(String aDeviceId) throws IOException {
        ActiveAasBase.getSubmodel(AasPartRegistry.NAME_SUBMODEL_RESOURCES)
                .createSubmodelElementCollectionBuilder(aDeviceId, false, false)
                .build();
    }

}
