package de.iip_ecosphere.platform.deviceMgt;

import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.InvocablesCreator;
import de.iip_ecosphere.platform.support.aas.ProtocolServerBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.iip_aas.AasContributor;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;

public class DeviceManagementAas implements AasContributor {

    public static final String NAME_SUBMODEL = AasPartRegistry.NAME_SUBMODEL_RESOURCES;
    public static final String NAME_COLL_DEVICE_MANAGER = "deviceManager";

    @Override
    public Aas contributeTo(AasBuilder aasBuilder, InvocablesCreator iCreator) {
        Submodel.SubmodelBuilder smB = aasBuilder.createSubmodelBuilder(NAME_SUBMODEL, null);

        smB.createSubmodelElementCollectionBuilder(NAME_COLL_DEVICE_MANAGER, false, false).build();

        smB.defer();
        return null;
    }

    @Override
    public void contributeTo(ProtocolServerBuilder sBuilder) {
        // TODO Auto-generated method stub
    }

    @Override
    public Kind getKind() {
        // TODO Auto-generated method stub
        return Kind.ACTIVE;
    }

    @Override
    public boolean isValid() {
        return true;
    }

}
