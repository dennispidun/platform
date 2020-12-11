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

package de.iip_ecosphere.platform.support.aas.basyx;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.basyx.submodel.metamodel.api.submodelelement.ISubmodelElementCollection;

import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.ReferenceElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.basyx.BaSyxElementTranslator.SubmodelElementsRegistrar;
import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.DataElement;
import de.iip_ecosphere.platform.support.aas.Operation.OperationBuilder;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Property.PropertyBuilder;
import de.iip_ecosphere.platform.support.aas.ReferenceElement.ReferenceElementBuilder;

/**
 * Wrapper for the BaSyx sub-model element collection.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxSubmodelElementCollection extends BaSyxSubmodelElement implements SubmodelElementCollection, 
    SubmodelElementsRegistrar {
    
    private ISubmodelElementCollection collection;
    private List<SubmodelElement> elements = new ArrayList<SubmodelElement>();
    
    /**
     * The sub-model element collection builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public static class BaSyxSubmodelElementCollectionBuilder extends BaSyxSubmodelElementContainerBuilder 
        implements SubmodelElementCollectionBuilder {
        
        private BaSyxSubmodelElementContainerBuilder parentBuilder;
        private BaSyxSubmodelElementCollection instance;
        private org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElementCollection collection;
        
        /**
         * Creates a sub-model element collection builder. The parent builder must be set by the calling
         * constructor.
         * 
         * @param parentBuilder the parent builder
         * @param idShort the short name of the sub-model element
         * @param ordered whether the collection is ordered
         * @param allowDuplicates whether the collection allows duplicates
         */
        BaSyxSubmodelElementCollectionBuilder(BaSyxSubmodelElementContainerBuilder parentBuilder, 
            String idShort, boolean ordered, boolean allowDuplicates) {
            this.parentBuilder = parentBuilder;
            this.instance = new BaSyxSubmodelElementCollection();
            this.collection = new org.eclipse.basyx.submodel.metamodel.map.submodelelement.SubmodelElementCollection();
            this.collection.setIdShort(idShort);
            this.collection.setOrdered(ordered);
            this.collection.setOrdered(allowDuplicates);
        }

        @Override
        public PropertyBuilder createPropertyBuilder(String idShort) {
            return new BaSyxProperty.BaSyxPropertyBuilder(this, idShort);
        }

        @Override
        public ReferenceElementBuilder createReferenceElementBuilder(String idShort) {
            return new BaSyxReferenceElement.BaSyxReferenceElementBuilder(this, idShort);
        }
        
        @Override
        public OperationBuilder createOperationBuilder(String idShort) {
            return new BaSyxOperation.BaSxyOperationBuilder(this, idShort);
        }

        @Override
        public SubmodelElementCollectionBuilder createSubmodelElementCollectionBuilder(String idShort, boolean ordered, 
            boolean allowDuplicates) {
            return new BaSyxSubmodelElementCollectionBuilder(this, idShort, ordered, allowDuplicates);
        }

        @Override
        public Reference createReference() {
            return new BaSyxReference(collection.getReference());
        }

        @Override
        BaSyxOperation register(BaSyxOperation operation) {
            this.collection.addElement(operation.getSubmodelElement());
            return instance.register(operation);
        }
        
        @Override
        BaSyxProperty register(BaSyxProperty property) {
            this.collection.addElement(property.getSubmodelElement());
            return instance.register(property);
        }

        @Override
        BaSyxReferenceElement register(BaSyxReferenceElement reference) {
            this.collection.addElement(reference.getSubmodelElement());
            return instance.register(reference);
        }

        @Override
        BaSyxSubmodelElementCollection register(BaSyxSubmodelElementCollection collection) {
            this.collection.addElement(collection.getSubmodelElement());
            return instance.register(collection);
        }

        @Override
        public BaSyxSubmodelElementCollection build() {
            instance.collection = collection;
            return parentBuilder.register(instance);
        }
        
    }
    
    /**
     * Creates an instance. Prevents external creation.
     */
    private BaSyxSubmodelElementCollection() {
    }
 
    /**
     * Creates an instance and sets the BaSyx instance directly.
     * @param collection
     */
    BaSyxSubmodelElementCollection(ISubmodelElementCollection collection) {
        this.collection = collection;
        BaSyxElementTranslator.registerDataElements(collection.getDataElements(), this);
        BaSyxElementTranslator.registerOperations(collection.getOperations(), this);
        BaSyxElementTranslator.registerRemainingSubmodelElements(collection.getSubmodelElements(), this);        
    }
    
    /**
     * Returns the number of elements.
     * 
     * @return the number of elements
     */
    public int getSize() {
        return elements.size();
    }
    
    @Override
    public Iterable<SubmodelElement> elements() {
        return elements();
    }

    @Override
    public String getIdShort() {
        return collection.getIdShort();
    }

    @Override
    ISubmodelElementCollection getSubmodelElement() {
        return collection;
    }

    @Override
    public DataElement getDataElement(String idShort) {
        return getElement(idShort, DataElement.class);
    }

    @Override
    public Property getProperty(String idShort) {
        return getElement(idShort, Property.class);
    }

    @Override
    public ReferenceElement getReferenceElement(String idShort) {
        return getElement(idShort, ReferenceElement.class);
    }

    @Override
    public SubmodelElement getElement(String idShort) {
        // looping may not be efficient, let's see
        SubmodelElement found = null;
        for (SubmodelElement se : elements) {
            if (se.getIdShort().equals(idShort)) {
                found = se;
                break;
            }
        }
        return found;
    }

    /**
     * {@link #getElement(String) combined with a type filter.
     * 
     * @param <T> the type
     * @param idShort the short id to search for
     * @param type the class representing the type
     * @return the element with given type or <b>null</b> for none
     */
    private <T extends SubmodelElement> T getElement(String idShort, Class<T> type) {
        T result = null;
        SubmodelElement tmp = getElement(idShort);
        if (type.isInstance(tmp)) {
            result = type.cast(tmp);
        }
        return result;
    }

    @Override
    public SubmodelElementCollection getSubmodelElementCollection(String idShort) {
        return getElement(idShort, SubmodelElementCollection.class);
    }

    @Override
    public BaSyxProperty register(BaSyxProperty property) {
        elements.add(property);
        return property;
    }

    @Override
    public BaSyxOperation register(BaSyxOperation operation) {
        elements.add(operation);
        return operation;
    }

    @Override
    public BaSyxReferenceElement register(BaSyxReferenceElement reference) {
        elements.add(reference);
        return reference;
    }

    @Override
    public BaSyxSubmodelElementCollection register(BaSyxSubmodelElementCollection collection) {
        elements.add(collection);
        return collection;
    }

    @Override
    public void accept(AasVisitor visitor) {
        visitor.visitSubmodelElementCollection(this);
        for (SubmodelElement se : elements) {
            se.accept(visitor);
        }
        visitor.endSubmodelElementCollection(this);
    }

}