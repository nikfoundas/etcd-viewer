package org.github.etcd.viewer.html.pages;

import javax.inject.Inject;

import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.github.etcd.service.EtcdProxyFactory;
import org.github.etcd.viewer.EtcdWebSession;

public class SignInPanel extends GenericPanel<String> {

    private static final long serialVersionUID = 1L;

    private String username;
    private String password;

    private IModel<String> registry = new LoadableDetachableModel<String>() {
        private static final long serialVersionUID = 1L;
        @Override
        protected String load() {
            return getPage().getPageParameters().get("cluster").toOptionalString();
        }
    };

    @Inject
    private EtcdProxyFactory proxyFactory;

    public SignInPanel(String id) {
        super(id);

        add(new AuthForm("authForm"));
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();

        setVisible(registry.getObject() != null &&
                proxyFactory.getEtcdProxy(registry.getObject()).isAuthEnabled() &&
                !EtcdWebSession.get().hasAuthentication(registry.getObject()));
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
