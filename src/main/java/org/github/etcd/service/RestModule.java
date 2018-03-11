package org.github.etcd.service;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.github.etcd.service.api.EtcdProxy;
import org.github.etcd.service.api.v2.EtcdV2ProxyImpl;
import org.github.etcd.service.api.v3.EtcdV3ProxyImpl;
import org.github.etcd.service.impl.ClusterManagerImpl;
import org.github.etcd.viewer.EtcdWebSession;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class RestModule extends AbstractModule {

    public static final String SELECTED_CLUSTER_NAME = "selectedCluster";

    @Override
    protected void configure() {
        bindParameterValue(ClusterManagerImpl.DEFAULT_CLIENT_URL_KEY);
        bindParameterValue(ClusterManagerImpl.CLUSTER_STORE_PATH_KEY);

        bind(ClusterManager.class).to(ClusterManagerImpl.class).in(Singleton.class);

        bind(EtcdProxyFactory.class).to(EtcdProxyFactoryImpl.class).in(Singleton.class);
    }

    private void bindParameterValue(String parameterName) {
        String parameterValue = System.getenv(parameterName);
        if (parameterValue == null) {
            parameterValue = System.getProperty(parameterName, "");
        }
        bindConstant().annotatedWith(Names.named(parameterName)).to(parameterValue);
    }

    private static class EtcdProxyFactoryImpl implements EtcdProxyFactory {

        @Inject
        private ClusterManager clusterManager;

        @Override
        public EtcdProxy getEtcdProxy(String registry, String address, ApiVersion apiVersion) {
            String authToken = null;
            if (EtcdWebSession.exists()) {
                authToken = EtcdWebSession.get().getBasicAuthenticationToken(registry);
            }
            if (ApiVersion.V2.equals(apiVersion)) {
                return new EtcdV2ProxyImpl(address, authToken);
            } else if (ApiVersion.V3.equals(apiVersion)) {
                return new EtcdV3ProxyImpl(address, authToken);
            } else {
                throw new IllegalArgumentException("Unknown api version: " + apiVersion);
            }
        }

        @Override
        public EtcdProxy getEtcdProxy(String registry) {
            EtcdCluster cluster = clusterManager.getCluster(registry);
            if (cluster == null) {
                throw new IllegalArgumentException("Unknown cluster " + registry);
            }
            return getEtcdProxy(registry, cluster.getAddress(), cluster.getApiVersion());
        }

    }

}
