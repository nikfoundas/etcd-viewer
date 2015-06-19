/**
 *
 */
package org.github.etcd.service.rest;

import java.io.Serializable;
import java.util.List;

public class EtcdNode implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long createdIndex;
    private Long modifiedIndex;
    private boolean dir;
    private String key;
    private String value;
    private Long ttl;
    private String expiration;

    private List<EtcdNode> nodes;

    public EtcdNode() {
    }
    public EtcdNode(String key, boolean dir, String value, Long ttl) {
        this.key = key;
        this.dir = dir;
        this.value = value;
        this.ttl = ttl;
    }
    public EtcdNode(String key, String value) {
        this(key, false, value, null);
    }
    public EtcdNode(String key, String value, Long ttl) {
        this(key, false, value, ttl);
    }
    public EtcdNode(String key) {
        this(key, true, null, null);
    }
    public EtcdNode(String key, Long ttl) {
        this(key, true, null, ttl);
    }

    public Long getCreatedIndex() {
        return createdIndex;
    }
    public void setCreatedIndex(Long createdIndex) {
        this.createdIndex = createdIndex;
    }
    public Long getModifiedIndex() {
        return modifiedIndex;
    }
    public void setModifiedIndex(Long modifiedIndex) {
        this.modifiedIndex = modifiedIndex;
    }
    public boolean isDir() {
        return dir;
    }
    public void setDir(boolean dir) {
        this.dir = dir;
    }
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    public Long getTtl() {
        return ttl;
    }
    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }
    public String getExpiration() {
        return expiration;
    }
    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }
    public List<EtcdNode> getNodes() {
        return nodes;
    }
    public void setNodes(List<EtcdNode> nodes) {
        this.nodes = nodes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((createdIndex == null) ? 0 : createdIndex.hashCode());
        result = prime * result + (dir ? 1231 : 1237);
        result = prime * result
                + ((expiration == null) ? 0 : expiration.hashCode());
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result
                + ((modifiedIndex == null) ? 0 : modifiedIndex.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EtcdNode other = (EtcdNode) obj;
        if (createdIndex == null) {
            if (other.createdIndex != null)
                return false;
        } else if (!createdIndex.equals(other.createdIndex))
            return false;
        if (dir != other.dir)
            return false;
        if (expiration == null) {
            if (other.expiration != null)
                return false;
        } else if (!expiration.equals(other.expiration))
            return false;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        if (modifiedIndex == null) {
            if (other.modifiedIndex != null)
                return false;
        } else if (!modifiedIndex.equals(other.modifiedIndex))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }
    @Override
    public String toString() {
        return "EtcdNode [key=" + key + ", dir=" + dir + ", value=" + value
                + ", ttl=" + ttl + ", nodes=" + nodes + "]";
    }

}
