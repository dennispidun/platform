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
import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

import static de.iip_ecosphere.platform.deviceMgt.StubDeviceRegistryFactoryDescriptor.mockDeviceRegistry;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link DeviceRegistryFactory}.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public class DeviceRegistryFactoryTest {

    public static final String A_DEVICE_ID = "A_DEVICE_ID";
    public static final String SOME_TELEMETRY = "someTelemetry";

    @After
    public void tearDown() throws Exception {
        // reset Mocks, as Implementation build DeviceRegistry only once
        // otherwise old invocations-counter won't reset
        Mockito.reset(mockDeviceRegistry());
    }

    @Test
    public void getDeviceRegistry_withServiceLoaderConfiguration_returnsDeviceRegistry() {
        DeviceRegistry stubRegistry = StubDeviceRegistryFactoryDescriptor.mockDeviceRegistry();
        when(stubRegistry.getIds()).thenReturn(Collections.singleton(A_DEVICE_ID));

        Assert.assertNotNull(DeviceRegistryFactory.getDeviceRegistry());

        DeviceRegistry dReg = DeviceRegistryFactory.getDeviceRegistry();
        Assert.assertTrue(dReg.getIds().contains(A_DEVICE_ID));
    }

    // ignore VAB-Exception: ProviderException, this test does not focus on AAS
    @Test
    public void allFunctions_withFakeRegistry_callsDownstreamRegistryFunction() throws ExecutionException {
        DeviceRegistry stubRegistry = StubDeviceRegistryFactoryDescriptor.mockDeviceRegistry();
        DeviceRegistry deviceRegistry = DeviceRegistryFactory.getDeviceRegistry();

        deviceRegistry.getDevices();
        verify(stubRegistry).getDevices();

        deviceRegistry.getDevice(A_DEVICE_ID);
        verify(stubRegistry).getDevice(eq(A_DEVICE_ID));

        deviceRegistry.removeDevice(A_DEVICE_ID);
        verify(stubRegistry).removeDevice(eq(A_DEVICE_ID));

        deviceRegistry.addDevice(A_DEVICE_ID);
        verify(stubRegistry).addDevice(eq(A_DEVICE_ID));

        deviceRegistry.imAlive(A_DEVICE_ID);
        verify(stubRegistry).imAlive(eq(A_DEVICE_ID));

        deviceRegistry.sendTelemetry(A_DEVICE_ID, SOME_TELEMETRY);
        verify(stubRegistry).sendTelemetry(eq(A_DEVICE_ID), eq(SOME_TELEMETRY));

        deviceRegistry.getIds();
        verify(stubRegistry).getIds();

    }
}
