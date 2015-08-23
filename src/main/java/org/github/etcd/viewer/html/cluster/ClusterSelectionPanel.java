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

//    @Inject
    private IModel<String> selectedCluster;

    private WebMarkupContainer selectInputGroup;
    private WebMarkupContainer body;
    private WebMarkupContainer clusterPeers;

    private AjaxLink<Void> deleteCluster;

    private AddClusterModalPanel addClusterModal;
    private DeleteClusterModalPanel deleteClusterModal;

/*    private IModel<Boolean> clusterExpanded = new IModel<Boolean>() {
        private static final long serialVersionUID = 1L;
        @Override
        public void detach() {
        }
        @Override
        public Boolean getObject() {
            Boolean value = (Boolean) Session.get().getAttribute("clusterExpanded");
            return value == null ? false : value;
        }
        @Override
        public void setObject(Boolean object) {
            Session.get().setAttribute("clusterExpanded", object);
        }
    };*/

    public ClusterSelectionPanel(String id) {
        super(id);

        selectedCluster = new LoadableDetachableModel<String>() {
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

                onSelectedClusterDeleted(target);
            }
        });

        add(deleteCluster = new TriggerModalLink<Void>("deleteCluster", deleteClusterModal) {
            private static final long serialVersionUID = 1L;
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
                return selectedCluster.getObject() == null || clusterManager.getCluster(selectedCluster.getObject()) == null ? null : selectedCluster.getObject() + " @ " + clusterManager.getCluster(selectedCluster.getObject()).getAddress();
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

                item.add(new AttributeAppender("class", new ChainingModel<String>(name) {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public String getObject() {
                        return super.getObject().equals(selectedCluster.getObject()) ? "disabled" : "";
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
                return clusterManager.getCluster(selectedCluster.getObject());
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

    private void refreshCluster() {
        try {
            clusterManager.refreshCluster(selectedCluster.getObject());
            success("Successfully refreshed etcd cluster: " + selectedCluster.getObject());
        } catch (Exception e) {
            error("Failed to connect to etcd cluster: " + selectedCluster.getObject());
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
