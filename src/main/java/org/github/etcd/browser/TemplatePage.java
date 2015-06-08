package org.github.etcd.browser;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.wicket.Page;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.devutils.debugbar.DebugBar;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
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
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.UrlResourceReference;
import org.github.etcd.rest.EtcdResource;

public class TemplatePage extends WebPage {

    private static final long serialVersionUID = 1L;

    @Inject
    private EtcdResource etcdResource;

    @Inject
    private IModel<String> currentClusterModel;

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

    private void createPage() {

        WebMarkupContainer currentCluster;
        add(currentCluster = new WebMarkupContainer("currentCluster") {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(currentClusterModel.getObject() != null);

                setVisible(false);
            }
        });
        currentCluster.add(new Label("name", new PropertyModel<>(currentClusterModel, "name")));
        currentCluster.add(new Label("address", new PropertyModel<>(currentClusterModel, "address")));


        add(new WebMarkupContainer("noCurrentCluster") {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(currentClusterModel.getObject() == null);
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
                return etcdResource.getVersion();
            }
        }));

        IModel<List<MenuItem>> menuItems = new LoadableDetachableModel<List<MenuItem>>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected List<MenuItem> load() {
                return Arrays.asList(
                        new MenuItem("home", HomePage.class, "Home"),
                        new MenuItem("navigation", NavigationPage.class, "Navigation"),
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

    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        String minified = getApplication().getResourceSettings().getUseMinifiedResources() ? ".min" : "";

        UrlResourceReference bootstrapCssRef = new UrlResourceReference(Url.parse("https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap" + minified + ".css"));

        final CssHeaderItem bootstrapCss = CssHeaderItem.forReference(bootstrapCssRef);

        UrlResourceReference bootstrapThemeCssRef = new UrlResourceReference(Url.parse("https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap-theme" + minified + ".css")) {
            private static final long serialVersionUID = 1L;
            @Override
            public Iterable<? extends HeaderItem> getDependencies() {
                return Arrays.asList(bootstrapCss);
            }
        };

        UrlResourceReference fontAwesomeCssRef = new UrlResourceReference(Url.parse("//maxcdn.bootstrapcdn.com/font-awesome/4.3.0/css/font-awesome" + minified + ".css"));

        UrlResourceReference bootstrapJsRef = new UrlResourceReference(Url.parse("https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap" + minified + ".js")) {
            private static final long serialVersionUID = 1L;
            @Override
            public Iterable<? extends HeaderItem> getDependencies() {
                return Arrays.asList(bootstrapCss, JavaScriptHeaderItem.forReference(getApplication().getJavaScriptLibrarySettings().getJQueryReference()));
            }
        };

        response.render(CssHeaderItem.forReference(bootstrapThemeCssRef));

        response.render(JavaScriptHeaderItem.forReference(bootstrapJsRef));

        response.render(CssHeaderItem.forReference(fontAwesomeCssRef));

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

