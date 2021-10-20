package de.iip_ecosphere.platform.deviceMgt.ecs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.iip_ecosphere.platform.deviceMgt.Credentials;
import de.iip_ecosphere.platform.support.iip_aas.AasPartRegistry;
import de.iip_ecosphere.platform.support.iip_aas.SubmodelElementsCollectionClient;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class EcsAasClient extends SubmodelElementsCollectionClient {

    public static final String NAME_OP_CREATE_REMOTE_CONNECTION_CREDENTIALS = "createRemoteConnectionCredentials";
    public static final String PROP_NAME_RUNTIME_NAME = "runtimeName";

    public EcsAasClient(String id) throws IOException {
        super(AasPartRegistry.NAME_SUBMODEL_RESOURCES, id);
    }

    public Credentials createRemoteConnectionCredentials() throws ExecutionException {
        String result = (String) getOperation(NAME_OP_CREATE_REMOTE_CONNECTION_CREDENTIALS).invoke();
        ObjectMapper mapper = new ObjectMapper();
        Credentials credentials = null;
        try {
            credentials = mapper.readValue(result, Credentials.class);
        } catch (JsonProcessingException ignore) {
            // should not happen
        }

        return credentials;
    }

    public String getRuntimeName() throws ExecutionException {
        return getPropertyStringValue(PROP_NAME_RUNTIME_NAME, "");
    }
}