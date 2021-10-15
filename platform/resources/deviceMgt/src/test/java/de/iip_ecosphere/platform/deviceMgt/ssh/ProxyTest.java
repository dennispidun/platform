package de.iip_ecosphere.platform.deviceMgt.ssh;

import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.net.Socket;

import static de.iip_ecosphere.platform.deviceMgt.ssh.SocketUtils.mockSocket;
import static org.mockito.Mockito.*;

public class ProxyTest {

    public static final String A_MESSAGE = "test";

    @Test
    public void proxy_withDateOnInputStream_proxiesDataToOutputStream() throws IOException, InterruptedException {
        // Create simple mock Sockets
        Socket inSocket = mockSocket();
        Socket outSocket = mockSocket();

        // Create mock OutputStreams which collects every byte into an byte array and link it to socket
        SocketUtils.MockOutputStream outputStream = new SocketUtils.MockOutputStream();
        when(outSocket.getOutputStream()).thenReturn(outputStream);

        // Link MockInputStream to socket and send a message
        when(inSocket.getInputStream()).thenReturn(new SocketUtils.MockInputStream(A_MESSAGE));

        // run the Proxy, the unit under test
        Proxy proxy = new Proxy(inSocket, outSocket);
        proxy.run();

        Assert.assertEquals(A_MESSAGE, outputStream.getAsString());
    }

    @Test
    public void proxy_withoutInputStream_wontProxyData() throws IOException {
        Socket inSocket = mockSocket();
        Socket outSocket = mockSocket();

        SocketUtils.MockOutputStream outputStream = new SocketUtils.MockOutputStream();
        when(outSocket.getOutputStream()).thenReturn(outputStream);

        when(inSocket.getInputStream()).thenReturn(null);

        // run the Proxy, the unit under test
        Proxy proxy = new Proxy(inSocket, outSocket);
        proxy.run();

        Assert.assertNull(outputStream.getAsString());
    }

    @Test
    public void proxy_withoutOutputStream_wontProxyData() throws IOException {
        Socket inSocket = mockSocket();
        Socket outSocket = mockSocket();

        when(outSocket.getOutputStream()).thenReturn(null);

        InputStream mockInputStream = mock(InputStream.class);
        when(inSocket.getInputStream()).thenReturn(mockInputStream);

        // run the Proxy, the unit under test
        Proxy proxy = new Proxy(inSocket, outSocket);
        proxy.run();

        // Verify that the InputStream is never read by Proxy
        verify(mockInputStream, times(0)).read();
    }


    @Test
    public void proxy_withThrowingInputStream_wontProxyData() throws IOException {
        Socket inSocket = mockSocket();
        Socket outSocket = mockSocket();

        SocketUtils.MockOutputStream outputStream = new SocketUtils.MockOutputStream();
        when(outSocket.getOutputStream()).thenReturn(outputStream);

        when(inSocket.getInputStream()).thenThrow(new IOException());

        // run the Proxy, the unit under test
        Proxy proxy = new Proxy(inSocket, outSocket);
        proxy.run();

        Assert.assertNull(outputStream.getAsString());
    }


    @Test
    public void proxy_withThrowingOutputStream_wontProxyData() throws IOException {
        Socket inSocket = mockSocket();
        Socket outSocket = mockSocket();

        InputStream mockInputStream = mock(InputStream.class);
        when(inSocket.getInputStream()).thenReturn(mockInputStream);

        when(inSocket.getOutputStream()).thenThrow(new IOException());

        // run the Proxy, the unit under test
        Proxy proxy = new Proxy(inSocket, outSocket);
        proxy.run();

        verify(mockInputStream, times(0)).read();
    }


}