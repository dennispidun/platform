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

package de.iip_ecosphere.platform.transport.spring.binder.mqttv3;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.transport.connectors.basics.MqttQoS;
import de.iip_ecosphere.platform.transport.connectors.impl.AbstractTransportConnector;

/**
 * A MQTT client for a single binder instance. Typically, different binders subscribe to different
 * topics. 
 * 
 * Partially public for testing. Initial implementation, not optimized.
 * 
 * @author Holger Eichelberger, SSE
 */
public class MqttClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttV3MessageBinder.class); // map all to binder
    private static MqttClient lastInstance;
    private static MqttAsyncClient client;
    private static MqttConfiguration configuration;
    private static Callback callback;
    private static MqttQoS qos = MqttQoS.AT_LEAST_ONCE;
    
    /**
     * Creates and registers an instance.
     */
    public MqttClient() {
        lastInstance = this;
    }
    
    /**
     * Returns the last instance created for this class. [testing]
     * 
     * @return the last instance
     */
    public static MqttClient getLastInstance() {
        return lastInstance;
    }
    
    /**
     * Called when a message for a topic arrives.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface ArrivedCallback {

        /**
         * This method is called when a message arrives from the server.
         *
         * <p>
         * This method is invoked synchronously by the MQTT client. An
         * acknowledgment is not sent back to the server until this
         * method returns cleanly.</p>
         * <p>
         * If an implementation of this method throws an <code>Exception</code>, then the
         * client will be shut down.  When the client is next re-connected, any QoS
         * 1 or 2 messages will be redelivered by the server.</p>
         * <p>
         * Any additional messages which arrive while an
         * implementation of this method is running, will build up in memory, and
         * will then back up on the network.</p>
         * <p>
         * If an application needs to persist data, then it
         * should ensure the data is persisted prior to returning from this method, as
         * after returning from this method, the message is considered to have been
         * delivered, and will not be reproducible.</p>
         * <p>
         * It is possible to send a new message within an implementation of this callback
         * (for example, a response to this message), but the implementation must not
         * disconnect the client, as it will be impossible to send an acknowledgment for
         * the message being processed, and a deadlock will occur.</p>
         *
         * @param topic name of the topic on the message was published to
         * @param message the actual message.
         */
        public void messageArrived(String topic, MqttMessage message);

    }
    
    /**
     * The internal MQTT reception callback.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class Callback implements MqttCallback {

        private Map<String, ArrivedCallback> callbacks = Collections.synchronizedMap(new HashMap<>());
        
        @Override
        public void connectionLost(Throwable cause) {
            LOGGER.info("Connection lost: " + cause.getMessage());
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            ArrivedCallback cb = callbacks.get(topic);
            if (null != cb) {
                cb.messageArrived(topic, message);
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            // nothing
        }

    }
    
    /**
     * Creates the client based on a given MQTT client configuration.
     * 
     * @param config the MQTT configuration to take the connection information from
     */
    synchronized void createClient(MqttConfiguration config) {
        if (null == client) {
            try {
                configuration = config;
                qos = config.getQos();
                String clientId = AbstractTransportConnector.getApplicationId(config.getClientId(), "stream", 
                    config.getAutoClientId());
                LOGGER.info("Connecting to " + config.getBrokerString() + " with client id " + clientId);
                MqttAsyncClient cl = new MqttAsyncClient(config.getBrokerString(), clientId, new MemoryPersistence());
                callback = new Callback();
                cl.setCallback(callback);
                MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setMaxInflight(1000); // inbound message limitation, rumor 0 for unlimited does not work
                connOpts.setCleanSession(false);
                connOpts.setKeepAliveInterval(config.getKeepAlive());
                connOpts.setAutomaticReconnect(true);
                waitForCompletion(cl.connect(connOpts));
                client = cl;
            } catch (MqttException e) {
                LOGGER.error("Connecting MQTT client: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Stops the client.
     */
    public void stopClient() {
        try {
            waitForCompletion(client.disconnect());
            client.close();
            callback = null;
            client = null;
        } catch (MqttException e) {
            LOGGER.error("Stopping MQTT client: " + e.getMessage(), e);
        }
    }

    /**
     * Subscribes to {@code topic} if {@code topic} is not blacklisted by 
     * {@link MqttConfiguration#isFilteredTopic(String)}.
     * 
     * @param topic the topic to unsubscribe from
     * @param arrivedCallback the callback to be called when a message arrived
     * @return {@code true} if done/successful, {@code false} else
     */
    boolean subscribeTo(String topic, ArrivedCallback arrivedCallback) {
        boolean done = false;
        if (!configuration.isFilteredTopic(topic) && null != client) {
            try {
                callback.callbacks.put(topic, arrivedCallback);
                waitForCompletion(client.subscribe(topic, MqttQoS.AT_LEAST_ONCE.value()));
                LOGGER.info("Subscribed to " + topic);
                done = true;
            } catch (MqttException e) {
                LOGGER.error("Subscribing to MQTT topic '" + topic + "': " + e.getMessage(), e);
            }
        }
        return done;
    }
    
    /**
     * Unsubscribes from {@code topic} if {@code topic} is not blacklisted by 
     * {@link MqttConfiguration#isFilteredTopic(String)}.
     * 
     * @param topic the topic to unsubscribe from
     * @return {@code true} if done/successful, {@code false} else
     */
    boolean unsubscribeFrom(String topic) {
        boolean done = false;
        if (!configuration.isFilteredTopic(topic) && null != client) {
            try {
                callback.callbacks.remove(topic);
                waitForCompletion(client.unsubscribe(topic));
                LOGGER.info("Unsubscribed from " + topic);
                done = true;
            } catch (MqttException e) {
                LOGGER.error("Unsubscribing from MQTT topic '" + topic + "': " + e.getMessage(), e);
            }
        }
        return done;
    }
    
    /**
     * Sends {@code payload} to {@code topic}.
     * 
     * @param topic the topic to send to
     * @param payload the payload to send
     */
    void send(String topic, byte[] payload) {
        if (null != client) {
            MqttMessage message = new MqttMessage(payload);
            message.setQos(qos.value());
            try {
                client.publish(topic, message);
            } catch (MqttException e) {
                LOGGER.error("Sending MQTT message with topic " + topic + ": " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Waits for completion until the {@code token} is processed.
     * 
     * @param token the token
     * @throws MqttException in case that processing of the token fails
     */
    static void waitForCompletion(IMqttToken token) throws MqttException {
        token.waitForCompletion(configuration.getActionTimeout());
    }
    
}
