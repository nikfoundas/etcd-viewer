package org.github.etcd.service;

import org.github.etcd.service.rest.EtcdProxy;

public interface EtcdProxyFactory {
    EtcdProxy getEtcdProxy(String registry, String clientURL);
    EtcdProxy getEtcdProxy(String registry);
}
