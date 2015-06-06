/**
 *
 */
package org.github.etcd.browser;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.EventPropagation;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.ChainingModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.github.etcd.html.node.DeleteNodeModalPanel;
import org.github.etcd.html.node.EditNodeModalPanel;
import org.github.etcd.rest.EtcdManager;
import org.github.etcd.rest.EtcdNode;

public class EtcdNodePanel extends GenericPanel<EtcdNode> {

    private static final long serialVersionUID = 1L;

    private static final Comparator<EtcdNode> NODE_SORTER = new Comparator<EtcdNode>() {
        @Override
        public int compare(EtcdNode o1, EtcdNode o2) {
            if (o1.isDir() == o2.isDir()) {
                return o1.getKey().compareTo(o2.getKey());
            }
            if (o1.isDir()) {
                return -1;
            }
            if (o2.isDir()) {
                return 1;
            }
            return 0;
        }
    };

//    private IModel<List<EtcdNode>> nodes;

    private IModel<EtcdNode> actionModel = Model.of(new EtcdNode());
    private IModel<Boolean> updating = Model.of(false);

    private EditNodeModalPanel editNodeModal;
    private DeleteNodeModalPanel deleteNodeModal;

    private WebMarkupContainer panelContainer;

    @Inject
    private Provider<EtcdManager> etcdManager;

    private IModel<String> keyModel = Model.of("/");

    public EtcdNodePanel(String id) {
        super(id);

        setModel(new LoadableDetachableModel<EtcdNode>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected EtcdNode load() {
                try {

                    System.out.println("-------- LOADING: " + keyModel.getObject());

                    return etcdManager.get().getNode(keyModel.getObject()).getNode();
                } catch (Exception e) {
//					e.printStackTrace();
                    return null;
                }
            }
        });

        setOutputMarkupId(true);

        add(editNodeModal = new EditNodeModalPanel("editNodeModal", actionModel, updating) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onNodeSaved(AjaxRequestTarget target) {
                super.onNodeSaved(target);
                target.add(panelContainer);
            }
        });
        add(deleteNodeModal = new DeleteNodeModalPanel("deleteNodeModal", actionModel) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onNodeDeleted(AjaxRequestTarget target) {
                super.onNodeDeleted(target);
                target.add(panelContainer);
            }
        });

        add(new AjaxLink<String>("addNode", new PropertyModel<String>(getModel(), "key")) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("data-target", "#" + editNodeModal.getMarkupId());
            }
            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                super.updateAjaxAttributes(attributes);
                attributes.setEventPropagation(EventPropagation.BUBBLE);
            }
            @Override
            public void onClick(AjaxRequestTarget target) {
                updating.setObject(false);
                actionModel.setObject(new EtcdNode());

                StringBuffer newKey = new StringBuffer(getModelObject());
                if (!getModelObject().endsWith("/")) {
                    newKey.append('/');
                }
                newKey.append("new_node");

                actionModel.getObject().setKey(newKey.toString());

                editNodeModal.onShowModal(target);
            }
            @Override
            protected void onConfigure() {
                super.onConfigure();
                if (EtcdNodePanel.this.getModelObject() != null) {
                    setVisible(EtcdNodePanel.this.getModelObject().isDir());
                }
            }
        });


        add(new WebMarkupContainer("icon").add(new AttributeModifier("class", new StringResourceModel("icon.node.dir.${dir}", getModel(), ""))));

        add(new Label("key", new PropertyModel<>(getModel(), "key")));

        add(new Label("createdIndex", new PropertyModel<>(getModel(), "createdIndex")));
        add(new Label("modifiedIndex", new PropertyModel<>(getModel(), "modifiedIndex")));
        add(new Label("ttl", new PropertyModel<>(getModel(), "ttl")));
        add(new Label("expiration", new PropertyModel<>(getModel(), "expiration")));

        IModel<List<PageParameters>> breadcrumb = new ChainingModel<List<PageParameters>>(getModel()) {
            private static final long serialVersionUID = 1L;
            @Override
            public List<PageParameters> getObject() {
                @SuppressWarnings("unchecked")
                IModel<EtcdNode> node = (IModel<EtcdNode>) super.getChainedModel();
                return ConvertUtils.getBreadcrumb(node.getObject() == null? null : node.getObject().getKey());
            }
        };

        add(new ListView<PageParameters>("breadcrumb", breadcrumb) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<PageParameters> item) {
//                BookmarkablePageLink<EtcdBrowserPage> link;
//                item.add(link = new BookmarkablePageLink<EtcdBrowserPage>("key", EtcdBrowserPage.class, item.getModelObject()) {
//                    private static final long serialVersionUID = 1L;
//                    @Override
//                    public String getBeforeDisabledLink() {
//                        return "";
//                    }
//                    @Override
//                    public String getAfterDisabledLink() {
//                        return "";
//                    }
//                });

                AjaxLink<String> link;
                item.add(link = new AjaxLink<String>("key", Model.of(ConvertUtils.getEtcdKey(item.getModelObject()))) {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        keyModel.setObject(getModelObject());

                        System.out.println("UPDATED KEY: " + keyModel.getObject());

                        target.add(EtcdNodePanel.this);
                    }
                    @Override
                    public String getBeforeDisabledLink() {
                        return "";
                    }
                    @Override
                    public String getAfterDisabledLink() {
                        return "";
                    }
                });

                Label label;

                link.add(label = new Label("label", ConvertUtils.getLabel(item.getModelObject())));

                // Apply icon to the ETCD root node
                if (item.getIndex() == 0) {
                    label.add(new AttributeAppender("class", new ResourceModel("icon.breadcrumb.root", "")));
                }
                // Last breadcrumb part should be active
                if (item.getIndex() == getViewSize() - 1) {
                    item.add(new AttributeAppender("class", Model.of("active"), " "));
                    item.setEnabled(false);
                }
            }
        });

        add(panelContainer = new WebMarkupContainer("panelContainer"));
        panelContainer.setOutputMarkupId(true);

        panelContainer.add(new MultiLineLabel("value", new PropertyModel<>(getModel(), "value")) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onConfigure() {
                super.onConfigure();
                // hide value for directory entries
                setVisible(EtcdNodePanel.this.getModelObject() != null && !EtcdNodePanel.this.getModelObject().isDir());
            }
        });

        final IModel<List<EtcdNode>> nodes = new LoadableDetachableModel<List<EtcdNode>>() {
            private static final long serialVersionUID = 1L;

            @Override
            protected List<EtcdNode> load() {
                System.out
                        .println("EtcdNodePanel.EtcdNodePanel(...).new LoadableDetachableModel() {...}.load()");
                List<EtcdNode> nodes = getModelObject().getNodes();
                if (nodes == null) {
                    return Collections.emptyList();
                }
                Collections.sort(nodes, NODE_SORTER);
                return nodes;
            }
        };

        panelContainer.add(new ListView<EtcdNode>("nodes", nodes) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void populateItem(ListItem<EtcdNode> item) {

                item.add(new WebMarkupContainer("icon").add(new AttributeModifier("class", new StringResourceModel("icon.node.dir.${dir}", item.getModel(), ""))));

//                PageParameters parameters = ConvertUtils.getPageParameters(item.getModelObject().getKey());

//                BookmarkablePageLink<EtcdBrowserPage> link;

//                item.add(link = new BookmarkablePageLink<>("key", EtcdBrowserPage.class, parameters));

//                link.add(new Label("label", ConvertUtils.getLabel(parameters)));

                AjaxLink<String> link;
                item.add(link = new AjaxLink<String>("key", new PropertyModel<String>(item.getModel(), "key")) { // Model.of(item.getModelObject().getKey())
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        keyModel.setObject(getModelObject());
                        System.out.println("UPDATED KEY: " + keyModel.getObject());

                        EtcdNodePanel.this.getModel().detach();
//                        ListView.this.getModel().detach();
                        nodes.detach();

                        target.add(EtcdNodePanel.this);
                    }
                });
                link.add(new Label("label", ConvertUtils.getLabel(item.getModelObject().getKey())));

                item.add(new AjaxLink<EtcdNode>("edit", item.getModel()) {
                    private static final long serialVersionUID = 1L;
                    @Override
                    protected void onComponentTag(ComponentTag tag) {
                        super.onComponentTag(tag);
                        tag.put("data-toggle", "modal");
                        tag.put("data-target", "#" + editNodeModal.getMarkupId());
                    }
                    @Override
                    protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                        super.updateAjaxAttributes(attributes);
                        attributes.setEventPropagation(EventPropagation.BUBBLE);
                    }
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        updating.setObject(true);
                        actionModel.setObject(getModelObject());

                        editNodeModal.onShowModal(target);
                    }
                });

                item.add(new AjaxLink<EtcdNode>("delete", item.getModel()) {
                    private static final long serialVersionUID = 1L;
                    @Override
                    protected void onComponentTag(ComponentTag tag) {
                        super.onComponentTag(tag);
                        tag.put("data-toggle", "modal");
                        tag.put("data-target", "#" + deleteNodeModal.getMarkupId());
                    }
                    @Override
                    protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                        super.updateAjaxAttributes(attributes);
                        attributes.setEventPropagation(EventPropagation.BUBBLE);
                    }
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        actionModel.setObject(getModelObject());

                        deleteNodeModal.onShowModal(target);
                    }
                });

                item.add(new MultiLineLabel("value", new PropertyModel<>(item.getModel(), "value")));
                item.add(new Label("createdIndex", new PropertyModel<>(item.getModel(), "createdIndex")));
                item.add(new Label("modifiedIndex", new PropertyModel<>(item.getModel(), "modifiedIndex")));
                item.add(new Label("ttl", new PropertyModel<>(item.getModel(), "ttl")));
                item.add(new Label("expiration", new PropertyModel<>(item.getModel(), "expiration")));

            }
            @Override
            protected void onConfigure() {
                super.onConfigure();
                // hide child nodes for non directory nodes
                setVisible(EtcdNodePanel.this.getModelObject() != null && EtcdNodePanel.this.getModelObject().isDir());
            }
        });
    }

    @Override
    protected void onDetach() {
        super.onDetach();

//        nodes.detach();
    }

}
