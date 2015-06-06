package org.github.etcd.html.node;

import javax.inject.Inject;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.github.etcd.browser.FormGroupBorder;
import org.github.etcd.rest.EtcdManager;
import org.github.etcd.rest.EtcdNode;

public class EditNodeModalPanel extends GenericPanel<EtcdNode> {

    private static final long serialVersionUID = 1L;

    private final IModel<Boolean> updating;

    private EtcdNodeForm form;

    @Inject
    private EtcdManager etcdManager;

    public EditNodeModalPanel(String id, IModel<EtcdNode> model, IModel<Boolean> updating) {
        super(id, model);
        this.updating = updating;

        setOutputMarkupId(true);
        add(AttributeAppender.append("class", "modal fade"));

        add(form = new EtcdNodeForm("form", new CompoundPropertyModel<>(model)));

        add(new AjaxSubmitLink("save", form) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);

                onNodeSave(target);

                target.appendJavaScript("$('#" + EditNodeModalPanel.this.getMarkupId() + "').modal('hide');");
            }

            @Override
            protected void onAfterSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onAfterSubmit(target, form);
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

        public EtcdNodeForm(String id, IModel<EtcdNode> model) {
            super(id, model);

//            add(new FeedbackPanel("feedback") {
//                private static final long serialVersionUID = 1L;
//                @Override
//                protected void onConfigure() {
//                    setVisible(anyMessage());
//                }
//            });

            add(new FormGroupBorder("keyGroup", new ResourceModel("key.label", "Key")).add(new TextField<String>("key") {
                private static final long serialVersionUID = 1L;
                @Override
                protected void onConfigure() {
                    super.onConfigure();
                    setEnabled(!updating.getObject());
                }
            }.setRequired(true)));

            add(new FormGroupBorder("valueGroup", new ResourceModel("value.label", "Value")).add(new TextArea<String>("value") {
                private static final long serialVersionUID = 1L;
                @Override
                protected void onConfigure() {
                    setEnabled(!EtcdNodeForm.this.getModelObject().isDir());
                }
            }));

            add(new AjaxCheckBox("dir") {
                private static final long serialVersionUID = 1L;
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.add(EtcdNodeForm.this);
                }
                @Override
                protected void onConfigure() {
                    super.onConfigure();
                    setEnabled(!updating.getObject());
                }
            });
//            add(new CheckBox("dir") {
//                private static final long serialVersionUID = 1L;
//                @Override
//                protected boolean wantOnSelectionChangedNotifications() {
//                    return true;
//                }
//                @Override
//                protected void onConfigure() {
//                    super.onConfigure();
//                    setEnabled(!updating.getObject());
//                }
//            });
            add(new Label("dirLabel", new ResourceModel("dir.label", "Directory Node")));

            add(new FormGroupBorder("ttlGroup", new ResourceModel("ttl.label", "Time To Live")).add(new TextField<>("ttl")));

//            add(new AjaxSubmitLink("cancel") {
//                private static final long serialVersionUID = 1L;
//                @SuppressWarnings("unchecked")
//                @Override
//                protected void onAfterSubmit(AjaxRequestTarget target, Form<?> form) {
//                    super.onAfterSubmit(target, form);
//
//                    onFormCancel(target, (Form<EtcdNode>) form);
//                }
//            }.setDefaultFormProcessing(false));
        }

    }

    public void onShowModal(AjaxRequestTarget target) {
        target.add(form);

        form.clearInput();
    }
    protected void onNodeSave(AjaxRequestTarget target) {
        System.out.println("Please save: " + getModelObject());

        etcdManager.saveOrUpdate(getModelObject(), updating.getObject());
    }
}
