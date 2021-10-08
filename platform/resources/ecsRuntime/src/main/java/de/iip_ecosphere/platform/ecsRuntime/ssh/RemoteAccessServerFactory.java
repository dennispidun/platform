package de.iip_ecosphere.platform.ecsRuntime.ssh;

public class RemoteAccessServerFactory {

    private static RemoteAccessServer server;

    public static RemoteAccessServer create() {
        if (null == server) {
            server = new RemoteAccessServer();
        }
        return server;
    }

}
