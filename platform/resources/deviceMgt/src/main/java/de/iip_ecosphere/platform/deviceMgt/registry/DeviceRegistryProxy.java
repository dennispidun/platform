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

package de.iip_ecosphere.platform.deviceMgt.registry;

import de.iip_ecosphere.platform.deviceMgt.DeviceDescriptor;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutionException;

class DeviceRegistryProxy extends AbstractDeviceRegistry {

    private DeviceRegistry sink;

    public DeviceRegistryProxy(DeviceRegistry sink) {
        this.sink = sink;
    }

    @Override
    public void addDevice(String id, String ip) throws ExecutionException {
        super.addDevice(id, ip);
        sink.addDevice(id, ip);
    }

    @Override
    public void removeDevice(String id) throws ExecutionException {
        super.removeDevice(id);
        sink.removeDevice(id);
    }

    @Override
    public void imAlive(String id) throws ExecutionException {
        sink.imAlive(id);
    }

    @Override
    public void sendTelemetry(String id, String telemetryData) throws ExecutionException {
        sink.sendTelemetry(id, telemetryData);
    }

    @Override
    public Set<String> getIds() {
        return sink.getIds();
    }

    @Override
    public Set<String> getManagedIds() {
        return sink.getManagedIds();
    }

    @Override
    public Collection<? extends DeviceDescriptor> getDevices() {
        return sink.getDevices();
    }

    @Override
    public DeviceDescriptor getDevice(String id) {
        return sink.getDevice(id);
    }

    @Override
    public DeviceDescriptor getDeviceByManagedId(String id) {
        return sink.getDeviceByManagedId(id);
    }
}
