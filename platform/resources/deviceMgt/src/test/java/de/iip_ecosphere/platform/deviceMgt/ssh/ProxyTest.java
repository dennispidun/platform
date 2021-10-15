package de.iip_ecosphere.platform.deviceMgt.ssh;

import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class ProxyTest {

    public static final String A_MESSAGE = "test";

    @Test
    public void proxy_withDateOnInputStream_proxiesDataToOutputStream() throws IOException, InterruptedException {
        // Create simple mock Sockets
        Socket inSocket = mockSocket();
        Socket outSocket = mockSocket();

        // Create mock OutputStreams which collects every byte into an byte array and link it to socket
        MockOutputStream outputStream = new MockOutputStream();
        when(outSocket.getOutputStream()).thenReturn(outputStream);

        // Link MockInputStream to socket and send a message
        when(inSocket.getInputStream()).thenReturn(new MockInputStream(A_MESSAGE));

        // run the Proxy, the unit under test
        Proxy proxy = new Proxy(inSocket, outSocket);
        proxy.run();

        Assert.assertEquals(A_MESSAGE, outputStream.getAsString());
    }

    @Test
    public void proxy_withoutInputStream_wontProxyData() throws IOException {
        Socket inSocket = mockSocket();
        Socket outSocket = mockSocket();

        MockOutputStream outputStream = new MockOutputStream();
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

        MockOutputStream outputStream = new MockOutputStream();
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

    private Socket mockSocket() {
        Socket inSocket = mock(Socket.class);
        when(inSocket.getInetAddress()).thenReturn(mock(InetAddress.class));
        return inSocket;
    }

    private static class MockOutputStream extends OutputStream {

        private final List<Integer> payload = new ArrayList<>();

        @Override
        public void write(int i) throws IOException {
            payload.add(i);
        }

        public String getAsString() {
            if (payload.size() == 0) {
                return null;
            }

            byte[] bytes = new byte[payload.size()];
            for (int i = 0; i < payload.size(); i++) {
                bytes[i] = payload.get(i).byteValue();
            }
            return new String(bytes);
        }


    }

    private static class MockInputStream extends InputStream {

        private final String payload;
        private int index = 0;

        public MockInputStream(String payload) {
            this.payload = payload;
        }

        @Override
        public int read() throws IOException {
            if (index >= payload.getBytes().length) {
                return -1;
            }
            byte aByte = payload.getBytes()[index];
            index++;
            return aByte;
        }
    }
}