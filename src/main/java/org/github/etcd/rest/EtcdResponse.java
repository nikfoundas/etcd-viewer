/**
 *
 */
package org.github.etcd.rest;

public class EtcdResponse {

    private String action;
    private EtcdNode node;
    private EtcdNode prevNode;

    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }
    public EtcdNode getNode() {
        return node;
    }
    public void setNode(EtcdNode node) {
        this.node = node;
    }
    public EtcdNode getPrevNode() {
        return prevNode;
    }
    public void setPrevNode(EtcdNode prevNode) {
        this.prevNode = prevNode;
    }
    @Override
    public String toString() {
        return "EtcdResponse [action=" + action + ", node=" + node
                + ", prevNode=" + prevNode + "]";
    }
}
