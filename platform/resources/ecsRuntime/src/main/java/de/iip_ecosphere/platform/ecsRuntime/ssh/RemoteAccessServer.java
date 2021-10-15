package de.iip_ecosphere.platform.ecsRuntime.ssh;

import de.iip_ecosphere.platform.support.Server;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.ProcessShellFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RemoteAccessServer implements Server {

    public static final String SSH_HOST = "0.0.0.0";
    public static final int SSH_PORT = 5555;
    
    private SshServer server;
    private boolean started = false;

    private CredentialsManager credentialsManager = new CredentialsManager();

    protected RemoteAccessServer() {

    }

    @Override
    public Server start() {
        if (server != null && server.isStarted()) {
            throw new RuntimeException("Server already started");
        }

        server = org.apache.sshd.server.SshServer.setUpDefaultServer();
        server.setHost(SSH_HOST);
        server.setPort(SSH_PORT);
        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File("file.ser").toPath()));
        server.setPasswordAuthenticator((username, password, session)
                -> credentialsManager.authenticate(username, password));
        // only works for Linux-like environments
        server.setShellFactory(new ProcessShellFactory("/bin/sh -i -l", "/bin/sh", "-i", "-l"));
        try {
            server.start();
            this.started = true;
        } catch (IOException e) {
            e.printStackTrace();
            server = null;
            return null;
        }
        return this;
    }

    @Override
    public void stop(boolean b) {
        if (null != this.server) {
            try {
                this.server.stop(b);
                this.started = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isStarted() {
        return started;
    }

    public CredentialsManager getCredentialsManager() {
        return credentialsManager;
    }

    public class CredentialsManager {

        private List<Credentials> credentials = new ArrayList<>();

        public List<Credentials> getCredentials() {
            return credentials;
        }

        public Credentials getCredentials(String edgeKey) {
            return credentials.stream()
                    .filter(ts -> ts.getKey().equals(edgeKey))
                    .findFirst()
                    .orElse(null);
        }

        public boolean authenticate(String key, String secret) {
            Credentials edgeTunnelSettings = getCredentials(key);
            return edgeTunnelSettings != null && edgeTunnelSettings.getSecret().equals(secret);
        }

        public void addTunnelSettings(Credentials tunnelSettings) {
            this.credentials.add(tunnelSettings);
        }

        public Credentials addGeneratedCredentials() {
            Credentials tunnelSetting = new Credentials(
                    UUID.randomUUID().toString().replaceAll("-", "").substring(0,16),
                    UUID.randomUUID().toString().replaceAll("-", "").substring(0,16));
            credentials.add(tunnelSetting);
            return tunnelSetting;
        }
    }

    public static class Credentials {

        private String key;
        private String secret;

        public Credentials(String key, String secret) {
            this.key = key;
            this.secret = secret;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        @Override
        public String toString() {
            return "TunnelSettings{" +
                    "edgeKey='" + key + '\'' +
                    ", edgeSecret='" + secret + '\'' +
                    '}';
        }
    }
}
