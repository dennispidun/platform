package de.iip_ecosphere.platform.ecsRuntime;

import de.iip_ecosphere.platform.ecsRuntime.ssh.RemoteAccessServer;

import java.util.concurrent.ExecutionException;

public interface DeviceManagementOperations {

    /**
     * Creates Connection Details (Credentials), a pair of username
     * and a password for the active remote access server
     * @return credentials for the remote access server
     * @throws ExecutionException if the execution fails
     */
    RemoteAccessServer.Credentials createRemoteConnectionCredentials() throws ExecutionException;


}
