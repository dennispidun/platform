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

package de.iip_ecosphere.platform.support.iip_aas;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Submodel;

/**
 * Basic functions for active AAS with notification calls.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ActiveAasBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActiveAasBase.class);
    private static boolean parallelNotification = true;
    private static ExecutorService exec = Executors.newFixedThreadPool(5);
    
    /**
     * Processes a notification on a submodel of {@link AasPartRegistry#retrieveIipAas()}.
     * 
     * @param subId the short id of the submodel
     * @param function the function to execute
     */
    public static void processNotification(String subId, Consumer<Submodel> function) {
        try {
            Aas aas = AasPartRegistry.retrieveIipAas();
            if (null != aas) {
                Submodel submodel = aas.getSubmodel(subId);
                if (null != submodel) {
                    if (parallelNotification) {
                        exec.execute(() -> function.accept(submodel));
                    } else {
                        function.accept(submodel);
                    }
                }
            } else {
                LOGGER.error("Cannot find submodel: " + subId);
            }
        } catch (IOException e) {
            LOGGER.error("While retrieving the IIP-Ecosphere AAS: " + e.getMessage(), e);
        }
    }

    /**
     * Changes the notification execution mode. [for testing]
     * 
     * @param parallel {@code true} for parallel executions, {@code false} for sequential
     * @return the last execution mode
     */
    public static boolean setParallelNotification(boolean parallel) {
        boolean old = parallelNotification;
        parallelNotification = parallel;
        return old;
    }
    
}
