package de.iip_ecosphere.platform.deviceMgt.thingsboard;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.thingsboard.rest.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PerformanceTests {

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
    public void testAddSpeed() {
        long time = add(generateRandomIds(1));
        System.out.printf("Single Add finished in: %dms%n", time);
        time = add(generateRandomIds(1));
        System.out.printf("Single Add finished in: %dms%n", time);
        time = add(generateRandomIds(1));
        System.out.printf("Single Add finished in: %dms%n", time);

        int count = 500;
        time = add(generateRandomIds(count));
        System.out.printf("Mass (%dx) Add finished in: %dms", count , time);
    }

    @Test
    public void testRemoveSpeed() {
        for (int i = 0; i < 5; i++) {
            List<String> ids = generateRandomIds(1);
            add(ids);
            long time = remove(ids);
            System.out.printf("Single Remove finished in: %dms%n", time);
        }


        List<String> ids = generateRandomIds(500);
        add(ids);
        long time = remove(ids);
        System.out.printf("Mass (%dx) Remove finished in: %dms", 500 , time);
    }

    private long remove(List<String> ids) {
        long start = System.currentTimeMillis();
        ids.forEach(id -> {
            registry.removeDevice(id);
        });
        long end = System.currentTimeMillis();
        long time = end - start;
        return time;
    }

    private long add(List<String> ids) {
        long start = System.currentTimeMillis();
        ids.forEach(id -> registry.addDevice(id, AN_IP));
        long end = System.currentTimeMillis();
        return end - start;
    }


    private List<String> generateRandomIds(int count) {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ids.add(UUID.randomUUID().toString());
        }
        return ids;
    }
}
