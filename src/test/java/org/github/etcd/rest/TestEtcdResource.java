package org.github.etcd.rest;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

public class TestEtcdResource extends Assert {

    private EtcdResource delegate;

    private EtcdResourceProxy etcdResource;

    private static final String TEST_ROOT = "/junit";

    @Before
    public void setup() {
        ObjectMapper mapper = new ObjectMapper();
        List<Object> providers = new ArrayList<>();
        providers.add(new JacksonJsonProvider(mapper));

//        providers.add(new EtcdNodeMessageBodyWriter());

//        providers.add(new ExceptionMapper<ForbiddenException>() {
//            @Override
//            public Response toResponse(ForbiddenException exception) {
//                return null;
//            }
//        });

        delegate = JAXRSClientFactory.create("http://192.168.122.101:4001/", EtcdResource.class, providers);

        etcdResource = new EtcdResourceProxy(delegate);

//        etcdResource.createDirectory(TEST_ROOT);
    }

    @After
    public void teardown() {

//        etcdResource.deleteDirectory(TEST_ROOT);

    }

    @Test
    public void testGetVersion() throws Exception {

        String version = etcdResource.getVersion();

        assertNotNull(version);

        assertTrue(version.startsWith("etcd"));

        System.out.println("ETCD Version: " + version);
    }

    @Test
    public void testGetRootNode() throws Exception {

        EtcdResponse response = etcdResource.getNode("/");

        assertNotNull(response);

        assertEquals("get", response.getAction());

        assertNotNull(response.getNode());

        assertTrue(response.getNode().isDir());

        System.out.println("Root node: " + response.getNode());
    }

    @Test
    public void testGetTestRootNode() throws Exception {

        EtcdResponse response = etcdResource.getNode(TEST_ROOT);

        assertNotNull(response);

        assertEquals("get", response.getAction());

        assertNotNull(response.getNode());

        assertTrue(response.getNode().isDir());

        System.out.println("Test root node: " + response.getNode());
    }

    @Test
    public void testCreateDeleteDirectory() throws Exception {

        String directoryName = TEST_ROOT + "/foobar";

//        String testDirectory = "/junit/test_" + System.currentTimeMillis();

        EtcdResponse create = etcdResource.createDirectory(directoryName);

        assertNotNull(create);

        System.out.println("Create response: " + create);

        assertEquals("set", create.getAction());
        assertNotNull(create.getNode());
        assertNull(create.getPrevNode());
        assertEquals(directoryName, create.getNode().getKey());
        assertTrue(create.getNode().isDir());

        EtcdResponse delete = etcdResource.deleteDirectory(directoryName);

        assertNotNull(delete);

        System.out.println("Delete response: " + delete);

        assertEquals("delete", delete.getAction());
        assertNotNull(delete.getNode());
        assertNotNull(delete.getPrevNode());
        assertEquals(directoryName, delete.getNode().getKey());
        assertTrue(delete.getNode().isDir());

        assertEquals(create.getNode(), delete.getPrevNode());
    }

    @Test
    public void testCreateDeleteValue() throws Exception {

        String key = TEST_ROOT + "/value_create_delete";
        String value = "this a a demo value for " + key;

//        String testDirectory = "/junit/test_" + System.currentTimeMillis();

        EtcdResponse create = etcdResource.createValue(key, value);

        assertNotNull(create);

        System.out.println("Create response: " + create);

        assertEquals("set", create.getAction());
        assertNotNull(create.getNode());
        assertNull(create.getPrevNode());
        assertEquals(key, create.getNode().getKey());
        assertFalse(create.getNode().isDir());

        assertEquals(value, create.getNode().getValue());


        EtcdResponse delete = etcdResource.deleteValue(key);

        assertNotNull(delete);

        System.out.println("Delete response: " + delete);

        assertEquals("delete", delete.getAction());
        assertNotNull(delete.getNode());
        assertNotNull(delete.getPrevNode());
        assertEquals(key, delete.getNode().getKey());
        assertFalse(delete.getNode().isDir());

        assertEquals(value, delete.getPrevNode().getValue());

        assertEquals(create.getNode(), delete.getPrevNode());
    }

    @Test
    public void testGetMembers() {

        EtcdResponse response = delegate.getMachines();

        List<EtcdNode> members = response.getNode().getNodes();

        assertNotNull(response);
        assertNotNull(response.getNode());
        assertNotNull(response.getNode().getNodes());
        assertEquals(3, members.size());

    }

    @Test
    public void testCreateDirTwice() {
        String key = TEST_ROOT + "/create_dir_twice";

        EtcdResponse response = etcdResource.createDirectory(key);

        assertEquals(key, response.getNode().getKey());
        assertTrue(response.getNode().isDir());

        try {
            Response r = etcdResource.createDir(key);

            System.out.println("Status: " + r.getStatus());
            System.out.println("Status Family: " + r.getStatusInfo().getFamily());
            System.out.println("Status Reason: " + r.getStatusInfo().getReasonPhrase());
            System.out.println("Headers: " + r.getHeaders());

            InputStream is = (InputStream) r.getEntity();

            StringWriter writer = new StringWriter();

            InputStreamReader isr = new InputStreamReader(is);

            char[] buffer = new char[128];
            long count = 0;
            int n = 0;
            while (-1 != (n = isr.read(buffer))) {
                writer.write(buffer, 0, n);
                count += n;
            }

            System.out.println("Entity: " + writer.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testSetValue() {
        EtcdResponse response;
//        response = etcdResource.setValue("/my/dummy/value", "Dummy content");
//        System.out.println("SET: " + response);
//        response = etcdResource.setEphemeralValue("/my/dummy/ephemeral", "auto erased value with TTL", 30);
//        System.out.println("SET: " + response);

        EtcdNode node = new EtcdNode();
        node.setValue("Hello");

//        response = etcdResource.setNode("/my/dummy/baroufa", node);


//        response = etcdResource.setNode("/test/data", false, "the actual data", null, false);


/*        MultivaluedMap<String, Object> theNode = new MultivaluedHashMap<>();
        theNode.add("dir", "true");
        theNode.add("ttl", "30");

        response = etcdResource.setNode("/my/dummy/baroufa_tmp", theNode);*/

//        System.out.println("SET: " + response);
    }
}
