package org.github.etcd.viewer;


import org.apache.wicket.util.tester.WicketTester;
import org.github.etcd.viewer.html.pages.HomePage;
import org.junit.Before;
import org.junit.Test;

/**
 * Simple test using the WicketTester
 */
public class TestHomePage
{
    private WicketTester tester;

    @Before
    public void setUp()
    {
        tester = new WicketTester(new EtcdViewerApplication());
    }

    @Test
    public void homePageRendersSuccessfully()
    {
        //start and render the test page
        tester.startPage(HomePage.class);

        //assert rendered page class
        tester.assertRenderedPage(HomePage.class);

    }
}
