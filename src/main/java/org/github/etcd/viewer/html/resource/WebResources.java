package org.github.etcd.viewer.html.resource;

import java.util.Arrays;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.UrlResourceReference;
import org.apache.wicket.resource.JQueryResourceReference;

public final class WebResources {

    private static final UrlResourceReference BOOTSTRAP_CSS = new UrlResourceReference(Url.parse("https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap" + getMinifiedPrefix() + ".css"));

    private static final UrlResourceReference BOOTSTRAP_THEME_CSS = new UrlResourceReference(Url.parse("https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap-theme" + getMinifiedPrefix() + ".css")) {
        private static final long serialVersionUID = 1L;
        @Override
        public Iterable<? extends HeaderItem> getDependencies() {
            return Arrays.asList(CssHeaderItem.forReference(BOOTSTRAP_CSS));
        }
    };

    private static final UrlResourceReference FONT_AWESOME_CSS = new UrlResourceReference(Url.parse("//maxcdn.bootstrapcdn.com/font-awesome/4.3.0/css/font-awesome" + getMinifiedPrefix() + ".css"));

    private static final UrlResourceReference BOOTSTRAP_JS = new UrlResourceReference(Url.parse("https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap" + getMinifiedPrefix() + ".js")) {
        private static final long serialVersionUID = 1L;
        @Override
        public Iterable<? extends HeaderItem> getDependencies() {

            final ResourceReference backingLibraryReference;
            if (Application.exists()) {
                backingLibraryReference = Application.get().getJavaScriptLibrarySettings().getJQueryReference();
            } else {
                backingLibraryReference = JQueryResourceReference.get();
            }

            return Arrays.asList(CssHeaderItem.forReference(BOOTSTRAP_CSS), JavaScriptHeaderItem.forReference(backingLibraryReference));
        }
    };

    private static String getMinifiedPrefix() {
        return Application.get().getResourceSettings().getUseMinifiedResources() ? ".min" : "";
    }

    public static void renderBootstrapHeader(IHeaderResponse response) {
        response.render(JavaScriptHeaderItem.forReference(BOOTSTRAP_JS));
    }

    public static void renderBootstrapThemeHeader(IHeaderResponse response) {
        response.render(CssHeaderItem.forReference(BOOTSTRAP_THEME_CSS));
    }

    public static void renderFontAwesomeHeader(IHeaderResponse response) {
        response.render(CssHeaderItem.forReference(FONT_AWESOME_CSS));
    }
}
