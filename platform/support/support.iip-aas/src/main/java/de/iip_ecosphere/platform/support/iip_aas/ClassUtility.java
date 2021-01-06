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

package de.iip_ecosphere.platform.support.iip_aas;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.SubmodelElement;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.Type;

/**
 * Utility functions for representing types in AAS. A Java type is turned into
 * 
 * <ul>
 *   <li>submodel: types
 *     <ul>
 *       <li>submodel elements collection: <i>Java type name, inner classes as "."</i>
 *       <ul>
 *         <li>Property: {@link #ATTRIBUTE_PREFIX} + attribute1 name, value = type (for primitives, arrays and 
 *             String)</li>
 *         <li>ReferenceElement: attribute1 name, value = ref-to collection (for reference types)</li>
 *       </ul>
 *     </ul>
 *   </li>
 * </ul>
 * 
 * Type usages, e.g., for input/output types are translated to
 * <ul>
 *   <li>Property: attribute1 name, value = type (for primitives, arrays and String)</li>
 *   <li>ReferenceElement: attribute1 name, value = ref-to collection (for reference types)</li>
 * </ul>
 * 
 * Attributes marked by {@link Skip} will not be listed. So far, arrays over reference types are represented as 
 * strings rather than references to the component type.
 * 
 * The implemented format is initial and will change over time (array of ref type unclear, generics).
 * 
 * @author Holger Eichelberger, SSE
 */
public class ClassUtility {

    public static final String NAME_TYPE_SUBMODEL = "types";
    public static final String ATTRIBUTE_PREFIX = "attr_"; //TODO BaSyx 0.1.0-SNAPSHOT fails with "value" etc
    private static final String JVM_NAME = ManagementFactory.getRuntimeMXBean().getName();
    private static final Map<Class<?>, String> NAME_MAPPING = new HashMap<>();
    
    /**
     * Registers a {@code type} by its simple name.
     * 
     * @param type the type
     */
    private static void registerBySimpleName(Class<?> type) {
        NAME_MAPPING.put(type, type.getSimpleName());
    }

    /**
     * Registers a {@code type} by the simple name of {@code nameType}, e.g., to map wrappers to their primitive types.
     * 
     * @param type the type
     * @param nameType the type providing the name
     */
    private static void registerBySimpleName(Class<?> type, Class<?> nameType) {
        NAME_MAPPING.put(type, nameType.getSimpleName());
    }

    static {
        // the primitive types
        registerBySimpleName(Integer.TYPE);
        registerBySimpleName(Long.TYPE);
        registerBySimpleName(Double.TYPE);
        registerBySimpleName(Float.TYPE);
        registerBySimpleName(Boolean.TYPE);
        registerBySimpleName(Short.TYPE);
        registerBySimpleName(Character.TYPE);
        // string as pseudo-primitive
        registerBySimpleName(String.class);
        // the wrappers via their primitive types (boxing/unboxing)
        registerBySimpleName(Integer.class, Integer.TYPE);
        registerBySimpleName(Long.class, Long.TYPE);
        registerBySimpleName(Double.class, Double.TYPE);
        registerBySimpleName(Float.class, Float.TYPE);
        registerBySimpleName(Boolean.class, Boolean.TYPE);
        registerBySimpleName(Short.class, Short.TYPE);
        registerBySimpleName(Character.class, Character.TYPE);
    }

    /**
     * Adds a type to an AAS as sub-model. If the type already exists in the AAS/submodel, no new element will be 
     * created just a reference to it will be returned. [static data]
     * 
     * @param aasBuilder the AAS builder
     * @param type the type to add
     * @return the reference to the sub-model (<b>null</b> if nothing was created)
     */
    public static Reference addType(AasBuilder aasBuilder, Class<?> type) {
        SubmodelBuilder smb = aasBuilder.createSubmodelBuilder(NAME_TYPE_SUBMODEL, null); // create or re-open
        SubmodelElementCollectionBuilder typeCollection = smb.createSubmodelElementCollectionBuilder(
            getName(type), false, false);
        Reference result = addType(typeCollection, type);
        typeCollection.build();
        smb.build(); // ok also in case of re-open
        return result;
    }

    /**
     * Adds a type to a sub-model. [static data]
     * 
     * @param builder the target sub-model
     * @param type the type to add
     * @return the reference to the type (<b>null</b> if nothing was created)
     */
    private static Reference addType(SubmodelElementCollectionBuilder builder, Class<?> type) {
        Reference result;
        if (type.isPrimitive() || type.isArray()) {
            result = null;
        } else {
            if (builder.isNew()) {
                for (Field f: type.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers()) && null == f.getAnnotation(Skip.class)) {
                        addTypeSubModelElement(builder, ATTRIBUTE_PREFIX 
                            + translateToAasName(f.getName()), f.getType());
                    }
                }
                if (Object.class != type.getSuperclass()) {
                    addType(builder, type.getSuperclass());
                }
            }
            result = builder.createReference();
        }
        return result;
    }
    
    /**
     * Adds a type-representing sub-model element. [static data]
     * 
     * @param subModelBuilder the target sub-model
     * @param idShort the name of the element
     * @param type the type to represent in the element
     * @return the created sub-model element
     */
    public static SubmodelElement addTypeSubModelElement(SubmodelElementContainerBuilder subModelBuilder, 
        String idShort, Class<?> type) {
        SubmodelElement result;
        // TODO not modifiable properties
        if (NAME_MAPPING.containsKey(type)) {
            result = subModelBuilder
                .createPropertyBuilder(idShort)
                .setType(Type.STRING)
                .setValue(NAME_MAPPING.get(type))
                .build();
        } else if (type.isArray()) {
            String simpleName = type.getSimpleName();
            String name = NAME_MAPPING.get(type.getComponentType());
            if (null == name) {
                name = simpleName; // TODO preliminary, reference, needed?
            } else {
                int pos = simpleName.indexOf('[');
                name = name + simpleName.substring(pos);
            }
            result = subModelBuilder
                .createPropertyBuilder(idShort)
                .setType(Type.STRING)
                .setValue(name)
                .build();
        } else {
            Reference aRef = addType(subModelBuilder.getAasBuilder(), type);
            result = subModelBuilder
                .createReferenceElementBuilder(idShort)
                .setValue(aRef)
                .build();
        }        
        return result;
    }
    
    /**
     * Translates a Java identifier name to an AAS short name.
     * 
     * @param javaIdentifier the Java identifier
     * @return the AAS short name
     */
    static String translateToAasName(String javaIdentifier) {
        return javaIdentifier.replace(".", "_");
    }
    
    /**
     * Returns the name of the associated model element.
     * 
     * @param type the type
     * @return the name
     */
    public static String getName(Class<?> type) {
        return translateToAasName(type.getName().replace("$", "."));
    }

    /**
     * Turns the {@code} object into a unique id with given (optional) prefix.
     * 
     * @param prefix an optional prefix, use an empty string for none; shall end with a separator, e.g., "_"
     * @param object the object to be turned into a unique id
     * @return the combined id
     */
    public static String getId(String prefix, Object object) {
        return prefix + JVM_NAME + "_" + System.identityHashCode(object);
    }

}
