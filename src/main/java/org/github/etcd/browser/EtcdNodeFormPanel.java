package org.github.etcd.browser;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.github.etcd.html.utils.FormGroupBorder;
import org.github.etcd.rest.EtcdNode;

public class EtcdNodeFormPanel extends GenericPanel<EtcdNode> {

    private static final long serialVersionUID = 1L;

    private final IModel<Boolean> updating;

    public EtcdNodeFormPanel(String id, IModel<EtcdNode> model, IModel<Boolean> updating) {
        super(id, model);
        this.updating = updating;

        add(new EtcdNodeForm("form", new CompoundPropertyModel<>(model)));
    }

    public class EtcdNodeForm extends Form<EtcdNode> {

        private static final long serialVersionUID = 1L;

        public EtcdNodeForm(String id, IModel<EtcdNode> model) {
            super(id, model);

            add(new FeedbackPanel("feedback") {
                private static final long serialVersionUID = 1L;
                @Override
                protected void onConfigure() {
                    setVisible(anyMessage());
                }
            });

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

            add(new CheckBox("dir") {
                private static final long serialVersionUID = 1L;
                @Override
                protected boolean wantOnSelectionChangedNotifications() {
                    return true;
                }
                @Override
                protected void onConfigure() {
                    super.onConfigure();
                    setEnabled(!updating.getObject());
                }
            });
            add(new Label("dirLabel", new ResourceModel("dir.label", "Directory Node")));

            add(new FormGroupBorder("ttlGroup", new ResourceModel("ttl.label", "Time To Live")).add(new TextField<>("ttl")));

            add(new AjaxSubmitLink("save") {
                private static final long serialVersionUID = 1L;
                @SuppressWarnings("unchecked")
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    super.onSubmit(target, form);

                    onFormSave(target, (Form<EtcdNode>) form);
                }

                @Override
                protected void onError(AjaxRequestTarget target, Form<?> form) {
                    // TODO Auto-generated method stub
                    super.onError(target, form);

                    target.add(form);
                }
            });

            add(new AjaxSubmitLink("cancel") {
                private static final long serialVersionUID = 1L;
                @SuppressWarnings("unchecked")
                @Override
                protected void onAfterSubmit(AjaxRequestTarget target, Form<?> form) {
                    super.onAfterSubmit(target, form);

                    onFormCancel(target, (Form<EtcdNode>) form);
                }
            }.setDefaultFormProcessing(false));
        }

    }

    protected void onFormSave(AjaxRequestTarget target, Form<EtcdNode> form) {
        System.out.println("EtcdNodeFormPanel.onFormSave()");
    }

    protected void onFormCancel(AjaxRequestTarget target, Form<EtcdNode> form) {
        System.out.println("EtcdNodeFormPanel.onFormCancel()");
    }
}
