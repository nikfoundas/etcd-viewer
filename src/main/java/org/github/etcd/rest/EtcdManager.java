package org.github.etcd.rest;

import java.util.List;

public interface EtcdManager {
    String getVersion();

    List<EtcdNode> getMachines();

    EtcdResponse getNode(String key);

    EtcdResponse createDirectory(String key, Long ttl, Boolean update);
    EtcdResponse deleteDirectory(String key);

    EtcdResponse createValue(String key, String value, Long ttl, Boolean update);
    EtcdResponse deleteValue(String key);
}
