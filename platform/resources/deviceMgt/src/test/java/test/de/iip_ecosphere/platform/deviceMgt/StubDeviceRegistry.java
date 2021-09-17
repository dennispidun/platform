package test.de.iip_ecosphere.platform.deviceMgt;

import de.iip_ecosphere.platform.deviceMgt.AbstractDeviceRegistry;
import de.iip_ecosphere.platform.deviceMgt.DeviceDescriptor;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class StubDeviceRegistry extends AbstractDeviceRegistry {

    public static final String A_VALID_ID = "TEST_ID";

    @Override
    public Set<String> getIds() {
        return Collections.singleton(A_VALID_ID);
    }

    @Override
    public Collection<? extends DeviceDescriptor> getDevices() {
        return Collections.singleton(() -> A_VALID_ID);
    }

    @Override
    public DeviceDescriptor getDevice(String id) {
        return () -> A_VALID_ID;
    }

    @Override
    public void addDevice(String id) {
        System.out.println("add: id = " + id);
    }

    @Override
    public void removeDevice(String id) {
        System.out.println("remove: id = " + id);
    }
}
