package com.apollocurrency.aplwallet.apl.tools.impl.heightmon.web;

import com.apollocurrency.aplwallet.api.dto.ShardDTO;
import com.apollocurrency.aplwallet.apl.tools.impl.heightmon.HeightMonitorService;
import com.apollocurrency.aplwallet.apl.tools.impl.heightmon.model.ForkEnum;
import com.apollocurrency.aplwallet.apl.tools.impl.heightmon.model.ForkStatus;
import com.apollocurrency.aplwallet.apl.tools.impl.heightmon.model.HeightMonitorConfig;
import com.apollocurrency.aplwallet.apl.tools.impl.heightmon.model.NetworkStats;
import com.apollocurrency.aplwallet.apl.tools.impl.heightmon.model.PeersConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.SneakyThrows;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.jboss.resteasy.spi.Dispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NetStatControllerTest {

    private static ObjectMapper mapper = new ObjectMapper();
    private Dispatcher dispatcher;
    private NetStatController controller;
    @Mock
    private HeightMonitorService heightMonitorService;
    @Mock
    private HeightMonitorConfig config;

    @BeforeEach
    void setUp() {
        this.dispatcher = MockDispatcherFactory.createDispatcher();
        this.controller = new NetStatController(heightMonitorService);
        dispatcher.getRegistry().addSingletonResource(controller);
    }

    @SneakyThrows
    @Test
    void getForkStatus_maxBlocksDiffPeriods_Empty() {
        when(config.getMaxBlocksDiffPeriods()).thenReturn(List.of());
        when(heightMonitorService.getConfig()).thenReturn(config);

        MockHttpRequest request = MockHttpRequest.get("/netstat/fork").contentType(MediaType.APPLICATION_JSON_TYPE);
        MockHttpResponse response = new MockHttpResponse();

        dispatcher.invoke(request, response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        String forkJson = response.getContentAsString();
        ForkStatus forkStatus = mapper.readValue(forkJson, new TypeReference<>() {});
        assertEquals(ForkEnum.OK, forkStatus.getStatus());
        assertEquals(-1, forkStatus.getMaxDiff());

        verify(heightMonitorService, never()).getLastStats();
    }

    @SneakyThrows
    @Test
    void getForkStatus_stats_Empty() {
        when(config.getMaxBlocksDiffPeriods()).thenReturn(List.of(0, 1, 2));
        when(heightMonitorService.getConfig()).thenReturn(config);
        NetworkStats stats = mock(NetworkStats.class);
        when(stats.getDiffForTime()).thenReturn(Map.of());
        when(heightMonitorService.getLastStats()).thenReturn(stats);

        MockHttpRequest request = MockHttpRequest.get("/netstat/fork").contentType(MediaType.APPLICATION_JSON_TYPE);
        MockHttpResponse response = new MockHttpResponse();

        dispatcher.invoke(request, response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        String forkJson = response.getContentAsString();
        ForkStatus forkStatus = mapper.readValue(forkJson, new TypeReference<>() {});
        assertEquals(ForkEnum.OK, forkStatus.getStatus());
        assertEquals(-1, forkStatus.getMaxDiff());

        verify(heightMonitorService, timeout(1)).getLastStats();
    }

    @SneakyThrows
    @Test
    void getForkStatus_OK_level() {
        when(config.getMaxBlocksDiffPeriods()).thenReturn(List.of(0, 1, 2));
        when(heightMonitorService.getConfig()).thenReturn(config);
        PeersConfig peersConfig = mock(PeersConfig.class);
        when(peersConfig.getWarningLevel()).thenReturn(720);
        when(peersConfig.getCriticalLevel()).thenReturn(2000);
        when(config.getPeersConfig()).thenReturn(peersConfig);
        when(heightMonitorService.getConfig()).thenReturn(config);
        NetworkStats stats = mock(NetworkStats.class);
        int maxDiffValue = 719;
        when(stats.getDiffForTime()).thenReturn(Map.of(0, maxDiffValue));
        when(heightMonitorService.getLastStats()).thenReturn(stats);

        MockHttpRequest request = MockHttpRequest.get("/netstat/fork").contentType(MediaType.APPLICATION_JSON_TYPE);
        MockHttpResponse response = new MockHttpResponse();

        dispatcher.invoke(request, response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        String forkJson = response.getContentAsString();
        ForkStatus forkStatus = mapper.readValue(forkJson, new TypeReference<>() {});
        assertEquals(ForkEnum.OK, forkStatus.getStatus());
        assertEquals(maxDiffValue, forkStatus.getMaxDiff());

        verify(heightMonitorService, timeout(1)).getLastStats();
    }

    @SneakyThrows
    @Test
    void getForkStatus_WARN_level() {
        when(config.getMaxBlocksDiffPeriods()).thenReturn(List.of(0, 1, 2));
        when(heightMonitorService.getConfig()).thenReturn(config);
        PeersConfig peersConfig = mock(PeersConfig.class);
        when(peersConfig.getWarningLevel()).thenReturn(720);
        when(peersConfig.getCriticalLevel()).thenReturn(2000);
        when(config.getPeersConfig()).thenReturn(peersConfig);
        when(heightMonitorService.getConfig()).thenReturn(config);
        NetworkStats stats = mock(NetworkStats.class);
        int maxDiffValue = 720;
        when(stats.getDiffForTime()).thenReturn(Map.of(0, maxDiffValue));
        when(heightMonitorService.getLastStats()).thenReturn(stats);

        MockHttpRequest request = MockHttpRequest.get("/netstat/fork").contentType(MediaType.APPLICATION_JSON_TYPE);
        MockHttpResponse response = new MockHttpResponse();

        dispatcher.invoke(request, response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        String forkJson = response.getContentAsString();
        ForkStatus forkStatus = mapper.readValue(forkJson, new TypeReference<>() {});
        assertEquals(ForkEnum.WARNING, forkStatus.getStatus());
        assertEquals(maxDiffValue, forkStatus.getMaxDiff());

        verify(heightMonitorService, timeout(1)).getLastStats();
    }

    @SneakyThrows
    @Test
    void getForkStatus_CRITICAL_level() {
        when(config.getMaxBlocksDiffPeriods()).thenReturn(List.of(0, 1, 2));
        when(heightMonitorService.getConfig()).thenReturn(config);
        PeersConfig peersConfig = mock(PeersConfig.class);
        when(peersConfig.getWarningLevel()).thenReturn(720);
        when(peersConfig.getCriticalLevel()).thenReturn(2000);
        when(config.getPeersConfig()).thenReturn(peersConfig);
        when(heightMonitorService.getConfig()).thenReturn(config);
        NetworkStats stats = mock(NetworkStats.class);
        int maxDiffValue = 2_000;
        when(stats.getDiffForTime()).thenReturn(Map.of(0, maxDiffValue));
        when(heightMonitorService.getLastStats()).thenReturn(stats);

        MockHttpRequest request = MockHttpRequest.get("/netstat/fork").contentType(MediaType.APPLICATION_JSON_TYPE);
        MockHttpResponse response = new MockHttpResponse();

        dispatcher.invoke(request, response);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        String forkJson = response.getContentAsString();
        ForkStatus forkStatus = mapper.readValue(forkJson, new TypeReference<>() {});
        assertEquals(ForkEnum.CRITICAL, forkStatus.getStatus());
        assertEquals(maxDiffValue, forkStatus.getMaxDiff());

        verify(heightMonitorService, timeout(1)).getLastStats();
    }
}