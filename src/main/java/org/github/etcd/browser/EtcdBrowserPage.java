/**
 *
 */
package org.github.etcd.browser;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.github.etcd.cluster.ClusterManager;
import org.github.etcd.html.cluster.ClusterSelectionPanel;
import org.github.etcd.rest.EtcdManager;
import org.github.etcd.rest.EtcdNode;
import org.github.etcd.rest.EtcdResponse;

public class EtcdBrowserPage extends TemplatePage {

    private static final long serialVersionUID = 1L;

    @Inject
    private ClusterManager clusterManager;

    @Inject
    private Provider<EtcdManager> manager;

    @Inject
    private IModel<String> selectedCluster;

    private EtcdNodePanel node;

    public EtcdBrowserPage(PageParameters parameters) {
        super(parameters);

        add(new ClusterSelectionPanel("clusterSelection") {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onSelectedClusterChanged(AjaxRequestTarget target) {
                super.onSelectedClusterChanged(target);

                target.add(node);
            }
        });

        IModel<EtcdResponse> response = new LoadableDetachableModel<EtcdResponse>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected EtcdResponse load() {

//                String key = ConvertUtils.getEtcdKey(getPageParameters());

                String key = getPageParameters().get("key").toString("/");

                System.out.println("################### LOADING: " + key);

                try {
                    return manager.get().getNode(key);
//                    return router.getResource(clusterManager.getCluster(selectedCluster.getObject()).getAddress()).getNode(key);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };

//        add(node = new EtcdNodePanel("node", new PropertyModel<EtcdNode>(response, "node")));

        add(node = new EtcdNodePanel("node"));

    }


}
