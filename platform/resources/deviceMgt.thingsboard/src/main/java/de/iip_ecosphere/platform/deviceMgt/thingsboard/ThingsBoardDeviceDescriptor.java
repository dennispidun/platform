package de.iip_ecosphere.platform.deviceMgt.thingsboard;

import de.iip_ecosphere.platform.deviceMgt.DeviceDescriptor;
import org.thingsboard.rest.client.RestClient;
import org.thingsboard.server.common.data.Device;
import org.thingsboard.server.common.data.id.DeviceId;
import org.thingsboard.server.common.data.kv.AttributeKvEntry;
import org.thingsboard.server.common.data.kv.TsKvEntry;

import java.util.*;

public class ThingsBoardDeviceDescriptor implements DeviceDescriptor {

    public static final int DEVICE_TIMEOUT = 15000;
    private Device tbDevice;
    private RestClient tbClient;

    public ThingsBoardDeviceDescriptor(Device tbDevice, RestClient tbClient) {
        this.tbDevice = tbDevice;
        this.tbClient = tbClient;
    }

    @Override
    public String getId() {
        return tbDevice.getName();
    }

    @Override
    public String getManagedId() {
        return tbDevice.getId().toString();
    }

    @Override
    public String getIp() {
        DeviceId entityId = new DeviceId(UUID.fromString(this.getManagedId()));
        List<String> attributesKeys = this.tbClient.getAttributeKeysByScope(entityId, "device");
        List<AttributeKvEntry> attributeKvEntries = this.tbClient.getAttributeKvEntries(entityId, attributesKeys);
        return attributeKvEntries.stream().filter(key -> key.getKey().equals("ip"))
                .map(kv -> (String) kv.getValue())
                .findFirst().orElse(null);
    }

    @Override
    public String getRuntimeVersion() {
        return null;
    }

    @Override
    public String getRuntimeName() {
        return null;
    }

    @Override
    public String getResourceId() {
        return tbDevice.getName();
    }

    @Override
    public State getState() {
        if (!this.tbClient.getTimeseriesKeys(this.tbDevice.getId()).contains("active")) {
            return State.STARTING;
        }
        List<TsKvEntry> active = this.tbClient.getLatestTimeseries(
                this.tbDevice.getId(), Collections.singletonList("active"));
        TsKvEntry latest = active.stream()
                .max(Comparator.comparingLong(TsKvEntry::getTs))
                .orElse(null);

        if (latest == null) {
            return State.STARTING;
        } else if (System.currentTimeMillis() - latest.getTs() < DEVICE_TIMEOUT) {
            return State.AVAILABLE;
        }

        return State.UNDEFINED;
    }
}
