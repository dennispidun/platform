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

package de.iip_ecosphere.platform.services;

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * A service provider interface for managing services in the IIP-Ecosphere platform.
 * Service implementations shall be bound by code generation to a specific (stream) service computing approach, e.g.,
 * Spring Cloud Streams. The management of such services happens via this interface, which shall utilize the respective
 * management capabilites of the underlying service computing approach. The interface is rather simple as it shall
 * be usable through an AAS. The id of a service used here must not be identical to the name in 
 * {@link ServiceDescriptor#getName()}, e.g., it may contain the version.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ServiceManager {

    /**
     * Adds an artifact (and transitively the contained services) to the management domain of this instance, e.g., 
     * by downloading it from an artifact/service store. This defines the {@code id} of the service within the 
     * management domain of this instance. After a successful execution, the artifact {@code id} is returned, artifact 
     * and service(s) shall be available and the service(s) shall be in state {@link ServiceState#AVAILABLE}.
     * 
     * @return the id of the service
     * @param location the location from where to download the service, e.g., an URL
     * @throws ExecutionException in case that adding the service fails for some reason
     */
    public String addArtifact(URI location) throws ExecutionException;

    /**
     * Starts a service. The service implementation must be already within the management domain of this instance by
     * {@link #addService(String, String)}. The service shall after some time be in state 
     * {@link ServiceState#STARTING}, {@link ServiceState#RUNNING} or {@link ServiceState#FAILED}.
     * 
     * @param serviceId the id of the service
     * @throws ExecutionException in case that starting the service fails for some reason
     */
    public void startService(String serviceId) throws ExecutionException;
    
    /**
     * Stops the service. The service shall afterwards be in state {@link ServiceState#STOPPED}.
     * 
     * @param serviceId the id of the service to stop
     * @throws ExecutionException if stopping the service fails
     */
    public void stopService(String serviceId) throws ExecutionException;
    
    /**
     * Migrates a service. However, it may be required to migrate/move the containing artifact. [adaptation]
     * 
     * @param serviceId the id of the service
     * @param location the target location, e.g., a device
     * @throws ExecutionException if migration fails
     */
    public void migrateService(String serviceId, URI location) throws ExecutionException;
    
    /**
     * Clones the artifact. As services belong to artifacts, it is not possible to migrate a single service (except
     * for if the artifact consists of a single service). [adaptation]
     * 
     * @param artifactId the id of the service
     * @param location the target location, e.g., a device
     * @throws ExecutionException if migration fails
     */
    public void cloneArtifact(String artifactId, URI location) throws ExecutionException;

    /**
     * Removes the artifact (and transitively its services) from the management domain of this instance. This operation 
     * shall only remove the implementation of non-operational services and, thus, perform a state transition to 
     * {@link ServiceState#UNDEPLOYING} and ultimately the service(s) shall be removed and their descriptors (including 
     * the artifact descriptor) shall not be available anymore.
     * 
     * @param artifactId the id of the artifact to remove
     * @throws ExecutionException if removing the service fails, e.g., because it is still running
     */
    public void removeArtifact(String artifactId) throws ExecutionException;
    
    /**
     * Updates the service by the service in the given {@code location}. This operation is responsible for stopping
     * the running service (if needed), replacing it, starting the new service.
     * 
     * @param serviceId the id of the service to be updated
     * @param location the location of the new service, e.g., an URL
     * @throws ExecutionException if the given service cannot be updated for some reason, e.g., because the replacement
     *   service is not an evolved version of the running service
     */
    public void updateService(String serviceId, URI location) throws ExecutionException;
    
    /**
     * Switches to an interface-compatible service. This method cares for stopping the old service, performing
     * a handover if adequate, starting the {@code target} service. [adaptation]
     * 
     * @param serviceId the id of the running service
     * @param targetId the id of the target service
     * @throws ExecutionException if switching the service cannot be performed for some reason
     */
    public void switchToService(String serviceId, String targetId) throws ExecutionException;
    
    /**
     * Activates the service. [adaptation]
     * 
     * @param id the id of the running service
     * @throws ExecutionException in case that activating fails, e.g., because the service is already active 
     */
    public void activate(String id) throws ExecutionException;

    /**
     * Passivates the service. [adaptation]
     * 
     * @param id the id of the running service
     * @throws ExecutionException in case that passivating fails, e.g., because the service is already passive 
     */
    public void passivate(String id) throws ExecutionException;
    
    /**
     * Reconfigures the underlying service. [adaptation]
     * 
     * @param serviceId the serviceId of the running service
     * @param values the (service-specific) values that shall lead to a reconfiguration of the service
     * @throws ExecutionException if reconfiguration fails
     */
    public void reconfigure(String serviceId, Map<String, Object> values) throws ExecutionException;

    /**
     * Sets the state of the service. [adaptation]
     * 
     * @param serviceId the id of the running service
     * @param state the new state of the service
     * @throws ExecutionException if changing to the target state is not possible 
     */
    public void setState(String serviceId, ServiceState state) throws ExecutionException;

    /**
     * Returns the state of the service.
     * 
     * @param serviceId the id of the running service
     * @return the state of the service
     */
    public ServiceState getState(String serviceId);

    /**
     * Returns the ids of all available artifacts.
     * 
     * @return the ids
     */
    public Set<String> getArtifactIds();
    
    /**
     * Returns the ids of all available services.
     * 
     * @return the ids
     */
    public Set<String> getServiceIds();

    /**
     * Returns the available (installed) artifacts.
     * 
     * @return the services
     */
    public Collection<? extends ArtifactDescriptor> getArtifacts();
    
    /**
     * Returns the available (installed) services in all artifacts.
     * 
     * @return the services
     */
    public Collection<? extends ServiceDescriptor> getServices();

    /**
     * Returns an artifact descriptor.
     * 
     * @param artifactId the id of the service (may be <b>null</b> or invalid)
     * @return the related artifavt descriptor or <b>null</b> if the artifact does not exist
     */
    public ArtifactDescriptor getArtifact(String artifactId); 
    
    /**
     * Returns a service descriptor.
     * 
     * @param serviceId the id of the service (may be <b>null</b> or invalid)
     * @return the related service descriptor or <b>null</b> if the service does not exist
     */
    public ServiceDescriptor getService(String serviceId); 
    
}
