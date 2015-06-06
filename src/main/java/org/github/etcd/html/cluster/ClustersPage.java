package org.github.etcd.html.cluster;

import java.util.List;

import javax.inject.Inject;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.EventPropagation;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.github.etcd.browser.TemplatePage;
import org.github.etcd.cluster.ClusterManager;
import org.github.etcd.cluster.EtcdCluster;
import org.github.etcd.rest.EtcdManagerRouter;

public class ClustersPage extends TemplatePage {

    private static final long serialVersionUID = 1L;

    @Inject
    private ClusterManager clusterManager;

    @Inject
    private EtcdManagerRouter router;


    private WebMarkupContainer container;

    private IModel<String> selectedCluster = Model.of();

    private ClusterDetailsPanel clusterDetails;

    private WebMarkupContainer deleteModal;

    private Label modalClusterName;

    private ClusterListPanel clusterList;

    public ClustersPage() {

        IModel<List<EtcdCluster>> clusters = new LoadableDetachableModel<List<EtcdCluster>>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected List<EtcdCluster> load() {
                return clusterManager.getClusters();
            }
        };

        add(deleteModal = new WebMarkupContainer("deleteModal"));

        deleteModal.setOutputMarkupId(true);

        deleteModal.add(modalClusterName = new Label("modalClusterName", selectedCluster));

        modalClusterName.setOutputMarkupId(true);

        deleteModal.add(new AjaxLink<Void>("delete") {
            private static final long serialVersionUID = 1L;
            @Override
            public void onClick(AjaxRequestTarget target) {
                clusterManager.removeCluster(selectedCluster.getObject());
//                target.add(container);
                target.appendJavaScript("$('#" + deleteModal.getMarkupId() + "').modal('hide');");

                selectedCluster.setObject(null);

                target.add(clusterDetails, clusterList);
            }
        });

        add(clusterList = new ClusterListPanel("clusterList", clusters, selectedCluster) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onSelectionChanged(AjaxRequestTarget target) {
                super.onSelectionChanged(target);

                System.out.println("Selected now is: " + selectedCluster.getObject());

                selectedCluster.setObject(selectedCluster.getObject());

                target.add(clusterDetails);
            }
        });

        container = new WebMarkupContainer("container");

        container.setOutputMarkupId(true);

        container.add(new AjaxLink<Void>("delete") {
            private static final long serialVersionUID = 1L;
            @Override
            public void onClick(AjaxRequestTarget target) {
                target.add(modalClusterName);
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();

                setEnabled(selectedCluster.getObject() != null && !"".equals(selectedCluster.getObject()));
            }

            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);

                tag.put("data-target", "#" + deleteModal.getMarkupId());
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);
                attributes.setEventPropagation(EventPropagation.BUBBLE);
            }
        }
        .add(AttributeAppender.append("class", new LoadableDetachableModel<String>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected String load() {
                return selectedCluster.getObject() != null && !"".equals(selectedCluster.getObject()) ? "" :"disabled";
            }
        })));


        add(new AddClusterFormPanel("addCluster") {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onClusterAdded(AjaxRequestTarget target, EtcdCluster addedCluster) {
                super.onClusterAdded(target, addedCluster);

                target.add(clusterList);
            }
        });

        IModel<EtcdCluster> clusterDetailsModel = new LoadableDetachableModel<EtcdCluster>() {
            @Override
            protected EtcdCluster load() {
                if (selectedCluster.getObject() == null) {
                    return null;
                }
                return clusterManager.getCluster(selectedCluster.getObject());
            }
        };

        add(clusterDetails = new ClusterDetailsPanel("clusterDetails", clusterDetailsModel, deleteModal) {
            @Override
            protected void onClusterDelete(AjaxRequestTarget target, String clusterName) {
                super.onClusterDelete(target, clusterName);

                target.add(modalClusterName);
            }
        });

    }

}
