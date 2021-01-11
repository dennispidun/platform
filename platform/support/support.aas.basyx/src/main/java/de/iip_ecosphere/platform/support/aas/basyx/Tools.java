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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.basyx.aas.metamodel.map.descriptor.CustomId;
import org.eclipse.basyx.aas.metamodel.map.descriptor.ModelUrn;
import org.eclipse.basyx.submodel.metamodel.api.identifier.IIdentifier;
import org.eclipse.basyx.submodel.metamodel.map.submodelelement.dataelement.property.valuetypedef.PropertyValueTypeDef;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.aas.AssetKind;
import de.iip_ecosphere.platform.support.aas.Type;

/**
 * Some utilities, such as for parameter checking. Public for testing.
 * 
 * @author Holger Eichelberger, SSE
 */
public class Tools {

    private static final Map<Type, PropertyValueTypeDef> TYPES2BASYX = new HashMap<>();
    private static final Map<PropertyValueTypeDef, Type> BASYX2TYPES = new HashMap<>();

    private static final Map<AssetKind, org.eclipse.basyx.aas.metamodel.api.parts.asset.AssetKind> ASSETKINDS2BASYX 
        = new HashMap<>();
    private static final Map<org.eclipse.basyx.aas.metamodel.api.parts.asset.AssetKind, AssetKind> BASYX2ASSETKINDS 
        = new HashMap<>();

    static {
        mapType(Type.BOOLEAN, PropertyValueTypeDef.Boolean);
        mapType(Type.DOUBLE, PropertyValueTypeDef.Double);
        mapType(Type.FLOAT, PropertyValueTypeDef.Float);
        mapType(Type.INTEGER, PropertyValueTypeDef.Integer);
        mapType(Type.STRING, PropertyValueTypeDef.String);

        mapType(Type.NON_POSITIVE_INTEGER, PropertyValueTypeDef.NonPositiveInteger);
        mapType(Type.NON_NEGATIVE_INTEGER, PropertyValueTypeDef.NonNegativeInteger);
        mapType(Type.POSITIVE_INTEGER, PropertyValueTypeDef.PositiveInteger);
        mapType(Type.NEGATIVE_INTEGER, PropertyValueTypeDef.NegativeInteger);
        
        mapType(Type.INT8, PropertyValueTypeDef.Int8);
        mapType(Type.INT16, PropertyValueTypeDef.Int16);
        mapType(Type.INT32, PropertyValueTypeDef.Int32);
        mapType(Type.INT64, PropertyValueTypeDef.Int64);
        
        mapType(Type.UINT8, PropertyValueTypeDef.UInt8);
        mapType(Type.UINT16, PropertyValueTypeDef.UInt16);
        mapType(Type.UINT32, PropertyValueTypeDef.UInt32);
        mapType(Type.UINT64, PropertyValueTypeDef.UInt64);
        
        mapType(Type.LANG_STRING, PropertyValueTypeDef.LangString);
        mapType(Type.ANY_URI, PropertyValueTypeDef.AnyURI);
        mapType(Type.BASE64_BINARY, PropertyValueTypeDef.Base64Binary);
        mapType(Type.HEX_BINARY, PropertyValueTypeDef.HexBinary);
        mapType(Type.NOTATION, PropertyValueTypeDef.NOTATION);
        mapType(Type.ENTITY, PropertyValueTypeDef.ENTITY);
        mapType(Type.ID, PropertyValueTypeDef.ID);
        mapType(Type.IDREF, PropertyValueTypeDef.IDREF);
        
        mapType(Type.DURATION, PropertyValueTypeDef.Duration);
        mapType(Type.DAY_TIME_DURATION, PropertyValueTypeDef.DayTimeDuration); 
        mapType(Type.YEAR_MONTH_DURATION, PropertyValueTypeDef.YearMonthDuration);
        mapType(Type.DATE_TIME, PropertyValueTypeDef.DateTime);
        mapType(Type.DATE_TIME_STAMP, PropertyValueTypeDef.DateTimeStamp);
        mapType(Type.G_DAY, PropertyValueTypeDef.GDay);
        mapType(Type.G_MONTH, PropertyValueTypeDef.GMonth); 
        mapType(Type.G_MONTH_DAY, PropertyValueTypeDef.GMonthDay); 
        mapType(Type.G_YEAR, PropertyValueTypeDef.GYear);
        mapType(Type.G_YEAR_MONTH, PropertyValueTypeDef.GYearMonth);
        mapType(Type.Q_NAME, PropertyValueTypeDef.QName);
        mapType(Type.NONE, PropertyValueTypeDef.None);
        
        mapType(Type.ANY_TYPE, PropertyValueTypeDef.AnyType); 
        mapType(Type.ANY_SIMPLE_TYPE, PropertyValueTypeDef.AnySimpleType);
    }
    
    static {
        mapKind(AssetKind.TYPE, org.eclipse.basyx.aas.metamodel.api.parts.asset.AssetKind.TYPE);
        mapKind(AssetKind.INSTANCE, org.eclipse.basyx.aas.metamodel.api.parts.asset.AssetKind.INSTANCE);
    }
    
    /**
     * Maps an implementation-independent type into a BaSyx property-value type.
     * 
     * @param type the implementation-independent type
     * @param basyxType the corresponding BaSyx property-value type
     */
    private static void mapType(Type type, PropertyValueTypeDef basyxType) {
        TYPES2BASYX.put(type, basyxType);
        BASYX2TYPES.put(basyxType, type);
    }

    /**
     * Maps an implementation-independent asset kind into a BaSyx asset kind.
     * 
     * @param kind the implementation-independent asset kind
     * @param basyxKind the corresponding BaSyx asset kind
     */
    private static void mapKind(AssetKind kind, org.eclipse.basyx.aas.metamodel.api.parts.asset.AssetKind basyxKind) {
        ASSETKINDS2BASYX.put(kind, basyxKind);
        BASYX2ASSETKINDS.put(basyxKind, kind);
    }

    /**
     * Checks a given URN for not being empty.
     * 
     * @param urn the URN
     * @return {@code urn}
     * @throws IllegalArgumentException if the urn is empty or <b>null</b>
     */
    public static String checkUrn(String urn) {
        if (null == urn || 0 == urn.length()) {
            throw new IllegalArgumentException("urn must be given");
        }
        return urn;
    }

    /**
     * Checks a given short id for not being empty.
     * 
     * @param idShort the short id
     * @return {@code idShort}
     * @throws IllegalArgumentException if the id is empty or <b>null</b>
     */
    public static String checkId(String idShort) {
        if (null == idShort || 0 == idShort.length()) {
            throw new IllegalArgumentException("idShort must be given");
        }
        
        //https://wiki.eclipse.org/BaSyx_/_Documentation_/_AssetAdministrationShell
        //Property idShort shall only feature letters, digits, underscore ("_"); starting mandatory with a letter. 
        //Property idShort shall be matched case-insensitive. 
        if (!idShort.matches("[a-zA-Z][a-zA-Z0-9_]+")) {
            throw new IllegalArgumentException("idShort '" + idShort + "' shall only feature letters, digits, "
                + "underscore (\"_\"); starting mandatory with a letter.");
        }
        if (idShort.equals("value") || idShort.equals("invocationList")) { 
            throw new IllegalArgumentException("idShort shall not be \"value\"");
        }
        return idShort;
    }

    /**
     * Translates a implementation-independent type to an implementation-specific type.
     * 
     * @param type the implementation-independent type
     * @return the implementation-specific type
     */
    public static PropertyValueTypeDef translate(Type type) {
        return TYPES2BASYX.get(type);
    }

    /**
     * Translates a implementation-specific type to an implementation-independent type.
     * 
     * @param type the implementation-specific type
     * @return the implementation-independent type
     */
    public static Type translate(PropertyValueTypeDef type) {
        return BASYX2TYPES.get(type);
    }

    /**
     * Translates a implementation-independent asset kind to an implementation-specific asset kind.
     * 
     * @param kind the implementation-independent asset kind
     * @return the implementation-specific asset kind
     */
    public static org.eclipse.basyx.aas.metamodel.api.parts.asset.AssetKind translate(AssetKind kind) {
        return ASSETKINDS2BASYX.get(kind);
    }

    /**
     * Translates a implementation-specific asset kind to an implementation-independent asset kind.
     * 
     * @param kind the implementation-specific asset kind
     * @return the implementation-independent asset kind
     */
    public static AssetKind translate(org.eclipse.basyx.aas.metamodel.api.parts.asset.AssetKind kind) {
        return BASYX2ASSETKINDS.get(kind);
    }

    /**
     * Turns an id into a URL path.
     * 
     * @param id the id
     * @return the URL path
     */
    static String idToUrlPath(String id) {
        return id; // to allow for translations, whitespaces, whatever
    }

    /**
     * Translates an identifier.
     * 
     * @param id the id showing some form of type, e.g., prefix "urn:", may be empty or <b>null</b> leading to 
     *   a custom identifier based on {@code dfltCustom}
     * @param dfltCustom the default value if id cannot be used
     * @return the identifier
     */
    public static IIdentifier translateIdentifier(String id, String dfltCustom) {
        IIdentifier result;
        if (null == id || id.length() == 0) {
            result = new CustomId(dfltCustom);
        } else if (id.startsWith("urn:")) {
            result = new ModelUrn(id);
        } else {
            result = new CustomId(id);
        } // IRI, others?
        return result;
    }
    
    /**
     * Tests the values in {@code pptions} against the constants in {@code cls} and returns 
     * a matching constant or {@code dflt}.
     * 
     * @param <E> the enum type
     * @param options the options to check
     * @param dflt the default value
     * @param cls the enum class providing the constants
     * @return the matching option or {@code dflt}
     */
    public static <E extends Enum<E>> E getOption(String[] options, E dflt, Class<E> cls) {
        E result = dflt;
        for (String o : options) {
            try {
                result = Enum.valueOf(cls, o);
            } catch (IllegalArgumentException e) {
                // ignore, not that options
            }
        }
        return result;
    }
    
    /**
     * Tries to dispose a Tomcat working directory.
     * 
     * @param baseDir the basic directory where the working directory is located in, may be <b>null</b> for default,
     *   i.e., program home directory
     * @param port the port number of the disposed Tomcat instance
     */
    static void disposeTomcatWorkingDir(File baseDir, int port) {
        if (null == baseDir) {
            baseDir = new File(".");
        }
        File workDir = new File(baseDir, "tomcat." + port);
        if (workDir.exists()) {
            if (!FileUtils.deleteQuietly(workDir)) { // may fail if process is not terminated, see Tomcats workaround
                try {
                    FileUtils.forceDeleteOnExit(workDir);
                } catch (IOException e) {
                }
            }
        } else {
            LoggerFactory.getLogger(Tools.class).warn("Tomcat working directory '" + workDir.getAbsolutePath() 
                + "' not found for disposal.");
        }
    }

}
