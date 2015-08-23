package org.github.etcd.service.rest;

import java.util.List;


public interface EtcdProxy extends AutoCloseable {

    void close();
    /**
     * Performs an http <b>GET</b> to the <b>/version</b> endpoint
     *
     * @return etcd registry version
     */
    String getVersion();

    Boolean isAuthEnabled();

    /**
     * Performs an http <b>GET</b> to the <b>/v2/stats/self</b> endpoint
     *
     * @return etcd node self statistics
     */
    EtcdSelfStats getSelfStats();

    /**
     * If the reported etcd version is 0.4.x it uses the peer address
     * <b>http://&lt;host&gt;:7001/</b> and performs an http <b>GET</b>
     * against the <b>/v2/admin/machines</b> endpoint. Otherwise it
     * uses the default client provided address URL and performs an
     * http <b>GET</b> to the <b>/v2/members</b> endpoint.
     *
     * @return the list of etcd cluster members and their roles
     */
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
