/**
 *
 */
package org.github.etcd.service;

import java.util.Date;
import java.util.List;

import org.github.etcd.service.rest.EtcdMember;

public class EtcdCluster {

    private String name;
    private List<EtcdMember> members;

    private String address;

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
    public List<EtcdMember> getMembers() {
        return members;
    }
    public void setMembers(List<EtcdMember> members) {
        this.members = members;
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
    @Override
    public String toString() {
        return "EtcdCluster [name=" + name + ", address=" + address + "]";
    }
}
