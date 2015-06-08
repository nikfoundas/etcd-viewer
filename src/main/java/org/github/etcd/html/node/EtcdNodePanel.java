/**
 *
 */
package org.github.etcd.html.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.EventPropagation;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.ChainingModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
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

    public static final String ROOT_KEY = "/";

    private static final List<String> ROOT_BREADCRUMB = Collections.unmodifiableList(Arrays.asList(ROOT_KEY));

    private IModel<EtcdNode> actionModel = Model.of(new EtcdNode());
    private IModel<Boolean> updating = Model.of(false);

    private EditNodeModalPanel editNodeModal;
    private DeleteNodeModalPanel deleteNodeModal;

    private WebMarkupContainer contents;

    @Inject
    private Provider<EtcdManager> etcdManager;

    private final IModel<String> key;// = Model.of(ROOT_KEY);

    public EtcdNodePanel(String id) {
        this(id, Model.of(ROOT_KEY));
    }

    public EtcdNodePanel(String id, IModel<String> keyModel) {
        super(id);

        this.key = keyModel;

        setModel(new LoadableDetachableModel<EtcdNode>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected EtcdNode load() {
                try {

                    System.out.println("-------- LOADING: " + key.getObject());

                    return etcdManager.get().getNode(key.getObject()).getNode();
                } catch (Exception e) {
//					e.printStackTrace();
                    return null;
                }
            }
        });

        setOutputMarkupId(true);

        createModalPanels();

        createBreadcrumb();

        add(new WebMarkupContainer("icon").add(new AttributeModifier("class", new StringResourceModel("icon.node.dir.${dir}", getModel(), ""))));

        add(new Label("key", new PropertyModel<>(getModel(), "key")));

        add(new Label("createdIndex", new PropertyModel<>(getModel(), "createdIndex")));
        add(new Label("modifiedIndex", new PropertyModel<>(getModel(), "modifiedIndex")));
        add(new Label("ttl", new PropertyModel<>(getModel(), "ttl")));
        add(new Label("expiration", new PropertyModel<>(getModel(), "expiration")));

        add(contents = new WebMarkupContainer("contents"));
        contents.setOutputMarkupId(true);

        contents.add(new MultiLineLabel("value", new PropertyModel<>(getModel(), "value")) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onConfigure() {
                super.onConfigure();
                // hide value for directory entries
                setVisible(EtcdNodePanel.this.getModelObject() != null && !EtcdNodePanel.this.getModelObject().isDir());
            }
        });

        contents.add(createNodesView("nodes"));
    }

    @Inject
    private IModel<String> selectedCluster;

    protected void onNodeKeyUpdated(AjaxRequestTarget target) {
        /*
         * TODO: how do we update the browser url to something bookmarkable ???
         *
        PageParameters parameters = ConvertUtils.getPageParameters(key.getObject());
        parameters.add("cluster", selectedCluster.getObject());
        String url = urlFor(EtcdBrowserPage.class, parameters).toString();
        System.out.println("Target URL: " + url);

        target.appendJavaScript("window.history.pushState(\"object or string\", \"LALALALA\", \"/fooo/"+absolute+"\");");
        target.appendJavaScript("setTimeout(\"window.location.href='/" + absolute + "'\", 10);");
        */
    }

    private void createModalPanels() {
        add(editNodeModal = new EditNodeModalPanel("editNodeModal", actionModel, updating) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onNodeSaved(AjaxRequestTarget target) {
                super.onNodeSaved(target);
                target.add(contents);
            }
        });
        add(deleteNodeModal = new DeleteNodeModalPanel("deleteNodeModal", actionModel) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onNodeDeleted(AjaxRequestTarget target) {
                super.onNodeDeleted(target);
                target.add(contents);
            }
        });
    }

    private void createBreadcrumb() {
        IModel<List<String>> breadcrumb = new ChainingModel<List<String>>(new PropertyModel<>(getModel(), "key")) {
            private static final long serialVersionUID = 1L;
            @Override
            public List<String> getObject() {
                @SuppressWarnings("unchecked")
                String key = ((IModel<String>) super.getChainedModel()).getObject();
                if (key == null || key.length() == 0 || "/".equals(key)) {
                    return ROOT_BREADCRUMB;
                }
                List<String> crumbs = new ArrayList<>();
                int index = -1;
                while ((index = key.indexOf('/', index + 1)) != -1) {
                    if (index == 0) {
                        crumbs.add("/");
                    } else {
                        crumbs.add(key.substring(0, index));
                    }
                }
                crumbs.add(key);

                return crumbs;
            }
        };

        add(new ListView<String>("breadcrumb", breadcrumb) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<String> item) {
                AjaxLink<String> link;
                item.add(link = new AjaxLink<String>("key", Model.of(item.getModelObject())) {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        key.setObject(getModelObject());

                        target.add(EtcdNodePanel.this);

                        onNodeKeyUpdated(target);
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

                link.add(new Label("label", new KeyLabelModel(item.getModel())));

                // Last breadcrumb part should be active
                if (item.getIndex() == getViewSize() - 1) {
                    item.add(new AttributeAppender("class", Model.of("active"), " "));
                    item.setEnabled(false);
                }
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
    }

    private Component createNodesView(String id) {

        IModel<List<EtcdNode>> nodes = new LoadableDetachableModel<List<EtcdNode>>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected List<EtcdNode> load() {
                List<EtcdNode> nodes = getModelObject().getNodes();
                if (nodes == null) {
                    return Collections.emptyList();
                }
                Collections.sort(nodes, NODE_SORTER);
                return nodes;
            }
        };

        return new ListView<EtcdNode>(id, nodes) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void populateItem(ListItem<EtcdNode> item) {

                item.add(new WebMarkupContainer("icon").add(new AttributeModifier("class", new StringResourceModel("icon.node.dir.${dir}", item.getModel(), ""))));

                item.add(new AjaxLink<String>("key", Model.of(item.getModelObject().getKey())) {
                    // use direct key value and not property model to avoid reloading on click
                    private static final long serialVersionUID = 1L;
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        key.setObject(getModelObject());

                        target.add(EtcdNodePanel.this);
                        onNodeKeyUpdated(target);

                    }
                }.add(new Label("label", new KeyLabelModel(new PropertyModel<String>(item.getModel(), "key")))));

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
        };
    }

    private class KeyLabelModel extends ChainingModel<String> {

        private static final long serialVersionUID = 1L;

        public KeyLabelModel(IModel<String> keyModel) {
            super(keyModel);
        }

        public KeyLabelModel(String key) {
            super(key);
        }

        @Override
        public String getObject() {
            String etcdKey = super.getObject();
            if (etcdKey == null || etcdKey.indexOf('/') == -1) {
                return etcdKey;
            }
            return etcdKey.substring(etcdKey.lastIndexOf('/') + 1);
        }

    }
}
