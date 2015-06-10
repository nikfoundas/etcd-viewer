package org.github.etcd.viewer;

import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.guice.GuiceComponentInjector;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.github.etcd.service.RestModule;
import org.github.etcd.viewer.html.pages.AboutPage;
import org.github.etcd.viewer.html.pages.HomePage;
import org.github.etcd.viewer.html.pages.NavigationPage;

/**
 * Application object for your web application.
 * If you want to run this application without deploying, run the Start class.
 *
 * @see org.github.etcd.viewer.Start#main(String[])
 */
public class EtcdViewerApplication extends WebApplication
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

        if (getConfigurationType() == RuntimeConfigurationType.DEVELOPMENT) {

            getResourceSettings().setUseMinifiedResources(false);
            getDebugSettings().setDevelopmentUtilitiesEnabled(true);

        } else {

            getResourceSettings().setUseMinifiedResources(true);
            getDebugSettings().setDevelopmentUtilitiesEnabled(false);

        }

        getMarkupSettings().setStripWicketTags(true);

        mountPage("/home", HomePage.class);
        mountPage("/etcd/#{cluster}", NavigationPage.class);
        mountPage("/about", AboutPage.class);

        // add your configuration here
    }
}
