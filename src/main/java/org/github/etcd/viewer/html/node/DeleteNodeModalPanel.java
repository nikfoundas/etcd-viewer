package org.github.etcd.viewer.html.node;

import javax.inject.Inject;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.github.etcd.service.EtcdProxyFactory;
import org.github.etcd.service.rest.EtcdException;
import org.github.etcd.service.rest.EtcdNode;
import org.github.etcd.service.rest.EtcdProxy;
import org.github.etcd.viewer.html.modal.GenericModalPanel;

public class DeleteNodeModalPanel extends GenericModalPanel<EtcdNode> {

    private static final long serialVersionUID = 1L;

    @Inject
    private EtcdProxyFactory proxyFactory;

    private final IModel<String> registry;

    private WebMarkupContainer body;

    public DeleteNodeModalPanel(String id, IModel<EtcdNode> model, IModel<String> registryName) {
        super(id, model);

        this.registry = registryName;

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

                try (EtcdProxy p = proxyFactory.getEtcdProxy(registry.getObject())) {
                    // TODO: support both recursive and non recursive delete
                    p.deleteNode(node, node.isDir());

                    warn("Deleted: " + node);

                } catch (EtcdException e) {
                    error("Failed to delete: " + node);
                }

                onNodeDeleted(target);

                modalHide(target);
            }
        });

    }

    @Override
    public void beforeModalShow(AjaxRequestTarget target) {
        target.add(body);
    }

    protected void onNodeDeleted(AjaxRequestTarget target) {
    }
}
