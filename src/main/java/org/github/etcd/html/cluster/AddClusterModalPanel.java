package org.github.etcd.html.cluster;

import javax.inject.Inject;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.UrlValidator;
import org.github.etcd.browser.FormGroupBorder;
import org.github.etcd.cluster.ClusterManager;
import org.github.etcd.cluster.EtcdCluster;

public class AddClusterModalPanel extends Panel {

    private static final long serialVersionUID = 1L;

    @Inject
    private ClusterManager clusterManager;

    private String name;
    private String address;

    private Form<?> form;

    public AddClusterModalPanel(String id) {
        super(id);

        setOutputMarkupId(true);
        add(AttributeAppender.append("class", "modal fade"));

        add(form = new Form<>("form", new CompoundPropertyModel<>(AddClusterModalPanel.this)));

        form.add(new FormGroupBorder("nameGroup", new ResourceModel("name.label", "Name"))
            .add(new TextField<String>("name").setRequired(true).add(new IValidator<String>() {
                private static final long serialVersionUID = 1L;
                @Override
                public void validate(IValidatable<String> validatable) {
                    final String value = validatable.getValue();

                    if (clusterManager.exists(value)) {
                        validatable.error(new ValidationError().addKey("clusterNameExists.validator").setVariable("cluster", value));
                    }
                }
            })));

        form.add(new FormGroupBorder("addressGroup", new ResourceModel("address.label", "Peer address"))
            .add(new TextField<String>("address").setRequired(true).add(new UrlValidator(new String[]{"http", "https"}))));

        add(new AjaxSubmitLink("addCluster", form) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);

                System.out.println("ON SUBMIT -- ADD CLUSTER");

                target.add(form);

                EtcdCluster cluster = clusterManager.addCluster(name, address);

                onClusterAdded(target, cluster);

                target.appendJavaScript("$('#" + AddClusterModalPanel.this.getMarkupId() + "').modal('hide');");
            }

            @Override
            protected void onAfterSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onAfterSubmit(target, form);
                name = null;
                address = null;
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                super.onError(target, form);

                target.add(form);
            }

        });
    }

    public void onShowModal(AjaxRequestTarget target) {
        target.add(form);

        form.clearInput();

        form.visitFormComponents(new IVisitor<FormComponent<?>, Void>() {
            @Override
            public void component(FormComponent<?> object, IVisit<Void> visit) {
                if (object.hasFeedbackMessage()) {
                    object.getFeedbackMessages().clear();
                }
            }
        });
    }

    protected void onClusterAdded(AjaxRequestTarget target, EtcdCluster addedCluster) {
        System.out.println("AddClusterModalPanel.onClusterAdded()");
    }

}
