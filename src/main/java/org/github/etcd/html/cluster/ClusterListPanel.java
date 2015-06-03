package org.github.etcd.html.cluster;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.ChainingModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.github.etcd.cluster.EtcdCluster;

public class ClusterListPanel extends GenericPanel<List<EtcdCluster>> {

    private static final long serialVersionUID = 1L;

    private final IModel<String> selected;

    public ClusterListPanel(String id, IModel<List<EtcdCluster>> model, IModel<String> selectedName) {
        super(id, model);

        this.selected = selectedName;

        setOutputMarkupId(true);

        add(new ListView<EtcdCluster>("clusters", getModel()) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void populateItem(ListItem<EtcdCluster> item) {

                IModel<String> clusterNameModel = new PropertyModel<String>(item.getModel(), "name");

                AjaxLink<String> link;
                // IndicatingAjaxLink would be nice
                item.add(link = new AjaxLink<String>("select", clusterNameModel) {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        if (getModelObject().equals(selected.getObject())) {
                            selected.setObject(null); // deselect
                        } else {
                            selected.setObject(getModelObject());
                        }
                        target.add(ClusterListPanel.this);

                        onSelectionChanged(target);
                    }
                });

                link.add(AttributeAppender.append("class", new ChainingModel<String>(clusterNameModel) {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public String getObject() {
                        return super.getObject().equals(selected.getObject()) ? "list-group-item-info- selected" : "";
                    }
                }));

                link.add(new Label("name", clusterNameModel));
                link.add(new Label("address", new PropertyModel<>(item.getModel(), "address")));

                link.add(new Label("size", new PropertyModel<>(item.getModel(), "peers.size")));

                /*

                item.add(new AjaxLink<EtcdCluster>("remove", item.getModel()) {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        EtcdCluster removed = getModelObject();

                        clusterManager.removeCluster(removed.getName());

                        addClusterModel.setObject(removed);

                        target.add(container, addClusterForm);
                    }
                });

                item.add(new AjaxLink<EtcdCluster>("select", item.getModel()) {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public void onClick(AjaxRequestTarget target) {

                        EtcdResource resource = router.getResource(getModelObject().getAddress());

                        EtcdResourceProxy p = new EtcdResourceProxy(resource);

                        try {
                            String version = p.getVersion();

                            info("Version of: " + getModelObject().getAddress() + " is: " + version);

                        } catch (Exception e) {

                            error("Server: " + getModelObject().getAddress() + " is not accessible");

                            target.add(feedbackPanel, membersContainer);

                            return;

                        }

                        try {
                            EtcdSelfStats selfStats = resource.getSelfStats();

                            String leaderId = selfStats.getLeaderInfo().getLeader();

                            info("Leader id: " + leaderId);

                            EtcdCluster leader = null;
                            for (EtcdPeer host: p.getEtcdPeers()) {
                                if (host.getId().equals(leaderId)) {
                                    System.out.println("Found the LEADER !!!! : " + host.getEtcd());

                                    leader  = new EtcdCluster(getModelObject().getName(), host.getEtcd());

                                    info("Found leader: " + leader);

                                    break;
                                }
                            }

                            if (leader == null) {

                                System.err.println("COULD NOT LOCATE LEADER FOR: " + getModelObject());

                                selectedCluster.setObject(getModelObject());

                            } else {

                                selectedCluster.setObject(leader);

                            }
                        } catch (Exception e) {

                            error("Could not locate leader node");

                            selectedCluster.setObject(getModelObject());
                        }

                        target.add(feedbackPanel, membersContainer);

                    }

                });

                */
            }
        });
    }

    protected void onSelectionChanged(AjaxRequestTarget target) {
    }
}
