package de.iip_ecosphere.platform.deviceMgt;

import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class DeviceRegistryFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceRegistryFactory.class.getName());

    private static DeviceRegistryFactoryDescriptor desc;
    private static DeviceRegistryProxy proxy;

    public static DeviceRegistry getDeviceRegistry() {
        if (null == desc) {
            Optional<DeviceRegistryFactoryDescriptor> first = ServiceLoaderUtils
                    .findFirst(DeviceRegistryFactoryDescriptor.class);
            if (first.isPresent()) {
                desc = first.get();
            } else {
                LOGGER.error("No Device Registry implementation available.");
            }
        }

        if (null == proxy) {
            if (null != desc) {
                proxy = new DeviceRegistryProxy(desc.createDeviceRegistryInstance());
            }
        }

        return proxy;
    }
}
