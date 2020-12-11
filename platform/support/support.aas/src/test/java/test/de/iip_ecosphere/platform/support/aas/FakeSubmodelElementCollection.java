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

package test.de.iip_ecosphere.platform.support.aas;

import java.util.HashMap;
import java.util.Map;

import de.iip_ecosphere.platform.support.aas.AasVisitor;
import de.iip_ecosphere.platform.support.aas.DataElement;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.ReferenceElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;

/**
 * Implements a fake sub-model element collection.
 * 
 * @author Holger Eichelberger, SSE
 */
public class FakeSubmodelElementCollection extends FakeElement implements SubmodelElementCollection {

    private Map<String, SubmodelElement> elements = new HashMap<>();
    
    static class FakeSubmodelElementCollectionBuilder extends FakeSubmodelElementContainerBuilder 
        implements SubmodelElementCollectionBuilder {

        private FakeSubmodelElementContainerBuilder parent;
        private FakeSubmodelElementCollection instance;
        
        /**
         * Creates a builder instance.
         * 
         * @param parent the parent builder
         * @param idShort the short id
         */
        FakeSubmodelElementCollectionBuilder(FakeSubmodelElementContainerBuilder parent, String idShort) {
            this.parent = parent;
            this.instance = new FakeSubmodelElementCollection(idShort);
        }
        
        @Override
        public SubmodelElementCollectionBuilder createSubmodelElementCollectionBuilder(String idShort, boolean ordered,
                boolean allowDuplicates) {
            return new FakeSubmodelElementCollectionBuilder(this, idShort); // fake, forget ordered, allowDuplicates
        }

        @Override
        public Reference createReference() {
            return new FakeReference();
        }

        @Override
        public SubmodelElementCollection build() {
            parent.register(instance);
            return instance;
        }

        @Override
        FakeOperation register(FakeOperation operation) {
            instance.elements.put(operation.getIdShort(), operation);
            return operation;
        }

        @Override
        FakeProperty register(FakeProperty property) {
            instance.elements.put(property.getIdShort(), property);
            return property;
        }

        @Override
        FakeReferenceElement register(FakeReferenceElement reference) {
            instance.elements.put(reference.getIdShort(), reference);
            return reference;
        }

        @Override
        FakeSubmodelElementCollection register(FakeSubmodelElementCollection collection) {
            instance.elements.put(collection.getIdShort(), collection);
            return collection;
        }
        
    }
    
    /**
     * Creates an instance.
     * 
     * @param idShort the id
     */
    protected FakeSubmodelElementCollection(String idShort) {
        super(idShort);
    }

    @Override
    public void accept(AasVisitor visitor) {
        visitor.visitSubmodelElementCollection(this);
        for (SubmodelElement sm : elements.values()) {
            sm.accept(visitor);
        }
        visitor.endSubmodelElementCollection(this);
    }

    @Override
    public Iterable<SubmodelElement> elements() {
        return elements.values();
    }
    
    /**
     * Filters {@code #elements()} for the given short id and type.
     * 
     * @param <T> the element type to return
     * @param idShort the short id
     * @param type the element type
     * @return the found element or <b>null</b>
     */
    private <T extends SubmodelElement> T filter(String idShort, Class<T> type) {
        T result = null;
        for (SubmodelElement sm : elements()) {
            if (sm.getIdShort().equals(idShort) && type.isInstance(sm)) {
                result = type.cast(sm);
            }
        }
        return result;
    }

    @Override
    public DataElement getDataElement(String idShort) {
        return filter(idShort, DataElement.class);
    }

    @Override
    public Property getProperty(String idShort) {
        return filter(idShort, Property.class);
    }

    @Override
    public ReferenceElement getReferenceElement(String idShort) {
        return filter(idShort, ReferenceElement.class);
    }

    @Override
    public SubmodelElement getElement(String idShort) {
        return elements.get(idShort);
    }

    @Override
    public SubmodelElementCollection getSubmodelElementCollection(String idShort) {
        return filter(idShort, SubmodelElementCollection.class);
    }

}