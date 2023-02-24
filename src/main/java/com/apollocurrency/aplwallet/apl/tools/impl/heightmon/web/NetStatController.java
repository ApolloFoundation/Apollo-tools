/*
 *  Copyright © 2018-2019 Apollo Foundation
 */

package com.apollocurrency.aplwallet.apl.tools.impl.heightmon.web;

import com.apollocurrency.aplwallet.apl.tools.impl.heightmon.HeightMonitorService;
import com.apollocurrency.aplwallet.apl.tools.impl.heightmon.model.ForkEnum;
import com.apollocurrency.aplwallet.apl.tools.impl.heightmon.model.ForkStatus;
import com.apollocurrency.aplwallet.apl.tools.impl.heightmon.model.HeightMonitorConfig;
import com.apollocurrency.aplwallet.apl.tools.impl.heightmon.model.NetworkStats;
import com.apollocurrency.aplwallet.apl.tools.impl.heightmon.model.PeerDiffStat;
import com.apollocurrency.aplwallet.apl.tools.impl.heightmon.model.PeerInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.security.PermitAll;
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
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Path("/netstat")
@Singleton
@NoArgsConstructor
public class NetStatController {

    private HeightMonitorService heightMonitorService;


    @Inject
    public NetStatController(HeightMonitorService heightMonitorService) {
        this.heightMonitorService = heightMonitorService;
    }

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
            .toList();
        if (diffStats.isEmpty()) {
            return Response.status(422, "No monitoring data found for peer " + ip).build();
        } else {
            return Response.ok(diffStats).build();
        }
    }

    @GET
    @Path("/fork")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Returns fork status",
        description = "Returns fork status. Possible values are: OK = 0, Warning = 1, Critical = 2",
        responses = {
            @ApiResponse(responseCode = "200", description = "Successful execution",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ForkStatus.class)))
        })
    @PermitAll
    public Response getForkStatus() {
        log.debug("Getting getForkStatus...");
        HeightMonitorConfig config = this.heightMonitorService.getConfig();
        List<Integer> maxBlocksDiffPeriods = config.getMaxBlocksDiffPeriods();
        log.debug("maxBlocksDiffPeriods = [{}] / empty ? = {}", maxBlocksDiffPeriods.size(), maxBlocksDiffPeriods.isEmpty());
        ForkStatus forkStatus = new ForkStatus();
        if (maxBlocksDiffPeriods.isEmpty()) {
            return Response.ok(forkStatus).build();
        }
        int minTimeValue = maxBlocksDiffPeriods.get(0);
        log.debug("minTimeValue = {}", minTimeValue);
        Optional<Map.Entry<Integer, Integer>> maxDiffAtLatestHour = heightMonitorService.getLastStats().getDiffForTime().entrySet().stream().filter(entry -> entry.getKey() == minTimeValue).findFirst();
        if (maxDiffAtLatestHour.isEmpty()) {
            log.debug("maxDiffAtLatestHour is empty !");
            forkStatus = new ForkStatus();
            return Response.ok(forkStatus).build();
        }

        int criticalLevel = config.getPeersConfig().getCriticalLevel();
        int warningLevel = config.getPeersConfig().getWarningLevel();
        Integer maxDiffMapValue = maxDiffAtLatestHour.get().getValue();
        if (maxDiffMapValue < criticalLevel
            && maxDiffMapValue < warningLevel) {
            log.debug("OK level, max diff = {}", maxDiffMapValue);
            forkStatus = new ForkStatus(ForkEnum.OK, maxDiffMapValue);

        } else if (maxDiffMapValue < criticalLevel
            && maxDiffMapValue >= warningLevel) {
            log.debug("WARNING level, max diff = {}", maxDiffMapValue);
            forkStatus = new ForkStatus(ForkEnum.WARNING, maxDiffMapValue);

        } else if (maxDiffMapValue >= criticalLevel) {
            log.debug("CRITICAL level, max diff = {}", maxDiffMapValue);
            forkStatus = new ForkStatus(ForkEnum.CRITICAL, maxDiffMapValue);
        }
        return Response.ok(forkStatus).build();
    }
}
