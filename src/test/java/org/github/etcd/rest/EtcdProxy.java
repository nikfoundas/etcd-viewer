package org.github.etcd.rest;

import java.util.List;

import org.github.etcd.service.rest.EtcdNode;
import org.github.etcd.service.rest.EtcdSelfStats;

public interface EtcdProxy extends AutoCloseable {

    /**
     * Performs an http GET to the /version endpoint
     *
     * @return etcd registry version
     */
    String getVersion();

    /**
     * Performs an http GET to the /v2/stats/self endpoint
     *
     * @return etcd node self statistics
     */
    EtcdSelfStats getSelfStats();


    List<EtcdMember> getMembers();

    /**
     * Performs an http GET to the /v2/keys/{key} endpoint.
     * If the supplied key is not found it throws an exception
     * indicating so.
     *
     * @param key The key-value key parameter
     * @return The requested key-value or directory node
     */
    EtcdNode getNode(String key);

    /**
     * Performs an http PUT to the /v2/keys/{key} endpoint by
     * submitting a url encoded form. This form contains the
     * 'value' parameter if it is a value node or the 'dir=true'
     * parameter if it is a directory. The 'prevExist' parameter
     * is sent always as false to indicate that this node does
     * not exist already. If the supplied node contains TTL then
     * the 'ttl' parameter is set as well.
     *
     * @param node The key-value pair or directory to save
     */
    void saveNode(EtcdNode node);

    /**
     * Performs an http PUT to the /v2/keys/{key} endpoint by
     * submitting a url encoded form. This form contains the
     * 'value' parameter if it is a value node or the 'dir=true'
     * parameter if it is a directory. The 'prevExist' parameter
     * is sent always as true to indicate that this node must
     * exist already. The 'ttl' parameter is always set to the
     * supplied TTL value if it is set otherwise it is sent as
     * empty '' to enable removing TTL from key-value pairs
     * or directories.
     *
     * @param node The key-value pair or directory to update
     * @return The node before the update
     */
    EtcdNode updateNode(EtcdNode node);

    /**
     * Performs an http DELETE to the /v2/keys/{key} endpoint.
     * If the supplied node is a directory then the query
     * parameter ?dir=true is appended to the endpoint url.

     * @param node The key-value pair or directory to delete.
     * @return The deleted node
     */
    EtcdNode deleteNode(EtcdNode node);

    /**
     * Performs an http DELETE to the /v2/keys/{key} endpoint.
     * If the supplied node is a directory then the query
     * parameter ?dir=true is appended to the endpoint url.

     * @param node The key-value pair or directory to delete.
     * @param recursive if it is set to true then even non
     * empty directory nodes can be removed. It appends the
     * ?recursive=true query parameter and skips the ?dir=true.
     * @return The deleted node
     */
    EtcdNode deleteNode(EtcdNode node, boolean recursive);

}
