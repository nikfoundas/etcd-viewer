package org.github.etcd.rest;

public interface EtcdResourceRouter {
    EtcdResource getResource(String address);
}
