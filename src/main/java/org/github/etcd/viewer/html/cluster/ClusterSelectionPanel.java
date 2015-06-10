package org.github.etcd.viewer.html.cluster;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.EventPropagation;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.github.etcd.service.ClusterManager;
import org.github.etcd.service.EtcdCluster;
import org.github.etcd.service.EtcdPeer;

public class ClusterSelectionPanel extends Panel {

    private static final long serialVersionUID = 1L;

    @Inject
    private ClusterManager clusterManager;

    @Inject
    private IModel<String> selectedCluster;

    private WebMarkupContainer selectInputGroup;
    private WebMarkupContainer body;
    private WebMarkupContainer clusterPeers;

    private AjaxLink<Void> deleteCluster;

    private AddClusterModalPanel addClusterModal;
    private DeleteClusterModalPanel deleteClusterModal;

    public ClusterSelectionPanel(String id) {
        super(id);

        add(addClusterModal = new AddClusterModalPanel("addClusterModal") {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onClusterAdded(AjaxRequestTarget target, EtcdCluster addedCluster) {
                super.onClusterAdded(target, addedCluster);

                selectedCluster.setObject(addedCluster.getName());
                onSelectedClusterChanged(target);
            }
        });

        add(deleteClusterModal = new DeleteClusterModalPanel("deleteClusterModal", selectedCluster) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onClusterDeleted(AjaxRequestTarget target) {
                super.onClusterDeleted(target);

                selectedCluster.setObject(null);
                onSelectedClusterChanged(target);
            }
        });

        add(deleteCluster = new AjaxLink<Void>("deleteCluster") {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("data-target", "#" + deleteClusterModal.getMarkupId());
            }
            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);
                attributes.setEventPropagation(EventPropagation.BUBBLE);
            }
            @Override
            public void onClick(AjaxRequestTarget target) {
                deleteClusterModal.onShowModal(target);
            }
            @Override
            protected void onConfigure() {
                super.onConfigure();
                if (selectedCluster.getObject() == null) {
                    add(AttributeAppender.append("disabled", "disabled"));
                } else {
                    add(AttributeModifier.remove("disabled"));
                }
            }
        });

        add(selectInputGroup = new WebMarkupContainer("selectInputGroup"));

        selectInputGroup.setOutputMarkupId(true);

        selectInputGroup.add(new TextField<>("selectedCluster", new LoadableDetachableModel<String>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected String load() {
                return selectedCluster.getObject() == null ? null : selectedCluster.getObject() + " @ " + clusterManager.getCluster(selectedCluster.getObject()).getAddress();
            }
        }));

        IModel<List<EtcdCluster>> clusterList = new LoadableDetachableModel<List<EtcdCluster>>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected List<EtcdCluster> load() {
                return clusterManager.getClusters();
            }
        };

        selectInputGroup.add(new ListView<EtcdCluster>("clusters", clusterList) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void populateItem(ListItem<EtcdCluster> item) {
                AjaxLink<String> select;
                IModel<String> name = new PropertyModel<String>(item.getModel(), "name");
                item.add(select = new AjaxLink<String>("select", name) {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        selectedCluster.setObject(getModelObject());

                        onSelectedClusterChanged(target);
                    }
                    @Override
                    protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                        super.updateAjaxAttributes(attributes);
                        attributes.setEventPropagation(EventPropagation.BUBBLE);
                    }
                });

                select.add(new Label("name", name));
                select.add(new Label("address", new PropertyModel<>(item.getModel(), "address")));
            }
        });

        selectInputGroup.add(new AjaxLink<Void>("addCluster") {
            private static final long serialVersionUID = 1L;
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


        add(body = new WebMarkupContainer("body"));
        body.setOutputMarkupId(true);

        IModel<EtcdCluster> cluster = new LoadableDetachableModel<EtcdCluster>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected EtcdCluster load() {
                return clusterManager.getCluster(selectedCluster.getObject());
            }
        };

//        body.add(new ClusterDetailsPanel("clusterDetails", cluster, null));

        body.add(new Label("name", new PropertyModel<>(cluster, "name")));

        body.add(DateLabel.forDatePattern("lastRefreshTime", new PropertyModel<Date>(cluster, "lastRefreshTime"), "yyyy-MM-dd'T'HH:mm:ss.SSSZ"));

        body.add(new Label("address", new PropertyModel<>(cluster, "address")));

        body.add(new AjaxLink<String>("refresh", new PropertyModel<String>(cluster, "name")) {
            private static final long serialVersionUID = 1L;
            @Override
            public void onClick(AjaxRequestTarget target) {

                clusterManager.refreshCluster(getModelObject());

                target.add(body, clusterPeers);
            }
        });

        add(clusterPeers = new ClusterPeersPanel("clusterPeers", new PropertyModel<List<EtcdPeer>>(cluster, "peers")));

    }

    protected void onSelectedClusterChanged(AjaxRequestTarget target) {

        try {
            clusterManager.refreshCluster(selectedCluster.getObject());
        } catch (Exception e) {
            e.printStackTrace();
        }

        target.add(selectInputGroup, deleteCluster, body, clusterPeers);
    }

}
