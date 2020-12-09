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

import org.eclipse.basyx.submodel.metamodel.api.ISubModel;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.dataelement.IDataElement;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.dataelement.IProperty;
import org.eclipse.basyx.submodel.metamodel.api.submodelelement.operation.IOperation;

/**
 * Represents a generic sub-model just given in terms of the BaSyx interface.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxISubModel extends AbstractSubModel<ISubModel> {

    /**
     * Creates submodel instance.
     * 
     * @param subModel the instance
     */
    public BaSyxISubModel(ISubModel subModel) {
        super(subModel);
        for (IDataElement elt : subModel.getDataElements().values()) {
            if (elt instanceof IProperty) {
                register(new BaSyxProperty((IProperty) elt));
            } // TODO else
        }
        for (IOperation op : subModel.getOperations().values()) {
            register(new BaSyxOperation(op));
        }
    }

}