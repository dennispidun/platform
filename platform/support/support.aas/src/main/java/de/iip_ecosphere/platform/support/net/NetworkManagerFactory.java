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

package de.iip_ecosphere.platform.support.net;

import java.util.Optional;
import java.util.logging.Logger;

import de.iip_ecosphere.platform.support.jsl.ServiceLoaderUtils;

/**
 * Provides access to the network manager.
 * 
 * @author Holger Eichelberger, SSE
 */
public class NetworkManagerFactory {

    private static final Logger LOGGER = Logger.getLogger(NetworkManagerFactory.class.getName());
    private static NetworkManager instance;

    /**
     * Returns the actual instance.
     * 
     * @return the actual instance
     */
    public static NetworkManager getInstance() {
        if (null == instance) {
            Optional<NetworkManagerDescriptor> first = ServiceLoaderUtils.findFirst(NetworkManagerDescriptor.class);
            if (first.isPresent()) {
                instance = first.get().createInstance();
                if (null != instance) {
                    LOGGER.fine("Network manager implementation registered: " + instance.getClass().getName());
                }
            } else {
                LOGGER.severe("No Network manager implementation known.");
            }
        }
        return instance;
    }
    
    /**
     * Convenience method to configure the actual network manager instance.
     * 
     * @param setup instance containing the configuration (may be <b>null</b>, ignored then)
     */
    public static void configure(NetworkManagerSetup setup) {
        NetworkManager mgr = getInstance();
        if (null != mgr && null != setup) {
            mgr.configure(setup);
        }
    }

}
