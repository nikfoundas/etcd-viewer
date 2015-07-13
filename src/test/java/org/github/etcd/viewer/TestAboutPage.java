package org.github.etcd.viewer;


import org.apache.wicket.util.tester.WicketTester;
import org.github.etcd.viewer.html.pages.AboutPage;
import org.junit.Before;
import org.junit.Test;

/**
 * Simple test using the WicketTester
 */
public class TestAboutPage
{
    private WicketTester tester;

    @Before
    public void setUp()
    {
        tester = new WicketTester(new EtcdViewerApplication());
    }

    @Test
    public void aboutPageRendersSuccessfully()
    {
        //start and render the test page
        tester.startPage(AboutPage.class);

        //assert rendered page class
        tester.assertRenderedPage(AboutPage.class);
    }
}
