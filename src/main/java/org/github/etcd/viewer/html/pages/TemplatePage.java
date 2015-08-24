package org.github.etcd.viewer.html.pages;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.devutils.debugbar.DebugBar;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.github.etcd.service.rest.EtcdProxy;
import org.github.etcd.viewer.html.resource.WebResources;

public class TemplatePage extends WebPage {

    private static final long serialVersionUID = 1L;

    @Inject
    private Provider<EtcdProxy> etcdResource;

    private IModel<String> registry;

    public TemplatePage() {
        super();
        createPage();
    }

    public TemplatePage(IModel<?> model) {
        super(model);
        createPage();
    }

    public TemplatePage(PageParameters parameters) {
        super(parameters);
        createPage();
    }

    protected IModel<?> getPageTitleModel() {
        return new StringResourceModel("page.title", this, getDefaultModel(), getDefaultPageTitle());
    }

    protected String getDefaultPageTitle() {
        return "etcd viewer";
    }

    private Label title;

    private void createPage() {

        registry = new LoadableDetachableModel<String>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected String load() {
                return getPageParameters().get("cluster").toOptionalString();
            }
        };

        add(title = new Label("title", new LoadableDetachableModel<Object>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected Object load() {
                return getPageTitleModel().getObject();
            }
        }));

        title.setOutputMarkupId(true);

        WebMarkupContainer currentCluster;
        add(currentCluster = new WebMarkupContainer("currentCluster") {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(registry.getObject() != null);

                setVisible(false);
            }
        });
        currentCluster.add(new Label("name", new PropertyModel<>(registry, "name")));
        currentCluster.add(new Label("address", new PropertyModel<>(registry, "address")));


        add(new WebMarkupContainer("noCurrentCluster") {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(registry.getObject() == null);
            }
        });

        if (getApplication().getDebugSettings().isDevelopmentUtilitiesEnabled()) {
            add(new DebugBar("debug"));
        } else {
            add(new EmptyPanel("debug").setVisible(false));
        }

        currentCluster.add(new Label("version", new LoadableDetachableModel<String>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected String load() {
                return etcdResource.get().getVersion();
            }
        }));

        add(new BookmarkablePageLink<>("home", HomePage.class));

        IModel<List<MenuItem>> menuItems = new LoadableDetachableModel<List<MenuItem>>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected List<MenuItem> load() {
                return Arrays.asList(
//                        new MenuItem("home", HomePage.class, "Home"),
//                        new MenuItem("navigation", NavigationPage.class, "Navigation"),
                        new MenuItem("about", AboutPage.class, "About")
                        );
            }
        };


        add(new ListView<MenuItem>("menu", menuItems) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void populateItem(ListItem<MenuItem> item) {
                if (getPageClass().equals(item.getModelObject().getPage())) {
                    item.add(AttributeAppender.append("class", "active"));
                }
                item.add(new BookmarkablePageLink<>("link", item.getModelObject().getPage())
                        .add(new Label("label", new StringResourceModel("menu.${resourceKey}", item.getModel(), item.getModelObject().getDefaultLabel()))));
            }
        });

        add(new SignInPanel("authPanel"));
        add(new SelectRegistryPanel("selectRegistry"));
        add(new SignOutPanel("signOut"));
    }

    @Override
    protected void onDetach() {
        super.onDetach();

        registry.detach();
    }

    protected void updatePageTitle(AjaxRequestTarget target) {
        target.add(title);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        WebResources.renderBootstrapHeader(response);
        WebResources.renderBootstrapThemeHeader(response);
        WebResources.renderFontAwesomeHeader(response);
    }

    @SuppressWarnings("unused")
    private static class MenuItem {
        private String resourceKey;
        private Class<? extends Page> page;
        private String defaultLabel;
        public MenuItem(String resourceKey, Class<? extends Page> page, String defaultLabel) {
            this.resourceKey = resourceKey;
            this.page = page;
            this.defaultLabel = defaultLabel;
        }
        public String getResourceKey() {
            return resourceKey;
        }
        public void setResourceKey(String resourceKey) {
            this.resourceKey = resourceKey;
        }
        public Class<? extends Page> getPage() {
            return page;
        }
        public void setPage(Class<? extends Page> page) {
            this.page = page;
        }
        public String getDefaultLabel() {
            return defaultLabel;
        }
        public void setDefaultLabel(String defaultLabel) {
            this.defaultLabel = defaultLabel;
        }
    }
}

