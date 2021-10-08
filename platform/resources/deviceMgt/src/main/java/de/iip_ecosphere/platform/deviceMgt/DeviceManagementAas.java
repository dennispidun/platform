package de.iip_ecosphere.platform.deviceMgt;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.iip_ecosphere.platform.deviceMgt.registry.DeviceRegistryFactory;
import de.iip_ecosphere.platform.support.aas.*;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.iip_aas.AasContributor;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper;

import java.util.concurrent.ExecutionException;

import static de.iip_ecosphere.platform.support.iip_aas.AasUtils.readString;

public class DeviceManagementAas implements AasContributor {

    public static final String NAME_SUBMODEL = AasPartRegistry.NAME_SUBMODEL_RESOURCES;
    public static final String NAME_COLL_DEVICE_MANAGER = "deviceManager";
    public static final String NAME_OP_UPDATE_RUNTIME = "updateRuntime";
    public static final String NAME_OP_ESTABLISH_SSH = "establishSsh";

    public static final String ECS_UPDATE_URI = "https://an.uri.local";

    @Override
    public Aas contributeTo(AasBuilder aasBuilder, InvocablesCreator iCreator) {
        System.out.println("Loading DeviceManagementAas");
        Submodel.SubmodelBuilder smB = aasBuilder.createSubmodelBuilder(NAME_SUBMODEL, null);

        SubmodelElementCollection.SubmodelElementCollectionBuilder deviceManager =
                smB.createSubmodelElementCollectionBuilder(NAME_COLL_DEVICE_MANAGER, false, false);

        deviceManager.createOperationBuilder(NAME_OP_UPDATE_RUNTIME)
                .setInvocable(iCreator.createInvocable(getQName(NAME_OP_UPDATE_RUNTIME)))
                .addInputVariable("deviceId", Type.STRING)
                .build();
        deviceManager.createOperationBuilder(NAME_OP_ESTABLISH_SSH)
                .setInvocable(iCreator.createInvocable(getQName(NAME_OP_ESTABLISH_SSH)))
                .addInputVariable("deviceId", Type.STRING)
                .addOutputVariable("result", Type.STRING)
                .build();

        deviceManager.build();

        smB.defer();
        return null;
    }
    @Override
    public void contributeTo(ProtocolServerBuilder sBuilder) {
        sBuilder.defineOperation(getQName(NAME_OP_UPDATE_RUNTIME),
            new JsonResultWrapper(p -> {
                DeviceManagementFactory.getDeviceManagement().updateRuntime(readString(p));
                return null;
            })
        );

        sBuilder.defineOperation(getQName(NAME_OP_ESTABLISH_SSH),
                new JsonResultWrapper(p -> {
                    DeviceRemoteManagementOperations.SSHConnectionDetails connectionDetails =
                            DeviceManagementFactory.getDeviceManagement().createSSHServer(readString(p));
                    return new ObjectMapper().writeValueAsString(connectionDetails);
                })
        );
    }

    public static void notifyUpdateRuntime(String id) {
        ActiveAasBase.processNotification(AasPartRegistry.NAME_SUBMODEL_RESOURCES, (sub, aas) -> {
            try {
                sub.getSubmodelElementCollection(id).getOperation("updateRuntime").invoke(ECS_UPDATE_URI);
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    private String getQName(String name) {
        return NAME_SUBMODEL + "_" + name;
    }


    @Override
    public Kind getKind() {
        return Kind.ACTIVE;
    }

    @Override
    public boolean isValid() {
        return null != DeviceManagementFactory.getDeviceManagement() || null != DeviceRegistryFactory.getDeviceRegistry();
    }

}
