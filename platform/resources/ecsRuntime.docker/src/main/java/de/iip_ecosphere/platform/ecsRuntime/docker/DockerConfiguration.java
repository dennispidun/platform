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

package de.iip_ecosphere.platform.ecsRuntime.docker;

import java.io.IOException;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.ecsRuntime.Configuration;
import de.iip_ecosphere.platform.support.iip_aas.Version;

/**
 * Implements the docker specific configuration. For configuration prerequisites, see {@link Configuration}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DockerConfiguration extends Configuration {

    private String dockerHost;
    private int id;
    private String name;
    private Version version;
   
    /**
     * Returns the docker host.
     * 
     * @return the docker host as Docker host string, e.g., unix:///var/run/docker.sock
     */
    public String getDockerHost() {
        return dockerHost;
    }
    
    /**
     * Defines the docker host. [required by SnakeYaml]
     * 
     * @param dockerHost the docker host as Docker host string, e.g., unix:///var/run/docker.sock
     */
    public void setDockerHost(String dockerHost) {
        this.dockerHost = dockerHost;
    }
    
    /**
     * TODO .
     * @return id
     */
    public int getId() {
        return id;
    }

    /**
     * TODO .
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * TODO.
     * @return name
     */
    public String getName() {
        return name;
    }
    
    /**
     * TODO .
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * TODO .
     * @return version
     */
    public Version getVersion() {
        return version;
    }

    /**
     * TODO .
     * @param version
     */
    public void setVersion(Version version) {
        this.version = version;
    }

     /**
     * Reads a {@link DockerConfiguration} instance from a default "ecsRuntime.yml" file in the root folder of the jar. 
     *
     * @return configuration instance
     */
    public static DockerConfiguration readFromYaml() {
        DockerConfiguration result;
        try {
            return Configuration.readFromYaml(DockerConfiguration.class);
        } catch (IOException e) {
            LoggerFactory.getLogger(DockerConfiguration.class).error("Reading configuration: " + e.getMessage());
            result = new DockerConfiguration();
        }
        return result;
    }
    
}
