package org.github.etcd.viewer.html.node;

import javax.inject.Inject;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.github.etcd.service.EtcdProxyFactory;
import org.github.etcd.service.rest.EtcdException;
import org.github.etcd.service.rest.EtcdNode;
import org.github.etcd.service.rest.EtcdProxy;
import org.github.etcd.viewer.html.modal.GenericModalPanel;
import org.github.etcd.viewer.html.utils.FormGroupBorder;

public class EditNodeModalPanel extends GenericModalPanel<EtcdNode> {

    private static final long serialVersionUID = 1L;

    private final IModel<Boolean> updating;

    private Label title;
    private EtcdNodeForm form;

    private final IModel<String> registry;

    @Inject
    private EtcdProxyFactory proxyFactory;

    public EditNodeModalPanel(String id, IModel<EtcdNode> model, IModel<String> registryName, IModel<Boolean> updatingModel) {
        super(id, model);
        this.updating = updatingModel;
        this.registry = registryName;

        add(title = new Label("title", new StringResourceModel("editModal.title.updating.${}", updating, "Edit Node")));
        title.setOutputMarkupId(true);

        add(form = new EtcdNodeForm("form", new CompoundPropertyModel<>(model)));

        add(new AjaxSubmitLink("save", form) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);

                try (EtcdProxy p = proxyFactory.getEtcdProxy(registry.getObject())) {
                    try {
                        if (updating.getObject()) {
                            p.updateNode(getModelObject());

                            success("Updated: " + getModelObject());

                        } else {
                            p.saveNode(getModelObject());

                            success("Created: " + getModelObject());
                        }
                    } catch (EtcdException e) {
                        System.err.println("Caught error: " + e);
                        error(e.toString());
                        error(" - API error: " + e.getApiError());
                        error(" - " + e.getCause());
                    }
                }

                onNodeSaved(target);

                modalHide(target);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                super.onError(target, form);

                target.add(form);
            }
        });

    }

    public class EtcdNodeForm extends Form<EtcdNode> {

        private static final long serialVersionUID = 1L;

        private FormGroupBorder valueGroup;

        public EtcdNodeForm(String id, IModel<EtcdNode> model) {
            super(id, model);

            add(new FormGroupBorder("keyGroup", new ResourceModel("editModal.form.key.label", "Key")).add(new TextField<String>("key") {
                private static final long serialVersionUID = 1L;
                @Override
                protected void onConfigure() {
                    super.onConfigure();
                    setEnabled(!updating.getObject());
                }
            }.setRequired(true)));

            add(valueGroup = new FormGroupBorder("valueGroup", new ResourceModel("editModal.form.value.label", "Value")) {
                private static final long serialVersionUID = 1L;
                @Override
                protected void onConfigure() {
                    setVisible(!EtcdNodeForm.this.getModelObject().isDir());
                }
            });
            valueGroup.setOutputMarkupPlaceholderTag(true);
            valueGroup.add(new TextArea<String>("value") {
                private static final long serialVersionUID = 1L;
                @Override
                protected void onConfigure() {
                    setEnabled(!EtcdNodeForm.this.getModelObject().isDir());
                }
            });

            WebMarkupContainer dirGroup;
            add(dirGroup = new WebMarkupContainer("dirGroup") {
                private static final long serialVersionUID = 1L;
                @Override
                protected void onConfigure() {
                    super.onConfigure();
                    setVisible(!updating.getObject());
                }
            });

            dirGroup.add(new AjaxCheckBox("dir") {
                private static final long serialVersionUID = 1L;
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.add(valueGroup);
                }
                @Override
                protected void onConfigure() {
                    super.onConfigure();
                    setEnabled(!updating.getObject());
                }
            });

            dirGroup.add(new Label("dirLabel", new ResourceModel("editModal.form.dir.label", "Directory")));

            add(new FormGroupBorder("ttlGroup", new ResourceModel("editModal.form.ttl.label", "Time to live")).add(new TextField<>("ttl")));

        }

    }

    @Override
    public void beforeModalShow(AjaxRequestTarget target) {
        target.add(title, form);

        form.clearInput();
    }

    protected void onNodeSaved(AjaxRequestTarget target) {
    }

}
