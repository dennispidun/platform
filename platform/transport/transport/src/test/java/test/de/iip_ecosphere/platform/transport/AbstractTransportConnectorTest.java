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
package test.de.iip_ecosphere.platform.transport;

import java.io.IOException;

import org.junit.Assert;

import de.iip_ecosphere.platform.transport.TransportFactory;
import de.iip_ecosphere.platform.transport.Utils;
import de.iip_ecosphere.platform.transport.connectors.AbstractReceptionCallback;
import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter;
import de.iip_ecosphere.platform.transport.serialization.Serializer;
import de.iip_ecosphere.platform.transport.serialization.SerializerRegistry;

/**
 * Reusable test steps without referring to specific protocols.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AbstractTransportConnectorTest {

    /**
     * Implements a simple reception callback.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class Callback extends AbstractReceptionCallback<Product> {

        private Product data;

        /**
         * Creates the callback instance.
         */
        protected Callback() {
            super(Product.class);
        }

        @Override
        public void received(Product data) {
            this.data = data;
        }

    }
    
    /**
     * Implements the test using the {@link TransportFactory}.
     * 
     * @param host the host to use (usually "localhost")
     * @param port the TCP port to use
     * @param serializerType the serializer type to use
     * @throws IOException in case that connection/communication fails
     */
    public static void doTest(String host, int port, Class<? extends Serializer<Product>> serializerType) 
        throws IOException {
        Product data1 = new Product("prod1", 10.2);
        Product data2 = new Product("prod2", 5.1);

        System.out.println("Using serializer: " + serializerType.getSimpleName());
        SerializerRegistry.registerSerializer(serializerType);
        TransportParameter param1 = new TransportParameter(host, port, "cl1");
        TransportConnector cl1 = TransportFactory.createConnector();
        System.out.println("Connecting connector 1");
        cl1.connect(param1);
        final String stream1 = cl1.composeStreamName("", "stream1");
        final String stream2 = cl1.composeStreamName("", "stream2");
        final Callback cb1 = new Callback();
        cl1.setReceptionCallback(stream2, cb1);

        TransportParameter param2 = new TransportParameter(host, port, "cl2");
        TransportConnector cl2 = TransportFactory.createConnector();
        System.out.println("Connecting connector 2");
        cl2.connect(param2);
        final Callback cb2 = new Callback();
        cl2.setReceptionCallback(stream1, cb2);

        System.out.println("Sending/Receiving");
        cl1.syncSend(stream1, data1);
        cl2.syncSend(stream2, data2);
        Utils.sleep(2000);
        assertProduct(data1, cb2);
        assertProduct(data2, cb1);

        System.out.println("Cleaning up");
        cl1.disconnect();
        cl2.disconnect();
        SerializerRegistry.unregisterSerializer(Product.class);
    }

    /**
     * Asserts that {@code expected} and the received value in {@code callback}
     * contain the same values.
     * 
     * @param expected expected value
     * @param received received value
     */
    private static void assertProduct(Product expected, Callback received) {
        int count = 0;
        while (received.data == null && count < 10) {
            Utils.sleep(100);
            count++;
        }
        Assert.assertNotNull(received.data);
        Assert.assertEquals(expected.getDescription(), received.data.getDescription());
        Assert.assertEquals(expected.getPrice(), received.data.getPrice(), 0.01);
        received.data = null;
    }
    
}
