/**
 *
 */
package org.github.etcd.viewer.html.pages;

import javax.inject.Inject;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.github.etcd.viewer.ConvertUtils;
import org.github.etcd.viewer.html.cluster.ClusterSelectionPanel;
import org.github.etcd.viewer.html.node.EtcdNodePanel;

public class NavigationPage extends TemplatePage {

    private static final long serialVersionUID = 1L;

    private EtcdNodePanel node;

    private IModel<String> key;

    @Inject
    private IModel<String> selectedCluster;

    private FeedbackPanel feedback;

    public NavigationPage(PageParameters parameters) {
        super(parameters);

        add(feedback = new FeedbackPanel("feedback") {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setOutputMarkupPlaceholderTag(true);
                setVisible(anyMessage());
                add(new AttributeModifier("class", new LoadableDetachableModel<String>() {
                    private static final long serialVersionUID = 1L;
                    @Override
                    protected String load() {
                        if (anyMessage(FeedbackMessage.ERROR)) {
                            return "alert alert-danger";
                        } else if (anyMessage(FeedbackMessage.WARNING)) {
                            return "alert alert-warning";
                        } else if (anyMessage(FeedbackMessage.SUCCESS)) {
                            return "alert alert-success";
                        } else if (anyMessage(FeedbackMessage.INFO)) {
                            return "alert alert-info";
                        } else {
                            return "alert";
                        }
                    }
                }));
            }
        });

        // get cluster name from the page parameters
        IModel<String> cluster = new LoadableDetachableModel<String>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected String load() {
                String clusterName = getPageParameters().get("cluster").toString(null);
                if (clusterName != null) {
                    selectedCluster.setObject(clusterName);
                }
                return clusterName;
            }
        };

        setDefaultModel(cluster);

        // get initial key from the page parameters
        key = new LoadableDetachableModel<String>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected String load() {
                return ConvertUtils.getEtcdKey(getPageParameters());
            }
        };

        add(new ClusterSelectionPanel("clusterSelection") {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onSelectedClusterChanged(AjaxRequestTarget target) {
                super.onSelectedClusterChanged(target);

                key.setObject(EtcdNodePanel.ROOT_KEY);

                target.add(node, feedback);

                NavigationPage.this.updatePageTitle(target);

                setResponsePage(NavigationPage.class, new PageParameters().add("cluster", selectedCluster.getObject()));
            }
            @Override
            protected void onSelectedClusterRefresh(AjaxRequestTarget target) {
                super.onSelectedClusterRefresh(target);

                target.add(feedback);
            }
            @Override
            protected void onSelectedClusterDeleted(AjaxRequestTarget target) {
                super.onSelectedClusterDeleted(target);

                setResponsePage(NavigationPage.class);
            }
        });

        add(node = new EtcdNodePanel("node", key) {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onNodedSaved(AjaxRequestTarget target) {
                super.onNodedSaved(target);
                target.add(feedback);
            }
            @Override
            protected void onNodedDeleted(AjaxRequestTarget target) {
                super.onNodedDeleted(target);
                target.add(feedback);
            }
            @Override
            protected void onNodeKeyUpdated(AjaxRequestTarget target) {
                super.onNodeKeyUpdated(target);

                NavigationPage.this.updatePageTitle(target);
            }
        });

    }

    @Override
    protected void onConfigure() {
        super.onConfigure();

        if (selectedCluster.getObject() == null) {
            info("Please select an etcd registry to view its contents");
        }
    }

    @Override
    protected IModel<?> getPageTitleModel() {
        return new StringResourceModel("navigation.title", NavigationPage.this, getDefaultModel(), selectedCluster, key);
    }

}
