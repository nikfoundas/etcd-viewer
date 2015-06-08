/**
 *
 */
package org.github.etcd.browser;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.github.etcd.cluster.ClusterManager;
import org.github.etcd.html.cluster.ClusterSelectionPanel;
import org.github.etcd.html.node.EtcdNodePanel;
import org.github.etcd.rest.EtcdManager;

public class NavigationPage extends TemplatePage {

    private static final long serialVersionUID = 1L;

    @Inject
    private ClusterManager clusterManager;

    @Inject
    private Provider<EtcdManager> manager;

    @Inject
    private IModel<String> selectedCluster;

    private IModel<String> cluster;

    private EtcdNodePanel node;

    private IModel<String> key;

    public NavigationPage(PageParameters parameters) {
        super(parameters);

        cluster = new LoadableDetachableModel<String>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected String load() {
                return getPageParameters().get("cluster").toString(null);
            }
        };

        key = new LoadableDetachableModel<String>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected String load() {
                return ConvertUtils.getEtcdKey(getPageParameters());
            }
        };

        add(new ClusterSelectionPanel("clusterSelection") {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onSelectedClusterChanged(AjaxRequestTarget target) {
                super.onSelectedClusterChanged(target);

                key.setObject(EtcdNodePanel.ROOT_KEY);

                target.add(node);
            }
        });

        add(node = new EtcdNodePanel("node", key));

    }


}
