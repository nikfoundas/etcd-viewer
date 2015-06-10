package org.github.etcd.service;

import java.util.List;

import org.github.etcd.service.rest.EtcdNode;
import org.github.etcd.service.rest.EtcdResponse;
import org.github.etcd.service.rest.EtcdSelfStats;

public interface EtcdManager {
    String getVersion();

    EtcdSelfStats getSelfStats();

    List<EtcdNode> getMachines();

    EtcdResponse getNode(String key);

    EtcdResponse saveOrUpdate(EtcdNode node, Boolean update);

    EtcdResponse delete(String key, Boolean directory);
}
