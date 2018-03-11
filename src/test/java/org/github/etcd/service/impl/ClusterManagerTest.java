package org.github.etcd.service.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.github.etcd.service.ApiVersion;
import org.github.etcd.service.ClusterManager;
import org.github.etcd.service.EtcdCluster;
import org.github.etcd.service.EtcdProxyFactory;
import org.github.etcd.service.api.EtcdMember;
import org.github.etcd.service.api.EtcdProxy;
import org.github.etcd.service.api.EtcdSelfStats;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ClusterManagerTest {

    private static final String STORE_READ_TEST_PATH = "src/test/resources/storereadtest";
    private static final String STORE_WRITE_TEST_PATH = "target/test-resources/storewritetest";
    private static final String BACKUP_STORE_FILENAME = "clusters.json.bak";

    private EtcdProxyFactory etcdProxyFactory;

    @Before
    public void setup() throws Exception {
        etcdProxyFactory = mock(EtcdProxyFactory.class);

        wipeWriteTestPath();
        backupReadData();
    }

    @After
    public void tearDown() throws Exception {
        replaceReadData();
        wipeWriteTestPath();
    }

    private void wipeWriteTestPath() throws IOException {
        Path storeWriteTestPath = Paths.get(STORE_WRITE_TEST_PATH);
        if (Files.exists(storeWriteTestPath)) {
            Files.walk(storeWriteTestPath)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
        }
    }

    private void backupReadData() throws Exception {
        Files.copy(Paths.get(STORE_READ_TEST_PATH, "clusters.json"),
                Paths.get(STORE_READ_TEST_PATH, BACKUP_STORE_FILENAME), StandardCopyOption.REPLACE_EXISTING);
    }

    private void replaceReadData() throws Exception {
        Files.move(Paths.get(STORE_READ_TEST_PATH, BACKUP_STORE_FILENAME),
                Paths.get(STORE_READ_TEST_PATH, "clusters.json"), StandardCopyOption.REPLACE_EXISTING);
    }
    
    @Test
    public void constructor_noDefaultUrl_noStorePath() {
        ClusterManager clusterManager = new ClusterManagerImpl("", "", etcdProxyFactory);
        List<EtcdCluster> clusters = clusterManager.getClusters();

        boolean foundDefaultV2 = false;
        boolean foundDefaultV3 = false;
        for (EtcdCluster cluster : clusters) {
            boolean v2 = "Local V2".equals(cluster.getName());
            boolean v3 = "Local V3".equals(cluster.getName());
            boolean urlMatches = "http://localhost:2379/".equals(cluster.getAddress());

            foundDefaultV2 |= v2 && urlMatches && cluster.getApiVersion() == ApiVersion.V2;
            foundDefaultV3 |= v3 && urlMatches && cluster.getApiVersion() == ApiVersion.V3;
        }

        assertTrue(foundDefaultV2);
        assertTrue(foundDefaultV3);
        assertFalse(Files.exists(Paths.get(STORE_WRITE_TEST_PATH)));
    }

    @Test
    public void constructor_defaultUrl_noStorePath() {
        String defaultClientUrl = "test default client URL";
        ClusterManager clusterManager = new ClusterManagerImpl(defaultClientUrl, "", etcdProxyFactory);
        List<EtcdCluster> clusters = clusterManager.getClusters();

        boolean foundDefaultV2 = false;
        boolean foundDefaultV3 = false;
        for (EtcdCluster cluster : clusters) {
            boolean v2 = "Local V2".equals(cluster.getName());
            boolean v3 = "Local V3".equals(cluster.getName());
            boolean urlMatches = defaultClientUrl.equals(cluster.getAddress());

            foundDefaultV2 |= v2 && urlMatches && cluster.getApiVersion() == ApiVersion.V2;
            foundDefaultV3 |= v3 && urlMatches && cluster.getApiVersion() == ApiVersion.V3;
        }

        assertTrue(foundDefaultV2);
        assertTrue(foundDefaultV3);
        assertFalse(Files.exists(Paths.get(STORE_WRITE_TEST_PATH)));
    }

    @Test
    public void constructor_noExistingFile() {
        assertFalse(Files.exists(Paths.get(STORE_WRITE_TEST_PATH)));

        String defaultClientUrl = "test default client URL";
        ClusterManager clusterManager = new ClusterManagerImpl(defaultClientUrl, STORE_WRITE_TEST_PATH,
                etcdProxyFactory);
        List<EtcdCluster> clusters = clusterManager.getClusters();

        boolean foundDefaultV2 = false;
        boolean foundDefaultV3 = false;
        for (EtcdCluster cluster : clusters) {
            boolean v2 = "Local V2".equals(cluster.getName());
            boolean v3 = "Local V3".equals(cluster.getName());
            boolean urlMatches = defaultClientUrl.equals(cluster.getAddress());

            foundDefaultV2 |= v2 && urlMatches && cluster.getApiVersion() == ApiVersion.V2;
            foundDefaultV3 |= v3 && urlMatches && cluster.getApiVersion() == ApiVersion.V3;
        }

        assertTrue(foundDefaultV2);
        assertTrue(foundDefaultV3);
        assertTrue(Files.exists(Paths.get(STORE_WRITE_TEST_PATH, "clusters.json")));
    }

    @Test
    public void constructor_existingFile() {
        ClusterManager clusterManager = new ClusterManagerImpl("", STORE_READ_TEST_PATH, etcdProxyFactory);
        List<EtcdCluster> clusters = clusterManager.getClusters();

        assertEquals(2, clusters.size());
        for (EtcdCluster cluster : clusters) {
            boolean v2NameMatch = "Existing V2".equals(cluster.getName());
            boolean v3NameMatch = "Existing V3".equals(cluster.getName());
            if (v2NameMatch) {
                assertTrue("V2 client URL".equals(cluster.getAddress()));
                assertTrue(cluster.getApiVersion() == ApiVersion.V2);
            } else if (v3NameMatch) {
                assertTrue("V3 client URL".equals(cluster.getAddress()));
                assertTrue(cluster.getApiVersion() == ApiVersion.V3);
            }
        }
    }

    @Test
    public void addCluster_remove() {
        ClusterManager clusterManager1 = new ClusterManagerImpl("", STORE_WRITE_TEST_PATH, etcdProxyFactory);
        assertEquals(2, clusterManager1.getClusters().size());
        clusterManager1.removeCluster(clusterManager1.getClusters().get(0).getName());
        assertEquals(1, clusterManager1.getClusters().size());

        ClusterManager clusterManager2 = new ClusterManagerImpl("", STORE_WRITE_TEST_PATH, etcdProxyFactory);
        assertEquals(1, clusterManager2.getClusters().size());
        assertEquals(clusterManager1.getClusters().get(0).getName(), clusterManager2.getClusters().get(0).getName());
    }

    @Test
    public void addCluster_persist() {
        ClusterManager clusterManager1 = new ClusterManagerImpl("", STORE_WRITE_TEST_PATH, etcdProxyFactory);
        for (EtcdCluster cluster : clusterManager1.getClusters()) {
            clusterManager1.removeCluster(cluster.getName());
        }
        assertTrue(clusterManager1.getClusters().isEmpty());

        String expectedName = "new cluster 1";
        String expectedUrl = "new client URL";
        ApiVersion expectedVersion = ApiVersion.V2;
        clusterManager1.addCluster(expectedName, expectedUrl, expectedVersion);

        assertTrue(Files.exists(Paths.get(STORE_WRITE_TEST_PATH, "clusters.json")));

        ClusterManager clusterManager2 = new ClusterManagerImpl("", STORE_WRITE_TEST_PATH, etcdProxyFactory);

        assertEquals(1, clusterManager2.getClusters().size());

        EtcdCluster cluster = clusterManager2.getClusters().get(0);
        assertEquals(expectedName, cluster.getName());
        assertEquals(expectedUrl, cluster.getAddress());
        assertEquals(expectedVersion, cluster.getApiVersion());
    }

    @Test
    public void refresh() {
        ClusterManager clusterManager = new ClusterManagerImpl("", "", etcdProxyFactory);
        List<EtcdCluster> clusters = clusterManager.getClusters();
        for (EtcdCluster cluster : clusters) {
            clusterManager.removeCluster(cluster.getName());
        }

        String clusterName = "new cluster 1";
        String clusterUrl = "new client URL";
        ApiVersion clusterVersion = ApiVersion.V2;
        clusterManager.addCluster(clusterName, clusterUrl, clusterVersion);
        EtcdCluster addedCluster = clusterManager.getCluster(clusterName);
        assertNull(addedCluster.getMembers());
        assertFalse(addedCluster.isRefreshed());

        EtcdMember leaderMember = new EtcdMember();
        leaderMember.setName("leader member");
        leaderMember.setClientURLs(Collections.singletonList("leaderUrl"));
        EtcdMember followerMember = new EtcdMember();
        followerMember.setName("follower member");
        followerMember.setClientURLs(Collections.singletonList("followerUrl"));
        EtcdProxy proxyToCluster = mock(EtcdProxy.class);
        when(proxyToCluster.getMembers()).thenReturn(Arrays.asList(leaderMember, followerMember));
        when(etcdProxyFactory.getEtcdProxy(clusterName, clusterUrl, clusterVersion)).thenReturn(proxyToCluster);

        EtcdProxy proxyToLeader = mock(EtcdProxy.class);
        EtcdSelfStats leaderStats = new EtcdSelfStats();
        leaderStats.setState("leader");
        when(proxyToLeader.getSelfStats()).thenReturn(leaderStats);
        when(etcdProxyFactory.getEtcdProxy(clusterName, leaderMember.getClientURLs().get(0), clusterVersion)).thenReturn(proxyToLeader);

        EtcdProxy proxyToFollower = mock(EtcdProxy.class);
        EtcdSelfStats followerStats = new EtcdSelfStats();
        followerStats.setState("follower");
        when(proxyToFollower.getSelfStats()).thenReturn(followerStats);
        when(etcdProxyFactory.getEtcdProxy(clusterName, followerMember.getClientURLs().get(0), clusterVersion)).thenReturn(proxyToFollower);

        clusterManager.refreshCluster(clusterName);

        EtcdCluster refreshedCluster = clusterManager.getCluster(clusterName);
        assertEquals(leaderMember.getClientURLs().get(0), refreshedCluster.getAddress());
        assertEquals(2, refreshedCluster.getMembers().size());
        assertTrue(refreshedCluster.isRefreshed());
    }
    
}
