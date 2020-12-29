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

import org.eclipse.basyx.aas.metamodel.api.parts.asset.IAsset;
import org.eclipse.basyx.aas.metamodel.map.AssetAdministrationShell;
import org.eclipse.basyx.aas.metamodel.map.parts.Asset;
import org.eclipse.basyx.submodel.metamodel.map.reference.Reference;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Asset.AssetBuilder;
import de.iip_ecosphere.platform.support.aas.AssetKind;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.basyx.AbstractAas.BaSyxSubmodelParent;

/**
 * Wraps a BaSyx AAS.
 * 
 * @author Holger Eichelberger, SSE
 */
public class BaSyxAas extends AbstractAas<AssetAdministrationShell> implements BaSyxSubmodelParent {

    /**
     * Builder for {@code BaSyxAas}.
     * 
     * @author Holger Eichelberger, SSE
     */
    static class BaSyxAasBuilder extends BaSyxAbstractAasBuilder {

        private BaSyxAas instance;
        
        /**
         * Creates an instance. Prevents external creation.
         * 
         * @param idShort the shortId of the AAS
         * @param identifier the identifier of the AAS (may be <b>null</b> or empty for an identification based on 
         *    {@code idShort}, interpreted as an URN if this starts with {@code urn})
         * @throws IllegalArgumentException if {@code idShort} or {@code urn} is <b>null</b> or empty
         */
        BaSyxAasBuilder(String idShort, String identifier) {
            AssetAdministrationShell aas = new AssetAdministrationShell();
            aas.setIdShort(Tools.checkId(idShort));
            aas.setIdentification(Tools.translateIdentifier(identifier, idShort));
            instance = new BaSyxAas(aas);
        }

        /**
         * Creates an instance from an existing BaSyx instance. Prevents external creation.
         * 
         * @param instance the BaSyx instance
         */
        BaSyxAasBuilder(BaSyxAas instance) {
            this.instance = instance;
        }

        @Override
        public Aas build() {
            return instance;
        }

        @Override
        public SubmodelBuilder createSubmodelBuilder(String idShort, String identifier) {
            SubmodelBuilder result;
            Submodel sub =  instance.getSubmodel(idShort);
            if (null == instance.getSubmodel(idShort)) {
                result = new BaSyxSubmodel.BaSyxSubmodelBuilder(this, idShort, identifier);
            } else { // no connected here
                result = new BaSyxSubmodel.BaSyxSubmodelBuilder(this, (BaSyxSubmodel) sub);
            }
            return result;
        }

        @Override
        public Submodel register(BaSyxSubmodel submodel) {
            if (null == instance.getSubmodel(submodel.getIdShort())) {
                instance.getAas().addSubModel(submodel.getSubmodel());
                instance.register(submodel);
            }
            return submodel;
        }
        
        /**
         * Returns the instance under creation.
         * 
         * @return the instance
         */
        BaSyxAas getInstance() {
            return instance;
        }

        @Override
        public BaSyxSubmodelParent getSubmodelParent() {
            return instance;
        }

        @Override
        public AssetBuilder createAssetBuilder(String idShort, String urn, AssetKind kind) {
            return new BaSyxAsset.BaSyxAssetBuilder(this, idShort, urn, kind);
        }

        @Override
        void setAsset(BaSyxAsset asset) {
            instance.setAsset(asset);
            IAsset a = asset.getAsset();
            instance.getAas().setAsset((Asset) a);
            // reference is needed for Reading back AASX; works also without setAsset; unclear wether both ar needed
            instance.getAas().setAssetReference((Reference) a.getReference()); 
        }
        
    }

    /**
     * Creates an instance. Prevents external creation.
     * 
     * @param aas the BaSyx AAS instance
     */
    BaSyxAas(AssetAdministrationShell aas) {
        super(aas);
    }

    @Override
    public SubmodelBuilder addSubmodel(String idShort, String identifier) {
        return new BaSyxSubmodel.BaSyxSubmodelBuilder(new BaSyxAasBuilder(this), idShort, identifier);
    }

    @Override
    public BaSyxAbstractAasBuilder createAasBuilder() {
        return new BaSyxAasBuilder(this);
    }

}
