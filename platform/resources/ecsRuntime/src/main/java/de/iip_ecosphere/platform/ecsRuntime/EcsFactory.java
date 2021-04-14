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

package de.iip_ecosphere.platform.ecsRuntime;

import java.util.Optional;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides access to the ECS instances.
 * 
 * @author Holger Eichelberger, SSE
 */
public class EcsFactory {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EcsFactory.class.getName());
    private static EcsFactoryDescriptor desc;
    private static ContainerManager manager = null;

    /**
     * Initializes this factory.
     */
    private static void init() {
        if (null == desc) {
            ServiceLoader<EcsFactoryDescriptor> loader = ServiceLoader.load(EcsFactoryDescriptor.class);
            Optional<EcsFactoryDescriptor> first = loader.findFirst();
            if (first.isPresent()) {
                desc = first.get();
            } else {
                LOGGER.error("No Container manager implementation known.");
            }
        }
    }

    /**
     * Returns the service manager.
     * 
     * @return the service manager
     */
    public static ContainerManager getContainerManager() {
        if (null == manager) {
            init();
            if (null != desc) {
                manager = desc.createContainerManagerInstance();
                if (null != manager) {
                    LOGGER.info("Container manager implementation registered: " + manager.getClass().getName());
                }
            }
        }
        return manager;
    }

    /**
     * Returns the actual configuration instance for the implementing container manager.
     * 
     * @return the configuration instance
     */
    static Configuration getConfiguration() {
        Configuration result;
        init();
        if (null != desc) {
            result = desc.getConfiguration();
        } else {
            result = new Configuration();
        }
        return result;
    }

}
