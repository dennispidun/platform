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

package de.iip_ecosphere.platform.services.spring.yaml;

import de.iip_ecosphere.platform.services.spring.descriptor.Relation;

/**
 * Represents a relation/connection between services. [Name taken from usage view]
 * 
 * @author Holger Eichelberger, SSE
 */
public class YamlRelation implements Relation {

    private String channel = "";
    private YamlEndpoint endpoint;
    
    @Override
    public String getChannel() {
        return channel;
    }
    
    @Override
    public YamlEndpoint getEndpoint() {
        return endpoint;
    }
    
    /**
     * Defines the name of the communication channel this relation is realized by. [Required by SnakeYaml]
     * 
     * @param channel the channel name, may be {@link #LOCAL_CHANNEL} referring to all channels used for 
     *   local communication
     */
    public void setChannel(String channel) {
        this.channel = channel;
    }

    /**
     * Defines communication endpoint (port/host) the service shall communicate with. 
     * 
     * @param endpoint the communication endpoint
     */
    public void setEndpoint(YamlEndpoint endpoint) {
        this.endpoint = endpoint;
    }
    
}
