package org.github.etcd.rest;

import java.util.List;

public interface EtcdManager {
    String getVersion();

    EtcdSelfStats getSelfStats();

    List<EtcdNode> getMachines();

    EtcdResponse getNode(String key);

    EtcdResponse saveOrUpdate(EtcdNode node, Boolean update);

    EtcdResponse delete(String key, Boolean directory);
}
