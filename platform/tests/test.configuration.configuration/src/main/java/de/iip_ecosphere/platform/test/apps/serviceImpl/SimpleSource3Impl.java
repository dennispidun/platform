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

package de.iip_ecosphere.platform.test.apps.serviceImpl;

import java.io.InputStream;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import de.iip_ecosphere.platform.services.environment.DataIngestor;
import de.iip_ecosphere.platform.services.environment.DefaultServiceImpl;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
import iip.datatypes.Rec13;
import iip.interfaces.SimpleDataSource3Service;

/**
 * A simple test source ingesting data according to a timer schema. Analogously, a connector can be linked to a
 * data ingestor.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SimpleSource3Impl extends DefaultServiceImpl implements SimpleDataSource3Service {

    private Timer timer = new Timer();
    private Random random = new Random();
    
    /**
     * Fallback constructor.
     */
    public SimpleSource3Impl() {
        super(ServiceKind.SOURCE_SERVICE);
    }
    
    /**
     * Creates a service instance from a service id and a YAML artifact.
     * 
     * @param serviceId the service id
     * @param ymlFile the YML file containing the YAML artifact with the service descriptor
     */
    public SimpleSource3Impl(String serviceId, InputStream ymlFile) {
        super(serviceId, ymlFile);
    }

    // no override here as createRec1 and attach... are alternatives
    
    /**
    * Creates data to be ingested.
    *
    * @return the created data, <b>null</b> for no data
    */
    public Rec13 createRec13() {
        Rec13 rec = new Rec13();
        rec.setIntField(random.nextInt());
        rec.setStringField("SYNC");
        return rec;
    }

    /**
     * Called by the platform to attach an asynchronous data ingestor for type "Rec1".
     *
     * @param ingestor the "Rec1" ingestor instance
     */
    public void attachcreateRec1_SimpleSourceIngestor(final DataIngestor<Rec13> ingestor) {
        if (null != ingestor) {
            timer.schedule(new TimerTask() {
                
                @Override
                public void run() {
                    Rec13 rec = new Rec13();
                    rec.setIntField(random.nextInt());
                    rec.setStringField("ASYNC");
                    ingestor.ingest(rec);
                }
            }, 0, 1000);
        }
    }

}
