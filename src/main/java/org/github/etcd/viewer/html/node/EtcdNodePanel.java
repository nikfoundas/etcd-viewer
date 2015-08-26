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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.ChainingModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.github.etcd.service.EtcdProxyFactory;
import org.github.etcd.service.rest.EtcdNode;
import org.github.etcd.service.rest.EtcdProxy;
import org.github.etcd.viewer.ConvertUtils;
import org.github.etcd.viewer.html.modal.TriggerModalLink;
import org.github.etcd.viewer.html.pages.NavigationPage;
import org.github.etcd.viewer.html.pages.NavigationPageLink;
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
    private EtcdProxyFactory proxyFactory;

    private IModel<String> registry;

    private final IModel<String> key;
    private final IModel<String> parentKey;

    public EtcdNodePanel(String id, IModel<String> etcdRegistry, IModel<String> keyModel) {
        super(id);

        this.registry = etcdRegistry;

        this.key = keyModel;

        setModel(new LoadableDetachableModel<EtcdNode>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected EtcdNode load() {
                if (registry.getObject() == null) {
                    return null;
                }
                try (EtcdProxy p = proxyFactory.getEtcdProxy(registry.getObject())) {

                    return p.getNode(key.getObject());
                } catch (Exception e) {
                    log.warn(e.getLocalizedMessage(), e);
                    // TODO: handle this exception and show some alert on page
                    error("Could not retrieve key " + key.getObject() + ": " + e.toString());
                    EtcdNodePanel.this.setEnabled(false);
                    return null;
                }
            }
        });

        parentKey = new ParentKeyModel();

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

        WebMarkupContainer currentNode;
        contents.add(currentNode = new WebMarkupContainer("currentNode"));

        currentNode.add(new AttributeAppender("class", new StringResourceModel("nodeClass", getModel(), "") , " "));

        currentNode.add(new Label("createdIndex", new PropertyModel<>(getModel(), "createdIndex")));
        currentNode.add(new Label("modifiedIndex", new PropertyModel<>(getModel(), "modifiedIndex")));
        currentNode.add(new Label("ttl", new PropertyModel<>(getModel(), "ttl")));
        currentNode.add(new Label("expiration", new PropertyModel<>(getModel(), "expiration")));

        contents.add(new TriggerModalLink<EtcdNode>("editValue", getModel(), editNodeModal) {
            private static final long serialVersionUID = 1L;
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
            @Override
            protected void onModalTriggerClick(AjaxRequestTarget target) {
                updating.setObject(true);
                actionModel.setObject(getModelObject());
            }
        } .add(new MultiLineLabel("value", new PropertyModel<>(getModel(), "value"))));


        AbstractLink goUp;
        contents.add(goUp = createNavigationLink("goUp", parentKey));
        goUp.add(new Behavior() {
            private static final long serialVersionUID = 1L;
            @Override
            public void onConfigure(Component component) {
                super.onConfigure(component);
                component.setEnabled(key.getObject() != null && !"".equals(key.getObject()) && !"/".equals(key.getObject()));
            }
        });

        contents.add(createNodesView("nodes"));
    }

    @Override
    protected void onDetach() {
        super.onDetach();

        registry.detach();
        key.detach();
        parentKey.detach();
    }
    protected void onNodeKeyUpdated(AjaxRequestTarget target) {
    }

    protected void onNodedSaved(AjaxRequestTarget target) {
    }

    protected void onNodedDeleted(AjaxRequestTarget target) {
    }

    private void createModalPanels() {
        add(editNodeModal = new EditNodeModalPanel("editNodeModal", actionModel, registry, updating) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onNodeSaved(AjaxRequestTarget target) {
                super.onNodeSaved(target);

                target.add(contents);

                EtcdNodePanel.this.onNodedSaved(target);
            }
        });
        add(deleteNodeModal = new DeleteNodeModalPanel("deleteNodeModal", actionModel, registry) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onNodeDeleted(AjaxRequestTarget target) {
                super.onNodeDeleted(target);

                PageParameters params = ConvertUtils.getPageParameters(parentKey.getObject());
                params.add("cluster", registry.getObject());

                setResponsePage(NavigationPage.class, params);

                key.setObject(parentKey.getObject());

//                target.add(EtcdNodePanel.this);

                onNodeKeyUpdated(target);

                target.add(contents, breadcrumbAndActions);

                EtcdNodePanel.this.onNodedDeleted(target);
            }
        });
    }

    private void createNodeActions() {
        breadcrumbAndActions.add(new TriggerModalLink<Void>("addNode", editNodeModal) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onConfigure() {
                super.onConfigure();
                if (EtcdNodePanel.this.getModelObject() != null && EtcdNodePanel.this.getModelObject().isDir()) {
                    add(AttributeModifier.remove("disabled"));
                } else {
                    add(AttributeAppender.append("disabled", "disabled"));
                }
            }
            @Override
            protected void onModalTriggerClick(AjaxRequestTarget target) {
                updating.setObject(false);
                actionModel.setObject(new EtcdNode());

                String currentKey = key != null? key.getObject() : "";

                StringBuffer newKey = new StringBuffer(currentKey);
                if (!currentKey.endsWith("/")) {
                    newKey.append('/');
                }
                newKey.append("new_node");

                actionModel.getObject().setKey(newKey.toString());
            }
        });

        breadcrumbAndActions.add(new TriggerModalLink<EtcdNode>("editNode", getModel(), editNodeModal) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onConfigure() {
                super.onConfigure();
                if (key.getObject() == null || "".equals(key.getObject()) || "/".equals(key.getObject())) {
                    add(AttributeAppender.append("disabled", "disabled"));
                } else {
                    add(AttributeModifier.remove("disabled"));
                }
            }
            @Override
            protected void onModalTriggerClick(AjaxRequestTarget target) {
                updating.setObject(true);
                actionModel.setObject(getModelObject());
            }
        });

        breadcrumbAndActions.add(new TriggerModalLink<EtcdNode>("deleteNode", getModel(), deleteNodeModal) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onConfigure() {
                super.onConfigure();
                if (key.getObject() == null || "".equals(key.getObject()) || "/".equals(key.getObject())) {
                    add(AttributeAppender.append("disabled", "disabled"));
                } else {
                    add(AttributeModifier.remove("disabled"));
                }
            }
            @Override
            protected void onModalTriggerClick(AjaxRequestTarget target) {
                actionModel.setObject(getModelObject());
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
                AbstractLink link;
                item.add(link = createNavigationLink("key", item.getModel()));

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
                if (getModelObject() == null || getModelObject().getNodes() == null) {
                    return Collections.emptyList();
                }
                List<EtcdNode> nodes = getModelObject().getNodes();
                Collections.sort(nodes, NODE_SORTER);
                return nodes;
            }
        };

        return new ListView<EtcdNode>(id, nodes) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void populateItem(ListItem<EtcdNode> item) {

                item.add(new AttributeModifier("class", new StringResourceModel("nodeClass", item.getModel(), "")));

                AbstractLink link;
                item.add(link = createNavigationLink("key", new PropertyModel<String>(item.getModel(), "key")));

                link.add(new Label("label", new KeyLabelModel(new PropertyModel<String>(item.getModel(), "key"))));

                item.add(new MultiLineLabel("value", new PropertyModel<>(item.getModel(), "value")));
                item.add(new Label("createdIndex", new PropertyModel<>(item.getModel(), "createdIndex")));
                item.add(new Label("modifiedIndex", new PropertyModel<>(item.getModel(), "modifiedIndex")));
                item.add(new Label("ttl", new PropertyModel<>(item.getModel(), "ttl")));
                item.add(new Label("expiration", new PropertyModel<>(item.getModel(), "expiration")));

            }
        };
    }

    private AbstractLink createNavigationLink(final String id, final IModel<String> targetKey) {
        return new NavigationPageLink(id, registry, targetKey);

/*        return new AjaxLink<String>(id, targetKey) {
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
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setEnabled(selectedCluster != null && selectedCluster.getObject() != null && getModelObject() != null );
            }
        };*/
    }

    private class ParentKeyModel extends LoadableDetachableModel<String> {

        private static final long serialVersionUID = 1L;

        @Override
        protected String load() {
            String etcdKey = key.getObject();
            if (etcdKey == null || etcdKey.indexOf('/') == -1) {
                return etcdKey;
            }
            return etcdKey.substring(0, etcdKey.lastIndexOf('/'));
        }
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
