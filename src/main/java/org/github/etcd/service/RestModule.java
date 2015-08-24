/**
 *
 */
package org.github.etcd.service;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.github.etcd.service.impl.ClusterManagerImpl;
import org.github.etcd.service.rest.EtcdProxy;
import org.github.etcd.service.rest.impl.EtcdProxyImpl;
import org.github.etcd.viewer.EtcdWebSession;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class RestModule extends AbstractModule {

    private static final String ETCD_NODE = "etcd.clientURL";

    public static final String SELECTED_CLUSTER_NAME = "selectedCluster";

    @Override
    protected void configure() {

        String etcdAddress = System.getenv(ETCD_NODE);
        if (etcdAddress == null) {
            etcdAddress = System.getProperty(ETCD_NODE, "http://localhost:2379/");
        }

        bindConstant().annotatedWith(Names.named(ETCD_NODE)).to(etcdAddress);

        bind(ClusterManager.class).to(ClusterManagerImpl.class).in(Singleton.class);

        bind(EtcdProxyFactory.class).to(EtcdProxyFactoryImpl.class).in(Singleton.class);

    }

    private static class EtcdProxyFactoryImpl implements EtcdProxyFactory {

        @Inject
        private ClusterManager clusterManager;

        @Override
        public EtcdProxy getEtcdProxy(String registry, String address) {
            String authToken = null;
            if (EtcdWebSession.exists()) {
                authToken = EtcdWebSession.get().getBasicAuthenticationToken(registry);
            }
            return new EtcdProxyImpl(address, authToken);
        }

        @Override
        public EtcdProxy getEtcdProxy(String registry) {
            return getEtcdProxy(registry, clusterManager.getCluster(registry).getAddress());
        }

    }

}
