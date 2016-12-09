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

    private static final String BOOTSTRAP_VERSION = "3.3.4";
    private static final String FONT_AWESOME_VERSION = "4.3.0";

    private static final String MAXCDN_URL_PREFIX = "https://maxcdn.bootstrapcdn.com/";
    private static final String WEBJARS_URL_PREFIX = "/webjars/";

    private static final String BOOTSTRAP_CSS_URL = "bootstrap/" + BOOTSTRAP_VERSION + "/css/bootstrap" + getMinifiedSuffix() + ".css";
    private static final String BOOTSTRAP_THEME_CSS_URL = "bootstrap/" + BOOTSTRAP_VERSION + "/css/bootstrap-theme" + getMinifiedSuffix() + ".css";
    private static final String BOOTSTRAP_JS_URL = "bootstrap/" + BOOTSTRAP_VERSION + "/js/bootstrap" + getMinifiedSuffix() + ".js";

    private static final String FONT_AWESOME_CSS_URL = "font-awesome/" + FONT_AWESOME_VERSION + "/css/font-awesome" + getMinifiedSuffix() + ".css";

    private static final ResourceReference BOOTSTRAP_CSS = new UrlResourceReference(Url.parse(getUrlPrefix() + BOOTSTRAP_CSS_URL));

    private static final ResourceReference BOOTSTRAP_THEME_CSS = new UrlResourceReference(Url.parse(getUrlPrefix() + BOOTSTRAP_THEME_CSS_URL)) {
        private static final long serialVersionUID = 1L;
        @Override
        public Iterable<? extends HeaderItem> getDependencies() {
            return Arrays.asList(CssHeaderItem.forReference(BOOTSTRAP_CSS));
        }
    };

    private static final ResourceReference BOOTSTRAP_JS = new UrlResourceReference(Url.parse(getUrlPrefix() + BOOTSTRAP_JS_URL)) {
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

    private static final ResourceReference FONT_AWESOME_CSS = new UrlResourceReference(Url.parse(getUrlPrefix() + FONT_AWESOME_CSS_URL));

    private static String getMinifiedSuffix() {
        return Application.get().getResourceSettings().getUseMinifiedResources() ? ".min" : "";
    }

    private static String getUrlPrefix() {
        return Boolean.parseBoolean(System.getProperty("USE_MAXCDN", "false")) ? MAXCDN_URL_PREFIX : WEBJARS_URL_PREFIX;
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
