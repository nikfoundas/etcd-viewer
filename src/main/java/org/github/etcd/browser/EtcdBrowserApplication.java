package org.github.etcd.browser;

import org.apache.wicket.guice.GuiceComponentInjector;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.github.etcd.rest.RestModule;

/**
 * Application object for your web application.
 * If you want to run this application without deploying, run the Start class.
 *
 * @see org.github.etcd.browser.Start#main(String[])
 */
public class EtcdBrowserApplication extends WebApplication
{
    /**
     * @see org.apache.wicket.Application#getHomePage()
     */
    @Override
    public Class<? extends WebPage> getHomePage()
    {
        return HomePage.class;
    }

    /**
     * @see org.apache.wicket.Application#init()
     */
    @Override
    public void init()
    {
        super.init();

        getComponentInstantiationListeners().add(new GuiceComponentInjector(this, new RestModule()));

        getDebugSettings().setDevelopmentUtilitiesEnabled(true);
        getResourceSettings().setUseMinifiedResources(true);

        getMarkupSettings().setStripWicketTags(true);

        mountPage("/home", HomePage.class);

        mountPage("/etcd/#{cluster}", EtcdBrowserPage.class);
        mountPage("/edit", EditNodePage.class);

        mountPage("/about", AboutPage.class);

        // add your configuration here
    }
}
