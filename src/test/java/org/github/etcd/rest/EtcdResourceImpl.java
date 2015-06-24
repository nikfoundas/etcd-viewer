package org.github.etcd.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.github.etcd.service.rest.EtcdMember;
import org.github.etcd.service.rest.EtcdMembers;
import org.github.etcd.service.rest.EtcdNode;
import org.github.etcd.service.rest.EtcdResponse;
import org.github.etcd.service.rest.EtcdSelfStats;
import org.github.etcd.service.rest.EtcdSelfStats.LeaderInfo;

@Path("/")
public class EtcdResourceImpl implements EtcdResource {

    private EtcdSelfStats selfStats;
    private EtcdMembers members;
    private String version = "etcd 2.0.11";

    private AtomicLong createdIndex = new AtomicLong();
    private AtomicLong modifiedIndex = new AtomicLong();

    private ScheduledExecutorService scheduler;

    private ConcurrentMap<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    private EtcdTreeNode rootNode;

    public EtcdResourceImpl() {
        EtcdNode node = new EtcdNode("/");
        node.setCreatedIndex(createdIndex.getAndIncrement());
        node.setModifiedIndex(modifiedIndex.getAndIncrement());
        rootNode = new EtcdTreeNode(node);

        selfStats = new EtcdSelfStats();
        selfStats.setId("123456");
        selfStats.setName("etcd_simulator");
        selfStats.setState("StateLeader");
        selfStats.setLeaderInfo(new LeaderInfo());
        selfStats.getLeaderInfo().setLeader(selfStats.getId());

        members = new EtcdMembers();

        EtcdMember selfMember = new EtcdMember();
        selfMember.setId(selfStats.getId());
        selfMember.setName(selfStats.getName());
        selfMember.setState(selfStats.getState());
        selfMember.setClientURLs(Arrays.asList("http://simulator:2379/"));
        selfMember.setPeerURLs(Arrays.asList("http://simulator:2380/"));

        members.setMembers(Arrays.asList(selfMember));

        scheduler = new ScheduledThreadPoolExecutor(4);
    }

    @Override
    public void close() throws Exception {
        for (ScheduledFuture<?> task : scheduledTasks.values()) {
            task.cancel(true);
        }
        scheduledTasks.clear();
        scheduler.shutdown();

        rootNode = null;
    }

    @Override
    @GET
    @Path("/version")
    @Produces("text/plain")
    public String getVersion() {
        return version;
    }

    @Override
    @GET
    @Path("/v2/stats/self")
    @Produces("application/json")
    public EtcdSelfStats getSelfStats() {
        return selfStats;
    }

    @Override
    @GET
    @Path("/v2/members")
    @Produces("application/json")
    public EtcdMembers getMembers() {
        return members;
    }

    @Override
    @GET
    @Path("/v2/keys/{key:(.*)?}")
    @Produces("application/json")
    public EtcdResponse getNode(@PathParam("key") String key) {
        System.out.println("EtcdResourceImpl.getNode(" + key + ")");
        EtcdTreeNode treeNode = rootNode;
        if (key != null && key.length() > 0) {
            String[] parts = key.split("/");
            for (String part : parts) {
                treeNode = treeNode.getChildren().get(part);
                if (treeNode == null) {
                    throw new RuntimeException("Node: /" + key + " not found");
                }
            }
        }
        EtcdResponse response = new EtcdResponse();
        response.setAction("get");
        response.setNode(treeNode.getClonedContent(true));
        return response;
    }

    @Override
    @PUT
    @Path("/v2/keys/{key}")
    @Produces("application/json")
    public EtcdResponse putNode(@PathParam("key") String key,
            @FormParam("dir") Boolean directory,
            @FormParam("value") String value, @FormParam("ttl") String ttl,
            @FormParam("prevExist") Boolean update) {
        System.out.println("EtcdResourceImpl.putNode(" + key + ")");

        boolean updating = update != null ? update : false;
        boolean isDir = directory != null ? directory : false;

        if (key == null || key.length() == 0) {
            throw new RuntimeException("Modifying root node is not allowed");
        }

        EtcdTreeNode parent = rootNode;

        String[] parts = key.split("/");

        StringBuffer currentKey = new StringBuffer(key.length());

        currentKey.append('/');

        // create parent directories if they do not exist already
        for (int i=0; i<parts.length - 1; i++) {
            currentKey.append(parts[i]);
            EtcdTreeNode temp = parent.getChildren().get(parts[i]);
            if (temp == null) {
                if (updating) {
                    throw new RuntimeException("Directory: " + currentKey.toString() + " does not exist!");
                } else {
                    EtcdNode content = new EtcdNode(currentKey.toString());
                    content.setCreatedIndex(createdIndex.getAndIncrement());
                    content.setModifiedIndex(modifiedIndex.getAndIncrement());
                    temp = new EtcdTreeNode(content);

                    parent.getChildren().put(parts[i], temp);
                }
            }
            parent = temp;
            currentKey.append('/');
        }

        currentKey.append(parts[parts.length - 1]);

        EtcdTreeNode temp = parent.getChildren().get(parts[parts.length - 1]);

        if (updating && temp == null) {
            throw new RuntimeException("Node: " + currentKey.toString() + " does not exist!");
        }
        if (!updating && temp != null) {
            throw new RuntimeException("Node: " + currentKey.toString() + " already exists. Do an update instead?");
        }

        EtcdResponse response = new EtcdResponse();
        response.setAction("set");

        Long ttlSeconds = ttl == null || ttl.isEmpty() ? null : Long.parseLong(ttl);

        if (temp != null) {
            response.setPrevNode(temp.getClonedContent(false));

            temp.getContent().setModifiedIndex(modifiedIndex.getAndIncrement());

            temp.getContent().setValue(value);
            temp.setExpiration(ttlSeconds);

        } else {
            EtcdNode content = isDir ? new EtcdNode(currentKey.toString()) : new EtcdNode(currentKey.toString(), value);

            temp = new EtcdTreeNode(content);

            temp.setExpiration(ttlSeconds);

            content.setCreatedIndex(createdIndex.getAndIncrement());
            content.setModifiedIndex(modifiedIndex.getAndIncrement());

            parent.getChildren().put(parts[parts.length - 1], temp);
        }

        ScheduledFuture<?> previousTask = scheduledTasks.remove(key);
        if (previousTask != null) {
            System.err.println("Cancelling expiration for: " + key);
            previousTask.cancel(false);
        }

        if (ttlSeconds != null) {
            System.err.println("Scheduling expiration for: " + key + " after: " + ttlSeconds + " seconds");
            scheduledTasks.put(key, scheduler.schedule(new ExpireNodeTask(key, isDir), ttlSeconds, TimeUnit.SECONDS));
        }

        response.setNode(temp.getClonedContent(false));

        return response;
    }

    private class ExpireNodeTask implements Runnable {
        private final String key;
        private final boolean isDir;
        public ExpireNodeTask(String key, boolean isDir) {
            this.key = key;
            this.isDir = isDir;
        }
        @Override
        public void run() {
            scheduledTasks.remove(key);
            System.err.println("Expiring: " + key);
            try {
                if (isDir) {
                    deleteNode(key, null, true);
                } else {
                    deleteNode(key, null, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    @DELETE
    @Path("/v2/keys/{key}")
    @Produces("application/json")
    public EtcdResponse deleteNode(@PathParam("key") String key,
            @QueryParam("dir") Boolean directory,
            @QueryParam("recursive") Boolean recursive) {

        boolean isDir = directory != null ? directory : false;
        boolean isRecursive = recursive != null ? recursive : false;
        if (isRecursive) {
            isDir = true;
        }

        System.out.println("EtcdResourceImpl.deleteNode(" + key + ")");

        EtcdTreeNode parent = rootNode;
        EtcdTreeNode treeNode = rootNode;

        String[] parts = key.split("/");

        for (int i=0; i<parts.length; i++) {
            parent = treeNode;
            treeNode = parent.getChildren().get(parts[i]);
            if (treeNode == null) {
                throw new RuntimeException("Node: /" + key + " not found");
            }
        }

        if (treeNode.hasChildren() && !isRecursive) {
            throw new RuntimeException("Cannot delete non empty directory: " + key);
        }

        if (treeNode.getContent().isDir() && !treeNode.hasChildren() && !isDir) {
            throw new RuntimeException("Node is directory: " + key);
        }

        System.err.println("Deleting: " + key);
        treeNode = parent.getChildren().remove(parts[parts.length - 1]);

        EtcdResponse response = new EtcdResponse();
        response.setAction("delete");
        response.setNode(treeNode.getClonedContent(false));
        response.setPrevNode(response.getNode());
        return response;
    }

    private static class EtcdTreeNode {
        private EtcdNode content;
        private Long expirationTime;
        private ConcurrentMap<String, EtcdTreeNode> children;

        public EtcdTreeNode(EtcdNode content) {
            this.content = content;
            if (content.isDir()) {
                children = new ConcurrentHashMap<>();
            }
        }
        public EtcdNode getContent() {
            return content;
        }
        public boolean hasChildren() {
            return children != null && children.size() > 0;
        }
        public ConcurrentMap<String, EtcdTreeNode> getChildren() {
            return children;
        }
        public void setExpiration(Long ttl) {
            if (ttl == null) {
                expirationTime = null;
            } else {
                expirationTime = System.currentTimeMillis() + ttl * 1000L;
            }
        }
        public EtcdNode getClonedContent(boolean withChildren) {
            EtcdNode result = new EtcdNode();
            result.setKey(content.getKey());
            result.setDir(content.isDir());
            result.setValue(content.getValue());
            result.setCreatedIndex(content.getCreatedIndex());
            result.setModifiedIndex(content.getModifiedIndex());
            if (expirationTime != null) {
                result.setExpiration(new Date(expirationTime).toString());
                result.setTtl((expirationTime - System.currentTimeMillis()) / 1000);
            }
            if (withChildren && children != null && children.size() > 0) {
                List<EtcdNode> nodes = new ArrayList<>(children.size());
                for (EtcdTreeNode childNode : children.values()) {
                    nodes.add(childNode.getClonedContent(false));
                }
                result.setNodes(nodes);
            }
            return result;
        }
    }

}
