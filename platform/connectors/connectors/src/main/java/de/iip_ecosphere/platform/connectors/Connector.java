/********************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.connectors;

import java.io.IOException;

import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;

/**
 * The interface of a platform/machine connector. A connector shall define a {@link ConnectorDescriptor} as top-level 
 * inner class and register the descriptor as service.
 * 
 * @param <O> the output type from the underlying machine/platform
 * @param <I> the input type to the underlying machine/platform
 * @param <CO> the output type of the connector
 * @param <CI> the input type of the connector
 * @param <D> the model data type (see @link {@link ModelAccess})
 *
 * @author Holger Eichelberger, SSE
 */
public interface Connector <O, I, CO, CI, D> {
    
    /**
     * Connects the connector to the underlying machine/platform.
     * 
     * @param params connection parameter
     * @throws IOException in case that connecting fails
     */
    public void connect(ConnectorParameter params) throws IOException;
    
    /**
     * Writes the given {@code data} to the underlying machine/platform.
     * 
     * @param data the data to send to {@code stream}
     * @throws IOException in case that problems during the connection happens
     */
    public void write(CI data) throws IOException;
    
    /**
     * Attaches a reception {@code callback} to this connector. The {@code callback}
     * is called upon a reception.
     * 
     * @param callback the callback to attach
     * @throws IOException in case that problems during registering the callback
     *                     (e.g., during subscription) happens
     */
    public void setReceptionCallback(ReceptionCallback<CO> callback) throws IOException;

    /**
     * Disconnects the connector from the underlying machine/platform.
     * 
     * @throws IOException in case that connecting fails
     */
    public void disconnect() throws IOException;
    
    /**
     * Final cleanup when platform shuts down, e.g., for shared resources.
     */
    public void dispose();
    
    /**
     * Returns a descriptive name of the connector/the connected protocol.
     * 
     * @return a descriptive name of the connected protocol
     */
    public String getName();
    
}
