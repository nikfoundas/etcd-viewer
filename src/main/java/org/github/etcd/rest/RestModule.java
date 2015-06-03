/**
 *
 */
package org.github.etcd.rest;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.wicket.Session;
import org.apache.wicket.model.IModel;
import org.github.etcd.cluster.ClusterManager;
import org.github.etcd.cluster.ClusterManagerImpl;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

public class RestModule extends AbstractModule {

    private static final String ETCD_NODE = "ETCD_NODE";

    public static final String SELECTED_CLUSTER_NAME = "selectedCluster";

    @Override
    protected void configure() {

        String etcdAddress = System.getenv(ETCD_NODE);
        if (etcdAddress == null) {
            etcdAddress = System.getProperty(ETCD_NODE, "http://localhost:4001/");
        }

        bindConstant().annotatedWith(Names.named(ETCD_NODE)).to(etcdAddress);

        bind(EtcdResource.class).toProvider(EtcdResourceProvider.class);//.asEagerSingleton();

//        bind(EtcdResourceProxy.class);

        bind(EtcdManager.class).to(EtcdResourceProxy.class).in(Singleton.class);

        bind(ClusterManager.class).to(ClusterManagerImpl.class).in(Singleton.class);

        bind(ResourceProxyFactory.class).to(CachingResourceProxyFactory.class).in(Singleton.class);

        bind(EtcdResourceRouter.class).to(EtcdResourceRouterImpl.class).in(Singleton.class);

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

    private static class EtcdResourceRouterImpl implements EtcdResourceRouter {

        @Inject
        private ResourceProxyFactory proxyFactory;

        @Override
        public EtcdResource getResource(String address) {
            return proxyFactory.createProxy(address, EtcdResource.class);
        }
    }

    public static class EtcdResourceProvider implements Provider<EtcdResource> {

        @Inject
        @Named(ETCD_NODE)
        private String etcdNodeAddress;

        @Inject
        private IModel<String> selectedCluster;

        @Inject
        private ResourceProxyFactory factory;

        @Inject
        private ClusterManager clusterManager;

        @Override
        public EtcdResource get() {
            System.out.println("RestModule.EtcdKeyValueStoreProvider.get() " + selectedCluster.getObject());

            if (selectedCluster.getObject() != null) {
                return factory.createProxy(clusterManager.getCluster(selectedCluster.getObject()).getAddress(), EtcdResource.class);
            } else {
                return factory.createProxy(etcdNodeAddress, EtcdResource.class);
            }

        }
    }
}
