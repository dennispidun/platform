/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.iip_aas;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.DeploymentBuilder;

/**
 * A registry for {@link AasContributor} instances to be loaded via the Java Service loader.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasPartRegistry {

    public static final String ID_SHORT = "IIP-Ecosphere";
    public static final String URN = "urn:::AAS:::iipEcosphere#";
    
    /**
     * Returns the contributor loader.
     * 
     * @return the loader instance
     */
    private static ServiceLoader<AasContributor> getContributorLoader() {
        return ServiceLoader.load(AasContributor.class);        
    }
    
    /**
     * Returns the contributors.
     * 
     * @return the contributors
     */
    public static Iterator<AasContributor> contributors() {
        return getContributorLoader().iterator();
    }
    
    /**
     * Returns the contributor classes.
     * 
     * @return the contributor classes
     */
    public static Set<Class<? extends AasContributor>> contributorClasses() {
        return getContributorLoader()
            .stream()
            .map((p) -> p.get().getClass())
            .collect(Collectors.toSet());        
    }
    
    /**
     * Build up all AAS of the currently running platform part. [public for testing]
     * 
     * @return the list of AAS
     */
    public static List<Aas> build() {
        List<Aas> aas = new ArrayList<>();
        AasBuilder aasBuilder = AasFactory.getInstance().createAasBuilder(ID_SHORT, URN);
        Iterator<AasContributor> iter = contributors();
        while (iter.hasNext()) {
            AasContributor contributor = iter.next();
            Aas partAas = contributor.contributeTo(aasBuilder);
            if (null != partAas) {
                aas.add(partAas);
            }
        }
        aas.add(0, aasBuilder.build());
        return aas;
    }
    
    /**
     * Deploy the given AAS to a local server. [preliminary]
     * 
     * @param aas the list of aas, e.g., from {@link #build()}
     * @param host the host to deploy to
     * @param port the TCP port to deploy to 
     * @param regPath the local registry path
     * @return the server instance
     */
    public static Server deployTo(List<Aas> aas, String host, int port, String regPath) {
        DeploymentBuilder dBuilder = AasFactory.getInstance().createDeploymentBuilder(host, port);
        dBuilder.addInMemoryRegistry(regPath);
        for (Aas a: aas) {
            dBuilder.deploy(a);
        }
        return dBuilder.createServer(200);
    }

}