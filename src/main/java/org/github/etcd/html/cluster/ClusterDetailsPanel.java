package org.github.etcd.html.cluster;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.EventPropagation;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.ChainingModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.github.etcd.cluster.ClusterManager;
import org.github.etcd.cluster.EtcdCluster;
import org.github.etcd.cluster.EtcdPeer;

public class ClusterDetailsPanel extends GenericPanel<EtcdCluster> {

    private static final long serialVersionUID = 1L;

    @Inject
    private ClusterManager clusterManager;

    private final WebMarkupContainer deleteModal;

    @Inject
    private IModel<String> selectedCluster;

    public ClusterDetailsPanel(String id, IModel<EtcdCluster> model, WebMarkupContainer parentDeleteModal) {
        super(id, model);

        this.deleteModal = parentDeleteModal;

        add(new Label("name", new PropertyModel<>(getModel(), "name")));


        add(DateLabel.forDatePattern("lastRefreshTime", new PropertyModel<Date>(getModel(), "lastRefreshTime"), "yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
//        add(new Label("lastRefreshTime", new PropertyModel<Date>(getModel(), "lastRefreshTime")));

        add(new Label("address", new PropertyModel<>(getModel(), "address")));

        add(new AjaxFallbackLink<String>("refresh", new PropertyModel<String>(getModel(), "name")) {
            private static final long serialVersionUID = 1L;
            @Override
            public void onClick(AjaxRequestTarget target) {
                clusterManager.refreshCluster(getModelObject());

                target.add(ClusterDetailsPanel.this);

                onClusterRefresh(target, getModelObject());
            }
        });

        add(new AjaxLink<String>("delete", new PropertyModel<String>(getModel(), "name")) {
            private static final long serialVersionUID = 1L;
            @Override
            public void onClick(AjaxRequestTarget target) {
//                clusterManager.refreshCluster(getModelObject());

                target.add(ClusterDetailsPanel.this);

                onClusterDelete(target, getModelObject());
            }
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);

//                tag.put("data-target", "#" + deleteModal.getMarkupId());
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);
                attributes.setEventPropagation(EventPropagation.BUBBLE);
            }
        });

        add(new ListView<EtcdPeer>("peers", new PropertyModel<List<EtcdPeer>>(getModel(), "peers")) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void populateItem(ListItem<EtcdPeer> item) {
                item.add(new Label("id", new PropertyModel<>(item.getModel(), "id")));
                item.add(new Label("etcd", new PropertyModel<>(item.getModel(), "etcd")));
                item.add(new Label("raft", new PropertyModel<>(item.getModel(), "raft")));
                item.add(new Label("status", new PropertyModel<>(item.getModel(), "status")));
                item.add(new Label("version", new PropertyModel<>(item.getModel(), "version")));

                item.add(new AttributeAppender("class", new ChainingModel<String>(item.getModel()) {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public String getObject() {
                        @SuppressWarnings("unchecked")
                        IModel<EtcdPeer> peerModel = (IModel<EtcdPeer>) getChainedModel();
                        if (peerModel.getObject().getStatus() == null) {
                            return "danger";
                        }
                        if ("leader".equals(peerModel.getObject().getStatus())) {
                            return "active";
                        }
                        return "";
                    }
                }));
            }
        });

    }


    protected void onClusterRefresh(AjaxRequestTarget target, String clusterName) {
        System.out.println("Refreshing ... " + clusterName);
    }
    protected void onClusterDelete(AjaxRequestTarget target, String clusterName) {
        System.out.println("Deleting ... " + clusterName);
    }
}
