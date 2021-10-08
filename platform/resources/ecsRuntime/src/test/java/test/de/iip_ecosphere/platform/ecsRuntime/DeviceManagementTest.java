package test.de.iip_ecosphere.platform.ecsRuntime;

import de.iip_ecosphere.platform.deviceMgt.DeviceDescriptor;
import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistry;
import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryAas;
import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryFactoryDescriptor;
import de.iip_ecosphere.platform.ecsRuntime.DeviceManagement;
import de.iip_ecosphere.platform.ecsRuntime.EcsAas;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasPrintVisitor;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.Id;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class DeviceManagementTest {

    private static Aas aas;
    private static Server implServer;
    private static Server aasServer;

    /**
     * Initializes the test.
     */
    @BeforeClass
    public static void startup() throws IOException {
        AasPartRegistry.AasBuildResult res = AasPartRegistry
                .build(c -> c instanceof DeviceRegistryAas
                        || c instanceof EcsAas);
        AasPartRegistry.setAasSetup(AasPartRegistry.AasSetup.createLocalEphemeralSetup());
        implServer = res.getProtocolServerBuilder().build();
        implServer.start();
        aasServer = AasPartRegistry.deploy(res.getAas());
        aasServer.start();
        aas = AasPartRegistry.retrieveIipAas();
        aas.accept(new AasPrintVisitor());
    }

    /**
     * Shuts down the test.
     */
    @AfterClass
    public static void shutdown() {
        implServer.stop(false);
        aasServer.stop(false);
    }


    @Test
    public void getDeviceRegistryClient_returnsActualRegistryClient() throws IOException {
        Assert.assertNotNull(DeviceManagement.getRegistryClient());
    }

    @Test
    public void initializeDevice_registersAtRegistry() throws IOException {
        DeviceManagement.initializeDevice();

        SubmodelElementCollection device = DeviceManagement.getRegistryClient()
                .getDevice(Id.getDeviceIdAas());
        Assert.assertNotNull(device);
    }

    public static class StubDeviceRegistryFactoryDescriptor implements DeviceRegistryFactoryDescriptor {

        public StubDeviceRegistryFactoryDescriptor() {
        }

        @Override
        public DeviceRegistry createDeviceRegistryInstance() {
            return new DeviceRegistry() {
                @Override
                public Set<String> getIds() {
                    return null;
                }

                @Override
                public Set<String> getManagedIds() {
                    return null;
                }

                @Override
                public Collection<? extends DeviceDescriptor> getDevices() {
                    return null;
                }

                @Override
                public DeviceDescriptor getDevice(String id) {
                    return null;
                }

                @Override
                public DeviceDescriptor getDeviceByManagedId(String id) {
                    return null;
                }

                @Override
                public void addDevice(String id) throws ExecutionException {

                }

                @Override
                public void removeDevice(String id) throws ExecutionException {

                }

                @Override
                public void imAlive(String id) throws ExecutionException {

                }

                @Override
                public void sendTelemetry(String id, String telemetryData) throws ExecutionException {

                }
            };
        }
    }
}