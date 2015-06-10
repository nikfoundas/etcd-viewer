package org.github.etcd.viewer.html.cluster;

import javax.inject.Inject;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.github.etcd.service.ClusterManager;

public class DeleteClusterModalPanel extends GenericPanel<String> {

    private static final long serialVersionUID = 1L;

    @Inject
    private ClusterManager clusterManager;

    private Label name;

    public DeleteClusterModalPanel(String id, IModel<String> model) {
        super(id, model);

        setOutputMarkupId(true);
        add(AttributeAppender.append("class", "modal fade"));

        add(name = new Label("name", getModel()));
        name.setOutputMarkupId(true);

        add(new AjaxLink<String>("delete", getModel()) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {

                clusterManager.removeCluster(getModelObject());

                onClusterDeleted(target);

                target.appendJavaScript("$('#" + DeleteClusterModalPanel.this.getMarkupId() + "').modal('hide');");
            }
        });
    }

    public void onShowModal(AjaxRequestTarget target) {
        target.add(name);
    }

    protected void onClusterDeleted(AjaxRequestTarget target) {

    }

}
