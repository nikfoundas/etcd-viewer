package org.github.etcd.viewer.html.node;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.github.etcd.service.rest.EtcdNode;
import org.github.etcd.service.rest.EtcdProxy;

public class DeleteNodeModalPanel extends GenericPanel<EtcdNode> {

    private static final long serialVersionUID = 1L;

    @Inject
    private Provider<EtcdProxy> etcdProxy;

    private WebMarkupContainer body;

    public DeleteNodeModalPanel(String id, IModel<EtcdNode> model) {
        super(id, model);

        setOutputMarkupId(true);
        add(AttributeAppender.append("class", "modal fade"));

        add(body = new WebMarkupContainer("body"));
        body.setOutputMarkupId(true);

        body.add(new Label("directory", new PropertyModel<>(getModel(), "key")) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(DeleteNodeModalPanel.this.getModelObject().isDir());
            }
        });

        body.add(new Label("key", new PropertyModel<>(getModel(), "key")) {
            private static final long serialVersionUID = 1L;
            protected void onConfigure() {
                super.onConfigure();
                setVisible(!DeleteNodeModalPanel.this.getModelObject().isDir());
            }
        });

        add(new AjaxLink<Void>("delete") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {

                EtcdNode node = DeleteNodeModalPanel.this.getModelObject();

                try (EtcdProxy p = etcdProxy.get()) {
                    // TODO: support both recursive and non recursive delete
                    p.deleteNode(node, node.isDir());
                }

                onNodeDeleted(target);

                target.appendJavaScript("$('#" + DeleteNodeModalPanel.this.getMarkupId() + "').modal('hide');");
            }
        });

    }

    public void onShowModal(AjaxRequestTarget target) {
        target.add(body);
    }

    protected void onNodeDeleted(AjaxRequestTarget target) {
    }
}
