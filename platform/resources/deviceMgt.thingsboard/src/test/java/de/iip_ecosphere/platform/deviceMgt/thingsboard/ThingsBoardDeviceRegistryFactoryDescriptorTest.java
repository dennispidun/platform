package de.iip_ecosphere.platform.deviceMgt.thingsboard;

import de.iip_ecosphere.platform.deviceMgt.DeviceDescriptor;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

public class ThingsBoardDeviceRegistryFactoryDescriptorTest {

    @Test
    public void createDeviceRegistryInstance_createsInstance() {
        ThingsBoardDeviceRegistryFactoryDescriptor factory =
                new ThingsBoardDeviceRegistryFactoryDescriptor();
        ThingsBoardDeviceRegistry registry =
                (ThingsBoardDeviceRegistry) factory.createDeviceRegistryInstance();
        Assert.assertNotNull(registry);
        Assert.assertNotNull(registry.getRestClient());

    }

    @Test
    public void name() throws ExecutionException, InterruptedException {
        ThingsBoardDeviceRegistryFactoryDescriptor factory =
                new ThingsBoardDeviceRegistryFactoryDescriptor();
        ThingsBoardDeviceRegistry registry =
                (ThingsBoardDeviceRegistry) factory.createDeviceRegistryInstance();

        registry.addDevice("testDevice", "testIp");
        //registry.imAlive("testDevice");
        Thread.sleep(500);
        DeviceDescriptor testDevice = registry.getDevice("testDevice");
        System.out.println(testDevice.getState());
        registry.imAlive("testDevice");
        testDevice = registry.getDevice("testDevice");
        System.out.println(testDevice.getState());

        registry.removeDevice("testDevice");
    }
}