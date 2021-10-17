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

import java.util.concurrent.ExecutionException;

/**
 * A service provider implementation for {@link DeviceFirmwareOperations} which
 * uses aas as the communication protocol. For this purpose it will notify the
 * DeviceManagementAas that a device needs an update.
 *
 * @author Dennis Pidun, University of Hildesheim
 */
public class AasDeviceFirmwareOperations implements DeviceFirmwareOperations {
    @Override
    public void updateRuntime(String id) throws ExecutionException {
        DeviceManagementAas.notifyUpdateRuntime(id);
    }
}
