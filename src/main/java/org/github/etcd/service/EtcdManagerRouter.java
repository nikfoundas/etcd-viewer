package org.github.etcd.service;

public interface EtcdManagerRouter {
    EtcdManager getEtcdManager(String address);
}
