package org.github.etcd.viewer.html.modal;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

public abstract class GenericModalPanel<T> extends GenericPanel<T> {

    private static final long serialVersionUID = 1L;

    public GenericModalPanel(String id, IModel<T> model) {
        super(id, model);

        setOutputMarkupId(true);
        add(AttributeAppender.append("class", "modal fade"));
    }

    public abstract void beforeModalShow(AjaxRequestTarget target);

    protected void modalHide(AjaxRequestTarget target) {
        target.appendJavaScript("$('#" + getMarkupId() + "').modal('hide');");
    }
}
