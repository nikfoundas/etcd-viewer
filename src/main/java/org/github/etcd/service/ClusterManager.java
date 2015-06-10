package org.github.etcd.service;

import java.util.List;


public interface ClusterManager {

    boolean exists(String name);
    EtcdCluster getCluster(String name);

    EtcdCluster addCluster(String name, String etcdPeerAddress);
    void removeCluster(String name);

    List<EtcdCluster> getClusters();

    List<String> getClusterNames();

    void refreshCluster(String name);

}
