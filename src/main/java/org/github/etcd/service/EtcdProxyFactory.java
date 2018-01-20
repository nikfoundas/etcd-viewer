package org.github.etcd.service;

import org.github.etcd.service.api.EtcdProxy;

public interface EtcdProxyFactory {
    EtcdProxy getEtcdProxy(String registry, String clientURL, ApiVersion apiVersion);
    EtcdProxy getEtcdProxy(String registry);
}
