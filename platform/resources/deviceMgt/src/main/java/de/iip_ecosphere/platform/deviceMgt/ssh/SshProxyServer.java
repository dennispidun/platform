package de.iip_ecosphere.platform.deviceMgt.ssh;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * With the Help of the {@link SshProxyServer} one can create a server
 * to which ssh clients can connect to.
 *
 * Main parts were acquired from https://github.com/oksuz/tcp-proxy (MIT)
 *
 * @author oksuz, Github on 29/10/2017.
 */
public class SshProxyServer implements Runnable {
    private final String remoteIp;
    private final int remotePort;
    private int port;

    /**
     * Creates a SshProxyServer, which can listen on
     * all interface and a given port
     * @param remoteIp the remote ip to connect to
     * @param remotePort the remote port to connect to
     * @param localPort the local port to accept connections
     */
    public SshProxyServer(String remoteIp, int remotePort, int localPort) {
        this.remoteIp = remoteIp;
        this.remotePort = remotePort;
        this.port = localPort;
    }

    /**
     * Starts listening, so others can connect to this proxy.
     * If the desired port is set to 0, a server socket is listening
     * on a randomly chosen port in the ephemeral port range (1024–65535).
     * One can get the assigned port through {@code getPort()}.
     */
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

    /**
     * Get the remote ip.
     * @return the remote ip
     */
    public String getRemoteIp() {
        return remoteIp;
    }

    /**
     * Get the remote port.
     * @return the remote port
     */
    public int getRemotePort() {
        return remotePort;
    }

    /**
     * Get the port the server is listening on.
     * @return the local port
     */
    public int getPort() {
        return port;
    }

    /**
     * Runs the SshProxyServer and listens on the specified/chosen port.
     */
    @Override
    public void run() {
        listen();
    }
}
