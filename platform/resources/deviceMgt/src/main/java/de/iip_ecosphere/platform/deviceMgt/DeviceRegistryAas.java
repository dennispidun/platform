package de.iip_ecosphere.platform.deviceMgt;

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
    public static final String NAME_COLL_MANAGED_DEVICES = "devices";

    public static final String NAME_OP_DEVICE_ADD = "addDevice";
    public static final String NAME_PROP_DEVICE_RESOURCE_ID = "resId";
    public static final String NAME_OP_DEVICE_REMOVE = "removeDevice";

    @Override
    public Aas contributeTo(Aas.AasBuilder aasBuilder, InvocablesCreator iCreator) {
        Submodel.SubmodelBuilder smB = aasBuilder.createSubmodelBuilder(NAME_SUBMODEL, null);

        SubmodelElementCollectionBuilder registryColl = smB
                .createSubmodelElementCollectionBuilder(NAME_COLL_DEVICE_REGISTRY, false, false);

        registryColl.createSubmodelElementCollectionBuilder(NAME_COLL_MANAGED_DEVICES, false, false)
                .build();

        registryColl.createOperationBuilder(NAME_OP_DEVICE_ADD)
                .setInvocable(iCreator.createInvocable(getQName(NAME_OP_DEVICE_ADD)))
                .addInputVariable("deviceId", Type.STRING)
                .build();

        registryColl.createOperationBuilder(NAME_OP_DEVICE_REMOVE)
                .setInvocable(iCreator.createInvocable(getQName(NAME_OP_DEVICE_REMOVE)))
                .addInputVariable("deviceId", Type.STRING)
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
        return true;
    }

    public static void notifyDeviceAdded(String internalId, String resId) {
        ActiveAasBase.processNotification(NAME_SUBMODEL, (sub, aas) -> {

            Submodel.SubmodelBuilder resources = aas.createSubmodelBuilder(NAME_SUBMODEL, null);
            SubmodelElementCollectionBuilder registry = resources
                    .createSubmodelElementCollectionBuilder(NAME_COLL_DEVICE_REGISTRY, false, false);

            SubmodelElementCollectionBuilder devices = registry
                    .createSubmodelElementCollectionBuilder(NAME_COLL_MANAGED_DEVICES, false, false);

            SubmodelElementCollectionBuilder device = devices
                .createSubmodelElementCollectionBuilder(fixId(internalId), false, false);

            device.createPropertyBuilder(NAME_PROP_DEVICE_RESOURCE_ID)
                    .setValue(Type.STRING, resId)
                    .build();

            // TODO: device.createReferenceElementBuilder("resourceUnit").setValue( ref to resourceUnit  )

            device.build();
            devices.build();
            registry.build();
            resources.build();
        });
    }

    public static void notifyDeviceRemoved(String id) {
        ActiveAasBase.processNotification(NAME_SUBMODEL, (sub, aas) -> {
            try {
                DeviceRegistryAasClient client = new DeviceRegistryAasClient();
                SubmodelElementCollection device = client.getDevice(id);

                sub.getSubmodelElementCollection(NAME_COLL_DEVICE_REGISTRY)
                    .getSubmodelElementCollection(NAME_COLL_MANAGED_DEVICES)
                    .deleteElement(device.getIdShort());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
