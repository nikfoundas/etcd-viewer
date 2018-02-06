package org.github.etcd.service.api.v3;

import com.coreos.jetcd.Client;
import com.coreos.jetcd.KV;
import com.coreos.jetcd.Lease;
import com.coreos.jetcd.cluster.Member;
import com.coreos.jetcd.cluster.MemberListResponse;
import com.coreos.jetcd.data.ByteSequence;
import com.coreos.jetcd.data.KeyValue;
import com.coreos.jetcd.kv.DeleteResponse;
import com.coreos.jetcd.kv.GetResponse;
import com.coreos.jetcd.kv.PutResponse;
import com.coreos.jetcd.lease.LeaseGrantResponse;
import com.coreos.jetcd.lease.LeaseTimeToLiveResponse;
import com.coreos.jetcd.maintenance.StatusResponse;
import com.coreos.jetcd.options.DeleteOption;
import com.coreos.jetcd.options.GetOption;
import com.coreos.jetcd.options.LeaseOption;
import com.coreos.jetcd.options.PutOption;
import com.google.common.base.CharMatcher;
import org.apache.cxf.common.util.StringUtils;
import org.github.etcd.service.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

public class EtcdV3ProxyImpl implements EtcdProxy {
    private static final Logger LOG = LoggerFactory.getLogger(EtcdProxy.class);

    private final String targetUrl;
    private final String authenticationToken;
    private Client client;

    public EtcdV3ProxyImpl(String targetUrl, String authenticationToken) {
        this.targetUrl = targetUrl;
        this.authenticationToken = authenticationToken;
    }

    public EtcdV3ProxyImpl(String targetUrl) {
        this(targetUrl, null);
    }

    private Client getClient() {
        if (client == null) {
            client = Client.builder().endpoints(targetUrl).build();

            // TODO: support authentication
        }
        return client;
    }

    private KV getKvClient() {
        return getClient().getKVClient();
    }

    @Override
    public void close() {
        if (client != null) {
            client.close();
            client = null;
        }
    }

    @Override
    public String getVersion() {
        CompletableFuture<StatusResponse> getFuture = getClient().getMaintenanceClient().statusMember(targetUrl);
        try {
            return getFuture.get().getVersion();
        } catch (Exception e) {
            throw new EtcdException(e);
        }
    }

    @Override
    public Boolean isAuthEnabled() {
        // TODO: check if auth is enabled
        return false;
    }

    @Override
    public EtcdSelfStats getSelfStats() {
        CompletableFuture<MemberListResponse> membersFuture = getClient().getClusterClient().listMember();
        CompletableFuture<StatusResponse> statusFuture = getClient().getMaintenanceClient().statusMember(targetUrl);
        try {
            List<Member> members = membersFuture.get().getMembers();
            EtcdSelfStats result = new EtcdSelfStats();
            StatusResponse response = statusFuture.get();
            long memberId = response.getHeader().getMemberId();
            result.setId(""+memberId);
            if (response.getLeader() == memberId) {
                result.setState("leader");
            } else {
                result.setState("follower");
            }
            result.setName("");
            members.forEach((m) -> {
                if (memberId == m.getId()) {
                    result.setName(m.getName());
                }
            });
            return result;
        } catch (Exception e) {
            throw new EtcdException(e);
        }
    }

    @Override
    public List<EtcdMember> getMembers() {

        String version = getVersion();

        LOG.info("Using version: '{}' to detect cluster members", version);

        LOG.info("Authentication is: " + (isAuthEnabled() ? "enabled" : "disabled"));

        CompletableFuture<MemberListResponse> membersFuture = getClient().getClusterClient().listMember();
        try {
            List<EtcdMember> result = new ArrayList<>();
            for (Member m : membersFuture.get().getMembers()) {
                EtcdMember member = new EtcdMember();
                member.setId(""+m.getId());
                member.setName(m.getName());
                member.setState(StringUtils.isEmpty(m.getName()) ? "started" : "offline");
                member.setClientURLs(m.getClientURLS());
                member.setPeerURLs(m.getPeerURLs());

                result.add(member);
            }
            return result;
        } catch (Exception e) {
            throw new EtcdException(e);
        }
    }

    private static<T> CompletableFuture<List<T>> sequence(List<CompletableFuture<T>> com) {
        return CompletableFuture.allOf(com.toArray(new CompletableFuture[com.size()]))
                .thenApply(v -> com.stream()
                        .map(CompletableFuture::join)
                        .collect(toList())
                );
    }

    //private static String stripPrefix

    @Override
    public EtcdNode getNode(final String key) {
        Lease leaseClient = getClient().getLeaseClient();
        KV kvClient = getKvClient();
        ByteSequence bKey = ByteSequence.fromString(key);
        CompletableFuture<GetResponse> getResponse = getKvClient().get(
                bKey,
                GetOption.newBuilder().withPrefix(bKey).withKeysOnly(true).build()
        );
        List<CompletableFuture<GetResponse>> valueGetters = new ArrayList<>();
        List<CompletableFuture<LeaseTimeToLiveResponse>> leaseGetters = new ArrayList<>();
        Map<Long, List<EtcdNode>> leaseToNodes = new HashMap<>();
        CharMatcher slashCounter = CharMatcher.is('/');
        int numSlashesInKey = slashCounter.countIn(key);
        Predicate<KeyValue> isKeyOrChild = (kv) -> {
            String keyUtf = kv.getKey().toStringUtf8();
            int numSlashes = slashCounter.countIn(keyUtf);
            return numSlashes == numSlashesInKey || (numSlashes == numSlashesInKey + 1 && keyUtf.endsWith("/"));
        };
        try {
            GetResponse response = getResponse.get();
            EtcdNode result = new EtcdNode();
            if (response.getCount() > 1) {
                result.setDir(true);
            } else if (response.getCount() == 1 && response.getKvs().get(0).getKey().toStringUtf8().endsWith("/")) {
                result.setDir(true);
            } else if ("".equals(key) || "/".equals(key)) {
                result.setDir(true);
            }
            result.setNodes(new ArrayList<>());
            Set<ByteSequence> directories = new HashSet<>();
            for (KeyValue kv : response.getKvs()) {
                if (isKeyOrChild.test(kv)) {
                    valueGetters.add(kvClient.get(kv.getKey()));
                    if (kv.getKey().toStringUtf8().endsWith("/")) {
                        directories.add(kv.getKey());
                    }
                } else {
                    String keyUtf = kv.getKey().toStringUtf8();
                    int slash = keyUtf.indexOf('/', key.length());
                    String childKey = keyUtf.substring(0, slash + 1);
                    ByteSequence bsChild = ByteSequence.fromString(childKey);
                    if (!directories.contains(bsChild)) {
                        // No key exists for this child node directory
                        directories.add(bsChild);
                        EtcdNode child = new EtcdNode();
                        child.setKey(childKey);
                        child.setDir(true);
                        result.getNodes().add(child);
                    }
                }
            }
            List<KeyValue> values = new ArrayList<>();
            //sequence(valueGetters).get().stream().map(resp -> resp.getKvs().get(0)).collect(toList());
            for (GetResponse r : sequence(valueGetters).get()) {
                values.addAll(r.getKvs());
            }
            for (KeyValue kv : values) {
                EtcdNode current;
                if (kv.getKey().equals(bKey)) {
                    current = result;
                } else {
                    current = new EtcdNode();
                    result.getNodes().add(current);
                }
                if (directories.contains(kv.getKey())) {
                    current.setDir(true);
                }
                current.setKey(kv.getKey().toStringUtf8());
                current.setValue(kv.getValue().toStringUtf8());
                current.setCreatedIndex(kv.getCreateRevision());
                current.setModifiedIndex(kv.getModRevision());
                if (kv.getLease() > 0) {
                    leaseToNodes.computeIfAbsent(kv.getLease(), (k) -> new ArrayList<>()).add(current);
                } // kv.getLease() == 0 means that there is no lease on this key
            }
            for (Long lease : leaseToNodes.keySet()) {
                leaseGetters.add(leaseClient.timeToLive(lease, LeaseOption.DEFAULT));
            }
            TimeZone tz = TimeZone.getTimeZone("UTC");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            df.setTimeZone(tz);
            Calendar now = Calendar.getInstance();
            for (LeaseTimeToLiveResponse r : sequence(leaseGetters).get()) {
                leaseToNodes.get(r.getID()).forEach(
                        n -> {
                            n.setTtl(r.getTTl());
                            Calendar expiration = ((Calendar) now.clone());
                            expiration.add(Calendar.SECOND, (int) r.getTTl());
                            n.setExpiration(df.format(expiration.getTime()));
                        }
                );
            }
            if (result.getKey() == null) {
                result.setKey(key);
            }
            if (result.getValue() == null) {
                result.setValue("");
            }
            return result;
        } catch (Exception e) {
            throw new EtcdException(e);
        }
    }

    @Override
    public void saveNode(EtcdNode node) {
        EtcdResponse response = saveOrUpdateNode(node, false);

        LOG.debug("Created Node: " + response);
    }

    @Override
    public EtcdNode updateNode(EtcdNode node) {
        EtcdResponse response = saveOrUpdateNode(node, true);

        LOG.debug("Updated Node: " + response);

        return response.getPrevNode();
    }

    @Override
    public EtcdNode deleteNode(EtcdNode node) {
        return deleteNode(node, false);
    }

    @Override
    public EtcdNode deleteNode(EtcdNode node, boolean recursive) {

        ByteSequence key = ByteSequence.fromString(node.getKey());
        DeleteOption option;
        if (node.isDir()) {
            option = DeleteOption.newBuilder().withPrefix(key).build();
        } else {
            option = DeleteOption.DEFAULT;
        }
        CompletableFuture<DeleteResponse> deleteFuture = getKvClient().delete(
                key,
                option
        );
        try {
            deleteFuture.get();
            return node;
        } catch (Exception e) {
            throw new EtcdException(e);
        }
    }

    private String normalizeKey(final String key) {
        if (key == null) {
            return "/";
        }
        return key.startsWith("/") ? key.substring(1) + "/" : key + "/";
    }

    protected EtcdResponse saveOrUpdateNode(EtcdNode node, Boolean update) {
        if (node.isDir() && update && node.getTtl() == null) {
            LOG.warn("Remove directory TTL is not supported by etcd version 0.4.9");
        }
        if (node.isDir() && !node.getKey().endsWith("/")) {
            String prevKey = node.getKey();
            String newKey = prevKey + "/";
            LOG.debug("APIv3 appending / to mark key as directory: " + prevKey + " -> " + newKey);
            node.setKey(newKey);
        }
        LeaseGrantResponse leaseGrantResponse = null;
        if (node.getTtl() != null && node.getTtl() > 0) {
            CompletableFuture<LeaseGrantResponse> leaseFuture = getClient().getLeaseClient().grant(node.getTtl());
            try {
                leaseGrantResponse = leaseFuture.get();
            } catch (Exception e) {
                throw new EtcdException(e);
            }
        }
        ByteSequence key = ByteSequence.fromString(node.getKey());
        ByteSequence value = ByteSequence.fromString(node.getValue() == null ? "" : node.getValue());
        PutOption.Builder optionBuilder = PutOption.newBuilder();
        if (leaseGrantResponse != null && leaseGrantResponse.getID() != 0) {
            optionBuilder.withLeaseId(leaseGrantResponse.getID());
        }
        optionBuilder.withPrevKV();
        CompletableFuture<PutResponse> putFuture = getKvClient().put(key, value, optionBuilder.build());
        try {
            EtcdResponse result = new EtcdResponse();
            PutResponse putResponse = putFuture.get();
            if (putResponse.hasPrevKv()) {
                EtcdNode prevNode = new EtcdNode(node);
                prevNode.setValue(putResponse.getPrevKv().getValue().toStringUtf8());
                result.setPrevNode(prevNode);
            }
            EtcdNode newNode = new EtcdNode(node);
            newNode.setModifiedIndex(putResponse.getHeader().getRevision());
            if (leaseGrantResponse != null && leaseGrantResponse.getID() != 0) {
                TimeZone tz = TimeZone.getTimeZone("UTC");
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                df.setTimeZone(tz);
                Calendar expiration = Calendar.getInstance();
                newNode.setTtl(leaseGrantResponse.getTTL());
                expiration.add(Calendar.SECOND, (int) leaseGrantResponse.getTTL());
                newNode.setExpiration(df.format(expiration.getTime()));
            }
            result.setNode(newNode);
            return result;
        } catch (Exception e) {
            throw new EtcdException(e);
        }
    }

}
