package org.github.etcd.cluster;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.github.etcd.rest.EtcdNode;
import org.github.etcd.rest.EtcdResource;
import org.github.etcd.rest.EtcdResourceRouter;
import org.github.etcd.rest.EtcdResponse;
import org.github.etcd.rest.ResourceProxyFactory;

public class ClusterManagerImpl implements ClusterManager {

    private static final Pattern MEMBER_PATTERN = Pattern.compile("^etcd=([^&]+)&raft=(.+)$");

    @Inject
    private ResourceProxyFactory proxyFactory;

    @Inject
    private EtcdResourceRouter etcdRouter;


//    Collections.synchronizedMap(new LinkedHashMap(...));
//    private ConcurrentMap<String, EtcdCluster> clusters = new ConcurrentHashMap<>();

    private Map<String, EtcdCluster> clusters = Collections.synchronizedMap(new LinkedHashMap<String, EtcdCluster>());

    private static final Comparator<EtcdPeer> PEER_SORTER = new Comparator<EtcdPeer>() {
        @Override
        public int compare(EtcdPeer o1, EtcdPeer o2) {
            return o1.getEtcd().compareTo(o2.getEtcd());
        }
    };

    public ClusterManagerImpl() {
        addCluster("local", "http://localhost:4001/");
        addCluster("local-kvm", "http://192.168.122.101:4001/");
        addCluster("ena", "http://10.0.0.1:4001/");
        addCluster("dyo", "http://10.0.0.2:4001/");
        addCluster("tria", "http://10.0.0.3:4001/");
        addCluster("tessera", "http://10.0.0.4:4001/");
    }

    @Override
    public boolean exists(String name) {
        return clusters.containsKey(name);
    }

    @Override
    public EtcdCluster getCluster(String name) {
        return name == null ? null : clusters.get(name);
    }

    @Override
    public EtcdCluster addCluster(String name, String etcdPeerAddress) {
        EtcdCluster cluster = new EtcdCluster(name, etcdPeerAddress);
        clusters.put(name, cluster);
        return cluster;
    }

    @Override
    public void removeCluster(String name) {

        EtcdCluster cluster = clusters.remove(name);

        if (cluster != null) {
            proxyFactory.closeProxy(cluster.getAddress(), EtcdResource.class);
        }
    }

    @Override
    public List<EtcdCluster> getClusters() {
        return new ArrayList<>(clusters.values());
    }

    @Override
    public List<String> getClusterNames() {
        return new ArrayList<>(clusters.keySet());
    }

    @Override
    public void refreshCluster(String name) {
        EtcdCluster cluster = clusters.get(name);

        String leaderId = null;
        EtcdResponse machines = null;

        if (cluster.getPeers() != null && cluster.getPeers().size() > 0) {
            // first try to retrieve cluster info from one of the past discovered peers
            for (int i=0; i<cluster.getPeers().size(); i++) {
                EtcdPeer peer = cluster.getPeers().get(i);
                try {
                    EtcdResource etcdResource = etcdRouter.getResource(peer.getEtcd());

                    leaderId = etcdResource.getSelfStats().getLeaderInfo().getLeader();
                    machines = etcdResource.getMachines();

                    break;

                } catch (Exception e) {

                    if (i == cluster.getPeers().size() - 1) {
                        // this was the last one
                        throw e;
                    } else {
                        // suppress this error and go to the next peer
                    }
                }
            }

        } else {
            // try to retrieve the cluster info from the initially provided peer
            EtcdResource providedPeer = etcdRouter.getResource(cluster.getAddress());

            leaderId = providedPeer.getSelfStats().getLeaderInfo().getLeader();
            machines = providedPeer.getMachines();
        }

        if (machines == null || machines.getNode() == null || machines.getNode().getNodes() == null) {
            throw new RuntimeException("Failed to retrieve peer nodes for cluster: " + name + " using: " + cluster.getAddress());
        }

        List<EtcdPeer> peers = new ArrayList<>(machines.getNode().getNodes().size());
        for (EtcdNode node : machines.getNode().getNodes()) {
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

                // update leader info
                if (leaderId.equals(host.getId())) {
                    cluster.setAddress(host.getEtcd());
                }

                try {
                    EtcdResource peerEtcd = etcdRouter.getResource(host.getEtcd());
                    host.setVersion(peerEtcd.getVersion());
                    host.setStatus(peerEtcd.getSelfStats().getState());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                peers.add(host);

            } else {
                System.err.println("Value: " + node.getValue() + " is not expected");
            }
        }

        Collections.sort(peers, PEER_SORTER);

        cluster.setPeers(peers);
        cluster.setLastRefreshTime(new Date());

//
//
//        EtcdResourceProxy p = new EtcdResourceProxy(resource);
//
//        try {
//            String version = p.getVersion();
//
//            info("Version of: " + getModelObject().getAddress() + " is: " + version);
//
//        } catch (Exception e) {
//
//            error("Server: " + getModelObject().getAddress() + " is not accessible");
//
//            target.add(feedbackPanel, membersContainer);
//
//            return;
//
//        }
//
//        try {
//            EtcdSelfStats selfStats = resource.getSelfStats();
//
//            String leaderId = selfStats.getLeaderInfo().getLeader();
//
//            info("Leader id: " + leaderId);
//
//            EtcdCluster leader = null;
//            for (EtcdPeer host: p.getMachines()) {
//                if (host.getId().equals(leaderId)) {
//                    System.out.println("Found the LEADER !!!! : " + host.getEtcd());
//
//                    leader  = new EtcdCluster(getModelObject().getName(), host.getEtcd());
//
//                    info("Found leader: " + leader);
//
//                    break;
//                }
//            }
//
//            if (leader == null) {
//
//                System.err.println("COULD NOT LOCATE LEADER FOR: " + getModelObject());
//
//                selectedCluster.setObject(getModelObject());
//
//            } else {
//
//                selectedCluster.setObject(leader);
//
//            }
//        } catch (Exception e) {
//
//            error("Could not locate leader node");
//
//            selectedCluster.setObject(getModelObject());
//        }
//
//        target.add(feedbackPanel, membersContainer);
    }

}
