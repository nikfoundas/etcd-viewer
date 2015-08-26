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
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.github.etcd.service.ClusterManager;
import org.github.etcd.service.EtcdCluster;
import org.github.etcd.service.rest.EtcdMember;

public class RegistryPanel extends GenericPanel<EtcdCluster> {

    private static final long serialVersionUID = 1L;

    @Inject
    private ClusterManager clusterManager;

    private WebMarkupContainer heading;
    private WebMarkupContainer collapsingBody;

    private IModel<Boolean> expanded = Model.of(false);

    public RegistryPanel(String id, IModel<EtcdCluster> model) {
        super(id, model);

        setOutputMarkupId(true);

        add(AttributeModifier.append("class", new AbstractReadOnlyModel<String>() {
            private static final long serialVersionUID = 1L;
            @Override
            public String getObject() {
                StringBuffer sb = new StringBuffer("panel panel-default");
                if (getModelObject().isAuthEnabled()) {
                    sb.append(" auth-enabled");
                }
                if (getModelObject().mustRefresh()) {
                    sb.append(" must-refresh");
                }
                return sb.toString();
            }
        }));

        add(heading = new WebMarkupContainer("heading"));
        heading.setOutputMarkupId(true);

        add(collapsingBody = new WebMarkupContainer("collapsingBody") {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);

                tag.put("aria-labelledby", heading.getMarkupId());

                tag.put("aria-expanded", expanded.getObject());
                if (expanded.getObject()) {
                    tag.append("class", "in", " ");
                }
            }
        });

        WebMarkupContainer link;

        heading.add(link = new WebMarkupContainer("link") {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);

                RegistryListPanel registryList = findParent(RegistryListPanel.class);
                if (registryList != null) {
                    tag.put("data-parent", "#" + registryList.getPanelGroupId());
                }

                tag.put("href", "#" + collapsingBody.getMarkupId());
                tag.put("aria-controls", collapsingBody.getMarkupId());

                tag.put("aria-expanded", expanded.getObject());
            }
        });

        link.add(new Label("name", new PropertyModel<>(getModel(), "name")));
        link.add(new Label("address", new PropertyModel<>(getModel(), "address")));

        collapsingBody.add(new ClusterMembersPanel("clusterPeers", new PropertyModel<List<EtcdMember>>(getModel(), "members")) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(RegistryPanel.this.getModelObject().isRefreshed());
            }
        });

        collapsingBody.add(DateLabel.forDatePattern("lastRefreshTime", new PropertyModel<Date>(getModel(), "lastRefreshTime"), "yyyy-MM-dd'T'HH:mm:ss.SSSZ"));

        collapsingBody.add(new AjaxLink<String>("refresh", new PropertyModel<String>(getModel(), "name")) {
            private static final long serialVersionUID = 1L;
            @Override
            public void onClick(AjaxRequestTarget target) {

                expanded.setObject(true);

                try {
                    clusterManager.refreshCluster(getModelObject());
                    info("Successfully refreshed information for cluster: " + getModelObject());
                } catch (Exception e) {
                    error("Failed to refresh information for cluster: " + getModelObject());
                }

                target.add(RegistryPanel.this);

                onRegistryRefresh(target);
            }
        });


        collapsingBody.add(new AjaxLink<Void>("delete") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);

                // let bootstrap do its magic
                attributes.setEventPropagation(EventPropagation.BUBBLE);
            }
            @Override
            protected void onConfigure() {
                super.onConfigure();

                RegistryListPanel registryList = findParent(RegistryListPanel.class);
                if (registryList != null) {
                    add(AttributeAppender.replace("data-target", "#" + registryList.getDeleteClusterModal().getMarkupId()));
                } else {
                    setVisible(false);
                }
            }
            @Override
            public void onClick(AjaxRequestTarget target) {
                RegistryListPanel registryList = findParent(RegistryListPanel.class);
                if (registryList != null) {

                    registryList.setRegistryToDelete(RegistryPanel.this.getModelObject().getName());

                    registryList.getDeleteClusterModal().beforeModalShow(target);
                }
            }
        });

    }


    protected void onRegistryRefresh(AjaxRequestTarget target) {

    }

}
