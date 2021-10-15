package de.iip_ecosphere.platform.deviceMgt.ssh;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SshProxy implements Runnable {
    private final String remoteIp;
    private final int remotePort;
    private int port;

    public SshProxy(String remoteIp, int remotePort, int localPort) {
        this.remoteIp = remoteIp;
        this.remotePort = remotePort;
        this.port = localPort;
    }

    public void listen() {
        try {
            ServerSocket serverSocket = createServerSocket(port);
            while (true) {
                Socket socket = serverSocket.accept();
                startThread(new Connection(socket, remoteIp, remotePort));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected ServerSocket createServerSocket(int port) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        this.port = serverSocket.getLocalPort();
        return serverSocket;
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
