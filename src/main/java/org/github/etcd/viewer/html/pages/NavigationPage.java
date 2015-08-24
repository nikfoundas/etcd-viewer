/**
 *
 */
package org.github.etcd.viewer.html.pages;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.github.etcd.viewer.ConvertUtils;
import org.github.etcd.viewer.html.cluster.ClusterSelectionPanel;
import org.github.etcd.viewer.html.node.EtcdNodePanel;

public class NavigationPage extends TemplatePage {

    private static final long serialVersionUID = 1L;

    private IModel<String> registry;
    private IModel<String> key;

    private EtcdNodePanel node;

    private FeedbackPanel feedback;

    private IModel<String> jumpToKey = Model.of();

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
        registry = new LoadableDetachableModel<String>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected String load() {
                return getPageParameters().get("cluster").toString(null);
            }
        };

        setDefaultModel(registry);

        // get initial key from the page parameters
        key = new LoadableDetachableModel<String>() {
            private static final long serialVersionUID = 1L;
            @Override
            protected String load() {
                return ConvertUtils.getEtcdKey(getPageParameters());
            }
        };

        Form<Void> jumpToForm;
        add(jumpToForm = new Form<Void>("jumpToForm") {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onSubmit() {
                super.onSubmit();

                setResponsePage(NavigationPage.class, ConvertUtils.getPageParameters(jumpToKey.getObject()).add("cluster", registry.getObject()));
            }
        });
        jumpToForm.add(new TextField<>("key", jumpToKey));

        add(new ClusterSelectionPanel("clusterSelection") {
            private static final long serialVersionUID = 1L;
            @Override
            protected void onSelectedClusterChanged(AjaxRequestTarget target) {
                super.onSelectedClusterChanged(target);

                key.setObject(EtcdNodePanel.ROOT_KEY);

                target.add(node, feedback);

                NavigationPage.this.updatePageTitle(target);

                setResponsePage(NavigationPage.class, new PageParameters().add("cluster", registry.getObject()));
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

        add(node = new EtcdNodePanel("node", registry, key) {
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

//        if (selectedCluster.getObject() == null) {
//            info("Please select an etcd registry to view its contents");
//        } else {
//
//            if (etcdProxy.get().isAuthEnabled() && !AuthenticatedWebSession.get().isSignedIn()) {
//                ((AuthenticatedWebApplication) getApplication()).restartResponseAtSignInPage();
//            }
//        }

    }

    @Override
    protected IModel<?> getPageTitleModel() {
        return new StringResourceModel("navigation.title", NavigationPage.this, getDefaultModel(), registry, key);
    }

}
