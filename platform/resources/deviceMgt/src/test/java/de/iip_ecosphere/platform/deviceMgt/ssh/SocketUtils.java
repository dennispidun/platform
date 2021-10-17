/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which is available
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.deviceMgt.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SocketUtils {

    public static Socket mockSocket() {
        Socket inSocket = mock(Socket.class);
        when(inSocket.getInetAddress()).thenReturn(mock(InetAddress.class));
        return inSocket;
    }

    public static class MockOutputStream extends OutputStream {

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

    public static class MockInputStream extends InputStream {

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
