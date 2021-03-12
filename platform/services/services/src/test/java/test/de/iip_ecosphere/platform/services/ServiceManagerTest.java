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

package test.de.iip_ecosphere.platform.services;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.services.ArtifactDescriptor;
import de.iip_ecosphere.platform.services.ServiceDescriptor;
import de.iip_ecosphere.platform.services.ServiceFactory;
import de.iip_ecosphere.platform.services.ServiceManager;
import de.iip_ecosphere.platform.services.ServiceState;
import de.iip_ecosphere.platform.support.CollectionUtils;

/**
 * Tests {@link ServiceManager}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ServiceManagerTest {
    
    /**
     * Tests {@link ServiceManager}.
     * 
     * @throws ExecutionException shall not occur
     * @throws URISyntaxException shall not occur
     */
    @Test
    public void testApp() throws ExecutionException, URISyntaxException {
        URI dummy = new URI("file:///dummy");
        ServiceManager mgr = ServiceFactory.getServiceManager();
        Assert.assertNotNull(mgr);
        
        try {
            mgr.addArtifact(null);
            Assert.fail("No exception");
        } catch (ExecutionException e) {
            // ok
        }
        String aId = mgr.addArtifact(dummy);
        Assert.assertNotNull(aId);
        ArtifactDescriptor aDesc = mgr.getArtifact(aId);
        Assert.assertNotNull(aId);
        Assert.assertTrue(mgr.getArtifactIds().contains(aId));
        Assert.assertTrue(mgr.getArtifacts().contains(aDesc));

        Assert.assertEquals(aId, aDesc.getId());
        Assert.assertTrue(aDesc.getName().length() > 0);
        Assert.assertTrue(aDesc.getServiceIds().size() > 0);
        List<String> sIds = CollectionUtils.toList(aDesc.getServiceIds().iterator());
        String sId = sIds.get(0);
        Assert.assertNotNull(sId);
        ServiceDescriptor sDesc = aDesc.getService(sId);
        Assert.assertNotNull(sDesc);
        Assert.assertTrue(aDesc.getServiceIds().contains(sDesc.getId()));
        Assert.assertTrue(aDesc.getServices().contains(sDesc));
        Assert.assertEquals(ServiceState.AVAILABLE, sDesc.getState());
        
        mgr.startService(sId);
        Assert.assertEquals(ServiceState.RUNNING, sDesc.getState());
        mgr.passivate(sId);
        Assert.assertEquals(ServiceState.PASSIVATED, sDesc.getState());
        mgr.activate(sId);
        Assert.assertEquals(ServiceState.RUNNING, sDesc.getState());
        mgr.reconfigure(sId, new HashMap<String, Object>());
        Assert.assertEquals(ServiceState.RUNNING, sDesc.getState());
        mgr.stopService(sId);
        Assert.assertEquals(ServiceState.STOPPED, sDesc.getState());

        //mgr.cloneArtifact(aId, sId);
        //mgr.migrateService(aId, sId);
        //mgr.switchToService(aId, sId);
        //mgr.updateService(aId, sId);
        
        mgr.removeArtifact(aId);
        Assert.assertFalse(mgr.getArtifactIds().contains(aId));
        Assert.assertFalse(mgr.getArtifacts().contains(aDesc));
    }
    
}