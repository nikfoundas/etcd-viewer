/**
 *
 */
package org.github.etcd.service;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.wicket.Session;
import org.apache.wicket.model.IModel;
import org.github.etcd.service.impl.CachingResourceProxyFactory;
import org.github.etcd.service.impl.ClusterManagerImpl;
import org.github.etcd.service.impl.EtcdManagerImpl;
import org.github.etcd.service.rest.EtcdApiResource;

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

        bind(EtcdApiResource.class).toProvider(EtcdResourceProvider.class);//.asEagerSingleton();

        bind(ResourceProxyFactory.class).to(CachingResourceProxyFactory.class).in(Singleton.class);


        bind(EtcdManager.class).toProvider(EtcdManagerProvider.class);//.in(Singleton.class);

        bind(ClusterManager.class).to(ClusterManagerImpl.class).in(Singleton.class);

        bind(EtcdManagerRouter.class).to(EtcdManagerRouterImpl.class).in(Singleton.class);

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

    private static class EtcdManagerProvider implements Provider<EtcdManager> {

        @Inject
        private Provider<EtcdApiResource> resource;

        @Override
        public EtcdManager get() {
            return new EtcdManagerImpl(resource.get());
        }
    }

    private static class EtcdManagerRouterImpl implements EtcdManagerRouter {

        @Inject
        private ResourceProxyFactory proxyFactory;

        @Override
        public EtcdManager getEtcdManager(String address) {
            EtcdApiResource resource = proxyFactory.createProxy(address, EtcdApiResource.class);
            return new EtcdManagerImpl(resource);
        }
    }

    public static class EtcdResourceProvider implements Provider<EtcdApiResource> {

//        @Inject
//        @Named(ETCD_NODE)
//        private String etcdNodeAddress;

        @Inject
        private IModel<String> selectedCluster;

        @Inject
        private ResourceProxyFactory factory;

        @Inject
        private ClusterManager clusterManager;

        @Override
        public EtcdApiResource get() {
            System.out.println("RestModule.EtcdKeyValueStoreProvider.get() " + selectedCluster.getObject());

            if (selectedCluster.getObject() != null) {
                return factory.createProxy(clusterManager.getCluster(selectedCluster.getObject()).getAddress(), EtcdApiResource.class);
            } else {
//                return factory.createProxy(etcdNodeAddress, EtcdResource.class);
                return null;
            }

        }
    }
}
