/**
 *
 */
package org.github.etcd.service;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.wicket.Session;
import org.apache.wicket.model.IModel;
import org.github.etcd.service.impl.ClusterManagerImpl;
import org.github.etcd.service.rest.EtcdProxy;
import org.github.etcd.service.rest.impl.EtcdProxyImpl;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
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

        bind(EtcdProxy.class).toProvider(EtcdProxyProvider.class);//.in(Singleton.class);

        bind(ClusterManager.class).to(ClusterManagerImpl.class).in(Singleton.class);

        bind(EtcdProxyFactory.class).to(EtcdProxyFactoryImpl.class).in(Singleton.class);

        bind(new TypeLiteral<IModel<String>>() {}).to(SessionStoredClusterNameModel.class).in(Singleton.class);
    }

    private static class SessionStoredClusterNameModel implements IModel<String> {
        private static final long serialVersionUID = 1L;
        @Override
        public void detach() {
        }
        @Override
        public String getObject() {
            return (String) Session.get().getAttribute(SELECTED_CLUSTER_NAME);
        }
        @Override
        public void setObject(String object) {
            Session.get().setAttribute(SELECTED_CLUSTER_NAME, object);
        }
    }

    private static class EtcdProxyProvider implements Provider<EtcdProxy> {

        @Inject
        private IModel<String> selectedCluster;

        @Inject
        private ClusterManager clusterManager;

        @Inject
        private EtcdProxyFactory proxyFactory;

        @Override
        public EtcdProxy get() {
            if (selectedCluster.getObject() == null) {
                throw new RuntimeException("There is no selected cluster yet");
            }
            return proxyFactory.getEtcdProxy(clusterManager.getCluster(selectedCluster.getObject()).getAddress());
        }
    }

    private static class EtcdProxyFactoryImpl implements EtcdProxyFactory {

        @Override
        public EtcdProxy getEtcdProxy(String address) {
            return new EtcdProxyImpl(address);
        }
    }

}
