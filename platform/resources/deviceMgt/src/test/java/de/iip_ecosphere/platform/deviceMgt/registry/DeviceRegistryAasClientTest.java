package de.iip_ecosphere.platform.deviceMgt.registry;

import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.AasPrintVisitor;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static de.iip_ecosphere.platform.deviceMgt.registry.StubDeviceRegistryFactoryDescriptor.mockDeviceRegistry;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

public class DeviceRegistryAasClientTest {

    private final static Class contributorClass = DeviceRegistryAas.class;
    public static final String A_DEVICE_ID = "A_DEVICE_ID";
    public static final String AN_INVALID_DEVICE_ID = "AN_INVALID_DEVICE";
    public static final String SOME_TELEMETRY = "{\"testField\": 123}";
    public static final String AN_IP = "1.1.1.1";

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

    @After
    public void tearDown() throws Exception {
        // reset Mocks, as Implementation build DeviceRegistry only once
        // otherwise old invocations-counter won't reset
        Mockito.reset(mockDeviceRegistry());
    }

    @Test
    public void getDevices_withNoDevices_shouldReturnEmptyCollection() {
        Assert.assertEquals(0, client.getDevices().size());
    }

    @Test
    public void getDevices_withOneDevice_shouldReturnCollectionWithTheOneDevice() throws ExecutionException, IOException {
        DeviceRegistryAasTest.mockDeviceResource(A_DEVICE_ID);
        client.addDevice(A_DEVICE_ID, AN_IP);

        AasPartRegistry.retrieveIipAas().accept(new AasPrintVisitor());

        // update client
        client = new DeviceRegistryAasClient();

        Assert.assertEquals(1, client.getDevices().size());
        Assert.assertNotNull(client.getDevice(A_DEVICE_ID));
    }

    @Test
    public void getDevice_withInvalidDevice_shouldReturnNull() {
        Assert.assertNull(client.getDevice(AN_INVALID_DEVICE_ID));
    }

    @Test
    public void getDevice_withValidDevice_shouldNotReturnNull() throws ExecutionException, IOException {
        DeviceRegistryAasTest.mockDeviceResource(A_DEVICE_ID);
        client.addDevice(A_DEVICE_ID, AN_IP);

        client = new DeviceRegistryAasClient();
        SubmodelElementCollection device = client.getDevice(A_DEVICE_ID);
        Assert.assertNotNull(device);
        Assert.assertNotNull(device.getProperty(DeviceRegistryAas.NAME_PROP_MANAGED_DEVICE_ID).getValue());
    }

    @Test
    public void addDevice_withDevice_shouldAddDevice() throws ExecutionException, IOException {
        DeviceRegistryAasTest.mockDeviceResource(A_DEVICE_ID);
        client.addDevice(A_DEVICE_ID, AN_IP);
        client = new DeviceRegistryAasClient();
        Assert.assertNotNull(client.getDevice(A_DEVICE_ID));

    }

    @Test
    public void sendTelemetry_withDeviceAndTelementry_shouldSendTelemetryToRegistry() throws ExecutionException {
        DeviceRegistry mockRegistry = mockDeviceRegistry();
        client.sendTelemetry(A_DEVICE_ID, SOME_TELEMETRY);
        verify(mockRegistry).sendTelemetry(eq(A_DEVICE_ID), eq(SOME_TELEMETRY));
    }

    @Test
    public void imAlive_withDevice_shouldCallImAliveFromRegistry() throws ExecutionException {
        DeviceRegistry mockRegistry = mockDeviceRegistry();
        client.imAlive(A_DEVICE_ID);
        verify(mockRegistry).imAlive(eq(A_DEVICE_ID));
    }
}