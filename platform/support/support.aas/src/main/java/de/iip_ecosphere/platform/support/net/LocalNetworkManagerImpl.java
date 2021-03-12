/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.net;

import java.util.HashMap;
import java.util.Map;

import de.iip_ecosphere.platform.support.NetUtils;
import de.iip_ecosphere.platform.support.Schema;
import de.iip_ecosphere.platform.support.ServerAddress;

/**
 * A simple network manager implementation, which just uses the full space of potential ephemerial ports.
 * 
 * @author Holger Eichelberger, SSE
 */
public class LocalNetworkManagerImpl implements NetworkManager {

    private Map<String, ServerAddress> keyToAddress = new HashMap<>();
    private Map<Integer, String> portToKey = new HashMap<>();
    private String host = NetUtils.getOwnIP();
    
    @Override
    public synchronized ManagedServerAddress obtainPort(String key) {
        ManagedServerAddress result = getManagedAddress(key); // calls checkKey(key)
        if (null == result) {
            do {
                int port = NetUtils.getEphemeralPort();
                if (!portToKey.containsKey(port)) {
                    ServerAddress address = new ServerAddress(Schema.IGNORE, host, port);
                    keyToAddress.put(key, address);
                    portToKey.put(port, key);
                    result = new ManagedServerAddress(address, true);
                } else {
                    result = null;
                }
            } while (result == null);
        }
        return result;
    }
    
    /**
     * Checks the key for structural validity.
     * 
     * @param key the key
     * @throws IllegalArgumentException if {@code key} is not structurally valid
     */
    private void checkKey(String key) {
        if (null == key) {
            throw new IllegalArgumentException("Key must be given");
        }
    }

    /**
     * Checks the address for structural validity.
     * 
     * @param address the address
     * @throws IllegalArgumentException if {@code address} is not structurally valid
     */
    private void checkAddress(ServerAddress address) {
        if (null == address) {
            throw new IllegalArgumentException("Address must be given");
        }
    }

    /**
     * Returns a managed address if {@code key} is already known. Calls {@link #checkKey(String)}.
     * 
     * @param key the key
     * @return the address if known, <b>null</b> else
     */
    private ManagedServerAddress getManagedAddress(String key) {
        checkKey(key);
        ServerAddress ex = keyToAddress.get(key);
        ManagedServerAddress result = null;
        if (null != ex) {
            result = new ManagedServerAddress(ex, false);
        }
        return result;
    }
    
    @Override
    public ManagedServerAddress reservePort(String key, ServerAddress address) {
        checkAddress(address);
        ManagedServerAddress result = getManagedAddress(key); // calls checkKey(key)
        if (null == result) {
            keyToAddress.put(key, address);
            portToKey.put(address.getPort(), key);
            result = new ManagedServerAddress(address, true);
        } 
        return result;
    }

    @Override
    public synchronized void releasePort(String key) {
        checkKey(key);
        ServerAddress ex = keyToAddress.remove(key);
        if (null != ex) {
            portToKey.remove(ex.getPort());
        }
    }

    @Override
    public synchronized boolean isInUse(int port) {
        return portToKey.containsKey(port);
    }

    @Override
    public boolean isInUse(ServerAddress adr) {
        return host.equals(adr.getHost()) && isInUse(adr.getPort());
    }

    @Override
    public int getLowPort() {
        return 1025;
    }

    @Override
    public int getHighPort() {
        return 65535;
    }

}
