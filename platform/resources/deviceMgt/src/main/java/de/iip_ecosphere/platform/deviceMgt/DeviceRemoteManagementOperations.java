package de.iip_ecosphere.platform.deviceMgt;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

public interface DeviceRemoteManagementOperations {

    SSHConnectionDetails establishSsh(String id) throws ExecutionException;

    class SSHConnectionDetails {

        private String host;
        private Integer port;
        private String username;
        private String password;

        public SSHConnectionDetails() {
        }

        public SSHConnectionDetails(String host, Integer port, String username, String password) {
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SSHConnectionDetails that = (SSHConnectionDetails) o;
            return host.equals(that.host)
                    && port.equals(that.port)
                    && username.equals(that.username)
                    && password.equals(that.password);
        }
    }
}
