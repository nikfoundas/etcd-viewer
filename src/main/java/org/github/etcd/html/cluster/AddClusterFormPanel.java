package org.github.etcd.html.cluster;

import javax.inject.Inject;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.UrlValidator;
import org.github.etcd.cluster.ClusterManager;
import org.github.etcd.cluster.EtcdCluster;

public class AddClusterFormPanel extends Panel {

    private static final long serialVersionUID = 1L;

    private String name;
    private String address;

    @Inject
    private ClusterManager clusterManager;

    private FeedbackPanel feedbackPanel;

    public AddClusterFormPanel(String id) {
        super(id);

        Form<?> form;
        add(form = new Form<>("form", new CompoundPropertyModel<>(AddClusterFormPanel.this)));

        form.add(feedbackPanel = new FeedbackPanel("feedback"));

        feedbackPanel.setOutputMarkupId(true);

        form.add(new TextField<String>("name").setRequired(true).add(new IValidator<String>() {
            private static final long serialVersionUID = 1L;
            @Override
            public void validate(IValidatable<String> validatable) {
                final String value = validatable.getValue();

                if (clusterManager.exists(value)) {
                    validatable.error(new ValidationError().addKey("clusterNameExists.validator").setVariable("cluster", value));
                }
            }
        }));

        form.add(new TextField<String>("address").setRequired(true).add(new UrlValidator(new String[]{"http", "https"})));

        form.add(new AjaxSubmitLink("add") {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);

                target.add(form);

                EtcdCluster cluster = clusterManager.addCluster(name, address);

                onClusterAdded(target, cluster);
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

                target.add(feedbackPanel);
            }
        });
    }

    protected void onClusterAdded(AjaxRequestTarget target, EtcdCluster addedCluster) {
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
}
