package org.github.etcd.viewer.html.pages;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

public class AboutPage extends TemplatePage {

    private static final long serialVersionUID = 1L;

    public AboutPage() {
        IModel<List<String>> references = new LoadableDetachableModel<List<String>>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected List<String> load() {
                return Arrays.asList(new StringResourceModel("references", AboutPage.this, Model.of()).getObject().split(","));
            }
        };

        add(new ListView<String>("references", references) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void populateItem(ListItem<String> item) {

                item.add(new WebMarkupContainer("image", new StringResourceModel("${}.image", item.getModel(), "")) {
                    private static final long serialVersionUID = 1L;
                    @Override
                    protected void onConfigure() {
                        super.onConfigure();
                        if (getDefaultModelObjectAsString().isEmpty()) {
                            setVisible(false);
                        } else {
                            add(AttributeAppender.append("src", getDefaultModel()));
                        }
                    }
                });

                item.add(new WebMarkupContainer("icon", new StringResourceModel("${}.icon", item.getModel(), "fa fa-question fa-5x")) {
                    private static final long serialVersionUID = 1L;
                    @Override
                    protected void onConfigure() {
                        super.onConfigure();
                        setVisible(!getParent().get("image").isVisible());
                        add(AttributeAppender.append("class", getDefaultModel()));
                    }
                });

                ExternalLink url;
                item.add(url = new ExternalLink("url", new StringResourceModel("${}.url", item.getModel())));
                url.add(new Label("label", new StringResourceModel("${}.label", item.getModel())));

                item.add(new Label("description", new StringResourceModel("${}.description", item.getModel())));
            }
        });
    }

    @Override
    protected String getDefaultPageTitle() {
        return "etcd viewer | about";
    }

}
