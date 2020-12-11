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

package de.iip_ecosphere.platform.support.aas;

import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;

/**
 * Represents an AAS sub-model.
 * 
 * @author Holger Eichelberger, SSE
*/
public interface Submodel extends Element, HasSemantics, Identifiable, Qualifiable, HasDataSpecification, HasKind, 
    ElementContainer {

    /**
     * Encapsulated logic to build a sub-model.
     * 
     * @author Holger Eichelberger, SSE
     */
    public interface SubmodelBuilder extends SubmodelElementContainerBuilder {
        
        /**
         * Returns the parent builder.
         * 
         * @return the parent builder
         */
        public AasBuilder getParentBuilder();
        
        /**
         * Creates a reference on the sub-model under construction.
         * 
         * @return the reference
         */
        public Reference createReference();

        /**
         * Builds the instance.
         * 
         * @return the sub-model instance
         */
        public Submodel build();

    }

    /**
     * Returns a data element with the given name.
     * 
     * @param idShort the short id of the data element
     * @return the data element, <b>null</b> for none
     */
    public DataElement getDataElement(String idShort);

    /**
     * Returns a property with the given name.
     * 
     * @param idShort the short id of the property
     * @return the property, <b>null</b> for none
     */
    public Property getProperty(String idShort);

    /**
     * Returns a reference element with the given name.
     * 
     * @param idShort the short id of the reference element
     * @return the property, <b>null</b> for none
     */
    public ReferenceElement getReferenceElement(String idShort);

    /**
     * Returns an operation with the given name and the given number of arguments.
     * 
     * @param idShort the short id of the property
     * @param numArgs the number of arguments regardless whether they are in/out/inout
     * @return the property, <b>null</b> for none
     */
    public Operation getOperation(String idShort, int numArgs);
    
    /**
     * Returns an operation with the given name and the given number of arguments.
     * 
     * @param idShort the short id of the property
     * @param inArgs the number of ingoing arguments/variables
     * @param outArgs the number of outgoing arguments/variables
     * @param inOutArgs the number of in/outgoing arguments/variables
     * @return the property, <b>null</b> for none
     */
    public Operation getOperation(String idShort, int inArgs, int outArgs, int inOutArgs);

    /**
     * Returns a submodel element with the given name.
     * 
     * @param idShort the short id of the property
     * @return the submodel element, <b>null</b> for none
     */
    public SubmodelElement getSubmodelElement(String idShort);

    /**
     * Returns a submodel element collection with the given name.
     * 
     * @param idShort the short id of the property
     * @return the submodel collection element, <b>null</b> for none
     */
    public SubmodelElementCollection getSubmodelElementCollection(String idShort);

}