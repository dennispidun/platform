package test.de.iip_ecosphere.platform.deviceMgt;

import de.iip_ecosphere.platform.deviceMgt.DeviceRegistry;
import de.iip_ecosphere.platform.deviceMgt.DeviceRegistryFactoryDescriptor;

public class StubDeviceRegistryFactoryDescriptor implements DeviceRegistryFactoryDescriptor {

    @Override
    public DeviceRegistry createDeviceRegistryInstance() {
        return new StubDeviceRegistry();
    }

}
