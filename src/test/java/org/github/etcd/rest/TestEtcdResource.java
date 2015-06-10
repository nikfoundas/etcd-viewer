package org.github.etcd.rest;

import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.github.etcd.service.EtcdManager;
import org.github.etcd.service.impl.EtcdManagerImpl;
import org.github.etcd.service.rest.EtcdNode;
import org.github.etcd.service.rest.EtcdApiResource;
import org.github.etcd.service.rest.EtcdResponse;
import org.junit.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

public class TestEtcdResource extends Assert {

    private EtcdApiResource delegate;

    private EtcdManager etcdResource;

    private static final String TEST_ROOT = "/junit";

//    @Before
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

        delegate = JAXRSClientFactory.create("http://localhost:4001/", EtcdApiResource.class, providers);

        etcdResource = new EtcdManagerImpl(delegate);


        EtcdNode node = new EtcdNode();
        node.setDir(true);
        node.setKey(TEST_ROOT);

        etcdResource.saveOrUpdate(node , false);
    }

//    @After
    public void teardown() {
        etcdResource.delete(TEST_ROOT, true);
    }

//    @Test
    public void testGetVersion() throws Exception {

        String version = etcdResource.getVersion();

        assertNotNull(version);

        assertTrue(version.startsWith("etcd"));

        System.out.println("ETCD Version: " + version);
    }

//    @Test
    public void testGetRootNode() throws Exception {

        EtcdResponse response = etcdResource.getNode("/");

        assertNotNull(response);

        assertEquals("get", response.getAction());

        assertNotNull(response.getNode());

        assertTrue(response.getNode().isDir());

        System.out.println("Root node: " + response.getNode());
    }

//    @Test
    public void testGetTestRootNode() throws Exception {

        EtcdResponse response = etcdResource.getNode(TEST_ROOT);

        assertNotNull(response);

        assertEquals("get", response.getAction());

        assertNotNull(response.getNode());

        assertTrue(response.getNode().isDir());

        System.out.println("Test root node: " + response.getNode());
    }

//    @Test
    public void testCreateDeleteDirectory() throws Exception {

        String directoryName = TEST_ROOT + "/foobar";

//        String testDirectory = "/junit/test_" + System.currentTimeMillis();

        EtcdNode node = new EtcdNode();
        node.setDir(true);
        node.setKey(directoryName);
        EtcdResponse create = etcdResource.saveOrUpdate(node, false);

        assertNotNull(create);

        System.out.println("Create response: " + create);

        assertEquals("set", create.getAction());
        assertNotNull(create.getNode());
        assertNull(create.getPrevNode());
        assertEquals(directoryName, create.getNode().getKey());
        assertTrue(create.getNode().isDir());

        EtcdResponse delete = etcdResource.delete(directoryName, true);

        assertNotNull(delete);

        System.out.println("Delete response: " + delete);

        assertEquals("delete", delete.getAction());
        assertNotNull(delete.getNode());
        assertNotNull(delete.getPrevNode());
        assertEquals(directoryName, delete.getNode().getKey());
        assertTrue(delete.getNode().isDir());

        assertEquals(create.getNode(), delete.getPrevNode());
    }

//    @Test
    public void testCreateDeleteValue() throws Exception {

        String key = TEST_ROOT + "/value_create_delete";
        String value = "this a a demo value for " + key;

//        String testDirectory = "/junit/test_" + System.currentTimeMillis();

        EtcdNode node = new EtcdNode();
        node.setKey(key);
        node.setValue(value);

        EtcdResponse create = etcdResource.saveOrUpdate(node, false);

        assertNotNull(create);

        System.out.println("Create response: " + create);

        assertEquals("set", create.getAction());
        assertNotNull(create.getNode());
        assertNull(create.getPrevNode());
        assertEquals(key, create.getNode().getKey());
        assertFalse(create.getNode().isDir());

        assertEquals(value, create.getNode().getValue());


        EtcdResponse delete = etcdResource.delete(key, false);

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

//    @Test
    public void testGetMembers() {

        EtcdResponse response = delegate.getMachines();

        List<EtcdNode> members = response.getNode().getNodes();

        assertNotNull(response);
        assertNotNull(response.getNode());
        assertNotNull(response.getNode().getNodes());
        assertEquals(1, members.size());

    }

//    @Test
    public void testCreateDirTwice() {
//        String key = TEST_ROOT + "/create_dir_twice";
//
//        EtcdResponse response = etcdResource.createDirectory(key);
//
//        assertEquals(key, response.getNode().getKey());
//        assertTrue(response.getNode().isDir());
//
//        try {
//            Response r = etcdResource.createDir(key);
//
//            System.out.println("Status: " + r.getStatus());
//            System.out.println("Status Family: " + r.getStatusInfo().getFamily());
//            System.out.println("Status Reason: " + r.getStatusInfo().getReasonPhrase());
//            System.out.println("Headers: " + r.getHeaders());
//
//            InputStream is = (InputStream) r.getEntity();
//
//            StringWriter writer = new StringWriter();
//
//            InputStreamReader isr = new InputStreamReader(is);
//
//            char[] buffer = new char[128];
//            long count = 0;
//            int n = 0;
//            while (-1 != (n = isr.read(buffer))) {
//                writer.write(buffer, 0, n);
//                count += n;
//            }
//
//            System.out.println("Entity: " + writer.toString());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
//    @Test
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
