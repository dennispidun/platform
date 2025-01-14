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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.ServiceStub;

/**
 * A basic re-usable implementation of the service manager. Implementations shall override at least 
 * {@link #removeService(String)}, {@link #switchToService(String, String)}, {@link #migrateService(String, String)}
 * and call the implementation of this class to perform the changes. Implementations shall call the notify methods 
 * in {@link ServicesAas}.
 *
 * @param <A> the actual type of the artifact descriptor
 * @param <S> the actual type of the service descriptor
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractServiceManager<A extends AbstractArtifactDescriptor<S>, 
    S extends AbstractServiceDescriptor<A>> implements ServiceManager {

    private Map<String, A> artifacts = Collections.synchronizedMap(new HashMap<>());

    /**
     * Returns the available connector testing predicate.
     * 
     * @return the predicate
     */
    protected abstract Predicate<TypedDataConnectorDescriptor> getAvailablePredicate();
    
    @Override
    public Set<String> getArtifactIds() {
        return artifacts.keySet();
    }
    
    @Override
    public Collection<A> getArtifacts() {
        return artifacts.values();
    }
    
    @Override
    public Set<String> getServiceIds() {
        Set<String> result = new HashSet<>();
        for (A a : getArtifacts()) {
            result.addAll(a.getServiceIds());
        }
        return result;
    }

    @Override
    public Collection<S> getServices() {
        Set<S> result = new HashSet<>();
        for (A a : getArtifacts()) {
            result.addAll(a.getServices());
        }
        return result;
    }

    @Override
    public A getArtifact(String artifactId) {
        return null == artifactId ? null : artifacts.get(artifactId);
    }
    
    @Override
    public S getService(String serviceId) {
        S result = null;
        for (A a : getArtifacts()) {
            result = a.getService(serviceId);
            if (null != result) {
                break;
            }
        }
        return result;
    }
    
    @Override
    public ServiceState getServiceState(String serviceId) {
        ServiceState result = ServiceState.UNKOWN;
        S service = getService(serviceId);
        if (null != service) {
            result = service.getState();
        }
        return result;
    }
    
    /**
     * Adds an artifact.
     * 
     * @param artifactId the artifact id
     * @param descriptor the service descriptor
     * @return {@code artifactId}
     * @throws ExecutionException in case that the id is invalid or already known
     */
    protected String addArtifact(String artifactId, A descriptor) throws ExecutionException {
        checkId(artifactId, "artifactId");
        if (artifacts.containsKey(artifactId)) {
            throw new ExecutionException("Artifact id '" + artifactId + "' is already known", null);
        }
        artifacts.put(artifactId, descriptor);
        ServicesAas.notifyArtifactAdded(descriptor);
        return artifactId;
    }

    @Override
    public void removeArtifact(String artifactId) throws ExecutionException {
        checkId(artifactId, "artifactId");
        if (!artifacts.containsKey(artifactId)) {
            throw new ExecutionException("Artifact id '" + artifactId 
                + "' is not known. Cannot remove artifact.", null);
        }
        A aDesc = artifacts.remove(artifactId);
        ServicesAas.notifyArtifactRemoved(aDesc);
    }
    
    /**
     * Checks the given {@code id} for basic validity.
     * 
     * @param id the id to check
     * @param text the text to include into the exception
     * @throws ExecutionException if {@code id} is not considered valid
     */
    protected static void checkId(String id, String text) throws ExecutionException {
        if (null == id || id.length() == 0) {
            throw new ExecutionException(text + "must be given (not null or empty)", null);
        }
    }
    
    @Override
    public void switchToService(String serviceId, String targetId) throws ExecutionException {
        checkId(serviceId, "id");
        checkId(serviceId, "targetId");
        if (!serviceId.equals(targetId)) {
            stopService(serviceId);
            startService(targetId);
        }
    }

    @Override
    public void migrateService(String serviceId, String resourceId) throws ExecutionException {
        checkId(serviceId, "serviceId");
        S cnt = getServiceDescriptor(serviceId, "serviceId", "migrate");
        if (ServiceState.RUNNING == cnt.getState()) {
            stopService(serviceId);
        } else {
            throw new ExecutionException("Service " + serviceId + " is in state " + cnt.getState() 
                + ". Cannot migrate service.", null);
        }
    }
    
    @Override
    public void passivateService(String serviceId) throws ExecutionException {
        S service = getServiceDescriptor(serviceId, "serviceId", "passivate");
        ServiceStub stub = service.getStub();
        if (ServiceState.RUNNING == service.getState() || null == stub) {
            setState(service, ServiceState.PASSIVATING);
            stub.passivate();
            setState(service, ServiceState.PASSIVATED);
        } else {
            throw new ExecutionException("Cannot passivate service '" + serviceId + "'as it is in state " 
                + service.getState() + "/not running.", null);
        }
    }
    
    @Override
    public void activateService(String serviceId) throws ExecutionException {
        S service = getServiceDescriptor(serviceId, "serviceId", "activate");
        ServiceStub stub = service.getStub();
        if (ServiceState.PASSIVATED == service.getState() || null == stub) {
            stub.activate();
            setState(service, ServiceState.RUNNING);
        } else {
            throw new ExecutionException("Cannot passivate as service is in state " + service.getState(), null);
        }
    }

    
    @Override
    public void reconfigureService(String serviceId, Map<String, String> values) throws ExecutionException {
        S service = getServiceDescriptor(serviceId, "serviceId", "reconfigure");
        ServiceStub stub = service.getStub();
        if (stub != null) {
            ServiceState state = service.getState();
            setState(service, ServiceState.RECONFIGURING);
            stub.reconfigure(values);
            setState(service, state);
        } else {
            throw new ExecutionException("Cannot reconfigure service '" + serviceId + "'as it is in state " 
                + service.getState() + "/not running.", null);
        }
    }
    
    /**
     * Returns the service stub for implementing the service operations.
     * 
     * @param service the service to return the stub for
     * @return the stub, may be <b>null</b> if the service is not running
     */
    protected ServiceStub getStub(S service) {
        return service.getStub();
    }

    /**
     * Returns a service descriptor.
     * 
     * @param artifactId the artifact id
     * @param idText the id text to be passed to {@link #checkId(String, String)}
     * @param activityText a description of the activity the service is requested for to construct an exception if 
     *   the service does not exist
     * @return the service (not <b>null</b>)
     * @throws ExecutionException if id is invalid or the service is unknown
     */
    protected A getArtifactDescriptor(String artifactId, String idText, String activityText) throws ExecutionException {
        checkId(artifactId, idText);
        A result = artifacts.get(artifactId);
        if (null == result) {
            throw new ExecutionException("Artifact id '" + artifactId + "' is not known. Cannot " + activityText 
                + " service.", null);
        }
        return result;
    }

    /**
     * Returns a service descriptor.
     * 
     * @param serviceId the service id
     * @param idText the id text to be passed to {@link #checkId(String, String)}
     * @param activityText a description of the activity the service is requested for to construct an exception if 
     *   the service does not exist
     * @return the service (not <b>null</b>)
     * @throws ExecutionException if id is invalid or the service is unknown
     */
    protected S getServiceDescriptor(String serviceId, String idText, String activityText) throws ExecutionException {
        checkId(serviceId, idText);
        S result = getService(serviceId);
        if (null == result) {
            throw new ExecutionException("Service id '" + serviceId + "' is not known. Cannot " + activityText 
                + " service.", null);
        }
        return result;
    }

    @Override
    public void setServiceState(String serviceId, ServiceState state) throws ExecutionException {
        setState(getServiceDescriptor(serviceId, "serviceId", "setState"), state);
    }
    
    /**
     * Changes the service state and notifies {@link ServicesAas}.
     * 
     * @param service the service
     * @param state the new state
     * @throws ExecutionException if changing the state fails
     */
    protected void setState(ServiceDescriptor service, ServiceState state) throws ExecutionException {
        ServiceState old = service.getState();
        service.setState(state);
        ServicesAas.notifyServiceStateChanged(old, state, service); 
    }
    
    @Override
    public List<TypedDataDescriptor> getParameters(String serviceId) {
        List<TypedDataDescriptor> result = null;
        S service = getService(serviceId);
        if (null != service) {
            result = service.getParameters();
        }
        return result;
    }

    @Override
    public List<TypedDataConnectorDescriptor> getInputDataConnectors(String serviceId) {
        List<TypedDataConnectorDescriptor> result = null;
        S service = getService(serviceId);
        if (null != service) {
            result = service.getInputDataConnectors();
        }
        return result;
    }

    @Override
    public List<TypedDataConnectorDescriptor> getOutputDataConnectors(String serviceId) {
        List<TypedDataConnectorDescriptor> result = null;
        S service = getService(serviceId);
        if (null != service) {
            result = service.getOutputDataConnectors();
        }
        return result;
    }

    /**
     * Sorts the given list by the dependencies specified in the deployment descriptor.
     * 
     * @param serviceIds the service ids to sort
     * @param start sequence for service start
     * @return the sorted service ids
     */
    protected String[] sortByDependency(String[] serviceIds, boolean start) {
        List<ServiceDescriptor> services = new ArrayList<ServiceDescriptor>();
        for (String s : serviceIds) {
            services.add(getService(s));
        }
        
        Predicate<TypedDataConnectorDescriptor> available;
        if (start) {
            available = getAvailablePredicate();
        } else {
            available = d -> true;
        }
        
        return sortByDependency(services, getServices(), available, !start)
            .stream()
            .map(s -> s.getId())
            .toArray(size -> new String[size]);
    }
    
    /**
     * Sorts a list of services by their dependencies, considering prerequisite input nodes outside the own ensemble.
     * Ensemble nodes are simply listed after their ensemble leader. [public, static for testing]
     * 
     * @param <S> the service type
     * @param services the services to sort
     * @param localServices all known local services including {@code services}
     * @param available the available predicate
     * @param reverse reverse the order (for stopping)
     * @return the list of sorted services
     */
    public static <S extends ServiceDescriptor> List<S> sortByDependency(List<S> services, 
        Collection<? extends ServiceDescriptor> localServices, Predicate<TypedDataConnectorDescriptor> available, 
        boolean reverse) {
        List<S> result = new ArrayList<S>();

        // idea... sort services by their output connections/dependencies adding first those that have no dependencies.
        // for all other, add them only if 1) ensemble leader has already been added (for ensemble members) or 2) all
        // non ensemble-connections (assuming that they will be available after ensemble start) are available
        // collect all ensemble-internal connectors
        Set<String> ensembleConnections = new HashSet<>();
        for (ServiceDescriptor s : services) {
            // empty for non-ensemble leaders, connections for ensemble leaders, ensemble-members just repeat the 
            // information; might be ok to use only non ensemble-members, but no guarantee that services also contains
            // all ensemble leaders
            ensembleConnections.addAll(AbstractServiceDescriptor.ensembleConnectorNames(s));
        }
        Set<String> internalConnections = AbstractServiceDescriptor.internalConnectorNames(localServices);
        internalConnections.removeAll(ensembleConnections);

        // process the services, exclude the ensemble connections
        Set<ServiceDescriptor> processed = new HashSet<ServiceDescriptor>();
        Set<String> avail = new HashSet<>();
        int before;
        boolean externalPrio = true;
        do {
            before = result.size();
            for (S sd : services) {
                boolean ok = true;
                if (processed.contains(sd)) {
                    continue;
                }
                if (null != sd.getEnsembleLeader()) { 
                    // ensemble leader must be started before, hull dependencies are "mapped" to ensemble leader
                    ok = processed.contains(sd.getEnsembleLeader());
                } else {
                    for (TypedDataConnectorDescriptor out : sd.getOutputDataConnectors()) {
                        String outName = out.getName();
                        if (externalPrio && internalConnections.contains(outName)) {
                            ok = false;
                            break; // defer to later stage
                        }
                        // ensemble-internal and already known available connections must not be tested
                        if (!ensembleConnections.contains(outName) && !avail.contains(outName)) {
                            ok = available.test(out);
                            if (ok) {
                                avail.add(outName);
                            } else {
                                break;
                            }
                        }             
                    }
                }
                if (ok) {
                    result.add(sd);
                    processed.add(sd);
                }
            }
            externalPrio = false;
        } while (before != result.size() && result.size() != services.size());
        // just add the remaining which may be parts of cycles
        for (S sd : services) {
            if (!processed.contains(sd)) {
                result.add(sd);
            }
        }
        if (reverse) {
            Collections.reverse(result);
        }
        return result;
    }

}
