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

package de.iip_ecosphere.platform.services;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Describes a service.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ServiceDescriptor {
    
    /**
     * Returns the unique id of the service.
     * 
     * @return the unique id
     */
    public String getId();
    
    /**
     * The name of the service.
     * 
     * @return the name
     */
    public String getName();
    
    /**
     * The version of the service.
     * 
     * @return the version
     */
    public Version getVersion();
    
    /**
     * The description of the service.
     * 
     * @return the description, may be empty
     */
    public String getDescription();

    /**
     * Returns the state the service is currently in. [R4c]
     * 
     * @return the state
     */
    public ServiceState getState();

    /**
     * Changes the state. [R133c]
     * 
     * @param state the new state
     * @throws ExecutionException if changing the state fails for some reason
     */
    public void setState(ServiceState state) throws ExecutionException;
    
    /**
     * Returns whether the service is deployable in distributable manner or fixed in deployment location.
     * 
     * @return {@code true} for deployable, {@code false} for fixed
     */
    public boolean isDeployable();
    
    /**
     * Returns the service kind.
     * 
     * @return the service kind
     */
    public ServiceKind getKind();
    
    /**
     * Returns the containing artifact.
     * 
     * @return the containing artifact (descriptor)
     */
    public ArtifactDescriptor getArtifact();
    
    /**
     * Returns all information about parameter for {@link #reconfigure(Map)}.
     * 
     * @return the name-descriptor mapping for all supported parameters
     */
    public List<TypedDataDescriptor> getParameters();
    
    /**
     * Returns all (asynchronous) input connectors into this service.
     * 
     * @return all input channels
     */
    public List<TypedDataConnectorDescriptor> getInputDataConnectors();

    /**
     * Returns all (asynchronous) output connectors from this service.
     * 
     * @return all input channels
     */
    public List<TypedDataConnectorDescriptor> getOutputDataConnectors();

}
