/**
 *
 */
package org.github.etcd.rest;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

class EtcdManagerImpl implements EtcdManager {

//    private static final Pattern MEMBER_PATTERN = Pattern.compile("^etcd=([^&]+)&raft=(.+)$");

    @Inject
    private EtcdResource delegate;

    @Inject
    public EtcdManagerImpl(EtcdResource delegate) {
        this.delegate = delegate;
    }

    public EtcdManagerImpl() {
    }

    @Override
    public List<EtcdNode> getMachines() {
        EtcdResponse response = delegate.getMachines();
        if (response == null || response.getNode() == null || response.getNode().getNodes() == null) {
            return Collections.emptyList();
        }
        return response.getNode().getNodes();
    }

//    public List<EtcdPeer> getMachines2() {
//        EtcdResponse response = delegate.getMachines();
//        if (response == null || response.getNode() == null || response.getNode().getNodes() == null) {
//            return Collections.emptyList();
//        }
//        List<EtcdPeer> members = new ArrayList<>(response.getNode().getNodes().size());
//        for (EtcdNode node : response.getNode().getNodes()) {
//            String decodedValue;
//            try {
//                decodedValue = URLDecoder.decode(node.getValue(), "UTF-8");
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//                decodedValue = node.getValue();
//            }
//
//            Matcher m = MEMBER_PATTERN.matcher(decodedValue);
//            if (m.matches()) {
//                EtcdPeer host = new EtcdPeer();
//                host.setId(node.getKey().substring(node.getKey().lastIndexOf('/') + 1));
//                host.setEtcd(m.group(1));
//                host.setRaft(m.group(2));
//
//                members.add(host);
//
//            } else {
//                System.err.println("Value: " + node.getValue() + " is not expected");
//            }
//        }
//        return members;
//    }

    @Override
    public String getVersion() {
        return delegate.getVersion();
    }

    @Override
    public EtcdSelfStats getSelfStats() {
        return delegate.getSelfStats();
    }

    @Override
    public EtcdResponse getNode(String key) {
        return delegate.getNode(key);
    }

    @Override
    public EtcdResponse saveOrUpdate(EtcdNode node, Boolean update) {
        if (node.isDir()) {
            return delegate.setNode(node.getKey(), true, null, node.getTtl(), update ? update : null);
        } else {
            return delegate.setNode(node.getKey(), null, node.getValue(), node.getTtl(), update ? update : null);
        }
    }

    @Override
    public EtcdResponse delete(String key, Boolean directory) {
        return directory ? delegate.deleteDirectory(key, true) : delegate.deleteKey(key);
    }

}
