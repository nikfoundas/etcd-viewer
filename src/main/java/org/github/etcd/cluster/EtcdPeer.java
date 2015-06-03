package org.github.etcd.cluster;


public class EtcdPeer {

    private String id;
    private String etcd;
    private String raft;
    private String status;
    private String version;

    public EtcdPeer() {
    }
    public EtcdPeer(String id, String etcd, String raft) {
        this.id = id;
        this.etcd = etcd;
        this.raft = raft;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getEtcd() {
        return etcd;
    }
    public void setEtcd(String etcd) {
        this.etcd = etcd;
    }
    public String getRaft() {
        return raft;
    }
    public void setRaft(String raft) {
        this.raft = raft;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
}
