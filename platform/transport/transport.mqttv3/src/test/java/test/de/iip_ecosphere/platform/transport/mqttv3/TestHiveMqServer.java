/********************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/
package test.de.iip_ecosphere.platform.transport.mqttv3;

import java.io.File;

import com.hivemq.embedded.EmbeddedHiveMQ;
import com.hivemq.embedded.EmbeddedHiveMQBuilder;

import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import test.de.iip_ecosphere.platform.transport.AbstractTestServer;

/**
 * A simple embedded HiveMQ/MQTT test server for testing/experiments.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestHiveMqServer extends AbstractTestServer {
    
    private EmbeddedHiveMQ hiveMQ;
    private ServerAddress addr;

    /**
     * Creates the server instance.
     * 
     * @param addr the server address (schema is ignored)
     */
    public TestHiveMqServer(ServerAddress addr) {
        this.addr = addr;
    }
    
    @Override
    public Server start() {
        if (null == hiveMQ) {
            File hiveTmp = createTmpFolder("hivemq_v3");

            System.setProperty("HIVEMQ_PORT", Integer.toString(addr.getPort()));
            System.setProperty("HIVEMQ_ADDRESS", addr.getHost());
            System.setProperty("hivemq.log.folder", hiveTmp.getAbsolutePath());
            
            File cfg = getConfigDir("./src/test");
            final EmbeddedHiveMQBuilder embeddedHiveMQBuilder = EmbeddedHiveMQBuilder.builder()
                .withConfigurationFolder(cfg.toPath())
                .withDataFolder(hiveTmp.toPath())
                .withExtensionsFolder(new File(cfg, "extensions").toPath());
    
            hiveMQ = embeddedHiveMQBuilder.build();
            hiveMQ.start().join();
        }
        return this;
    }
    
    @Override
    public void stop(boolean dispose) {
        hiveMQ.stop().join();
        hiveMQ = null;
    }

}
