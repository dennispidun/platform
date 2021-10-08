package de.iip_ecosphere.platform.ecsRuntime.ssh;

import de.iip_ecosphere.platform.ecsRuntime.ssh.RemoteAccessServer.Credentials;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RemoteAccessServerTest {

    public static final String A_KEY = "A_KEY";
    public static final String A_SECRET = "A_SECRET";
    private RemoteAccessServer remoteAccessServer;

    @Before
    public void setUp() throws Exception {
        remoteAccessServer = RemoteAccessServerFactory.create();
    }

    @Test
    public void testServerCreation() {
        Assert.assertNotNull(remoteAccessServer);
    }

    @Test
    public void getCredentialsManager_createsCredentialsManager() {
        RemoteAccessServer.CredentialsManager credentialsManager = remoteAccessServer.getCredentialsManager();
        Assert.assertNotNull(credentialsManager);
    }

    @Test
    public void createCredentials_createsCredentials() {
        remoteAccessServer.getCredentialsManager().addTunnelSettings(new Credentials(A_KEY, A_SECRET));
        Credentials tunnelSettings = remoteAccessServer.getCredentialsManager().getCredentials(A_KEY);
        Assert.assertNotNull(tunnelSettings);
        Assert.assertEquals(A_SECRET, tunnelSettings.getSecret());
        Assert.assertEquals(A_KEY, tunnelSettings.getKey());
    }

    @Test
    public void createRandomCredentials_addsAndCreatesCredentials() {
        Credentials credentials = remoteAccessServer.getCredentialsManager().addGeneratedCredentials();
        Assert.assertNotNull(credentials);
        Credentials credentials1 = remoteAccessServer.getCredentialsManager().getCredentials(credentials.getKey());
        Assert.assertNotNull(credentials1);
        Assert.assertEquals(credentials.getKey(), credentials1.getKey());
        Assert.assertEquals(credentials.getSecret(), credentials1.getSecret());
    }

    @Test
    public void name() {
        RemoteAccessServer remoteAccessServer = RemoteAccessServerFactory.create();
        remoteAccessServer.start();
        remoteAccessServer.getCredentialsManager().addTunnelSettings(new Credentials("abc", "abc"));
        while(true) {

        }
    }
}