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

package de.iip_ecosphere.platform.services.environment;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.iip_ecosphere.platform.support.iip_aas.config.AbstractConfiguration;

/**
 * Information about an artifact containing services. The artifact is to be deployed. We assume that the underlying
 * yaml file is generated, i.e., repeated information such as relations can be consistently specified.
 * 
 * @author Holger Eichelberger, SSE
 */
public class YamlArtifact extends AbstractYamlArtifact {

    private List<YamlService> services;

    /**
     * Returns the services.
     * 
     * @return the services
     */
    public List<YamlService> getServices() {
        return services;
    }

    /**
     * Sets the service instances. [required by SnakeYaml]
     * 
     * @param services the services
     */
    public void setServices(List<YamlService> services) {
        this.services = services;
    }

    /**
     * Reads an {@link YamlArtifact} from a YAML input stream. The returned artifact may be invalid.
     * 
     * @param in the input stream (may be <b>null</b>)
     * @return the artifact info
     * @throws IOException if the data cannot be read, the configuration class cannot be instantiated
     */
    public static YamlArtifact readFromYaml(InputStream in) throws IOException {
        YamlArtifact result = AbstractConfiguration.readFromYaml(YamlArtifact.class, in);
        if (null == result.services) {
            result.services = new ArrayList<>();
        }
        return result;
    }

}
