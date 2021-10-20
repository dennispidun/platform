package de.iip_ecosphere.platform.deviceMgt;

import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistry;
import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryClient;
import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryClientFactory;
import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryFactory;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasPrintVisitor;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static de.iip_ecosphere.platform.support.iip_aas.AasUtils.fixId;

public class PerformanceTests {

    private static Aas aas;
    private static Server implServer;
    private static Server aasServer;

    /**
     * Initializes the test.
     */
    @BeforeClass
    public static void startup() throws IOException {
        AasPartRegistry.AasBuildResult res = AasPartRegistry.build(); //c -> c instanceof DeviceManagementAas
        AasPartRegistry.setAasSetup(AasPartRegistry.AasSetup.createLocalEphemeralSetup());
        implServer = res.getProtocolServerBuilder().build();
        implServer.start();
        aasServer = AasPartRegistry.deploy(res.getAas());
        aasServer.start();
        aas = AasPartRegistry.retrieveIipAas();

        ActiveAasBase.setNotificationMode(ActiveAasBase.NotificationMode.SYNCHRONOUS);
    }

    @Test
    public void addDevice_single() throws IOException {
        DeviceRegistryClient deviceRegistryClient = DeviceRegistryClientFactory.createDeviceRegistryClient();

        List<String> ids = generateIds(10);
        createDevices(ids);
        System.out.println("time = " + add(ids,deviceRegistryClient));

        for (int i = 0; i < 3; i++) {
            ids = generateIds(1);
            createDevices(ids);
            System.out.println("time = " + add(ids,deviceRegistryClient));
        }
    }

    @Test
    public void addDevice_mass() throws IOException {
        DeviceRegistryClient deviceRegistryClient = DeviceRegistryClientFactory.createDeviceRegistryClient();

        List<String> ids = generateIds(500);
        createDevices(ids);
        System.out.println("time = " + add(ids,deviceRegistryClient));
    }

    @Test
    public void removeDevice_single() throws IOException {
        DeviceRegistryClient deviceRegistryClient = DeviceRegistryClientFactory.createDeviceRegistryClient();

        List<String> ids = generateIds(10);
        createDevices(ids);
        add(ids,deviceRegistryClient);
        System.out.println("time = " + remove(ids,deviceRegistryClient));

        for (int i = 0; i < 3; i++) {
            ids = generateIds(1);
            createDevices(ids);
            add(ids,deviceRegistryClient);
            System.out.println("time = " + remove(ids,deviceRegistryClient));
        }
    }

    @Test
    public void removeDevice_mass() throws IOException, InterruptedException {
        DeviceRegistryClient deviceRegistryClient = DeviceRegistryClientFactory.createDeviceRegistryClient();

        List<String> ids = generateIds(100);
        createDevices(ids);
        add(ids,deviceRegistryClient);
        Thread.sleep(200);
        System.out.println("time = " + remove(ids,deviceRegistryClient));
    }

    private long add(List<String> ids, DeviceRegistryClient deviceRegistryClient){
        long start = System.currentTimeMillis();
        ids.forEach(id -> {
            try {
                deviceRegistryClient.addDevice(id, "AN_IP");
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
        long end = System.currentTimeMillis();
        return end - start;
    }

    private long remove(List<String> ids, DeviceRegistryClient deviceRegistryClient){
        long start = System.currentTimeMillis();
        ids.forEach(id -> {
            try {
                deviceRegistryClient.removeDevice(id);
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
        long end = System.currentTimeMillis();
        return end - start;
    }

    private List<String> generateIds(int count) {
        List<String> ids = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            ids.add("device"+i);
        }
        return ids;
    }

    private void createDevices(List<String> ids) throws IOException {
        ids.forEach(id -> {
            try {
                AasPartRegistry
                        .retrieveIipAas()
                        .getSubmodel(AasPartRegistry.NAME_SUBMODEL_RESOURCES)
                        .createSubmodelElementCollectionBuilder(fixId(id), false, false)
                        .build();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        AasPartRegistry.retrieveIipAas().accept(new AasPrintVisitor());
    }
}
