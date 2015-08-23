package org.github.etcd.viewer;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.xml.bind.DatatypeConverter;

import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.request.Request;
import org.github.etcd.service.rest.EtcdProxy;

public class EtcdWebSession extends AuthenticatedWebSession {

    private static final long serialVersionUID = 1L;

    private String selectedCluster;
    private String username;
    private String basicAuthentication;

    public static class AuthenticationData implements Serializable {
        private static final long serialVersionUID = 1L;

        private String cluster;
        private String username;
        private String token;

        public AuthenticationData() {
        }
        public AuthenticationData(String cluster, String username) {
            this.cluster = cluster;
            this.username = username;
        }
        public AuthenticationData(String cluster, String username, String password) {
            this.cluster = cluster;
            this.username = username;
            this.token = DatatypeConverter.printBase64Binary((username + ":" + password).getBytes());
        }
        public String getCluster() {
            return cluster;
        }
        public void setCluster(String cluster) {
            this.cluster = cluster;
        }
        public String getUsername() {
            return username;
        }
        public void setUsername(String username) {
            this.username = username;
        }
        public String getToken() {
            return token;
        }
        public void setToken(String token) {
            this.token = token;
        }
        @Override
        public String toString() {
            return "AuthenticationData [cluster=" + cluster + ", username="
                    + username + "]";
        }
    }

    @Inject
    private Provider<EtcdProxy> etcdProxy;

    private Map<String, AuthenticationData> authenticationData = new LinkedHashMap<>();

    public EtcdWebSession(Request request) {
        super(request);

        Injector.get().inject(this);
    }

    public static EtcdWebSession get() {
        return (EtcdWebSession)AuthenticatedWebSession.get();
    }

    @Override
    public boolean authenticate(String username, String password) {

        System.out.println("EtcdWebSession.authenticate(" + username + ", " + password + ")");

        if (username == null && password == null) {

            this.username = "GUEST";
            this.basicAuthentication = null;

        } else {

            this.username = username;

            this.basicAuthentication = DatatypeConverter.printBase64Binary((username + ":" + password).getBytes());
        }

        return true;

    }

    public void clearAllAuthentications() {
        authenticationData.clear();
    }

    public String getBasicAuthenticationToken(String registry) {
        if (!authenticationData.containsKey(registry)) {
            return null;
        }
        return authenticationData.get(registry).getToken();

    }

    public Collection<AuthenticationData> getAuths() {
        return Collections.unmodifiableCollection(authenticationData.values());
    }

    public boolean hasAuthentication(String registry) {
        return authenticationData.containsKey(registry);
    }

    public void signIn(String cluster, String username, String password) {
        System.out.println("EtcdWebSession.signIn(" + cluster + ", " + username + ", " + password + ")");

        if (password != null) {
            authenticationData.put(cluster, new AuthenticationData(cluster, username, password));
        } else {
            authenticationData.put(cluster, new AuthenticationData(cluster, username));
        }
    }

    public boolean signOut(String cluster, String username) {
        if (authenticationData.containsKey(cluster) && authenticationData.get(cluster).getUsername().equals(username)) {
            authenticationData.remove(cluster);
            return true;
        }
        return false;
    }

    @Override
    public Roles getRoles() {
        return null;
    }

    public String getSelectedCluster() {
        return selectedCluster;
    }
    public void setSelectedCluster(String selectedCluster) {
        this.selectedCluster = selectedCluster;
    }
    public String getBasicAuthentication() {
        return basicAuthentication;
    }
    public String getUsername() {
        return username;
    }
}
