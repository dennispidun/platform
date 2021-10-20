package de.iip_ecosphere.platform.deviceMgt.thingsboard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.iip_ecosphere.platform.deviceMgt.DeviceDescriptor;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.thingsboard.rest.client.RestClient;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.device.DeviceSearchQuery;
import org.thingsboard.server.common.data.id.DeviceId;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ThingsBoardDeviceRegistryTest {

    private static final String A_DEVICE = "A_DEVICE";
    public static final String AN_IP = "AN_IP";
    public static final String ANOTHER_IP = "ANOTHER_IP";
    public static final String ANOTHER_DEVICE = "ANOTHER_DEVICE";
    private ThingsBoardDeviceRegistry deviceRegistry;
    private RestClient thingsBoardMock;

    @Before
    public void setUp() {
        thingsBoardMock = mock(RestClient.class);
        deviceRegistry = new ThingsBoardDeviceRegistry(thingsBoardMock);
    }

    @Test
    public void getIds_withTwoDevices_shouldReturnListWithTwoIds() {
        List<Device> devices = new ArrayList<>();
        Device d1 = new Device(new DeviceId(UUID.randomUUID()));
        d1.setName(A_DEVICE);
        Device d2 = new Device(new DeviceId(UUID.randomUUID()));
        d2.setName(ANOTHER_DEVICE);
        devices.add(d1);
        devices.add(d2);


        when(thingsBoardMock.findByQuery(any(DeviceSearchQuery.class))).thenReturn(devices);

        Set<String> ids = deviceRegistry.getIds();

        Assert.assertNotNull(ids);
        Assert.assertTrue(ids.contains(A_DEVICE));
        Assert.assertTrue(ids.contains(ANOTHER_DEVICE));
        Assert.assertEquals(2, ids.size());
    }

    @Test
    public void getManagedIds_withTwoDevices_shouldReturnListWithTwoIds() {
        List<Device> devices = new ArrayList<>();
        Device d1 = new Device(new DeviceId(UUID.randomUUID()));
        d1.setName(A_DEVICE);
        Device d2 = new Device(new DeviceId(UUID.randomUUID()));
        d2.setName(ANOTHER_DEVICE);
        devices.add(d1);
        devices.add(d2);
        when(thingsBoardMock.findByQuery(any(DeviceSearchQuery.class))).thenReturn(devices);

        Set<String> ids = deviceRegistry.getManagedIds();

        Assert.assertNotNull(ids);
        Assert.assertTrue(ids.contains(d1.getId().toString()));
        Assert.assertTrue(ids.contains(d2.getId().toString()));
        Assert.assertEquals(2, ids.size());
    }

    @Test
    public void getDivices_withTwoDevices_shouldReturnListWithTwoIds() {
        List<Device> devices = new ArrayList<>();
        Device d1 = new Device(new DeviceId(UUID.randomUUID()));
        d1.setName(A_DEVICE);
        Device d2 = new Device(new DeviceId(UUID.randomUUID()));
        d2.setName(ANOTHER_DEVICE);
        devices.add(d1);
        devices.add(d2);
        when(thingsBoardMock.findByQuery(any(DeviceSearchQuery.class))).thenReturn(devices);


        Collection<? extends DeviceDescriptor> actualDevices = deviceRegistry.getDevices();
        Assert.assertEquals(2, actualDevices.size());
    }

    @Test
    public void getDevice_withValidId_returnsDevice() {
        Device device = mockDevice();

        DeviceDescriptor desc = deviceRegistry.getDevice(A_DEVICE);
        Assert.assertEquals(A_DEVICE, desc.getId());
        Assert.assertEquals(A_DEVICE, desc.getResourceId());
        Assert.assertEquals(device.getId().toString(), desc.getManagedId());
    }

    @Test
    public void getDevice_withInvalidId_returnsNull() {
        when(thingsBoardMock.getTenantDevice(eq(A_DEVICE))).thenReturn(Optional.empty());

        DeviceDescriptor desc = deviceRegistry.getDevice(A_DEVICE);
        Assert.assertNull(desc);
    }

    @Test
    public void getDeviceByManagedId_withValidId_returnsDevice() {
        Device device = mockDevice();

        String id = device.getId().toString();
        DeviceDescriptor desc = deviceRegistry.getDeviceByManagedId(id);
        Assert.assertNotNull(desc);
        Assert.assertEquals(id, desc.getManagedId());
        Assert.assertEquals(device.getName(), desc.getId());
        Assert.assertEquals(device.getName(), desc.getResourceId());
    }

    @Test
    public void getDeviceByManagedId_withInvalidId_returnsNull() {
        String uuid = UUID.randomUUID().toString();
        when(thingsBoardMock.getTenantDevice(eq(uuid))).thenReturn(Optional.empty());

        DeviceDescriptor desc = deviceRegistry.getDeviceByManagedId(uuid);
        Assert.assertNull(desc);
    }

    @Test
    public void addDevice_withValidDevice_addsADevice() throws JsonProcessingException {
        DeviceId id = new DeviceId(UUID.randomUUID());
        Device device = new Device();
        device.setName(A_DEVICE);
        when(thingsBoardMock.saveDevice(eq(device))).thenAnswer((a) -> {
            Device d = a.getArgument(0);
            d.setId(id);
            return d;
        });

        deviceRegistry.addDevice(A_DEVICE, AN_IP);

        JsonNode attribute = getJsonNode("{\"ip\": \"" + AN_IP + "\"}");
        verify(thingsBoardMock).saveDeviceAttributes(eq(id), any(), eq(attribute));
        verify(thingsBoardMock, times(1)).saveDevice(any());
    }

    @Test
    public void addDevice_withNoIp_wontAddDevice() {
        deviceRegistry.addDevice(A_DEVICE, null);
        deviceRegistry.addDevice(A_DEVICE, "");

        verify(thingsBoardMock, never()).saveDeviceAttributes(any(), any(), any());
        verify(thingsBoardMock, never()).saveDevice(any());
    }

    @Test
    public void addDevice_withNoDeviceIdentifier_wontAddDevice() {
        deviceRegistry.addDevice(null, AN_IP);
        deviceRegistry.addDevice("", AN_IP);

        verify(thingsBoardMock, never()).saveDeviceAttributes(any(), any(), any());
        verify(thingsBoardMock, never()).saveDevice(any());
    }

    @Test
    public void addDevice_withAlreadyRegisteredDevice_updatesDevice() {
        mockDevice();
        deviceRegistry.addDevice(A_DEVICE, ANOTHER_IP);

        ArgumentCaptor<JsonNode> captor = ArgumentCaptor.forClass(JsonNode.class);
        verify(thingsBoardMock, times(1)).saveDeviceAttributes(any(), any(), captor.capture());
        verify(thingsBoardMock, times(0)).saveDevice(any());

        Assert.assertEquals(ANOTHER_IP, captor.getValue().findValue("ip").asText());
    }

    @Test
    public void removeDevice_withValidDevice_removesDevice() {
        Device device = mockDevice();

        deviceRegistry.removeDevice(A_DEVICE);

        verify(thingsBoardMock, times(1)).deleteDevice(eq(device.getId()));
    }

    @Test
    public void removeDevice_withInvalidDevice_wontRemoveDevice() {
        deviceRegistry.removeDevice(A_DEVICE);

        verify(thingsBoardMock, times(0)).deleteDevice(any());
    }

    @Test
    public void removeDevice_withNoDevice_wontRemoveDevice() {
        deviceRegistry.removeDevice("");
        deviceRegistry.removeDevice(null);

        verify(thingsBoardMock, times(0)).deleteDevice(any());
    }

    @Test
    public void sendTelemetry_withValidData_shouldSaveTelemetry() throws ExecutionException, JsonProcessingException {
        Device device = mockDevice();
        deviceRegistry.sendTelemetry(A_DEVICE, "{\"telemetryKey\": \"telemetryValue\"}");

        verify(thingsBoardMock).saveEntityTelemetry(eq(device.getId()), any(), eq(getJsonNode("{\"telemetryKey\": \"telemetryValue\"}")));
    }

    @Test(expected = ExecutionException.class)
    public void sendTelemetry_withInvalidData_shouldThrowException() throws ExecutionException {
        deviceRegistry.sendTelemetry(A_DEVICE, "{someNonsense: \"telemetryValue\"}");

        verify(thingsBoardMock, never()).saveEntityTelemetry(any(), any(), any());
    }

    @Test
    public void imAlive_sendImAliveAsTelemetry() throws JsonProcessingException, ExecutionException {
        Device device = mockDevice();
        deviceRegistry.imAlive(A_DEVICE);

        verify(thingsBoardMock).saveEntityTelemetry(eq(device.getId()), any(), eq(getJsonNode("{\"active\": true}")));
    }

    @Test
    public void imAlive_withUnknownDevice_shouldNotSendImAliveAsTelemetry() throws JsonProcessingException, ExecutionException {
        deviceRegistry.imAlive(A_DEVICE);

        verify(thingsBoardMock, never()).saveEntityTelemetry(any(), any(), any());
    }

    @NotNull
    private Device mockDevice() {
        UUID deviceId = UUID.randomUUID();
        Device device = new Device(new DeviceId(deviceId));
        device.setName(A_DEVICE);
        Optional<Device> deviceOptional = Optional.of(device);
        when(thingsBoardMock.getTenantDevice(eq(A_DEVICE))).thenReturn(deviceOptional);
        when(thingsBoardMock.getDeviceById(eq(new DeviceId(deviceId)))).thenReturn(deviceOptional);
        return device;
    }

    private JsonNode getJsonNode(String s) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(s);
    }

}