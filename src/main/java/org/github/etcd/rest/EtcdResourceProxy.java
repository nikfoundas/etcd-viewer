/**
 *
 */
package org.github.etcd.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.github.etcd.cluster.EtcdPeer;

class EtcdResourceProxy implements EtcdManager {

    private static final Pattern MEMBER_PATTERN = Pattern.compile("^etcd=([^&]+)&raft=(.+)$");

    @Inject
    private EtcdResource delegate;

    @Inject
    public EtcdResourceProxy(EtcdResource delegate) {
        this.delegate = delegate;
    }

    public EtcdResourceProxy() {
    }

    @Override
    public List<EtcdNode> getMachines() {
        EtcdResponse response = delegate.getMachines();
        if (response == null || response.getNode() == null || response.getNode().getNodes() == null) {
            return Collections.emptyList();
        }
        return response.getNode().getNodes();
    }

    public List<EtcdPeer> getMachines2() {
        EtcdResponse response = delegate.getMachines();
        if (response == null || response.getNode() == null || response.getNode().getNodes() == null) {
            return Collections.emptyList();
        }
        List<EtcdPeer> members = new ArrayList<>(response.getNode().getNodes().size());
        for (EtcdNode node : response.getNode().getNodes()) {
            String decodedValue;
            try {
                decodedValue = URLDecoder.decode(node.getValue(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                decodedValue = node.getValue();
            }

            Matcher m = MEMBER_PATTERN.matcher(decodedValue);
            if (m.matches()) {
                EtcdPeer host = new EtcdPeer();
                host.setId(node.getKey().substring(node.getKey().lastIndexOf('/') + 1));
                host.setEtcd(m.group(1));
                host.setRaft(m.group(2));

                members.add(host);

            } else {
                System.err.println("Value: " + node.getValue() + " is not expected");
            }
        }
        return members;
    }

    @Override
    public String getVersion() {
        return delegate.getVersion();
    }

    @Override
    public EtcdResponse getNode(String key) {
        return delegate.getNode(key);
    }

    public Response createDir(String key) {
        return delegate.setNodeRaw(key, true, null, null, null);
    }
//    public EtcdResponse createDirectory(String key) {
//        return createDirectory(key, null);
//    }

    @Override
    public EtcdResponse createDirectory(String key, Long ttl, Boolean update) {
        return delegate.setNode(key, true, null, ttl, update);
    }

//    public EtcdResponse createValue(String key, String value) {
//        return createValue(key, value, null, false);
//    }
    @Override
    public EtcdResponse createValue(String key, String value, Long ttl, Boolean update) {
        return delegate.setNode(key, null, value, ttl, update);
    }

    @Override
    public EtcdResponse deleteDirectory(String key) {
        return delegate.deleteNode(key, true);
    }

    @Override
    public EtcdResponse deleteValue(String key) {
        return delegate.deleteNode(key, false);
    }
    @Override
    public EtcdResponse saveOrUpdate(EtcdNode node, Boolean update) {
        if (node.isDir()) {
            return createDirectory(node.getKey(), node.getTtl(), update);
        } else {
            return createValue(node.getKey(), node.getValue(), node.getTtl(), update);
        }
    }
}
