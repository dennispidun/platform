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
package test.de.iip_ecosphere.platform.transport.connectors.rabbitmq;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.qpid.server.SystemLauncher;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.Server;
import de.iip_ecosphere.platform.support.ServerAddress;
import test.de.iip_ecosphere.platform.transport.AbstractTestServer;

/**
 * A simple AMQP server for testing/experiments. This class works with Java 8.
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestQpidServer extends AbstractTestServer {
    
    private SystemLauncher systemLauncher;
    private ServerAddress addr;

    /**
     * Creates the server instance.
     * 
     * @param addr the server address (schema ignored)
     */
    public TestQpidServer(ServerAddress addr) {
        this.addr = addr;
    }
    
    // checkstyle: stop exception type check

    @Override
    public Server start() {
        try {
            System.setProperty("qpid.amqp_port", Integer.toString(addr.getPort()));
            systemLauncher = new SystemLauncher();
            Map<String, Object> attributes = new HashMap<String, Object>();
            File f = new File(getConfigDir("./src/test"), "config.json");
            URL initialConfig = f.toURI().toURL();
            // https://qpid.apache.org/releases/qpid-broker-j-8.0.0/book/
            // Java-Broker-Initial-Configuration-Configuration-Properties.html
            attributes.put("type", "Memory");
            attributes.put("initialConfigurationLocation", initialConfig.toExternalForm());
            attributes.put("startupLoggedToSystemOut", true);
            attributes.put("initialSystemPropertiesLocation", ""); // breaks otherwise in (Spring-)packaged jars
            systemLauncher.startup(attributes);
        } catch (Exception e) {
            LoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
        }
        return this;
    }
    
    // checkstyle: resume exception type check

    @Override
    public void stop(boolean dispose) {
        systemLauncher.shutdown();
    }

}
