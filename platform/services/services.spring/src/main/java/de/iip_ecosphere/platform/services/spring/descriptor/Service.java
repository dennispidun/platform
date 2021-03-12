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

package de.iip_ecosphere.platform.services.spring.descriptor;

import java.util.List;

import de.iip_ecosphere.platform.services.ServiceKind;
import de.iip_ecosphere.platform.services.Version;

/**
 * Information about a single service. {@link #getId()} and {@link #getName()} must be given, both not empty, 
 * {@code #getKind()} and {@link #getVersion()} must be given, with version in format of {@link Version}. 
 * {@code #getCmdArg()} or {@link #getEnsembleWith()} be empty. {@code #getDependencies()} and {@code #getRelations()} 
 * must be given but may be empty. If elements are given, the elements must be valid. {@code #getProcess()} may be 
 * absent, i.e. <b>null</b>, but if given it must be valid. {@link #getInstances()}, {@link #getMemory()}, 
 * {@link #getDisk()}, {@link #getCpus()} are replaced by default values if invalid.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface Service {

    /**
     * Returns the name of the service.
     * 
     * @return the name
     */
    public String getId();

    /**
     * Returns the name of the service.
     * 
     * @return the name
     */
    public String getName();

    /**
     * Returns the version of the service.
     * 
     * @return the version
     */
    public String getVersion();

    /**
     * Returns the description of the service.
     * 
     * @return the description
     */
    public String getDescription();

    /**
     * Returns the command line arguments.
     * 
     * @return the command line arguments (may be empty for none)
     */
    public List<String> getCmdArg();

    /**
     * Returns the service ids this service is starting as an ensemble with itself.
     * The ensemble services shall not be started on their own.
     * 
     * @return the service ids (may be empty for none)
     */
    public List<String> getEnsembleWith();

    /**
     * Defines the command line arguments. [required by SnakeYaml]
     * 
     * @return the service dependences(may be empty for none)
     */
    public List<? extends ServiceDependency> getDependencies();

    /**
     * Returns the service-specific relations and command line arguments.
     * 
     * @return the relations, may be empty
     */
    public List<? extends Relation> getRelations();
    
    /**
     * Returns an optional attached process realizing the service.
     * 
     * @return the process information, may be <b>null</b>
     */
    public Process getProcess();
    
    /**
     * Sets the service kind. [required by SnakeYaml]
     * 
     * @return the service kind
     */
    public ServiceKind getKind();
    
    /**
     * Sets whether this service is decentrally deployable.
     * 
     * @return {@code true} for deployable, {@code false} for not deployable 
     */
    public boolean isDeployable();

    /**
     * Returns the desired number of instances of this service to be started in the same process. This property is 
     * considered during deployment only if the deployer supports it.
     * 
     * @return the number of instances, ignored if not positive
     */
    public int getInstances();

    /**
     * Returns the desired memory for instances of this service. This property is considered during deployment
     * only if the deployer supports it.
     * 
     * @return the desired memory in bytes, ignored if not positive
     */
    public int getMemory();

    /**
     * Returns the desired disk space for instances of this service. This property is considered during deployment
     * only if the deployer supports it.
     * 
     * @return the desired disk space in bytes, ignored if not positive
     */
    public int getDisk();

    /**
     * Returns the desired number of CPUs for instances of this service. This property is considered during deployment
     * only if the deployer supports it.
     * 
     * @return the desired number of CPUs , ignored if not positive
     */
    public int getCpus();
    
}