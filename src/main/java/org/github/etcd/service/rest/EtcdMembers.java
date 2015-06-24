package org.github.etcd.service.rest;

import java.util.List;

public class EtcdMembers {
    private List<EtcdMember> members;

    public List<EtcdMember> getMembers() {
        return members;
    }
    public void setMembers(List<EtcdMember> members) {
        this.members = members;
    }
    @Override
    public String toString() {
        return "EtcdMembers [members=" + members + "]";
    }
}
