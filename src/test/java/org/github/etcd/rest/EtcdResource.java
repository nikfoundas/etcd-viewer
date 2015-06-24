/**
 *
 */
package org.github.etcd.rest;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.github.etcd.service.rest.EtcdMembers;
import org.github.etcd.service.rest.EtcdResponse;
import org.github.etcd.service.rest.EtcdSelfStats;

@Path("/")
public interface EtcdResource extends AutoCloseable {

    @GET
    @Path("/version")
    @Produces(MediaType.TEXT_PLAIN)
    String getVersion();

//    @GET
//    @Path("/v2/stats/leader")
//    @Produces(MediaType.APPLICATION_JSON)
//    String getLeaderStats();

    @GET
    @Path("/v2/stats/self")
    @Produces(MediaType.APPLICATION_JSON)
    EtcdSelfStats getSelfStats();

    @GET
    @Path("/v2/members")
    @Produces(MediaType.APPLICATION_JSON)
    EtcdMembers getMembers();

    @GET
    @Path("/v2/keys/{key:(.*)?}")
    @Produces(MediaType.APPLICATION_JSON)
    EtcdResponse getNode(@PathParam("key") String key);

    @PUT
    @Path("/v2/keys/{key:(.*)}")
    @Produces(MediaType.APPLICATION_JSON)
    EtcdResponse putNode(@PathParam("key") String key, @FormParam("dir") Boolean directory, @FormParam("value") String value, @FormParam("ttl") String ttl, @FormParam("prevExist") Boolean update);

    @DELETE
    @Path("/v2/keys/{key:(.*)}")
    @Produces(MediaType.APPLICATION_JSON)
    EtcdResponse deleteNode(@PathParam("key") String key, @QueryParam("dir") Boolean directory, @QueryParam("recursive") Boolean recursive);

}
