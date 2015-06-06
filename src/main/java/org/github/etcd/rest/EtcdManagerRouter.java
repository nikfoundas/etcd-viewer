package org.github.etcd.rest;

public interface EtcdManagerRouter {
    EtcdManager getEtcdManager(String address);
}
