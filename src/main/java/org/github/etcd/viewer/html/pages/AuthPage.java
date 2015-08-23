package org.github.etcd.viewer.html.pages;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.github.etcd.viewer.html.resource.WebResources;

public class AuthPage extends WebPage {

    private static final long serialVersionUID = 1L;

    public AuthPage() {

        add(new SignInPanel("authPanel"));
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        WebResources.renderBootstrapHeader(response);

        WebResources.renderBootstrapThemeHeader(response);

        WebResources.renderFontAwesomeHeader(response);

    }

}
