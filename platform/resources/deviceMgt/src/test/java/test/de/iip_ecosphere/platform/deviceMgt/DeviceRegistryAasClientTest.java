package test.de.iip_ecosphere.platform.deviceMgt;

import de.iip_ecosphere.platform.deviceMgt.DeviceRegistryAas;
import de.iip_ecosphere.platform.deviceMgt.DeviceRegistryAasClient;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

public class DeviceRegistryAasClientTest {

    private final static Class contributorClass = DeviceRegistryAas.class;
    public static final String A_DEVICE_ID = "A_DEVICE_ID";
    public static final String AN_INVALID_DEVICE_ID = "AN_INVALID_DEVICE";

    private Server implServer;
    private Server aasServer;
    private DeviceRegistryAasClient client;

    @Before
    public void setUp() throws Exception {
        ActiveAasBase.setNotificationMode(ActiveAasBase.NotificationMode.SYNCHRONOUS);

        AasPartRegistry.setAasSetup(AasPartRegistry.AasSetup.createLocalEphemeralSetup());
        AasPartRegistry.AasBuildResult res = AasPartRegistry.build(contributorClass::isInstance);

        implServer = res.getProtocolServerBuilder().build();
        implServer.start();
        aasServer = AasPartRegistry.deploy(res.getAas());
        aasServer.start();

        client = new DeviceRegistryAasClient();
    }

    @Test
    public void getDevices_withNoDevices_shouldReturnEmptyCollection() {
        Assert.assertEquals(0, client.getDevices().getElementsCount());
    }

    @Test
    public void getDevices_withOneDevice_shouldReturnCollectionWithTheOneDevice() throws ExecutionException {
        client.addDevice(A_DEVICE_ID);

        Assert.assertEquals(1, client.getDevices().getElementsCount());
        Assert.assertNotNull(client.getDevice(A_DEVICE_ID));
    }

    @Test
    public void getDevice_withInvalidDevice_shouldReturnNull() {
        Assert.assertNull(client.getDevice(AN_INVALID_DEVICE_ID));
    }

    @Test
    public void getDevice_withValidDevice_shouldNotReturnNull() throws ExecutionException {
        client.addDevice(A_DEVICE_ID);

        SubmodelElementCollection device = client.getDevice(A_DEVICE_ID);
        Assert.assertNotNull(device);
        Assert.assertEquals(A_DEVICE_ID, device.getProperty(DeviceRegistryAas.NAME_PROP_DEVICE_RESOURCE_ID).getValue());
    }

    @Test
    public void addDevice_withDevice_shouldAddDevice() throws ExecutionException {
        client.addDevice(A_DEVICE_ID);

        Assert.assertNotNull(client.getDevice(A_DEVICE_ID));

    }
}