package org.github.etcd.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.github.etcd.service.ClusterManager;
import org.github.etcd.service.EtcdCluster;
import org.github.etcd.service.EtcdProxyFactory;
import org.github.etcd.service.rest.EtcdMember;
import org.github.etcd.service.rest.EtcdProxy;
import org.github.etcd.service.rest.EtcdSelfStats;

public class ClusterManagerImpl implements ClusterManager {

    private static final Comparator<EtcdMember> MEMBER_SORTER = new Comparator<EtcdMember>() {
        @Override
        public int compare(EtcdMember o1, EtcdMember o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    private static final Map<String, String> STATE_MAPPINGS = new HashMap<>();

    static {
        STATE_MAPPINGS.put("leader", "leader");
        STATE_MAPPINGS.put("follower", "follower");
        STATE_MAPPINGS.put("StateLeader", "leader");
        STATE_MAPPINGS.put("StateFollower", "follower");
    }

    @Inject
    private EtcdProxyFactory proxyFactory;

    private Map<String, EtcdCluster> clusters = Collections.synchronizedMap(new LinkedHashMap<String, EtcdCluster>());


    public ClusterManagerImpl() {
        addCluster("default", "http://localhost:2379/");
        addCluster("kvm", "http://192.168.122.101:4001/");
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
        clusters.remove(name);
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
        if (name == null) {
            return;
        }
        EtcdCluster cluster = clusters.get(name);

        // default leader address is the provided one
        String leaderAddress = cluster.getAddress();

        try (EtcdProxy proxy = proxyFactory.getEtcdProxy(leaderAddress)) {

            List<EtcdMember> members = proxy.getMembers();

            Collections.sort(members, MEMBER_SORTER);

            EtcdSelfStats selfStats = proxy.getSelfStats();

            String leaderId = selfStats.getLeaderInfo().getLeader();

            // collect self statistics from each member
            for (EtcdMember member : members) {

                member.setState(null);

                // do not collect statistics for the provided client address again
                if (leaderId.equals(member.getId())) {
                    member.setState(STATE_MAPPINGS.get(selfStats.getState()));
                    continue;
                }

                for (String clientURL : member.getClientURLs()) {

                    try (EtcdProxy memberProxy = proxyFactory.getEtcdProxy(clientURL)) {

                        EtcdSelfStats memberStats = memberProxy.getSelfStats();
                        member.setState(STATE_MAPPINGS.get(memberStats.getState()));

                        if ("leader".equals(member.getState())) {
                            leaderAddress = clientURL;
                        }

                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            cluster.setMembers(members);

            cluster.setAddress(leaderAddress);
            cluster.setVersion(proxy.getVersion());
            cluster.setLastRefreshTime(new Date());

        }

    }

}
