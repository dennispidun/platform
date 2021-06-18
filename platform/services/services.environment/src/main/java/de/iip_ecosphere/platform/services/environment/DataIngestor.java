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

/**
 * Allows to ingest data asynchronously. An ingestor encapsulates access to a certain named channel but
 * does not expose the channel or its name.
 *
 * @param <D> the type od data to ingest
 * @author Holger Eichelberger, SSE
 */
public interface DataIngestor<D> {
    
    /**
     * Ingest data.
     * 
     * @param data the data to ingest
     */
    public void ingest(D data);

}
