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

package de.iip_ecosphere.platform.platform;

import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry.AasSetup;

/**
 * Extends the AAS setup with (server-sided) persistence information.
 *  
 * @author Holger Eichelberger, SSE
 */
public class PersistentAasSetup extends AasSetup {

    /**
     * Common persistence types.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum ConfiguredPersistenceType {
        INMEMORY,
        MONGO // let's see for other types, may be we need some exclusions on the configuration level
    }
    
    private ConfiguredPersistenceType persistence = ConfiguredPersistenceType.INMEMORY;

    /**
     * Returns the persistence type.
     * 
     * @return the persistence type
     */
    public ConfiguredPersistenceType getPersistence() {
        return persistence;
    }
    
    /**
     * Setting the persistence type.
     * 
     * @param persistence the persistence type
     */
    public void setPersistence(ConfiguredPersistenceType persistence) {
        this.persistence = persistence;
    }
    
    /**
     * Setting the persistence type.
     * 
     * @param persistence the persistence type
     */
    public void setPersistence(String persistence) {
        try {
            this.persistence = ConfiguredPersistenceType.valueOf(persistence);
        } catch (IllegalArgumentException e) {
            // stay with default
        }
    }

}
