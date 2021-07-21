/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.ecsRuntime;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.ecsRuntime.ContainerDescriptor;
import de.iip_ecosphere.platform.ecsRuntime.ContainerManager;
import de.iip_ecosphere.platform.ecsRuntime.ContainerState;
import de.iip_ecosphere.platform.ecsRuntime.EcsAas;
import de.iip_ecosphere.platform.ecsRuntime.EcsAasClient;
import de.iip_ecosphere.platform.ecsRuntime.EcsFactory;
import de.iip_ecosphere.platform.services.environment.metricsProvider.metricsAas.MetricsAasConstants;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.LifecycleHandler;
import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.AasPrintVisitor;
import de.iip_ecosphere.platform.support.aas.AasServer;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.ServerRecipe;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.Id;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase.NotificationMode;

/**
 * Tests the {@link EcsAas}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class EcsAasTest {

    /**
     * A predicate testing the value for Double type and whether the value is positive.
     */
    private static final Predicate<Object> POSITIVE_DBL_METRICS_VALUE = o -> { 
        Assert.assertTrue(o instanceof Double); 
        double val = (Double) o; 
        return val > 0; 
    };
    
    /**
     * A predicate testing the value for Integer type and whether the value is positive.
     */
    private static final Predicate<Object> POSITIVE_INT_METRICS_VALUE = o -> { 
        Assert.assertTrue(o instanceof Integer); 
        int val = (Integer) o; 
        return val > 0; 
    };
    
    /**
     * Tests the {@link EcsAas}.
     * 
     * @throws IOException shall not occur
     * @throws ExecutionException shall not occur 
     * @throws URISyntaxException shall not occur
     */
    @Test
    public void testAas() throws IOException, ExecutionException, URISyntaxException {
        NotificationMode oldM = ActiveAasBase.setNotificationMode(NotificationMode.SYNCHRONOUS);
        Assert.assertTrue(AasPartRegistry.contributorClasses().contains(EcsAas.class));
        
        AasSetup oldSetup = AasPartRegistry.setAasSetup(AasSetup.createLocalEphemeralSetup());
        AasPartRegistry.AasBuildResult res = AasPartRegistry.build(c -> c instanceof EcsAas);
        
        // active AAS require two server instances and a deployment
        Server implServer = res.getProtocolServerBuilder().build();
        implServer.start();
        Server aasServer = AasPartRegistry.deploy(res.getAas()); 
        aasServer.start();
        AasPartRegistry.retrieveIipAas().accept(new AasPrintVisitor());
        
        EcsAasClient client = new EcsAasClient(Id.getDeviceIdAas());
        test(client);

        // no values for provider-defined metrics here as regular update started through lifecycle descriptor, see below
        Map<String, Predicate<Object>> expectedMetrics = new HashMap<>();
        expectedMetrics.put(MetricsAasConstants.SYSTEM_MEMORY_TOTAL, null);
        assertMetrics(expectedMetrics);
        
        aasServer.stop(true);
        implServer.stop(true);
        AasPartRegistry.setAasSetup(oldSetup);
        ActiveAasBase.setNotificationMode(oldM);
    }

    /**
     * Tests the {@link EcsAas} via the lifecycle descriptors.
     * 
     * @throws IOException shall not occur
     * @throws ExecutionException shall not occur 
     * @throws URISyntaxException shall not occur
     */
    @Test
    public void testLifecycle() throws IOException, ExecutionException, URISyntaxException {
        NotificationMode oldM = ActiveAasBase.setNotificationMode(NotificationMode.SYNCHRONOUS);
        AasSetup aasSetup = AasSetup.createLocalEphemeralSetup(null, false);
        AasSetup oldSetup = AasPartRegistry.setAasSetup(aasSetup);
        EcsFactory.getConfiguration().setAas(aasSetup);

        ServerRecipe rcp = AasFactory.getInstance().createServerRecipe();
        Endpoint regEndpoint = aasSetup.getRegistryEndpoint();
        Server registryServer = rcp
            .createRegistryServer(regEndpoint, ServerRecipe.LocalPersistenceType.INMEMORY)
            .start();
        AasServer aasServer = rcp
            .createAasServer(aasSetup.getServerEndpoint(), ServerRecipe.LocalPersistenceType.INMEMORY, regEndpoint)
            .start();

        LifecycleHandler.startup(new String[] {});

        EcsAasClient client = new EcsAasClient(Id.getDeviceIdAas());
        test(client);
        
        Map<String, Predicate<Object>> expectedMetrics = new HashMap<>();
        expectedMetrics.put(MetricsAasConstants.SYSTEM_MEMORY_TOTAL, POSITIVE_INT_METRICS_VALUE);
        expectedMetrics.put(MetricsAasConstants.SYSTEM_MEMORY_USAGE, POSITIVE_DBL_METRICS_VALUE);
        assertMetrics(expectedMetrics);
        
        LifecycleHandler.shutdown();

        aasServer.stop(true);
        registryServer.stop(true);

        AasPartRegistry.setAasSetup(oldSetup);
        ActiveAasBase.setNotificationMode(oldM);
    }
    
    /**
     * Asserts the existence of selected AAS metrics and/or their values.
     * 
     * @param expected the expected metrics as key-predicate pairs, whereby the predicate may be <b>null</b> to 
     *     indicated that the value shall not be tested 
     * @throws IOException if the AAS cannot be retrieved
     * @throws ExecutionException if a property cannot be queried
     */
    private void assertMetrics(Map<String, Predicate<Object>> expected) throws IOException, ExecutionException {
        Aas aas = AasPartRegistry.retrieveIipAas();
        Submodel resources = aas.getSubmodel(AasPartRegistry.NAME_SUBMODEL_RESOURCES);
        Assert.assertNotNull(resources);
        SubmodelElementCollection resource = resources.getSubmodelElementCollection(Id.getDeviceIdAas());
        Assert.assertNotNull(resource);
        for (Map.Entry<String, Predicate<Object>> ent : expected.entrySet()) {
            Property prop = resource.getProperty(ent.getKey());
            Assert.assertNotNull(prop);
            Predicate<Object> pred = ent.getValue();
            if (null != pred) {
                Object val = prop.getValue();
                Assert.assertTrue(pred.test(val));
            }
        }
    }

    /**
     * Tests the given {@code client}.
     * 
     * @param client the client to test
     * @throws ExecutionException shall not occur 
     * @throws URISyntaxException shall not occur
     * @throws IOException shall not occur
     */
    private void test(EcsAasClient client) throws ExecutionException, URISyntaxException, IOException {
        ContainerManager mgr = EcsFactory.getContainerManager(); // for x-checking
        final URI dummy = new URI("file:///dummy");
        String id = client.addContainer(dummy);
        Assert.assertNotNull(id);
        Assert.assertTrue(id.length() > 0);
        Assert.assertNotNull(client.getContainers());
        
        ContainerDescriptor cnt = mgr.getContainer(id);
        Assert.assertEquals(ContainerState.AVAILABLE, mgr.getState(id));
        Assert.assertEquals(ContainerState.AVAILABLE, client.getState(id));
        Assert.assertTrue(mgr.getContainers().contains(cnt));
        Assert.assertTrue(mgr.getIds().contains(id));
        try {
            client.startContainer("");
            Assert.fail("No exception");
        } catch (ExecutionException e) {
            // this is ok
        }
        client.startContainer(id);
        TimeUtils.sleep(300);
        Assert.assertEquals(ContainerState.DEPLOYED, mgr.getState(id));
        Assert.assertEquals(ContainerState.DEPLOYED, client.getState(id));
        client.updateContainer(id, dummy);
        TimeUtils.sleep(300);
        client.stopContainer(id);
        TimeUtils.sleep(300);
        Assert.assertEquals(ContainerState.STOPPED, mgr.getState(id));
        Assert.assertEquals(ContainerState.STOPPED, client.getState(id));

        client.startContainer(id);
        TimeUtils.sleep(300);
        Assert.assertEquals(ContainerState.DEPLOYED, mgr.getState(id));
        Assert.assertEquals(ContainerState.DEPLOYED, client.getState(id));

        AasPartRegistry.retrieveIipAas().accept(new AasPrintVisitor());

        try {
            client.undeployContainer(id);
            Assert.fail("No exception");
        } catch (ExecutionException e) {
            // this is ok
        }

        client.stopContainer(id);
        TimeUtils.sleep(300);
        Assert.assertEquals(ContainerState.STOPPED, mgr.getState(id));
        Assert.assertEquals(ContainerState.STOPPED, client.getState(id));
        client.undeployContainer(id);
        TimeUtils.sleep(300);
        Assert.assertEquals(ContainerState.UNKNOWN, mgr.getState(id));
        Assert.assertEquals(ContainerState.UNKNOWN, client.getState(id));
        
        id = client.addContainer(dummy);
        TimeUtils.sleep(300);
        cnt = mgr.getContainer(id);
        client.startContainer(id);
        TimeUtils.sleep(300);
        client.migrateContainer(id, "other");
        TimeUtils.sleep(300);
        if (ContainerState.STOPPED == cnt.getState()) {
            client.undeployContainer(id);
            TimeUtils.sleep(300);
        }
        Assert.assertEquals(ContainerState.UNKNOWN, mgr.getState(id));
        Assert.assertEquals(ContainerState.UNKNOWN, client.getState(id));

        // cnt is gone, but there is a new instance on "bla"
        Assert.assertFalse(mgr.getContainers().contains(cnt));
        Assert.assertTrue(mgr.getContainers().size() > 0);
        Assert.assertFalse(mgr.getIds().contains(id));
        Assert.assertTrue(mgr.getIds().size() > 0);
    }

}
