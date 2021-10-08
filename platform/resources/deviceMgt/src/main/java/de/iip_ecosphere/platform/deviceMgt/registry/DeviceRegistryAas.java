package de.iip_ecosphere.platform.deviceMgt.registry;

import de.iip_ecosphere.platform.support.aas.*;
import de.iip_ecosphere.platform.support.iip_aas.AasContributor;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.ActiveAasBase;
import de.iip_ecosphere.platform.support.iip_aas.json.JsonResultWrapper;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;


import java.io.IOException;

import static de.iip_ecosphere.platform.support.iip_aas.AasUtils.fixId;
import static de.iip_ecosphere.platform.support.iip_aas.AasUtils.readString;

public class DeviceRegistryAas implements AasContributor {

    public static final String NAME_SUBMODEL = AasPartRegistry.NAME_SUBMODEL_RESOURCES;

    public static final String NAME_COLL_DEVICE_REGISTRY = "deviceRegistry";

    public static final String NAME_PROP_DEVICE_RESOURCE = "resource";
    public static final String NAME_PROP_MANAGED_DEVICE_ID = "managedId";

    public static final String NAME_OP_DEVICE_ADD = "addDevice";
    public static final String NAME_OP_DEVICE_REMOVE = "removeDevice";
    public static final String NAME_OP_IM_ALIVE = "imAlive";
    public static final String NAME_OP_SEND_TELEMETRY = "sendTelemetry";

    @Override
    public Aas contributeTo(Aas.AasBuilder aasBuilder, InvocablesCreator iCreator) {
        Submodel.SubmodelBuilder smB = aasBuilder.createSubmodelBuilder(NAME_SUBMODEL, null);

        SubmodelElementCollectionBuilder registryColl = smB
                .createSubmodelElementCollectionBuilder(NAME_COLL_DEVICE_REGISTRY, false, false);

        registryColl.createOperationBuilder(NAME_OP_DEVICE_ADD)
                .setInvocable(iCreator.createInvocable(getQName(NAME_OP_DEVICE_ADD)))
                .addInputVariable("deviceId", Type.STRING)
                .build();

        registryColl.createOperationBuilder(NAME_OP_DEVICE_REMOVE)
                .setInvocable(iCreator.createInvocable(getQName(NAME_OP_DEVICE_REMOVE)))
                .addInputVariable("deviceId", Type.STRING)
                .build();

        registryColl.createOperationBuilder(NAME_OP_IM_ALIVE)
                .setInvocable(iCreator.createInvocable(getQName(NAME_OP_IM_ALIVE)))
                .addInputVariable("deviceId", Type.STRING)
                .build();

        registryColl.createOperationBuilder(NAME_OP_SEND_TELEMETRY)
                .setInvocable(iCreator.createInvocable(getQName(NAME_OP_SEND_TELEMETRY)))
                .addInputVariable("deviceId", Type.STRING)
                .addInputVariable("telemetryData", Type.STRING)
                .build();

        registryColl.build();

        smB.defer();
        return null;
    }

    @Override
    public void contributeTo(ProtocolServerBuilder sBuilder) {
        sBuilder.defineOperation(getQName(NAME_OP_DEVICE_ADD),
                new JsonResultWrapper(p -> {
                    DeviceRegistryFactory.getDeviceRegistry().addDevice(readString(p));
                    return null;
                })
        );

        sBuilder.defineOperation(getQName(NAME_OP_DEVICE_REMOVE),
                new JsonResultWrapper(p -> {
                    DeviceRegistryFactory.getDeviceRegistry().removeDevice(readString(p));
                    return null;
                })
        );

        sBuilder.defineOperation(getQName(NAME_OP_IM_ALIVE),
                new JsonResultWrapper(p -> {
                    DeviceRegistryFactory.getDeviceRegistry().imAlive(readString(p));
                    return null;
                })
        );

        sBuilder.defineOperation(getQName(NAME_OP_SEND_TELEMETRY),
                new JsonResultWrapper(p -> {
                    DeviceRegistryFactory.getDeviceRegistry().sendTelemetry(readString(p), readString(p, 1));
                    return null;
                })
        );
    }

    private static String getQName(String name) {
        return NAME_COLL_DEVICE_REGISTRY + "_" + name;
    }

    @Override
    public Kind getKind() {
        return Kind.ACTIVE;
    }

    @Override
    public boolean isValid() {
        return null != DeviceRegistryFactory.getDeviceRegistry();
    }

    public static void notifyDeviceAdded(String internalId, String resId) {
        ActiveAasBase.processNotification(NAME_SUBMODEL, (sub, aas) -> {
            Submodel.SubmodelBuilder resources = aas.createSubmodelBuilder(NAME_SUBMODEL, null);
            SubmodelElementCollectionBuilder registry = resources
                    .createSubmodelElementCollectionBuilder(NAME_COLL_DEVICE_REGISTRY, false, false);

            SubmodelElementCollectionBuilder device = resources
                .createSubmodelElementCollectionBuilder(fixId(resId), false, false);

            device.createPropertyBuilder(NAME_PROP_MANAGED_DEVICE_ID)
                    .setValue(Type.STRING, internalId)
                    .build();

            device.build();
            registry.build();
            resources.build();
        });
    }

    public static void notifyDeviceRemoved(String id) {
        ActiveAasBase.processNotification(NAME_SUBMODEL, (sub, aas) -> {
            try {
                DeviceRegistryAasClient client = new DeviceRegistryAasClient();
                SubmodelElementCollection device = client.getDevice(id);

                sub.getSubmodelElementCollection(fixId(id))
                    .deleteElement(NAME_PROP_MANAGED_DEVICE_ID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
