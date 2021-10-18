package de.iip_ecosphere.platform.deviceMgt.storage;

import java.util.Set;

public interface Storage {

    String getPrefix();

    Set<String> list();

    String generateDownloadUrl(String relativePath);

}
