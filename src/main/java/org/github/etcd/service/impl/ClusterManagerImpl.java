package org.github.etcd.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.github.etcd.service.ApiVersion;
import org.github.etcd.service.ClusterManager;
import org.github.etcd.service.EtcdCluster;
import org.github.etcd.service.EtcdProxyFactory;
import org.github.etcd.service.api.EtcdMember;
import org.github.etcd.service.api.EtcdProxy;
import org.github.etcd.service.api.EtcdSelfStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.inject.name.Named;

public class ClusterManagerImpl implements ClusterManager {

    private static final Logger log = LoggerFactory.getLogger(ClusterManager.class);

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

    private final EtcdProxyFactory proxyFactory;

    private Map<String, EtcdCluster> clusters = Collections.synchronizedMap(new LinkedHashMap<String, EtcdCluster>());

    public static final String DEFAULT_CLIENT_URL_KEY = "ETCD_CLIENT_URL";
    private static final String DEFAULT_CLIENT_URL = "http://localhost:2379/";
    public static final String CLUSTER_STORE_PATH_KEY = "CLUSTER_STORE_PATH";
    private static final String CLUSTER_STORE_FILENAME = "clusters.json";
    
    private final File clusterStoreFile;
    private final ObjectMapper mapper = new ObjectMapper();
    
    @Inject
    public ClusterManagerImpl(@Named(DEFAULT_CLIENT_URL_KEY) String defaultClientUrl,
            @Named(CLUSTER_STORE_PATH_KEY) String clusterStorePath,
            EtcdProxyFactory proxyFactory) {
        log.debug("{}: {}", DEFAULT_CLIENT_URL_KEY, defaultClientUrl);
        log.debug("{}: {}", CLUSTER_STORE_PATH_KEY, clusterStorePath);
        
        if (Strings.isNullOrEmpty(defaultClientUrl)) {
            defaultClientUrl = DEFAULT_CLIENT_URL;
            log.debug("Using default {}: {}", DEFAULT_CLIENT_URL_KEY, defaultClientUrl);
        }
        
        this.proxyFactory = proxyFactory;
        
        if (!Strings.isNullOrEmpty(clusterStorePath)) {
            clusterStoreFile = new File(clusterStorePath, CLUSTER_STORE_FILENAME);
            if (clusterStoreFile.exists()) {
                loadClusters();
            }
        } else {
            clusterStoreFile = null;
        }

        if (clusters.isEmpty()) {
            addCluster("Local V2", defaultClientUrl, ApiVersion.V2);
            addCluster("Local V3", defaultClientUrl, ApiVersion.V3);
        }
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
    public EtcdCluster addCluster(String name, String etcdPeerAddress, ApiVersion apiVersion) {
        EtcdCluster cluster = new EtcdCluster(name, etcdPeerAddress, apiVersion);
        clusters.put(name, cluster);
        
        persistClusters();
        
        return cluster;
    }

    @Override
    public void removeCluster(String name) {
        clusters.remove(name);
        
        persistClusters();
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
        ApiVersion apiVersion = cluster.getApiVersion();

        List<EtcdMember> members;

        try (EtcdProxy proxy = proxyFactory.getEtcdProxy(name, leaderAddress, apiVersion)) {

            members = proxy.getMembers();
            Collections.sort(members, MEMBER_SORTER);

            cluster.setAuthEnabled(proxy.isAuthEnabled());

        } catch (Exception e) {
            log.error("Last known leader " + leaderAddress + " is not accessible: " + e.getLocalizedMessage(), e);
            // use the previously discovered members if they exist
            if (cluster.getMembers() == null) {
                throw e;
            }
            members = cluster.getMembers();
        }

        // collect self statistics from each member
        for (EtcdMember member : members) {

            member.setState(null);
            member.setVersion(null);

            for (String clientURL : member.getClientURLs()) {

                try (EtcdProxy proxy = proxyFactory.getEtcdProxy(name, clientURL, apiVersion)) {

                    EtcdSelfStats memberStats = proxy.getSelfStats();

                    member.setState(STATE_MAPPINGS.get(memberStats.getState()));

                    if ("leader".equals(member.getState())) {
                        leaderAddress = clientURL;

                        cluster.setAuthEnabled(proxy.isAuthEnabled());

                    }

                    member.setVersion(proxy.getVersion());


                    break;
                } catch (Exception e) {
                    log.warn("etcd server " + clientURL + " is not accessible: " + e.getLocalizedMessage(), e);
                }
            }
        }

        cluster.setMembers(members);

        cluster.setAddress(leaderAddress);
        cluster.setLastRefreshTime(new Date());
        cluster.setRefreshed(true);

        persistClusters();
    }

    private void loadClusters() {
        log.info("Loading {}", clusterStoreFile.getAbsolutePath());
        try {
            Map<String, EtcdCluster> incoming = mapper.readValue(clusterStoreFile,
                    new TypeReference<Map<String, EtcdCluster>>() {});
            clusters = Collections.synchronizedMap(incoming);
            
            log.debug("Loaded {} clusters", clusters.size());
        } catch (IOException e) {
            log.error("Unable to read cluster config from {}", clusterStoreFile.getAbsolutePath());
        }
    }

    private synchronized void persistClusters() {
        if (clusterStoreFile != null) {
            try {
                Files.createDirectories(clusterStoreFile.getParentFile().toPath());
            } catch (IOException e) {
                log.error("Unable to create configured cluster storage directory {}",
                        clusterStoreFile.getParentFile().getAbsolutePath(), e);
                return;
            }

            log.debug("Writing {} clusters", clusters.size());
            
            try {
                mapper.writeValue(clusterStoreFile, clusters);
            } catch (IOException e) {
                log.error("Unable to write cluster config to {}", clusterStoreFile.getAbsolutePath(), e);
            }
        }
    }

}
