package org.github.etcd.service.rest.impl;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.RedirectionException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.github.etcd.service.rest.EtcdError;
import org.github.etcd.service.rest.EtcdException;
import org.github.etcd.service.rest.EtcdMember;
import org.github.etcd.service.rest.EtcdMembers;
import org.github.etcd.service.rest.EtcdNode;
import org.github.etcd.service.rest.EtcdProxy;
import org.github.etcd.service.rest.EtcdResponse;
import org.github.etcd.service.rest.EtcdSelfStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

public class EtcdProxyImpl implements EtcdProxy {
    private static final Logger LOG = LoggerFactory.getLogger(EtcdProxy.class);

    private final String targetUrl;
    private final String authenticationToken;
    private Client client;

    public EtcdProxyImpl(String targetUrl, String authenticationToken) {
        this.targetUrl = targetUrl;
        this.authenticationToken = authenticationToken;
    }

    public EtcdProxyImpl(String targetUrl) {
        this(targetUrl, null);
    }

    private WebTarget getWebTarget() {
        if (client == null) {
            client = ClientBuilder.newClient();
            client.register(JacksonJsonProvider.class);

            // register the basic authentication filter if authentication information is provided
            if (authenticationToken != null) {
                client.register(new ClientRequestFilter() {
                    @Override
                    public void filter(ClientRequestContext requestContext) throws IOException {
                        requestContext.getHeaders().add("Authorization", "Basic " + authenticationToken);
                    }
                });
            }

        }

        WebTarget target = client.target(targetUrl);

        return target;
    }

    @Override
    public void close() {
        if (client != null) {
            client.close();
            client = null;
        }
    }

    @Override
    public String getVersion() {
        return getWebTarget()
                .path("/version")
                .request(MediaType.TEXT_PLAIN)
                .get(String.class);
    }

    @Override
    public Boolean isAuthEnabled() {
        try {
            Map<String, Object> result = getWebTarget().path("/v2/auth/enable").request(MediaType.APPLICATION_JSON).get(new GenericType<Map<String, Object>>() {});
            return (Boolean) result.get("enabled");
        } catch (NotFoundException e) {
//            LOG.warn(e.toString(), e);
            return false;
        }
    }

    @Override
    public EtcdSelfStats getSelfStats() {
        return new ExceptionHandlingProcessor<>(EtcdSelfStats.class).process(getWebTarget().path("/v2/stats/self").request(MediaType.APPLICATION_JSON).buildGet());
    }

    @Override
    public List<EtcdMember> getMembers() {

        String version = getVersion();

        LOG.info("Using version: '{}' to detect cluster members", version);

        LOG.info("Authentication is: " + (isAuthEnabled() ? "enabled" : "disabled"));

        if (version.contains("etcd 0.4")) {

            // alternatively we could use the key-value to retrieve the nodes contained at /v2/keys/_etcd/machines

            URI raftUri = UriBuilder.fromUri(targetUrl).port(7001).build();

            List<Map<String, String>> items = client.target(raftUri).path("/v2/admin/machines").request(MediaType.APPLICATION_JSON).get(new GenericType<List<Map<String, String>>>() {});

            System.out.println("Retrieved: " + items);

            List<EtcdMember> members = new ArrayList<>(items.size());

            for (Map<String, String> item : items) {
                EtcdMember member = new EtcdMember();
                member.setId(item.get("name"));
                member.setName(member.getId());
                member.setState(item.get("state"));
                member.setClientURLs(Arrays.asList(item.get("clientURL")));
                member.setPeerURLs(Arrays.asList(item.get("peerURL")));

                members.add(member);
            }

            return members;

        } else {

            EtcdMembers members = new ExceptionHandlingProcessor<>(EtcdMembers.class).process(getWebTarget().path("/v2/members").request(MediaType.APPLICATION_JSON).buildGet());
            return members.getMembers();
        }

    }

    @Override
    public EtcdNode getNode(final String key) {

        Invocation getNode = getWebTarget()
                .path("/v2/keys/{key}")
                .resolveTemplate("key", normalizeKey(key), false)
                .request(MediaType.APPLICATION_JSON)
                .buildGet();

        return new ExceptionHandlingProcessor<>(EtcdResponse.class).process(getNode).getNode();
    }

    @Override
    public void saveNode(EtcdNode node) {
        EtcdResponse response = saveOrUpdateNode(node, false);

        LOG.debug("Created Node: " + response);
    }

    @Override
    public EtcdNode updateNode(EtcdNode node) {
        EtcdResponse response = saveOrUpdateNode(node, true);

        LOG.debug("Updated Node: " + response);

        return response.getPrevNode();
    }

    @Override
    public EtcdNode deleteNode(EtcdNode node) {
        return deleteNode(node, false);
    }

    @Override
    public EtcdNode deleteNode(EtcdNode node, boolean recursive) {

        WebTarget target = getWebTarget().path("/v2/keys/{key}").resolveTemplate("key", normalizeKey(node.getKey()), false);

        if (node.isDir()) {
            if (recursive) {
                target = target.queryParam("recursive", recursive);
            } else {
                target = target.queryParam("dir", node.isDir());
            }
        }

        Invocation deleteInvocation = target.request(MediaType.APPLICATION_JSON).buildDelete();

        return new ExceptionHandlingProcessor<>(EtcdResponse.class).process(deleteInvocation).getNode();
    }

    private String normalizeKey(final String key) {
        if (key == null) {
            return "/";
        }
        return key.startsWith("/") ? key.substring(1) + "/" : key + "/";
    }

    protected EtcdResponse saveOrUpdateNode(EtcdNode node, Boolean update) {
        if (node.isDir() && update && node.getTtl() == null) {
            LOG.warn("Remove directory TTL is not supported by etcd version 0.4.9");
        }
        Form form = new Form();
        if (node.isDir()) {
            form.param("dir", Boolean.TRUE.toString());
        } else {
            form.param("value", node.getValue());
        }
        if (update) {
            form.param("ttl", node.getTtl() == null ? "" : node.getTtl().toString());
        } else if (node.getTtl() != null) {
            form.param("ttl", node.getTtl().toString());
        }
        // we include prevExist parameter within all requests for safety
        form.param("prevExist", update.toString());

        Invocation invocation = getWebTarget()
                .path("/v2/keys/{key}")
                .resolveTemplate("key", normalizeKey(node.getKey()), false)
                .request(MediaType.APPLICATION_JSON)
                .buildPut(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED));

        return new ExceptionHandlingProcessor<>(EtcdResponse.class).process(invocation);
    }

    private static class ExceptionHandlingProcessor<T> {
        private final Class<T> responseType;

        public ExceptionHandlingProcessor(Class<T> responseType) {
            this.responseType = responseType;
        }

        public T process(Invocation invocation) {
            try {
                return invocation.invoke(responseType);
            } catch (RedirectionException e) {
//              TODO: maybe create another invocation and start over ???

                throw new EtcdException(e);

            } catch (WebApplicationException e) {

                try {
                    // try to read the contained api error if it exists
                    EtcdError error = e.getResponse().readEntity(EtcdError.class);
                    throw new EtcdException(e, error);
                } catch (EtcdException e1) {
                    throw e1;
                } catch (Exception e1) {
                    // just ignore this one and wrap the original
                    LOG.debug(e1.getLocalizedMessage(), e1);
                    throw new EtcdException(e);
                }
            }
        }
    }

}
