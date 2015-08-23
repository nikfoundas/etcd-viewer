package org.github.etcd.viewer.html.pages;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.StatelessLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.github.etcd.viewer.EtcdWebSession;
import org.github.etcd.viewer.EtcdWebSession.AuthenticationData;

public class SignOutPanel extends Panel {

    private IModel<List<AuthenticationData>> authentications = new LoadableDetachableModel<List<AuthenticationData>>() {
        private static final long serialVersionUID = 1L;
        @Override
        protected List<AuthenticationData> load() {
            return new ArrayList<>(EtcdWebSession.get().getAuths());
        }
    };

    public SignOutPanel(String id) {
        super(id);

        add(new Label("authCount", new LoadableDetachableModel<Integer>() {
            @Override
            protected Integer load() {
                return authentications.getObject().size();
            }
        }));
        add(new ListView<AuthenticationData>("authentications", authentications) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<AuthenticationData> item) {
                Link<AuthenticationData> signOut;
                item.add(signOut = new Link<AuthenticationData>("signOut", item.getModel()) {
                    @Override
                    public void onClick() {
                        System.out.println("Trying to sign out: " + getModelObject());

                        String username = getModelObject().getUsername();
                        String cluster = getModelObject().getCluster();

                        if (EtcdWebSession.get().signOut(cluster, username)) {
                            System.out.println("Successfully signed out user: " + username + " from: " + cluster);
                        } else {
                            System.out.println("Failed to sign out user: " + username + " from: " + cluster);
                        }

                        setResponsePage(getPage().getPageClass(), getPage().getPageParameters());
                    }
                });

                signOut.add(new Label("username", new PropertyModel<>(item.getModel(), "username")));

                signOut.add(new Label("cluster", new PropertyModel<String>(item.getModel(), "cluster")));

/*                item.add(new AttributeAppender("class", new ChainingModel<String>(nameModel) {
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
                }));*/
            }
        });

        add(new StatelessLink<Void>("signOutAll") {
            @Override
            public void onClick() {
//                EtcdWebSession.get().clearAllAuthentications();

                EtcdWebSession.get().invalidate();

                setResponsePage(Application.get().getHomePage());

//                setResponsePage(getPage().getPageClass(), getPage().getPageParameters());
            }
        });
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        setVisible(!authentications.getObject().isEmpty());
    }

}
