/*
 *  Copyright © 2018-2019 Apollo Foundation
 */

package com.apollocurrency.aplwallet.apl.tools.impl.heightmon.web;

import com.apollocurrency.aplwallet.apl.tools.impl.heightmon.HeightMonitorService;
import com.apollocurrency.aplwallet.apl.tools.impl.heightmon.model.NetworkStats;
import com.apollocurrency.aplwallet.apl.tools.impl.heightmon.model.PeerDiffStat;
import com.apollocurrency.aplwallet.apl.tools.impl.heightmon.model.PeerInfo;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

@Path("/netstat")
@Singleton
public class NetStatController {

    @Inject
    private HeightMonitorService heightMonitorService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStats() {
        NetworkStats last = heightMonitorService.getLastStats();
        if (last == null) {
            return Response.serverError()
                .entity("Network statistics is not ready yet").build();
        }
        return Response.ok(last).build();
    }

    @GET
    @Path("/peers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllPeers() {
        return Response.ok(heightMonitorService.getAllPeers()).build();
    }

    @POST
    @Path("/peers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addPeer(@NotNull @QueryParam("ip") String ip, @QueryParam("port") Integer port, @QueryParam("schema") String schema) {
        try {
            PeerInfo peerInfo = new PeerInfo(ip);
            if (schema != null) {
                peerInfo.setSchema(schema);
            }
            if (port != null) {
                peerInfo.setPort(port);
            }
            return Response.ok(heightMonitorService.addPeer(peerInfo)).build();
        } catch (UnknownHostException e) {
            return Response.status(422, e.getLocalizedMessage()).build();
        }
    }

    @GET
    @Path("/{ip}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPeerStats(@NotNull @PathParam("ip") String ip) {
        List<PeerDiffStat> diffStats = heightMonitorService.getLastStats()
            .getPeerDiffStats()
            .stream()
            .filter(peerDiffStat -> peerDiffStat.getPeer1().equalsIgnoreCase(ip)
                || peerDiffStat.getPeer2().equalsIgnoreCase(ip))
            .collect(Collectors.toList());
        if (diffStats.isEmpty()) {
            return Response.status(422, "No monitoring data found for peer " + ip).build();
        } else {
            return Response.ok(diffStats).build();
        }
    }
}
