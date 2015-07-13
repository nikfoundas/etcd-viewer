/**
 *
 */
package org.github.etcd.viewer.html.node;

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
import org.github.etcd.service.rest.EtcdNode;
import org.github.etcd.service.rest.EtcdProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EtcdNodePanel extends GenericPanel<EtcdNode> {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(EtcdNodePanel.class);

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

    private WebMarkupContainer breadcrumbAndActions;
    private WebMarkupContainer contents;

    @Inject
    private Provider<EtcdProxy> etcdProxy;

    private final IModel<String> key;

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
                try (EtcdProxy p = etcdProxy.get()) {
                    return p.getNode(key.getObject());
                } catch (Exception e) {
                    log.warn(e.getLocalizedMessage(), e);
                    // TODO: handle this exception and show some alert on page
                    return null;
                }
            }
        });

        setOutputMarkupId(true);

        createModalPanels();

        add(breadcrumbAndActions = new WebMarkupContainer("breadcrumbAndActions"));
        breadcrumbAndActions.setOutputMarkupId(true);

        createBreadcrumb();

        createNodeActions();

        add(new WebMarkupContainer("icon").add(new AttributeModifier("class", new StringResourceModel("icon.node.dir.${dir}", getModel(), ""))));

        add(new Label("key", new PropertyModel<>(getModel(), "key")));

        add(contents = new WebMarkupContainer("contents"));
        contents.setOutputMarkupId(true);


        contents.add(new Label("createdIndex", new PropertyModel<>(getModel(), "createdIndex")));
        contents.add(new Label("modifiedIndex", new PropertyModel<>(getModel(), "modifiedIndex")));
        contents.add(new Label("ttl", new PropertyModel<>(getModel(), "ttl")));
        contents.add(new Label("expiration", new PropertyModel<>(getModel(), "expiration")));


        contents.add(new AjaxLink<EtcdNode>("editValue", getModel()) {
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
            @Override
            protected void onConfigure() {
                super.onConfigure();
                if (key.getObject() == null || "".equals(key.getObject()) || "/".equals(key.getObject())) {
                    add(AttributeAppender.append("disabled", "disabled"));
                } else {
                    add(AttributeModifier.remove("disabled"));
                }

                // hide value for directory entries
                setVisible(EtcdNodePanel.this.getModelObject() != null && !EtcdNodePanel.this.getModelObject().isDir());
            }
        } .add(new MultiLineLabel("value", new PropertyModel<>(getModel(), "value"))));

        contents.add(new AjaxLink<Void>("goUp") {
            private static final long serialVersionUID = 1L;
            @Override
            public void onClick(AjaxRequestTarget target) {
                String parentKey = key.getObject().substring(0, key.getObject().lastIndexOf('/'));
                key.setObject(parentKey);

                target.add(breadcrumbAndActions, contents);
            }
            @Override
            protected void onConfigure() {
                super.onConfigure();
//                boolean isDir = EtcdNodePanel.this.getModelObject() != null && EtcdNodePanel.this.getModelObject().isDir();
                boolean isRoot = key.getObject() == null || "".equals(key.getObject()) || "/".equals(key.getObject());
                setVisible(!isRoot);
            }
        });

        contents.add(createNodesView("nodes"));
    }

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

                String parentKey = actionModel.getObject().getKey().substring(0, actionModel.getObject().getKey().lastIndexOf('/'));

                System.out.println("Going to parent: " + parentKey);

                key.setObject(parentKey);

//                target.add(EtcdNodePanel.this);

                onNodeKeyUpdated(target);

                target.add(contents, breadcrumbAndActions);
            }
        });
    }

    private void createNodeActions() {
        breadcrumbAndActions.add(new AjaxLink<Void>("addNode") {
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
                updating.setObject(false);
                actionModel.setObject(new EtcdNode());

                String currentKey = key != null? key.getObject() : "";

                StringBuffer newKey = new StringBuffer(currentKey);
                if (!currentKey.endsWith("/")) {
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
                    if (EtcdNodePanel.this.getModelObject().isDir()) {
                        add(AttributeModifier.remove("disabled"));
                    } else {
                        add(AttributeAppender.append("disabled", "disabled"));
                    }
                }
            }
        });

        breadcrumbAndActions.add(new AjaxLink<EtcdNode>("editNode", getModel()) {
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
            @Override
            protected void onConfigure() {
                super.onConfigure();
                if (key.getObject() == null || "".equals(key.getObject()) || "/".equals(key.getObject())) {
                    add(AttributeAppender.append("disabled", "disabled"));
                } else {
                    add(AttributeModifier.remove("disabled"));
                }
            }
        });

        breadcrumbAndActions.add(new AjaxLink<EtcdNode>("deleteNode", getModel()) {
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
            @Override
            protected void onConfigure() {
                super.onConfigure();
                if (key.getObject() == null || "".equals(key.getObject()) || "/".equals(key.getObject())) {
                    add(AttributeAppender.append("disabled", "disabled"));
                } else {
                    add(AttributeModifier.remove("disabled"));
                }
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

        breadcrumbAndActions.add(new ListView<String>("breadcrumb", breadcrumb) {
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
                setVisible(EtcdNodePanel.this.getModelObject() != null); // && EtcdNodePanel.this.getModelObject().isDir()
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
