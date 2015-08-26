package org.github.etcd.viewer.html.modal;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.EventPropagation;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;

public class TriggerModalLink<T> extends AjaxLink<T> {

    private static final long serialVersionUID = 1L;

    private final GenericModalPanel<?> targetModal;

    public TriggerModalLink(String id, IModel<T> model, GenericModalPanel<?> targetModal) {
        super(id, model);
        this.targetModal = targetModal;
    }

    public TriggerModalLink(String id, GenericModalPanel<?> targetModal) {
        super(id);
        this.targetModal = targetModal;
    }

    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        tag.put("data-toggle", "modal");
        tag.put("data-target", "#" + targetModal.getMarkupId());
    }

    @Override
    protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
        super.updateAjaxAttributes(attributes);

        // let bootstrap do its magic
        attributes.setEventPropagation(EventPropagation.BUBBLE);
    }

    @Override
    public void onClick(AjaxRequestTarget target) {
        onModalTriggerClick(target);

        targetModal.beforeModalShow(target);
    }

    /**
     * Override this method to prepare the modal before it shows up
     */
    protected void onModalTriggerClick(AjaxRequestTarget target) {
    }
}
