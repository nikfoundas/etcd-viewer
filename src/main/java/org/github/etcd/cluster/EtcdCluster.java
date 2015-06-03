/**
 *
 */
package org.github.etcd.cluster;

import java.util.Date;
import java.util.List;

public class EtcdCluster {

    private String name;
    private List<EtcdPeer> peers;

    private String address;
    private String version;

    private Date lastRefreshTime;

    public EtcdCluster() {
    }
    public EtcdCluster(String name, String address) {
        this.name = name;
        this.address = address;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public List<EtcdPeer> getPeers() {
        return peers;
    }
    public void setPeers(List<EtcdPeer> peers) {
        this.peers = peers;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public Date getLastRefreshTime() {
        return lastRefreshTime;
    }
    public void setLastRefreshTime(Date lastRefreshTime) {
        this.lastRefreshTime = lastRefreshTime;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    @Override
    public String toString() {
        return "EtcdCluster [name=" + name + ", address=" + address + "]";
    }
}
