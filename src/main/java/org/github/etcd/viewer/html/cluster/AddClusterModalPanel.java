package org.github.etcd.viewer.html.cluster;

import javax.inject.Inject;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.UrlValidator;
import org.github.etcd.service.ClusterManager;
import org.github.etcd.service.EtcdCluster;
import org.github.etcd.viewer.html.modal.GenericModalPanel;
import org.github.etcd.viewer.html.utils.FormGroupBorder;

public class AddClusterModalPanel extends GenericModalPanel<Void> {

    private static final long serialVersionUID = 1L;

    @Inject
    private ClusterManager clusterManager;

    private String name;
    private String address;

    private Form<?> form;

    public AddClusterModalPanel(String id) {
        super(id, null);

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

//                System.out.println("ON SUBMIT -- ADD CLUSTER");

                target.add(form);

                EtcdCluster cluster = clusterManager.addCluster(name, address);

                onClusterAdded(target, cluster);

                modalHide(target);
            }

//            @Override
//            protected void onAfterSubmit(AjaxRequestTarget target, Form<?> form) {
//                super.onAfterSubmit(target, form);
//                name = null;
//                address = null;
//            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                super.onError(target, form);

                target.add(form);
            }

        });
    }

    protected void onClusterAdded(AjaxRequestTarget target, EtcdCluster addedCluster) {
//        System.out.println("AddClusterModalPanel.onClusterAdded()");
    }

    @Override
    public void beforeModalShow(AjaxRequestTarget target) {
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

}
