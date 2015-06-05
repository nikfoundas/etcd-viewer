package org.github.etcd.browser;

import java.util.List;

import javax.inject.Inject;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.EventPropagation;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.github.etcd.cluster.ClusterManager;
import org.github.etcd.cluster.EtcdCluster;
import org.github.etcd.html.cluster.AddClusterModalPanel;
import org.github.etcd.html.cluster.ClusterSelectionPanel;

public class HomePage extends TemplatePage {

    private AddClusterModalPanel addClusterModal;

    private IModel<String> clusterName = Model.of();

    private IModel<String> clusterAddress = Model.of();


    @Inject
    private IModel<String> selectedCluster;

    @Inject
    private ClusterManager clusterManager;

    public HomePage() {

        add(new ClusterSelectionPanel("selectClusterPanel"));

        add(addClusterModal = new AddClusterModalPanel("addClusterModal"));

        add(new TextField<>("selectedCluster", new LoadableDetachableModel<String>() {
            @Override
            protected String load() {
                return selectedCluster.getObject() == null ? null : selectedCluster.getObject() + " @ " + clusterManager.getCluster(selectedCluster.getObject()).getAddress();
            }
        }));

        IModel<List<EtcdCluster>> clusterList = new LoadableDetachableModel<List<EtcdCluster>>() {
            @Override
            protected List<EtcdCluster> load() {
                System.out.println("Loading clusters");
                return clusterManager.getClusters();
            }
        };

        add(new ListView<EtcdCluster>("clusters", clusterList) {
            @Override
            protected void populateItem(ListItem<EtcdCluster> item) {
                AjaxLink<String> select;
                item.add(select = new AjaxLink<String>("select", new PropertyModel<String>(item.getModel(), "name")) {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        selectedCluster.setObject(getModelObject());

                        System.out.println("Selected: " + selectedCluster.getObject());
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

        add(new AjaxLink("addCluster") {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("data-target", "#" + addClusterModal.getMarkupId());
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);
                attributes.setEventPropagation(EventPropagation.BUBBLE);
            }

            @Override
            public void onClick(AjaxRequestTarget target) {
                addClusterModal.onShowModal(target);
            }
        });
    }

}
