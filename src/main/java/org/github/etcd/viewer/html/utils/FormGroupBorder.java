package org.github.etcd.viewer.html.utils;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.feedback.IFeedback;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.border.Border;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

public class FormGroupBorder extends Border implements IFeedback {

    private static final long serialVersionUID = 1L;

    private IModel<FormComponent<?>> formComponent;

    private FeedbackPanel feedbackPanel;
    public FormGroupBorder(String id, IModel<String> labelModel) {
        super(id);

        formComponent = new LoadableDetachableModel<FormComponent<?>>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected FormComponent<?> load() {
                return getBodyContainer().visitChildren(FormComponent.class, new IVisitor<FormComponent<?>, FormComponent<?>>() {
                    @Override
                    public void component(FormComponent<?> object, IVisit<FormComponent<?>> visit) {
                        visit.stop(object);
                    }
                });
            }
        };

        Label label;
        addToBorder(label = new Label("label", labelModel));

        label.add(new AttributeAppender("for", new PropertyModel<>(formComponent, "markupId")));

        addToBorder(feedbackPanel = new FeedbackPanel("feedback", new ContainerFeedbackMessageFilter(getBodyContainer())));

        WebMarkupContainer feedbackIcon;
        addToBorder(feedbackIcon = new WebMarkupContainer("feedbackIcon"));

        feedbackIcon.add(new AttributeAppender("class", new LoadableDetachableModel<String>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected String load() {
                if (!feedbackPanel.anyMessage()) {
                    return "";
                }
                return feedbackPanel.anyErrorMessage() ? "glyphicon-remove" : "glyphicon-ok";
            }
        }, " "));

        add(new AttributeAppender("class", new LoadableDetachableModel<String>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected String load() {
                if (!feedbackPanel.anyMessage()) {
                    return "";
                }
                return feedbackPanel.anyErrorMessage() ? "has-feedback has-error" : "has-feedback has-success";
            }
        }, " "));
    }

}
