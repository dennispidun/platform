package de.iip_ecosphere.platform.deviceMgt;

import de.iip_ecosphere.platform.deviceMgt.ssh.SshRemoteManagementOperations;
import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class DeviceManagementFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceManagementFactory.class.getName());


    private static DeviceManagement management;
    private static DeviceRemoteManagementOperations remoteManagementOperations;
    private static DeviceFirmwareOperations firmwareOperations;
    private static DeviceResourceConfigOperations resourceConfigOperations;

    public static DeviceManagement getDeviceManagement() {
        if (null == management) {
            if (null == firmwareOperations) {
                Optional<DeviceFirmwareOperations> first = ServiceLoaderUtils
                        .findFirst(DeviceFirmwareOperations.class);
                if (first.isPresent()) {
                    firmwareOperations = first.get();
                } else {
                    LOGGER.error("No DeviceFirmwareOperations implementation available.");
                }
            }

            if (null == resourceConfigOperations) {
                Optional<DeviceResourceConfigOperations> first = ServiceLoaderUtils
                        .findFirst(DeviceResourceConfigOperations.class);
                if (first.isPresent()) {
                    resourceConfigOperations = first.get();
                } else {
                    LOGGER.error("No DeviceResourceConfigOperations implementation available.");
                }
            }

            if (null == remoteManagementOperations) {
                Optional<DeviceRemoteManagementOperations> first = ServiceLoaderUtils
                        .findFirst(DeviceRemoteManagementOperations.class);
                if (first.isPresent()) {
                    remoteManagementOperations = first.get();
                } else {
                    LOGGER.error("No DeviceRemoteManagementOperations implementation available, " +
                            "fall back to default implementation: ApacheSSHD.");
                    remoteManagementOperations = SshRemoteManagementOperations.getInstance();
                }
            }
            management = new DeviceManagementImpl(firmwareOperations,
                    remoteManagementOperations, resourceConfigOperations);
        }

        return management;
    }
}
