package org.github.etcd.service;

import org.github.etcd.service.rest.EtcdProxy;

public interface EtcdProxyFactory {
    EtcdProxy getEtcdProxy(String clientURL);
}
