package de.iip_ecosphere.platform.deviceMgt.ssh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ServerSocket;
import java.net.Socket;

public class SshProxy implements Runnable {
    private final String remoteIp;
    private final int remotePort;
    private int port;

    public SshProxy(String remoteIp, int remotePort, int port) {
        this.remoteIp = remoteIp;
        this.remotePort = remotePort;
        this.port = port;
    }

    public void listen() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            this.port = serverSocket.getLocalPort();
            while (true) {
                Socket socket = serverSocket.accept();
                startThread(new Connection(socket, remoteIp, remotePort));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void startThread(Connection connection) {
        Thread t = new Thread(connection);
        t.start();
    }

    public int getPort() {
        return port;
    }

    @Override
    public void run() {
        listen();
    }
}
