package org.github.etcd.browser;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;


public class HomePage extends TemplatePage {

    public HomePage() {
        add(new BookmarkablePageLink<>("navigate", NavigationPage.class));
    }

}
