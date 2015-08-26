/**
 *
 */
package org.github.etcd.service;

import java.util.Date;
import java.util.List;

import org.github.etcd.service.rest.EtcdMember;

public class EtcdCluster {

    private static final long REFRESH_EXPIRATION_MILLIS = 1L * 60L * 1000L; // 10 minutes

    private String name;
    private List<EtcdMember> members;

    private boolean refreshed = false;
    private boolean authEnabled;
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
    public boolean isRefreshed() {
        return refreshed;
    }
    public void setRefreshed(boolean refreshed) {
        this.refreshed = refreshed;
    }
    public Date getLastRefreshTime() {
        return lastRefreshTime;
    }
    public void setLastRefreshTime(Date lastRefreshTime) {
        this.lastRefreshTime = lastRefreshTime;
    }
    public boolean isAuthEnabled() {
        return authEnabled;
    }
    public void setAuthEnabled(boolean authEnabled) {
        this.authEnabled = authEnabled;
    }
    public boolean mustRefresh() {
        if (!refreshed) {
            return true;
        } else {
            return System.currentTimeMillis() - lastRefreshTime.getTime() > REFRESH_EXPIRATION_MILLIS;
        }
    }
    @Override
    public String toString() {
        return "EtcdCluster [name=" + name + ", address=" + address + "]";
    }
}
