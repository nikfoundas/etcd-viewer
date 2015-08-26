package org.github.etcd.viewer.html.cluster;

import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.ChainingModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.github.etcd.service.rest.EtcdMember;

public class ClusterMembersPanel extends GenericPanel<List<EtcdMember>> {

    private static final long serialVersionUID = 1L;

    public ClusterMembersPanel(String id, IModel<List<EtcdMember>> model) {
        super(id, model);

        setOutputMarkupId(true);

        add(new ListView<EtcdMember>("peers", getModel()) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void populateItem(ListItem<EtcdMember> item) {
                item.add(new Label("id", new PropertyModel<>(item.getModel(), "id")));
                item.add(new Label("name", new PropertyModel<>(item.getModel(), "name")));
                item.add(new Label("clientURLs", new PropertyModel<>(item.getModel(), "clientURLs")));
                item.add(new Label("peerURLs", new PropertyModel<>(item.getModel(), "peerURLs")));
                item.add(new Label("state", new PropertyModel<>(item.getModel(), "state")));
                item.add(new Label("version", new PropertyModel<>(item.getModel(), "version")));

                item.add(new AttributeAppender("class", new ChainingModel<String>(item.getModel()) {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public String getObject() {
                        @SuppressWarnings("unchecked")
                        IModel<EtcdMember> peerModel = (IModel<EtcdMember>) getChainedModel();
                        if (peerModel.getObject().getState() == null) {
                            return "danger";
                        }
                        if ("leader".equals(peerModel.getObject().getState())) {
                            return "success";
                        }
                        return "";
                    }
                }));
            }
        });
    }

}
