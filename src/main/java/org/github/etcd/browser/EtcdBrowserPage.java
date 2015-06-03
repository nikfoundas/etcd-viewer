/**
 *
 */
package org.github.etcd.browser;

import java.util.List;

import javax.inject.Inject;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.EventPropagation;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.github.etcd.cluster.ClusterManager;
import org.github.etcd.cluster.EtcdCluster;
import org.github.etcd.html.cluster.AddClusterFormPanel;
import org.github.etcd.html.cluster.ClusterDetailsPanel;
import org.github.etcd.rest.EtcdNode;
import org.github.etcd.rest.EtcdResource;
import org.github.etcd.rest.EtcdResourceProxy;
import org.github.etcd.rest.EtcdResponse;

public class EtcdBrowserPage extends TemplatePage {

    private static final long serialVersionUID = 4514087203542463203L;

    @Inject
    private EtcdResource etcdResource;

    @Inject
    private EtcdResourceProxy proxy;

    @Inject
    private ClusterManager clusterManager;

    @Inject
    private IModel<String> selectedCluster;

    private WebMarkupContainer clustersContainer;

    public EtcdBrowserPage(PageParameters parameters) {
        super(parameters);

        IModel<EtcdCluster> currentCluster = new LoadableDetachableModel<EtcdCluster>() {
            @Override
            protected EtcdCluster load() {
                return selectedCluster.getObject() == null ? null : clusterManager.getCluster(selectedCluster.getObject());
            }
        };

        add(clustersContainer = new WebMarkupContainer("clusters"));

        clustersContainer.setOutputMarkupId(true);

        WebMarkupContainer connectedCluster;

        clustersContainer.add(connectedCluster = new WebMarkupContainer("connectedCluster") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(selectedCluster.getObject() != null);
            }
        });

        connectedCluster.add(new Label("name", selectedCluster));

        connectedCluster.add(new Label("address", new PropertyModel<>(currentCluster, "address")));

        clustersContainer.add(new WebMarkupContainer("noConnectedCluster") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(selectedCluster.getObject() == null);
            }
        });

        IModel<List<EtcdCluster>> clusterList = new LoadableDetachableModel<List<EtcdCluster>>() {
            @Override
            protected List<EtcdCluster> load() {
                System.out.println("Loading clusters");
                return clusterManager.getClusters();
            }
        };

        clustersContainer.add(new ListView<EtcdCluster>("clustersDropdown", clusterList) {
            @Override
            protected void populateItem(ListItem<EtcdCluster> item) {
                AjaxLink<String> select;
                item.add(select = new AjaxLink<String>("select", new PropertyModel<String>(item.getModel(), "name")) {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        selectedCluster.setObject(getModelObject());

                        System.out.println("Selected: " + selectedCluster.getObject());

                        target.add(clustersContainer);

                    }
                    @Override
                    protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                        super.updateAjaxAttributes(attributes);
                        attributes.setEventPropagation(EventPropagation.BUBBLE);
                    }
                });

                select.add(new Label("name", new PropertyModel<>(item.getModel(), "name")));
                select.add(new Label("address", new PropertyModel<>(item.getModel(), "address")));
            }
        });

        clustersContainer.add(new AjaxLink<Void>("disconnect") {
            @Override
            public void onClick(AjaxRequestTarget target) {

                selectedCluster.setObject(null);

                System.out.println("Disconnected");

                target.add(clustersContainer);
            }
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setEnabled(selectedCluster.getObject() != null);
            }
        });

        add(new ClusterDetailsPanel("clusterDetails", currentCluster, null));

        add(new AddClusterFormPanel("addCluster"));

        IModel<EtcdResponse> response = new LoadableDetachableModel<EtcdResponse>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected EtcdResponse load() {

                String key = ConvertUtils.getEtcdKey(getPageParameters());
                System.out.println("################### LOADING: " + key);

                try {
                    return etcdResource.getNode(key);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };

        add(new EtcdNodePanel("node", new PropertyModel<EtcdNode>(response, "node")) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onDeleteNode(AjaxRequestTarget target, EtcdNode nodeToDelete) {
                super.onDeleteNode(target, nodeToDelete);

                System.out.println("$$$$$$$$$$$$$$$$$$$ DELETING: " + nodeToDelete.getKey());

                if (nodeToDelete.isDir()) {
                    proxy.deleteDirectory(nodeToDelete.getKey());
                } else {
                    proxy.deleteValue(nodeToDelete.getKey());
                }

            }
        });

        add(new BookmarkablePageLink<>("addNode", EditNodePage.class));

        createClustersDropDown();
    }

    private void createClustersDropDown() {

        IModel<List<String>> choices = new LoadableDetachableModel<List<String>>() {
            @Override
            protected List<String> load() {
                return clusterManager.getClusterNames();
            }
        };

        IChoiceRenderer<String> choiceRenderer = new IChoiceRenderer<String>() {
            @Override
            public Object getDisplayValue(String object) {
                return object + " @ " + clusterManager.getCluster(object).getAddress();
            }
            @Override
            public String getIdValue(String object, int index) {
                return object;
            }
        };

        add(new DropDownChoice<String>("ddc", selectedCluster, choices, choiceRenderer) {
            @Override
            protected boolean wantOnSelectionChangedNotifications() {
                return true;
            }
            @Override
            protected void onSelectionChanged(String newSelection) {
                super.onSelectionChanged(newSelection);

                System.out.println("Selection is now: " + newSelection);

                System.out.println("Value of selected cluster is: " + selectedCluster.getObject());
            }
        });
    }

}
