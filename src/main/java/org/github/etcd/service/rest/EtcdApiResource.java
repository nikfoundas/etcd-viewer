package org.github.etcd.service.rest;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * etcd rest API interface with jax-rs annotations
 * <p>
 * CXF client generates a proxy around this interface
 * allowing us to hide the low level jax-rs client API
 * </p>
 */
public interface EtcdApiResource {

    @GET
    @Path("/version")
    @Produces(MediaType.TEXT_PLAIN)
    String getVersion();

    @GET
    @Path("/v2/stats/leader")
    @Produces(MediaType.APPLICATION_JSON)
    String getLeaderStats();

    @GET
    @Path("/v2/stats/self")
    @Produces(MediaType.APPLICATION_JSON)
    EtcdSelfStats getSelfStats();

    @GET
    @Path("/v2/keys/_etcd/machines")
    @Produces(MediaType.APPLICATION_JSON)
    EtcdResponse getMachines();

    @GET
    @Path("/v2/keys/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    EtcdResponse getNode(@PathParam("key") String key);

    @PUT
    @Path("/v2/keys/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    EtcdResponse setNode(@PathParam("key") String key, @FormParam("dir") Boolean directory, @FormParam("value") String value, @FormParam("ttl") Long ttl, @FormParam("prevExist") Boolean update);

    @DELETE
    @Path("/v2/keys/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    EtcdResponse deleteKey(@PathParam("key") String key);

    @DELETE
    @Path("/v2/keys/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    EtcdResponse deleteDirectory(@PathParam("key") String key, @QueryParam("recursive") Boolean recursive);

//    @PUT
//    @Path("/v2/keys/{key}")
//    @Produces(MediaType.APPLICATION_JSON)
//    Response setNodeRaw(@PathParam("key") String key, @FormParam("dir") Boolean directory, @FormParam("value") String value, @FormParam("ttl") Long ttl, @FormParam("prevExist") Boolean update);
//
//    @DELETE
//    @Path("/v2/keys/{key}")
//    @Produces(MediaType.APPLICATION_JSON)
//    Response deleteNodeRaw(@PathParam("key") String key, @QueryParam("recursive") Boolean recursive);
}
