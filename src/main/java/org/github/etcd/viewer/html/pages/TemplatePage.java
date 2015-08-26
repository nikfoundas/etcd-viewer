package org.github.etcd.viewer.html.pages;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.devutils.debugbar.DebugBar;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.github.etcd.viewer.html.resource.WebResources;

public class TemplatePage extends WebPage {

    private static final long serialVersionUID = 1L;

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

    protected SelectRegistryPanel selectRegistryPanel;

    private void createPage() {

        add(title = new Label("title", new LoadableDetachableModel<Object>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected Object load() {
                return getPageTitleModel().getObject();
            }
        }));

        title.setOutputMarkupId(true);

        if (getApplication().getDebugSettings().isDevelopmentUtilitiesEnabled()) {
            add(new DebugBar("debug"));
        } else {
            add(new EmptyPanel("debug").setVisible(false));
        }

        add(createMenuItem("homeMenuItem", "home", HomePage.class));
        add(createMenuItem("aboutMenuItem", "about", AboutPage.class));

        add(selectRegistryPanel = new SelectRegistryPanel("selectRegistry"));
        selectRegistryPanel.setOutputMarkupId(true);

        add(new SignInPanel("authPanel"));
        add(new SignOutPanel("signOut"));
    }

    private <C extends Page> WebMarkupContainer createMenuItem(String menuId, String linkId, final Class<C> pageClass) {
        WebMarkupContainer menuItem = new WebMarkupContainer(menuId) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onConfigure() {
                super.onConfigure();
                if (getPageClass().equals(pageClass)) {
                    add(AttributeAppender.append("class", "active"));
                }
            }
        };
        menuItem.add(new BookmarkablePageLink<>(linkId, pageClass));
        return menuItem;
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

}

