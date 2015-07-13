package org.github.etcd.viewer;


import org.apache.wicket.util.tester.WicketTester;
import org.github.etcd.viewer.html.pages.NavigationPage;
import org.junit.Before;
import org.junit.Test;

/**
 * Simple test using the WicketTester
 */
public class TestNavigationPage
{
    private WicketTester tester;

    @Before
    public void setUp()
    {
        tester = new WicketTester(new EtcdViewerApplication());
    }

    @Test
    public void navigationPageRendersSuccessfully()
    {
        //start and render the test page
        tester.startPage(NavigationPage.class);

        //assert rendered page class
        tester.assertRenderedPage(NavigationPage.class);

        tester.assertVisible("clusterSelection");

        tester.assertVisible("clusterSelection:clusterPeers");

    }
}
