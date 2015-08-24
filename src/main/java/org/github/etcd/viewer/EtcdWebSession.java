package org.github.etcd.viewer;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.Request;

public class EtcdWebSession extends AuthenticatedWebSession {

    private static final long serialVersionUID = 1L;

    private Map<String, AuthenticationData> authenticationData = new LinkedHashMap<>();

    public EtcdWebSession(Request request) {
        super(request);
    }

    public static EtcdWebSession get() {
        return (EtcdWebSession)AuthenticatedWebSession.get();
    }

    @Override
    public boolean authenticate(String username, String password) {
        return true;
    }
    @Override
    public Roles getRoles() {
        return new Roles("USER");
    }

    public String getBasicAuthenticationToken(String registry) {
        if (!authenticationData.containsKey(registry)) {
            return null;
        }
        return authenticationData.get(registry).getToken();

    }

    public Collection<AuthenticationData> getAuthentications() {
        return Collections.unmodifiableCollection(authenticationData.values());
    }

    public boolean hasAuthentication(String registry) {
        return authenticationData.containsKey(registry);
    }

    public void signIn(String registry, String username, String password) {
        authenticationData.put(registry, new AuthenticationData(registry, username, password));
    }

    public boolean signOut(String cluster, String username) {
        if (authenticationData.containsKey(cluster) && authenticationData.get(cluster).getUsername().equals(username)) {
            authenticationData.remove(cluster);
            return true;
        }
        return false;
    }


    public static class AuthenticationData implements Serializable {
        private static final long serialVersionUID = 1L;

        private String cluster;
        private String username;
        private String token;

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
}
