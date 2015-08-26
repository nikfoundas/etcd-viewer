package org.github.etcd.viewer.html.cluster;

import java.util.List;

import javax.inject.Inject;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.github.etcd.service.ClusterManager;
import org.github.etcd.service.EtcdCluster;
import org.github.etcd.viewer.html.modal.GenericModalPanel;
import org.github.etcd.viewer.html.modal.TriggerModalLink;

public class RegistryListPanel extends GenericPanel<List<EtcdCluster>> {

    private static final long serialVersionUID = 1L;

    @Inject
    private ClusterManager clusterManager;

    private WebMarkupContainer panelGroup;

    private AddClusterModalPanel addClusterModal;
    private DeleteClusterModalPanel deleteClusterModal;

    private IModel<String> registryToDelete = Model.of();

    public RegistryListPanel(String id) {
        super(id);

        setModel(new LoadableDetachableModel<List<EtcdCluster>>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected List<EtcdCluster> load() {
                return clusterManager.getClusters();
            }
        });

        add(addClusterModal = new AddClusterModalPanel("addClusterModal") {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onClusterAdded(AjaxRequestTarget target, EtcdCluster addedCluster) {
                super.onClusterAdded(target, addedCluster);

                target.add(panelGroup);
            }
        });

        add(deleteClusterModal = new DeleteClusterModalPanel("deleteClusterModal", registryToDelete) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onClusterDeleted(AjaxRequestTarget target) {
                super.onClusterDeleted(target);

                target.add(panelGroup);
            }
        });

        add(panelGroup = new WebMarkupContainer("panelGroup"));
        panelGroup.setOutputMarkupId(true);

        panelGroup.add(new ListView<EtcdCluster>("registries", getModel()) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void populateItem(ListItem<EtcdCluster> item) {
                item.add(new RegistryPanel("registry", item.getModel()));
            }
        });

        add(new TriggerModalLink<Void>("addCluster", addClusterModal));

    }

    protected String getPanelGroupId() {
        return panelGroup.getMarkupId();
    }

    protected GenericModalPanel<?> getDeleteClusterModal() {
        return deleteClusterModal;
    }

    protected void setRegistryToDelete(String registryName) {
        registryToDelete.setObject(registryName);
    }

}
