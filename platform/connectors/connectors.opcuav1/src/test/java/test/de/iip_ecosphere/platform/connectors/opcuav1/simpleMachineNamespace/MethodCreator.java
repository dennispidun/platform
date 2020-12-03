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

package test.de.iip_ecosphere.platform.connectors.opcuav1.simpleMachineNamespace;

import org.eclipse.milo.opcua.sdk.server.api.methods.AbstractMethodInvocationHandler;
import org.eclipse.milo.opcua.sdk.server.nodes.UaMethodNode;

/**
 * A simplemethod creator.
 * 
 * @param <T> the type of the method handler
 * @author Holger Eichelberger, SSE
 */
public interface MethodCreator <T extends AbstractMethodInvocationHandler> {

    /**
     * Creates a method instance.
     * 
     * @param node the creating method node
     * @return the method instance
     */
    public T create(UaMethodNode node);
    
}
