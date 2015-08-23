package org.github.etcd.viewer.html.pages;

import java.util.List;

import javax.inject.Inject;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ChainingModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.github.etcd.service.ClusterManager;
import org.github.etcd.service.EtcdCluster;

public class SelectRegistryPanel extends Panel {

    @Inject
    private ClusterManager clusterManager;

    public SelectRegistryPanel(String id) {
        super(id);

        IModel<List<EtcdCluster>> clusterList = new LoadableDetachableModel<List<EtcdCluster>>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected List<EtcdCluster> load() {
                return clusterManager.getClusters();
            }
        };

        add(new Label("currentRegistry", new LoadableDetachableModel<String>() {
            @Override
            protected String load() {
                return getPage().getPageParameters().get("cluster").toString("Select registry");
            }
        }));

        add(new ListView<EtcdCluster>("clusters", clusterList) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<EtcdCluster> item) {
                NavigationPageLink link;
                IModel<String> nameModel = new PropertyModel<String>(item.getModel(), "name");
                item.add(link = new NavigationPageLink("link", nameModel, Model.of("/")));

                link.add(new Label("name", nameModel));

                link.add(new Label("address", new PropertyModel<>(item.getModel(), "address")));

                item.add(new AttributeAppender("class", new ChainingModel<String>(nameModel) {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public String getObject() {
                        String clusterName = super.getObject();
                        if (NavigationPage.class.equals(getPage().getPageClass()) && clusterName.equals(getPage().getPageParameters().get("cluster").toString(""))) {
                            return "my-active";
                        } else {
                            return "";
                        }
                    }
                }));
            }
        });
    }

}
