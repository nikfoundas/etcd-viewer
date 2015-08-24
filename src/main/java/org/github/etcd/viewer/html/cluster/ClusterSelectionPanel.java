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
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ChainingModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.github.etcd.service.ClusterManager;
import org.github.etcd.service.EtcdCluster;
import org.github.etcd.service.rest.EtcdMember;
import org.github.etcd.viewer.html.modal.TriggerModalLink;

public class ClusterSelectionPanel extends Panel {

    private static final long serialVersionUID = 1L;

    @Inject
    private ClusterManager clusterManager;

    private IModel<String> registry;

    private WebMarkupContainer selectInputGroup;
    private WebMarkupContainer body;
    private WebMarkupContainer clusterPeers;

    private AjaxLink<Void> deleteCluster;

    private AddClusterModalPanel addClusterModal;
    private DeleteClusterModalPanel deleteClusterModal;

    public ClusterSelectionPanel(String id) {
        super(id);

        registry = new LoadableDetachableModel<String>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected String load() {
                return getPage().getPageParameters().get("cluster").toOptionalString();
            }
        };

        add(addClusterModal = new AddClusterModalPanel("addClusterModal") {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onClusterAdded(AjaxRequestTarget target, EtcdCluster addedCluster) {
                super.onClusterAdded(target, addedCluster);

                registry.setObject(addedCluster.getName());
                onSelectedClusterChanged(target);
            }
        });

        add(deleteClusterModal = new DeleteClusterModalPanel("deleteClusterModal", registry) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onClusterDeleted(AjaxRequestTarget target) {
                super.onClusterDeleted(target);

                registry.setObject(null);

                onSelectedClusterDeleted(target);
            }
        });

        add(deleteCluster = new TriggerModalLink<Void>("deleteCluster", deleteClusterModal) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onConfigure() {
                super.onConfigure();
                if (registry.getObject() == null) {
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
                return registry.getObject() == null || clusterManager.getCluster(registry.getObject()) == null ? null : registry.getObject() + " @ " + clusterManager.getCluster(registry.getObject()).getAddress();
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
                        registry.setObject(getModelObject());

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

                item.add(new AttributeAppender("class", new ChainingModel<String>(name) {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public String getObject() {
                        return super.getObject().equals(registry.getObject()) ? "disabled" : "";
                    }
                }));
            }
        });

        selectInputGroup.add(new TriggerModalLink<Void>("addCluster", addClusterModal));


        add(body = new WebMarkupContainer("body"));
        body.setOutputMarkupId(true);

        IModel<EtcdCluster> cluster = new LoadableDetachableModel<EtcdCluster>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected EtcdCluster load() {
                return clusterManager.getCluster(registry.getObject());
            }
        };

        body.add(new Label("name", new PropertyModel<>(cluster, "name")));

        body.add(DateLabel.forDatePattern("lastRefreshTime", new PropertyModel<Date>(cluster, "lastRefreshTime"), "yyyy-MM-dd'T'HH:mm:ss.SSSZ"));

        body.add(new Label("address", new PropertyModel<>(cluster, "address")));

        body.add(new AjaxLink<String>("refresh", new PropertyModel<String>(cluster, "name")) {
            private static final long serialVersionUID = 1L;
            @Override
            public void onClick(AjaxRequestTarget target) {
                onSelectedClusterRefresh(target);
            }
        });

        add(clusterPeers = new ClusterMembersPanel("clusterPeers", new PropertyModel<List<EtcdMember>>(cluster, "members")));

    }
    @Override
    protected void onDetach() {
        super.onDetach();

        registry.detach();
    }

    private void refreshCluster() {
        try {
            clusterManager.refreshCluster(registry.getObject());
            success("Successfully refreshed etcd cluster: " + registry.getObject());
        } catch (Exception e) {
            error("Failed to connect to etcd cluster: " + registry.getObject());
        }
    }

    protected void onSelectedClusterRefresh(AjaxRequestTarget target) {
        refreshCluster();
        target.add(body, clusterPeers);
    }

    protected void onSelectedClusterChanged(AjaxRequestTarget target) {
        refreshCluster();
        target.add(selectInputGroup, deleteCluster, body, clusterPeers);
    }

    protected void onSelectedClusterDeleted(AjaxRequestTarget target) {
        target.add(selectInputGroup, deleteCluster, body, clusterPeers);
    }

}
