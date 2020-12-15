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

package de.iip_ecosphere.platform.connectors;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.iip_aas.AasContributor;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.ClassUtility;

/**
 * Implements the transport AAS contributor. Do not rename, this class is referenced in {@code META-INF/services}.
 * The following structure is built up:
 * <ul>
 *   <li>submodel: installedConnectors
 *     <ul>
 *       <li>submodel element connection = <i>connector class name</i> (not descriptor class name!)
 *         <ul>
 *           <li>name = <i>connector name</i></li>
 *           <li>supportsEvents = <i>true or false</i></li>
 *           <li>hasModel = <i>true or false</i></li>
 *           <li>supportsQualifiedNames = <i>true or false</i></li>
 *           <li>supportsProperties = <i>true or false</i></li>
 *           <li>supportsCalls = <i>true or false</i></li>
 *           <li>supportsStructs = <i>true or false</i></li>
 *         </ul>
 *       </li>
 *     </ul>
 *   </li>
 *   <li>submodel: activeConnectors
 *     <ul>
 *       <li>submodel element connection = <i>unique id of running connector</i>
 *         <ul>
 *           <li>name = <i>connector name</i></li>
 *           <li>property or referenceElement: inType = <i>string or ref to types submodel</i>
 *             (see {@link ClassUtility})</li>
 *           <li>property or referenceElement: outType = <i>string or ref to types submodel</i>
 *             (see {@link ClassUtility})</li>
 *           <li>property: active = <i>true or false</i></li>
 *           <li>referenceElement: descriptor = <i>ref to respective submodel element collection in 
 *             installedConnectors</i> (present only if the descriptor is in the target submodel)</li>
 *         </ul>
 *       </li>
 *     </ul>
 *   </li>
 * </ul>
 *  
 * @author Holger Eichelberger, SSE
 */
public class ConnectorsAas implements AasContributor {

    public static final String NAME_DESCRIPTORS_SUBMODEL = "installedConnectors";
    public static final String NAME_DESC_VAR_NAME = "name";
    public static final String NAME_DESC_VAR_SUPPORTS_EVENTS = "supportsEvents";
    public static final String NAME_DESC_VAR_HAS_MODEL = "hasModel";
    public static final String NAME_DESC_VAR_SUPPORTS_QNAMES = "supportsQualifiedNames";
    public static final String NAME_DESC_VAR_SUPPORTS_PROPERTIES = "supportsProperties";
    public static final String NAME_DESC_VAR_SUPPORTS_CALLS = "supportsCalls";
    public static final String NAME_DESC_VAR_SUPPORTS_STRUCTS = "supportsStructs"; 
    
    public static final String NAME_CONNECTORS_SUBMODEL = "activeConnectors";
    public static final String NAME_SMC_CONNECTOR_PREFIX = "connector_";
    public static final String NAME_SMC_VAR_CONNECTOR = "name";
    public static final String NAME_SMC_VAR_OUT = "outType";
    public static final String NAME_SMC_VAR_IN = "inType";
    public static final String NAME_SMC_VAR_ACTIVE = "active";
    public static final String NAME_SMC_VAR_DESCRIPTOR = "descriptor";

    /**
     * Defines whether changing the {@link #NAME_SMC_VAR_ACTIVE} property is permitted. So far, this does not work, 
     * either due to a bug in the abstraction or in BaSyx. May be, the property is somehow not allowed for writing
     * and the error message is weird.
     */
    public static final boolean ENABLE_ACTIVE_WRITING = false; // TODO check and fix, remove then

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorsAas.class);

    @Override
    public Aas contributeTo(AasBuilder aasBuilder) {
        // BaSyx: shall not be here, but there seems to be a problem creating a SubModel after first deployment
        SubmodelBuilder tsmB = aasBuilder.createSubmodelBuilder(ClassUtility.NAME_TYPE_SUBMODEL);
        tsmB.build();
        
        SubmodelBuilder ismB = aasBuilder.createSubmodelBuilder(NAME_DESCRIPTORS_SUBMODEL);
        Iterator<ConnectorDescriptor> iter = ConnectorRegistry.getRegisteredConnectorDescriptors();
        while (iter.hasNext()) {
            ConnectorDescriptor desc = iter.next();
            Class<?> cls = desc.getClass();
            SubmodelElementCollectionBuilder secB = ismB.createSubmodelElementCollectionBuilder(
                ClassUtility.getName(desc.getType()), false, false);
            secB.createPropertyBuilder(NAME_DESC_VAR_NAME)
                .setValue(Type.STRING, desc.getName())
                .build();
            addAnnotationInformation(secB, cls);
            secB.build();
        }
        Submodel descriptors = ismB.build();
        
        SubmodelBuilder csmB = aasBuilder.createSubmodelBuilder(NAME_CONNECTORS_SUBMODEL);
        Iterator<Connector<?, ?, ?, ?>> iterC = ConnectorRegistry.getRegisteredConnectorInstances();
        while (iterC.hasNext()) {
            Connector<?, ?, ?, ?> connector = iterC.next();
            String idShort = ClassUtility.getId(NAME_SMC_CONNECTOR_PREFIX, connector);
            SubmodelElementCollectionBuilder smcb = csmB.createSubmodelElementCollectionBuilder(idShort, 
                false, false);
            addConnector(smcb, connector, descriptors);
            smcb.build();
        }
        csmB.build();
        
        return null;
    }

    /**
     * Called to notify that a connector instance is about to be discarded/removed.
     * 
     * @param connector the connector instance
     */
    static void notifyRemoveConnector(Connector<?, ?, ?, ?> connector) {
        // TODO run threaded locked on this model
        try {
            Aas aas = AasPartRegistry.retrieveIipAas();
            if (null != aas) {
                Submodel submodel = aas.getSubmodel(NAME_CONNECTORS_SUBMODEL);
                if (null != submodel) {
                    String idShort = ClassUtility.getId(NAME_SMC_CONNECTOR_PREFIX, connector);
                    SubmodelElementCollection coll = submodel.getSubmodelElementCollection(idShort);
                    if (null != coll) {
                        setPropertyValue(coll, NAME_SMC_VAR_ACTIVE, false);
                    } else {
                        LOGGER.error("No element collection for connector: " + NAME_CONNECTORS_SUBMODEL 
                            + "/" + idShort);
                    }
                } else {
                    LOGGER.error("No submodel: " + NAME_CONNECTORS_SUBMODEL);
                }
            }
        } catch (IOException e) {
            LOGGER.error("While retrieving the IIP-Ecosphere AAS: " + e.getMessage(), e);
        } catch (ExecutionException e) {
            LOGGER.error("While retrieving the IIP-Ecosphere AAS: " + e.getMessage(), e);
        }
    }
    
    /**
     * Changes a property value on a (deployed) AAS.
     * 
     * @param coll the sub-model element collection
     * @param idShort the short id
     * @param value the new value
     * @throws ExecutionException in case that writing fails for some reason
     */
    private static void setPropertyValue(SubmodelElementCollection coll, String idShort, Object value) 
        throws ExecutionException {
        Property prop = coll.getProperty(idShort);
        if (null != prop) {
            if (ENABLE_ACTIVE_WRITING) {
                prop.setValue(value);  
            } else {
                prop.getValue(); // useless, but works anyway
            }
        } else {
            LOGGER.error("No property: " + NAME_CONNECTORS_SUBMODEL + "/" + coll.getIdShort() 
                + "/" + NAME_SMC_VAR_ACTIVE);
        }
    }

    /**
     * Called to notify that a connector instance is about to be connected.
     * 
     * @param connector the connector instance
     */
    static void notifyAddConnector(Connector<?, ?, ?, ?> connector) {
        // TODO run threaded locked on this model
        try {
            Aas aas = AasPartRegistry.retrieveIipAas();
            if (null != aas) {
                Submodel descriptors = aas.getSubmodel(NAME_DESCRIPTORS_SUBMODEL);
                Submodel submodel = aas.getSubmodel(NAME_CONNECTORS_SUBMODEL);
                if (null != submodel && null != descriptors) {
                    String idShort = ClassUtility.getId(NAME_SMC_CONNECTOR_PREFIX, connector);
                    SubmodelElementCollectionBuilder smcb = submodel.addSubmodelElementCollection(
                        idShort, false, false);
                    addConnector(smcb, connector, descriptors);
                    smcb.build();
                } else {
                    LOGGER.error("No submodel: " + NAME_CONNECTORS_SUBMODEL);
                }
            }
        } catch (IOException e) {
            LOGGER.error("While retrieving the IIP-Ecosphere AAS: " + e.getMessage());
        }
    }
    
    /**
     * Adds a connector to a known {@code submodelBuiler}.
     * 
     * @param smcb the sub-model element collection builder to add the connector to
     * @param connector the connector instance
     * @param descriptors the descriptors sub-model
     */
    private static void addConnector(SubmodelElementCollectionBuilder smcb, Connector<?, ?, ?, ?> connector, 
        Submodel descriptors) {
        
        smcb.createPropertyBuilder(NAME_SMC_VAR_CONNECTOR)
            .setValue(Type.STRING, connector.getName())
            .build();
        ClassUtility.addTypeSubModelElement(smcb, NAME_SMC_VAR_OUT, connector.getConnectorOutputType());
        ClassUtility.addTypeSubModelElement(smcb, NAME_SMC_VAR_IN, connector.getConnectorInputType());
        smcb.createPropertyBuilder(NAME_SMC_VAR_ACTIVE)
            .setValue(Type.BOOLEAN, true)
            .build();

        String descName = ClassUtility.getName(connector.getClass());
        SubmodelElementCollection descC = descriptors.getSubmodelElementCollection(descName);
        if (null != descC) {
            smcb.createReferenceElementBuilder(NAME_SMC_VAR_DESCRIPTOR).setValue(descC.createReference()).build();
        } else {
            LOGGER.warn("Warning while adding connector instance: Descriptor for " + descName + " does not exist.");
        }
    }
    
    /**
     * We use this class only for reading out the default values of an annotation if none is present on a connector.
     * 
     * @author Holger Eichelberger, SSE
     */
    @MachineConnector
    private static class DefaultAnnotationProvider {
    }
    
    /**
     * Returns the machine annotation for {@code cls}.
     * 
     * @param cls the class to return the machine annotation for
     * @return the machine annotation, the one from {@link DefaultAnnotationProvider} if none is present
     */
    public static MachineConnector getMachineConnectorAnnotation(Class<?> cls) {
        MachineConnector conn = cls.getAnnotation(MachineConnector.class);
        if (null == conn) {
            conn = DefaultAnnotationProvider.class.getAnnotation(MachineConnector.class);
        }
        return conn;
    }

    /**
     * Adds information from {@link MachineConnector} annotated to {@code cls} to the given sub-model element 
     * collection {@code smbc}.
     * 
     * @param smcb the sub-model element collection builder
     * @param cls the class to read the information from; if no annotation is present, the default annotation
     *   information from {@link DefaultAnnotationProvider} will be used instead
     */
    private static void addAnnotationInformation(SubmodelElementCollectionBuilder smcb, Class<?> cls) {
        MachineConnector conn = getMachineConnectorAnnotation(cls);
        
        smcb.createPropertyBuilder(NAME_DESC_VAR_SUPPORTS_EVENTS)
            .setValue(Type.BOOLEAN, conn.supportsEvents())
            .build();
        smcb.createPropertyBuilder(NAME_DESC_VAR_HAS_MODEL)
            .setValue(Type.BOOLEAN, conn.hasModel())
            .build();
        smcb.createPropertyBuilder(NAME_DESC_VAR_SUPPORTS_QNAMES)
            .setValue(Type.BOOLEAN, conn.supportsHierarchicalQNames())
            .build();
        smcb.createPropertyBuilder(NAME_DESC_VAR_SUPPORTS_PROPERTIES)
            .setValue(Type.BOOLEAN, conn.supportsModelProperties())
            .build();
        smcb.createPropertyBuilder(NAME_DESC_VAR_SUPPORTS_CALLS)
            .setValue(Type.BOOLEAN, conn.supportsModelCalls())
            .build();
        smcb.createPropertyBuilder(NAME_DESC_VAR_SUPPORTS_STRUCTS)
            .setValue(Type.BOOLEAN, conn.supportsModelStructs())
            .build();
    }

}
