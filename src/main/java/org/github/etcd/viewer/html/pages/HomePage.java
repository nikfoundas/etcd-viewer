package org.github.etcd.viewer.html.pages;

import org.apache.wicket.markup.html.link.BookmarkablePageLink;


public class HomePage extends TemplatePage {

    private static final long serialVersionUID = 1L;

    public HomePage() {
        add(new BookmarkablePageLink<>("navigate", NavigationPage.class));
    }

}
