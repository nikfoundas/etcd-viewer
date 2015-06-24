/**
 *
 */
package org.github.etcd.viewer.html.pages;

import javax.inject.Inject;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.github.etcd.viewer.ConvertUtils;
import org.github.etcd.viewer.html.cluster.ClusterSelectionPanel;
import org.github.etcd.viewer.html.node.EtcdNodePanel;

public class NavigationPage extends TemplatePage {

    private static final long serialVersionUID = 1L;

    private EtcdNodePanel node;

    private IModel<String> key;

    @Inject
    private IModel<String> selectedCluster;

    public NavigationPage(PageParameters parameters) {
        super(parameters);

        // get cluster name from the page parameters
        IModel<String> cluster = new LoadableDetachableModel<String>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected String load() {
                System.out
                        .println("NavigationPage.NavigationPage(...).new LoadableDetachableModel() {...}.load()");
                String clusterName = getPageParameters().get("cluster").toString(null);
                if (clusterName != null) {
                    selectedCluster.setObject(clusterName);
                }
                return clusterName;
            }
        };

        setDefaultModel(cluster);

        // get initial key from the page parameters
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
