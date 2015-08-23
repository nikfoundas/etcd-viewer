package org.github.etcd.rest;

import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.github.etcd.service.rest.EtcdException;
import org.github.etcd.service.rest.EtcdMember;
import org.github.etcd.service.rest.EtcdNode;
import org.github.etcd.service.rest.EtcdProxy;
import org.github.etcd.service.rest.EtcdSelfStats;
import org.github.etcd.service.rest.impl.EtcdProxyImpl;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestEtcdProxy extends Assert {

    private static final Logger log = LoggerFactory.getLogger(TestEtcdProxy.class);

    private static EtcdProxy etcdProxy;

    private static final String JUNIT_ROOT = "/junit_" + System.currentTimeMillis();

    private static EtcdSimulator simulator;

    @BeforeClass
    public static void setUp() {

        boolean hasClientURL = System.getProperty("etcd.clientURL") != null;

        String clientURL = System.getProperty("etcd.clientURL", "http://localhost:2379/");

//        clientURL = "http://192.168.122.103:4001/";
        String username = "root";
        String password = "root";
        String token = DatatypeConverter.printBase64Binary((username + ":" + password).getBytes());

        etcdProxy = new EtcdProxyImpl(clientURL, token);

        // just check accessibility of etcd server
        // if it is not reachable for any reason then
        // start an inner simulator to do the tests
        try {
            etcdProxy.getVersion();
        } catch (Exception e) {

            if (!hasClientURL) {
                simulator = new EtcdSimulator();
                simulator.start();
            }
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (etcdProxy != null) {
            etcdProxy.close();
        }
        if (simulator != null) {
            simulator.stop();
        }
    }

    @Test
    public void testGetVersion() {
        String version = etcdProxy.getVersion();

        assertNotNull(version);
        assertTrue("Version: " + version + " does not contain etcd", version.contains("etcd"));
    }

    @Test
    public void testGetSelfStatistics() {
        EtcdSelfStats selfStats = etcdProxy.getSelfStats();

        log.info("Self Statistics: " + selfStats);

        assertNotNull(selfStats);

        // id is null on etcd v0.4.9
        assertNotNull(selfStats.getName());
        assertNotNull(selfStats.getLeaderInfo());
    }

    @Test
    public void testGetMembers() {
        List<EtcdMember> members = etcdProxy.getMembers();


        assertNotNull(members);
        assertFalse(members.isEmpty());

        StringBuffer sb = new StringBuffer(256);
        for (EtcdMember m : members) {
            sb.append('\n');
            sb.append(m.toString());
        }
        log.info("The cluster members are:" + sb.toString());
    }

    @Test
    public void testGetRoot() {
        EtcdNode root = etcdProxy.getNode("/");

        log.info("Retrieved: " + root);

        assertNotNull(root);

        assertTrue("/".equals(root.getKey()) || root.getKey() == null);
        assertTrue(root.isDir());
    }

    private String createTestKey() {
        String testName = new Throwable().getStackTrace()[1].getMethodName();
        return JUNIT_ROOT + "/" + testName;
    }
    @Test
    public void testSaveAndGetValueNode() {
        String key = createTestKey();
        String value = "some_test_content";
        EtcdNode toSave = new EtcdNode(key, value);
        try {
            etcdProxy.saveNode(toSave);
        } catch (Exception e) {
            fail("Failed to save node: " + toSave);
        }

        EtcdNode retrieved = etcdProxy.getNode(key);

        assertEquals(key, retrieved.getKey());
        assertEquals(value, retrieved.getValue());
        assertEquals(false, retrieved.isDir());
        assertNull(retrieved.getTtl());
    }

    @Test
    public void testSaveAndGetDirectory() {
        String key = createTestKey();
        EtcdNode toSave = new EtcdNode(key);
        try {
            etcdProxy.saveNode(toSave);
        } catch (Exception e) {
            fail("Failed to save node: " + toSave);
        }

        EtcdNode retrieved = etcdProxy.getNode(key);

        assertEquals(key, retrieved.getKey());
        assertEquals(true, retrieved.isDir());
        assertNull(retrieved.getTtl());
    }

    @Test
    public void testDeleteValue() {
        String key = createTestKey();

        etcdProxy.saveNode(new EtcdNode(key, "foobar"));

        EtcdNode retrieved = etcdProxy.getNode(key);

        assertNotNull(retrieved);
        assertFalse(retrieved.isDir());
        assertEquals(key, retrieved.getKey());

        EtcdNode deleted = etcdProxy.deleteNode(retrieved);

        assertNotNull(deleted);
        assertEquals(key, deleted.getKey());

        try {
            retrieved = etcdProxy.getNode(key);
            fail("Node: " + key + " should be deleted");
        } catch (Exception e) {
        }
    }

    @Test
    public void testDeleteDirectory() {
        String key = createTestKey();

        etcdProxy.saveNode(new EtcdNode(key));

        EtcdNode retrieved = etcdProxy.getNode(key);

        assertNotNull(retrieved);
        assertTrue(retrieved.isDir());
        assertEquals(key, retrieved.getKey());

        EtcdNode deleted = etcdProxy.deleteNode(retrieved);

        assertNotNull(deleted);
        assertEquals(key, deleted.getKey());

        try {
            retrieved = etcdProxy.getNode(key);
            fail("Node: " + key + " should be deleted");
        } catch (Exception e) {
        }
    }

    @Test
    public void testDeleteNonEmptyDirectory() {
        String key = createTestKey();

        etcdProxy.saveNode(new EtcdNode(key));

        etcdProxy.saveNode(new EtcdNode(key + "/hello", "hello world"));

        EtcdNode retrieved = etcdProxy.getNode(key);

        assertNotNull(retrieved);
        assertTrue(retrieved.isDir());
        assertEquals(key, retrieved.getKey());

        EtcdNode deleted = null;
        try {
            deleted = etcdProxy.deleteNode(retrieved);
            fail("Should not delete non empty directories without recursive=true");
        } catch (EtcdException e) {
            log.warn(e.getLocalizedMessage() + " API: " + e.getApiError(), e);
        }

        assertNull(deleted);

        assertNotNull(etcdProxy.getNode(key));
    }

    @Test
    public void testDeleteDirectoryRecursive() {
        String key = createTestKey();

        etcdProxy.saveNode(new EtcdNode(key));

        etcdProxy.saveNode(new EtcdNode(key + "/hello", "hello world"));

        EtcdNode retrieved = etcdProxy.getNode(key);

        assertNotNull(retrieved);
        assertTrue(retrieved.isDir());
        assertEquals(key, retrieved.getKey());

        EtcdNode deleted = etcdProxy.deleteNode(retrieved, true);

        assertNotNull(deleted);
        assertEquals(key, deleted.getKey());

        try {
            retrieved = etcdProxy.getNode(key);
            fail("Node: " + key + " should be deleted");
        } catch (Exception e) {
        }
    }

    @Test
    public void testCreateExpiringValue() throws Exception {
        String key = createTestKey();
        String value = "some_test_content";
        Long ttl = 2L;
        EtcdNode toSave = new EtcdNode(key, value, ttl);
        try {
            etcdProxy.saveNode(toSave);
        } catch (Exception e) {
            fail("Failed to save node: " + toSave);
        }

        EtcdNode retrieved = etcdProxy.getNode(key);

        log.info("Retrieved: " + retrieved);
        assertEquals(key, retrieved.getKey());
        assertEquals(value, retrieved.getValue());
        assertEquals(false, retrieved.isDir());
        assertNotNull(retrieved.getTtl());

        // wait for node to expire
        Thread.sleep(ttl * 1000L + 500L);

        try {
            retrieved = etcdProxy.getNode(key);
            fail("Node: " + retrieved + " should have expire by now");
        } catch (EtcdException e) {
            log.info("Node is expired as expected: " + e.getLocalizedMessage() + " - " + e.getApiError(), e);
        }
    }

    @Test
    public void testCreateValueNodeTwice() {
        String key = createTestKey();
        String value = "some_test_content";

        etcdProxy.saveNode(new EtcdNode(key, value));

        try {
            etcdProxy.saveNode(new EtcdNode(key, value));
            fail("Save again with the same key should fail. We should update instead.");
        } catch (EtcdException e) {
            log.info("Node already exists. Update instead: " + e.getLocalizedMessage() + " - " + e.getApiError(), e);
        }
    }

    @Test
    public void testUpdateValue() {
        String key = createTestKey();

        String[] values = new String[] { "initial value", "first update", "second update", "third update" };

        etcdProxy.saveNode(new EtcdNode(key, values[0]));

        EtcdNode retrieved = etcdProxy.getNode(key);

        assertNotNull(retrieved);
        assertEquals(key, retrieved.getKey());
        assertEquals(values[0], retrieved.getValue());


        for (int i=1; i<values.length; i++) {
            EtcdNode previous = etcdProxy.updateNode(new EtcdNode(key, values[i]));

            assertNotNull(previous);
            assertEquals(key, previous.getKey());
            assertEquals(values[i-1], previous.getValue());

            retrieved = etcdProxy.getNode(key);

            assertNotNull(retrieved);
            assertEquals(key, retrieved.getKey());
            assertEquals(values[i], retrieved.getValue());
        }
    }

    @Test
    public void testRemoveDirectoryTTL() throws Exception {

        if (etcdProxy.getVersion().contains("etcd 0.4")) {
            // skip this test
            return;
        }

        String key = createTestKey();
        Long ttl = 3L;
        etcdProxy.saveNode(new EtcdNode(key, ttl));

        Thread.sleep(1000L);

        EtcdNode previous = null;
        try {
            previous = etcdProxy.updateNode(new EtcdNode(key));
        } catch (EtcdException e) {
            log.error(e.getLocalizedMessage() + " - " + e.getApiError(), e);
            fail(e.getLocalizedMessage() + " - " + e.getApiError());
        }

        assertNotNull(previous);
        assertEquals(key, previous.getKey());

        assertNotNull(previous.getTtl());

        assertTrue(previous.getTtl() > 0);

        Thread.sleep(ttl * 1000);

        EtcdNode updated = etcdProxy.getNode(key);

        assertNotNull(updated);
        assertEquals(key, updated.getKey());

        assertNull(updated.getTtl());
    }

    @Test
    public void testCreateDirectoryTwice() {
        String key = createTestKey();
        etcdProxy.saveNode(new EtcdNode(key));
        try {
            etcdProxy.saveNode(new EtcdNode(key));
            fail("Save again with the same key should fail. We should update instead.");
        } catch (EtcdException e) {
            log.info("Node already exists. Update instead: " + e.getLocalizedMessage() + " - " + e.getApiError(), e);
        }
    }

    @Test
    public void testAddDirectoryTTL() throws Exception {
        String key = createTestKey();
        Long ttl = 3L;

        etcdProxy.saveNode(new EtcdNode(key));

        EtcdNode previous = null;
        try {
            previous = etcdProxy.updateNode(new EtcdNode(key, ttl));

        } catch (EtcdException e) {

            fail(e.getLocalizedMessage() + " - " + e.getApiError());
        }

        assertNotNull(previous);
        assertEquals(key, previous.getKey());
        assertTrue(previous.isDir());

        EtcdNode current = etcdProxy.getNode(key);

        assertNotNull(current);
        assertNotNull(current.getTtl());
        assertTrue(current.getTtl() > 0);

        Thread.sleep(ttl * 1000 + 500);

        try {
            EtcdNode retrieved = etcdProxy.getNode(key);
            fail("Node: " + retrieved + " should have expire by now");
        } catch (EtcdException e) {
            log.info("Node is expired as expected: " + e.getLocalizedMessage() + " - " + e.getApiError(), e);
        }

    }

}
