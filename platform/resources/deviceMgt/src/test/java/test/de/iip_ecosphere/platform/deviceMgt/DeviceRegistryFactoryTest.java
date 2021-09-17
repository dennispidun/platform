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

package test.de.iip_ecosphere.platform.deviceMgt;

import de.iip_ecosphere.platform.deviceMgt.DeviceRegistry;
import de.iip_ecosphere.platform.deviceMgt.DeviceRegistryFactory;
import de.iip_ecosphere.platform.deviceMgt.DeviceRegistryProxy;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link DeviceRegistryFactory}.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public class DeviceRegistryFactoryTest {

    @Test
    public void getDeviceRegistry_withServiceLoaderConfiguration_returnsStubDeviceRegistry() {
        DeviceRegistry deviceRegistry = DeviceRegistryFactory.getDeviceRegistry();
        Assert.assertTrue(deviceRegistry instanceof DeviceRegistryProxy);
    }
}
