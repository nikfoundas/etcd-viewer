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
import org.github.etcd.service.EtcdPeer;

public class ClusterPeersPanel extends GenericPanel<List<EtcdPeer>> {

    private static final long serialVersionUID = 1L;

    public ClusterPeersPanel(String id, IModel<List<EtcdPeer>> model) {
        super(id, model);

        setOutputMarkupId(true);

        add(new ListView<EtcdPeer>("peers", getModel()) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void populateItem(ListItem<EtcdPeer> item) {
                item.add(new Label("id", new PropertyModel<>(item.getModel(), "id")));
                item.add(new Label("etcd", new PropertyModel<>(item.getModel(), "etcd")));
                item.add(new Label("raft", new PropertyModel<>(item.getModel(), "raft")));
                item.add(new Label("status", new PropertyModel<>(item.getModel(), "status")));
                item.add(new Label("version", new PropertyModel<>(item.getModel(), "version")));

                item.add(new AttributeAppender("class", new ChainingModel<String>(item.getModel()) {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public String getObject() {
                        @SuppressWarnings("unchecked")
                        IModel<EtcdPeer> peerModel = (IModel<EtcdPeer>) getChainedModel();
                        if (peerModel.getObject().getStatus() == null) {
                            return "danger";
                        }
                        if ("leader".equals(peerModel.getObject().getStatus())) {
                            return "active";
                        }
                        return "";
                    }
                }));
            }
        });
    }

}
