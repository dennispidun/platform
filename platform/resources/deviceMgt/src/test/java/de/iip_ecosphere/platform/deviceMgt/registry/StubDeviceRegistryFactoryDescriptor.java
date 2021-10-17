package de.iip_ecosphere.platform.deviceMgt.registry;

import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistry;
import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryFactoryDescriptor;

import static org.mockito.Mockito.mock;

public class StubDeviceRegistryFactoryDescriptor implements DeviceRegistryFactoryDescriptor {

    private static DeviceRegistry stub;

    public static DeviceRegistry mockDeviceRegistry() {
        if (stub == null) {
            stub = mock(DeviceRegistry.class);
        }
        return stub;
    }

    @Override
    public DeviceRegistry createDeviceRegistryInstance() {
        return mockDeviceRegistry();
    }

}
