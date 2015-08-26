package org.github.etcd.viewer.html.pages;

import javax.inject.Inject;

import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.github.etcd.service.ClusterManager;
import org.github.etcd.service.EtcdCluster;
import org.github.etcd.viewer.EtcdWebSession;

public class SignInPanel extends GenericPanel<String> {

    private static final long serialVersionUID = 1L;

    private String username;
    private String password;

    @Inject
    private ClusterManager clusterManager;

    private IModel<String> registry = new LoadableDetachableModel<String>() {
        private static final long serialVersionUID = 1L;
        @Override
        protected String load() {
            return getPage().getPageParameters().get("cluster").toOptionalString();
        }
    };

    public SignInPanel(String id) {
        super(id);

        add(new AuthForm("authForm"));
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();

        if (registry.getObject() == null) {
            setVisible(false);
        } else {
            EtcdCluster cluster = clusterManager.getCluster(registry.getObject());

            if (cluster == null) {
                setVisible(false);
            } else {

                if (!cluster.isRefreshed()) {
                    try {
                        clusterManager.refreshCluster(registry.getObject());
                        info("Successfully refreshed information for cluster: " + registry.getObject());
                    } catch (Exception e) {
                        error("Failed to refresh information for cluster: " + registry.getObject());
                    }
                }

                setVisible(cluster.isRefreshed() && cluster.isAuthEnabled() && !EtcdWebSession.get().hasAuthentication(registry.getObject()));
            }
        }
    }

    @Override
    protected void onDetach() {
        super.onDetach();

        registry.detach();
    }

    public final class AuthForm extends StatelessForm<SignInPanel> {

        private static final long serialVersionUID = 1L;

        public AuthForm(String id) {
            super(id);

            setModel(new CompoundPropertyModel<SignInPanel>(SignInPanel.this));

            add(new TextField<String>("username").setRequired(true));
            add(new PasswordTextField("password").setRequired(true));

        }

        @Override
        protected void onSubmit() {
            super.onSubmit();

            EtcdWebSession.get().signIn(registry.getObject(), username, password);

            setResponsePage(getPage().getPageClass(), getPage().getPageParameters());

        }

    }
}
