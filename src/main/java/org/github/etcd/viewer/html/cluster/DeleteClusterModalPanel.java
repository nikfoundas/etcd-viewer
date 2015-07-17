package org.github.etcd.viewer.html.cluster;

import javax.inject.Inject;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.github.etcd.service.ClusterManager;
import org.github.etcd.viewer.html.modal.GenericModalPanel;

public class DeleteClusterModalPanel extends GenericModalPanel<String> {

    private static final long serialVersionUID = 1L;

    @Inject
    private ClusterManager clusterManager;

    private Label name;

    public DeleteClusterModalPanel(String id, IModel<String> model) {
        super(id, model);

        add(name = new Label("name", getModel()));
        name.setOutputMarkupId(true);

        add(new AjaxLink<String>("delete", getModel()) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {

                clusterManager.removeCluster(getModelObject());

                onClusterDeleted(target);

                modalHide(target);
            }
        });
    }

    @Override
    public void beforeModalShow(AjaxRequestTarget target) {
        target.add(name);
    }

    protected void onClusterDeleted(AjaxRequestTarget target) {

    }

}
