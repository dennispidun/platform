/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.connectors.opcuav1;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.processing.Generated;

import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.connectors.model.ModelAccess;
import de.iip_ecosphere.platform.connectors.opcuav1.DataItem;
import de.iip_ecosphere.platform.connectors.opcuav1.OpcUaConnector;
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorInputTypeTranslator;
import de.iip_ecosphere.platform.connectors.types.AbstractConnectorOutputTypeTranslator;
import de.iip_ecosphere.platform.connectors.types.TranslatingProtocolAdapter;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;
//import test.de.iip_ecosphere.platform.connectors.ConnectorTest;
import test.de.iip_ecosphere.platform.connectors.opcuav1.simpleMachineNamespace.Namespace;
import test.de.iip_ecosphere.platform.connectors.opcuav1.simpleMachineNamespace.VendorStruct;

/**
 * An abstract test setup for the {@code simpleMachineNamespace}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AbstractOpcUaConnectorTest {

    public static final String VENDOR_NAME2 = "PhoenixContact";
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractOpcUaConnectorTest.class);
    private static ServerSetup setup;

    /**
     * Defines the setup instance.
     * 
     * @param instance the setup instance
     */
    protected static void setSetup(ServerSetup instance) {
        setup = instance;
    }
    
    /**
     * Returns the setup instance.
     * 
     * @return the setup instance
     */
    protected static ServerSetup getSetup() {
        return setup;
    }
    
    /**
     * Blocks until a certain number of (accumulated) receptions is reached or fails after 4s.
     * 
     * @param count the counter
     * @param receptions the xecpected number of receptions 
     */
    private void block(AtomicInteger count, int receptions) {
        int max = 20; // longer than polling interval in params, 30 may be required depending on machine speed
        while (count.get() < receptions && max > 0) {
            Utils.sleep(200);
            max--;
        }
        Assert.assertTrue("Operation took too long", max > 0);
    }
    
    @Generated("Defined in IVML, generated by EASy")
    private static class MachineData {
        private int lotSize;
        private double powerConsumption;
        private String vendor; // usually would go into an own class/instance
        
        /**
         * Creates a machine data object.
         * 
         * @param lotSize the lot size
         * @param powerConsumption the power consumption
         * @param vendor the vendor name
         */
        private MachineData(int lotSize, double powerConsumption, String vendor) {
            this.lotSize = lotSize;
            this.powerConsumption = powerConsumption;
            this.vendor = vendor;
        }
    }
    
    @Generated("Defined in IVML, generated by EASy")
    private static class MachineCommand {
        
        private boolean start;
        private boolean stop;
        private int lotSize;
        
    }
    
    @Generated("Specified in IVML, generated by EASy")
    private static class OutputTranslator extends AbstractConnectorOutputTypeTranslator<DataItem<Variant>, MachineData, 
        Variant> {
        
        private boolean withNotifications;
        
        /**
         * Creates instance.
         * 
         * @param withNotifications operate with/without notifications (for testing)
         */
        private OutputTranslator(boolean withNotifications) {
            this.withNotifications = withNotifications;
        }

        @Override
        public MachineData to(DataItem<Variant> source) throws IOException {
            // do not compare, we assume here that the server cares for that for simplicity here
            // source may contain information about what item changed if this helps for incremental changes (only if
            // setDetailNotifiedItem is enabled in initializeModelAccess below)
            ModelAccess<Variant> access = getModelAccess();
            VendorStruct vendor = access.getStruct(Namespace.QNAME_VAR_STRUCT, VendorStruct.class);
            return new MachineData(
                access.toInt(access.get(Namespace.QNAME_VAR_LOT_SIZE)), 
                access.toDouble(access.get(Namespace.QNAME_VAR_POWER_CONSUMPTION)),
                vendor.getVendor());
        }

        @Override
        public void initializeModelAccess() throws IOException {
            ModelAccess<Variant> access = getModelAccess();
            access.useNotifications(withNotifications);
            if (withNotifications) { // for testing
                // access.setDetailNotifiedItem(true); may be set here, then source in "to" above will receive values 
                access.monitor(Namespace.QNAME_VAR_LOT_SIZE, Namespace.QNAME_VAR_POWER_CONSUMPTION);
            }
            access.registerCustomType(VendorStruct.class);
        }

    }

    @Generated("Specified in IVML, generated by EASy")
    private static class InputTranslator extends AbstractConnectorInputTypeTranslator<MachineCommand, Object, Variant> {

        @Override
        public Object from(MachineCommand data) throws IOException {
            // generated code with "semantic" from configuration model
            // will probably not look like this; simplification for test
            if (data.start) {
                getModelAccess().call(Namespace.QNAME_METHOD_START);
            }
            if (data.stop) {
                getModelAccess().setStruct(Namespace.QNAME_VAR_STRUCT, 
                    new VendorStruct(Namespace.VENDOR_NAME, 2020, true));
                getModelAccess().call(Namespace.QNAME_METHOD_END);
            }
            if (data.lotSize > 0) {
                getModelAccess().set(Namespace.QNAME_VAR_LOT_SIZE, getModelAccess().fromInt(data.lotSize));
                getModelAccess().setStruct(Namespace.QNAME_VAR_STRUCT, 
                    new VendorStruct(VENDOR_NAME2, 2020, true));
            }
            return null; // irrelevant
        }
        
    }
    
    /**
     * Tests the connector.
     * 
     * @param withNotifications operate with/without notifications (for testing)
     * @throws IOException in case that creating the connector fails
     */
    public void testConnector(boolean withNotifications) throws IOException {
        AtomicReference<MachineData> md = new AtomicReference<MachineData>();
        AtomicInteger count = new AtomicInteger(0);
        
        OpcUaConnector<MachineData, MachineCommand> connector = new OpcUaConnector<>(
            new TranslatingProtocolAdapter<DataItem<Variant>, Object, MachineData, MachineCommand, Variant>(
                 new OutputTranslator(withNotifications), new InputTranslator()));
        //ConnectorTest.assertInstance(connector, false);
        Assert.assertTrue(connector.getName().length() > 0);
        connector.setReceptionCallback(new ReceptionCallback<MachineData>() {
            
            @Override
            public void received(MachineData data) {
                md.set(data);
                count.incrementAndGet();
            }
            
            @Override
            public Class<MachineData> getType() {
                return MachineData.class;
            }
        });
        connector.connect(setup.getConnectorParameter());
        //ConnectorTest.assertInstance(connector, true);
        LOGGER.info("OPC connector started");

        block(count, 2); // init changes powConsumption and lotSize
        
        // TODO with struct, namespace setup
        MachineData tmp = md.get();
        Assert.assertNotNull("We shall have received some data although the machine is not running", tmp);
        Assert.assertEquals(1, tmp.lotSize);
        Assert.assertTrue(tmp.powerConsumption < 1);
        Assert.assertEquals(Namespace.VENDOR_NAME, tmp.vendor);
        
        // try starting the machine
        MachineCommand cmd = new MachineCommand();
        cmd.start = true;
        connector.write(cmd);
        
        block(count, 3); // cmd changes powConsuption
        
        tmp = md.get();
        Assert.assertEquals(1, tmp.lotSize);
        Assert.assertTrue(tmp.powerConsumption > 5);
        Assert.assertEquals(Namespace.VENDOR_NAME, tmp.vendor);
        
        cmd = new MachineCommand();
        cmd.lotSize = 5;
        connector.write(cmd);

        block(count, 4); // cmd changes lotSize

        tmp = md.get();
        Assert.assertEquals(5, tmp.lotSize);
        Assert.assertTrue(tmp.powerConsumption > 5);
        Assert.assertEquals(VENDOR_NAME2, tmp.vendor);

        cmd = new MachineCommand();
        cmd.stop = true;
        connector.write(cmd);

        block(count, 6); // cmd changes powConsuption and lot size

        tmp = md.get();
        Assert.assertEquals(1, tmp.lotSize);
        Assert.assertTrue(tmp.powerConsumption < 1);
        Assert.assertEquals(Namespace.VENDOR_NAME, tmp.vendor);

        //ConnectorTest.assertInstance(connector, true);
        connector.disconnect();
        //ConnectorTest.assertInstance(connector, false);
        LOGGER.info("OPC connector disconnected");
    }
    
}
