package de.iip_ecosphere.platform.deviceMgt.ssh;

public class SshProxyFactory {

    public static SshProxy createProxy(String id) {
        // find edge ip
        SshProxy edge = new SshProxy("localhost", 5555, 0);
        new Thread(edge).start();
        return edge;
    }

}
