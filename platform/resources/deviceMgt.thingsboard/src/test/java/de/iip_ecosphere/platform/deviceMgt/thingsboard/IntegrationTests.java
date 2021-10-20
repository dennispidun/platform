package de.iip_ecosphere.platform.deviceMgt.thingsboard;

import de.iip_ecosphere.platform.deviceMgt.DeviceDescriptor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.thingsboard.rest.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class IntegrationTests {

    public static final String A_DEVICE = "A_DEVICE";
    public static final String AN_IP = "AN_IP";

    ThingsBoardDeviceRegistry registry;
    RestClient restClient;

    @Before
    public void setUp() {
        ThingsBoardDeviceRegistryFactoryDescriptor factory =
                new ThingsBoardDeviceRegistryFactoryDescriptor();
        registry = (ThingsBoardDeviceRegistry) factory.createDeviceRegistryInstance();
        restClient = new RestClient(ThingsBoardDeviceRegistryFactoryDescriptor.BASE_URL);
        restClient.login(ThingsBoardDeviceRegistryFactoryDescriptor.USERNAME,
                ThingsBoardDeviceRegistryFactoryDescriptor.PASSWORD);
    }

    @After
    public void tearDown() {
        registry.getIds().forEach(d -> {
            registry.removeDevice(d);
        });
    }

    @Test
    public void restClientIsAbleToConnect() {
        Assert.assertNotNull(restClient);
    }

    @Test
    public void createDeviceRegistryInstance_createsInstance() {
        Assert.assertNotNull(registry);
        Assert.assertNotNull(registry.getRestClient());

    }

    @Test
    public void addDevice_withDevice_shouldAddDevice() {
        registry.addDevice(A_DEVICE, AN_IP);
        Assert.assertTrue(this.restClient.getTenantDevice(A_DEVICE).isPresent());
    }

    @Test
    public void addDevice_withOutDevice_shouldNotAddDevice() {
        int sizeBefore = registry.getIds().size();
        registry.addDevice("", AN_IP);
        registry.addDevice(null, AN_IP);
        registry.addDevice(A_DEVICE, "");
        registry.addDevice(A_DEVICE, null);
        Assert.assertEquals(sizeBefore, registry.getIds().size());
        Assert.assertFalse(this.restClient.getTenantDevice(A_DEVICE).isPresent());
    }

    @Test
    public void addMultipleDevices_shouldAddAllDevices() {
        List<String> ids = generateRandomIds();
        ids.forEach(id -> {
            registry.addDevice(id, AN_IP);
        });

        ids.forEach(id -> {
            Assert.assertTrue(this.restClient.getTenantDevice(id).isPresent());
        });
    }

    @Test
    public void removeDevice_removesDevice() {
        registry.addDevice(A_DEVICE, AN_IP);
        Assert.assertTrue(this.restClient.getTenantDevice(A_DEVICE).isPresent());
        registry.removeDevice(A_DEVICE);
        Assert.assertFalse(this.restClient.getTenantDevice(A_DEVICE).isPresent());
    }

    @Test
    public void addDevice_withDeviceAndIp_hasDeviceIdInternalIdAndIp() {
        registry.addDevice(A_DEVICE, AN_IP);
        DeviceDescriptor device = registry.getDevice(A_DEVICE);

        Assert.assertEquals(A_DEVICE, device.getId());
        Assert.assertEquals(AN_IP, device.getIp());
        Assert.assertNotNull(device.getManagedId());
        Assert.assertNotEquals("", device.getManagedId());
    }

    @Test
    public void getState_withDeviceAddedButNoAlive_shouldBeSTARTING() {
        registry.addDevice(A_DEVICE, AN_IP);
        DeviceDescriptor device = registry.getDevice(A_DEVICE);

        Assert.assertEquals(DeviceDescriptor.State.STARTING, device.getState());
    }

    @Test
    public void getState_withDeviceAddedAndAlive_shouldBeAVAILABLE() throws ExecutionException {
        registry.addDevice(A_DEVICE, AN_IP);
        registry.imAlive(A_DEVICE);
        DeviceDescriptor device = registry.getDevice(A_DEVICE);

        Assert.assertEquals(DeviceDescriptor.State.AVAILABLE, device.getState());
    }

    @Test
    public void getState_withTimedOutDevice_shouldBeUNDEFINED() throws ExecutionException, InterruptedException {
        int timeOutBefore = ThingsBoardDeviceDescriptor.DEVICE_TIMEOUT;
        registry.addDevice(A_DEVICE, AN_IP);
        registry.imAlive(A_DEVICE);
        ThingsBoardDeviceDescriptor.DEVICE_TIMEOUT = 250;
        DeviceDescriptor device = registry.getDevice(A_DEVICE);
        Thread.sleep(300);
        Assert.assertEquals(DeviceDescriptor.State.UNDEFINED, device.getState());
        ThingsBoardDeviceDescriptor.DEVICE_TIMEOUT = timeOutBefore;
    }

    @Test
    public void getRuntime_withValidDevice_returnsNull() {
        // Usually a device' runtimeName/runtimeVersion is managed by the platform
        registry.addDevice(A_DEVICE, AN_IP);
        DeviceDescriptor device = registry.getDevice(A_DEVICE);

        Assert.assertNull(device.getRuntimeName());
        Assert.assertNull(device.getRuntimeVersion());
    }

    private List<String> generateRandomIds() {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ids.add(UUID.randomUUID().toString());
        }
        return ids;
    }
}