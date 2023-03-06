package com.apollocurrency.aplwallet.apl.tools.impl.heightmon;

import com.apollocurrency.aplwallet.apl.tools.impl.heightmon.model.HeightMonitorConfig;
import com.apollocurrency.aplwallet.apl.tools.impl.heightmon.model.PeerInfo;
import com.apollocurrency.aplwallet.apl.tools.impl.heightmon.model.PeerMonitoringResult;
import com.apollocurrency.aplwallet.apl.tools.impl.heightmon.model.PeersConfig;
import com.apollocurrency.aplwallet.apl.tools.impl.heightmon.model.ShardDTO;
import com.apollocurrency.aplwallet.apl.util.Version;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.dynamic.HttpClientTransportDynamic;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.io.ClientConnector;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

@Slf4j
@NoArgsConstructor
public class FetchHostResultServiceImpl implements FetchHostResultService {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final int CONNECT_TIMEOUT = 5_000;
    private static final int IDLE_TIMEOUT = 5_000;
    private static final int BLOCKS_TO_RETRIEVE = 1000;
    private static final Version DEFAULT_VERSION = new Version("0.0.0");
    private static final String URL_FORMAT = "%s://%s:%d";
    private HttpClient client;
    private List<PeerInfo> peers;
    private int port;
    private List<String> peerApiUrls;
    private ExecutorService executor;
    private HeightMonitorConfig config;

    static {
        objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public FetchHostResultServiceImpl(HeightMonitorConfig config) {
        log.debug("Init FetchHostResultService...");
        this.config = config;
        PeersConfig peersConfig = this.config.getPeersConfig();
        this.port = peersConfig.getDefaultPort();
        this.peers = Collections.synchronizedList(peersConfig.getPeersInfo().stream().peek(this::setDefaultPortIfNull).toList());
        this.peerApiUrls = Collections.synchronizedList(this.peers.stream().map(this::createUrl).toList());

        client = createHttpClient();
        try {
            client.start();
            int numberOfThreads = this.peers.size() * 3; // http client threads in pool
            executor = Executors.newFixedThreadPool(numberOfThreads);
            log.debug("HTTP client started with pool of '{}' threads...", numberOfThreads);
        } catch (Exception e) {
            log.error("Http Client init error", e);
            throw new RuntimeException(e.toString(), e);
        }
    }

    private HttpClient createHttpClient() {
        SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
        sslContextFactory.setTrustAll(true);

        ClientConnector clientConnector = new ClientConnector();
        clientConnector.setSslContextFactory(sslContextFactory);

        HttpClient httpClient = new HttpClient(new HttpClientTransportDynamic(clientConnector));
        httpClient.setIdleTimeout(IDLE_TIMEOUT);
        httpClient.setConnectTimeout(CONNECT_TIMEOUT);
        httpClient.setFollowRedirects(false);
        return httpClient;
    }

    @PreDestroy
    public void shutdown() {
        try {
            client.stop();
            executor.shutdownNow();
        } catch (Exception e) {
            log.error("FetchHostResultService shutdown error...", e);
//            throw new RuntimeException(e.toString(), e);
        }
    }

    @Override
    public Map<String, PeerMonitoringResult> getPeersMonitoringResults() {
        Map<String, PeerMonitoringResult> peerBlocks = new HashMap<>(peerApiUrls.size());
        List<CompletableFuture<PeerMonitoringResult>> getBlocksRequests = new ArrayList<>(peerApiUrls.size());
        for (int i = 0; i < peerApiUrls.size(); i++) {
            String peerUrl = peerApiUrls.get(i);
            int finalI = i;
            getBlocksRequests.add(CompletableFuture.supplyAsync(() -> {
                Map<String, Block> blocks = new HashMap<>();
                int height1 = getPeerHeight(peerUrl);
                log.trace("processing peerUrl = '{}'", peerUrl);
                int height2 = 0;
                for (int j = finalI + 1; j < peerApiUrls.size(); j++) {
                    height2 = getPeerHeight(peerApiUrls.get(j));
                    Block lastMutualBlock = findLastMutualBlock(height1, height2, peerUrl, peerApiUrls.get(j));
                    if (lastMutualBlock != null) {
                        blocks.put(peerApiUrls.get(j), lastMutualBlock);
                    }
                }
                Version version = getPeerVersion(peerUrl);
                List<ShardDTO> shards = getShards(peerUrl);
                log.debug("DONE peerUrl = '{}' is live='{}'", peerUrl, height1 != -1);
                return new PeerMonitoringResult(peerUrl, shards, height1, version, blocks,
                    height1 != -1, height2, this.config.getPeersConfig().getCriticalLevel());
            }, executor));
        }
        for (int i = 0; i < getBlocksRequests.size(); i++) {
            String host = peers.get(i).getHost();
            try {
                peerBlocks.put(host, getBlocksRequests.get(i).get());
            } catch (Exception e) {
                log.error("Error getting blocks for " + host, e);
            }
        }
        return peerBlocks;
    }

    public List<String> getPeerApiUrls() {
        return peerApiUrls;
    }

    private List<ShardDTO> getShards(String peerUrl) {
        List<ShardDTO> shards = new ArrayList<>();
        String uriToCall = peerUrl + "/rest/shards";
        log.trace("Call to Shards = {}", uriToCall);
        Request request = client.newRequest(uriToCall)
            .method(HttpMethod.GET);
        ContentResponse response;
        try {
            response = request.send();
            shards = objectMapper.readValue(response.getContentAsString(), new TypeReference<List<ShardDTO>>() {
            });
        } catch (InterruptedException | TimeoutException | ExecutionException | IOException e) {
            log.error("Unable to get Shards or parse response from {} - {}", uriToCall, e.toString());
        } catch (Exception e) {
            log.error("Unknown exception:", e);
        }
        log.trace("getShards result = {} by uri='{}'", shards, uriToCall);
        return shards;
    }

    private int getPeerHeight(String peerUrl) {
        String uriToCall = peerUrl + "/apl";
        log.trace("Call to Remote = {}", uriToCall);
        JsonNode jsonNode = performRequest(uriToCall, Map.of("requestType", "getBlock"));
        int height = -1;
        if (jsonNode != null) {
            height = jsonNode.get("height").asInt();
        }
        log.trace("getBlock peerHeight result = {} by uri='{}'", height, uriToCall);
        return height;
    }

    private long getPeerBlockId(String peerUrl, int height) {
        String uriToCall = peerUrl + "/apl";
        JsonNode jsonNode = performRequest(uriToCall, Map.of("requestType", "getBlockId", "height", height));
        long blockId = -1;
        if (jsonNode != null) {
            blockId = Long.parseUnsignedLong(jsonNode.get("block").asText());
        }
        log.trace("peerBlockId result = {}", blockId);
        return blockId;
    }

    private Block getPeerBlock(String peerUrl, int height) {
        String uriToCall = peerUrl + "/apl";
        JsonNode node = performRequest(uriToCall, Map.of("requestType", "getBlock", "height", height));
        Block block = null;
        if (node != null) {
            try {
                block = objectMapper.readValue(node.toString(), Block.class);
            } catch (JsonProcessingException e) {
                log.error("Unable to parse block from {} by peerUrl = '{}' at height = {}", node, peerUrl, height);
            }
        }
        log.trace("peerBlock result = {}", block);
        return block;
    }

    private JsonNode performRequest(String url, Map<String, Object> params) {
        JsonNode result = null;
        log.trace("Call to Remote request = {} + {}", url, params);
        Request request = client.newRequest(url)
            .method(HttpMethod.GET);
        params.forEach((name, value) -> request.param(name, value.toString()));

        ContentResponse response;
        try {
            response = request.send();
            if (response.getStatus() != 200) {
                log.info("Bad response: from {}", request.getURI());
            } else {
                JsonNode jsonNode = objectMapper.readTree(response.getContentAsString());
                if (jsonNode.has("errorDescription")) {
                    log.info("Error received from node: {} - {}", request.getURI(), jsonNode.get("errorDescription"));
                } else {
                    result = jsonNode;
                    log.trace("Call result = {}", result);
                }
            }
        } catch (InterruptedException | TimeoutException | ExecutionException | IOException e) {
            log.info("Unable to get or parse response from {} {} - {}", url, params, e.toString());
        } catch (Exception e) {
            log.info("Unknown exception:", e);
        }
        return result;
    }

    private Version getPeerVersion(String peerUrl) {
        Version res = DEFAULT_VERSION;
        String uriToCall = peerUrl + "/apl";
        log.trace("Call to peerVersion = {}", uriToCall);
        Request request = client.newRequest(uriToCall)
            .method(HttpMethod.GET)
            .param("requestType", "getBlockchainStatus");
        ContentResponse response = null;
        try {
            response = request.send();
            JsonNode jsonNode = objectMapper.readTree(response.getContentAsString());
            res = new Version(jsonNode.get("version").asText());
            log.trace("Call result = {}", res);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            log.error("Unable to get peerVersion response from {} - {}", uriToCall, e.toString());
        } catch (IOException e) {
            log.error("Unable to parse peerVersion from json for {} - {}", uriToCall, e.toString());
        }
        return res;
    }

    private Block findLastMutualBlock(int height1, int height2, String host1, String host2) {
        log.trace("find Mutual between '{}' ({}) vs '{}' ({}) ...", host1, height1, host2, height2);
        if (height2 == -1 || height1 == -1) {
            return null;
        }
        int minHeight = Math.min(height1, height2);
        int stHeight = minHeight;
        int step = 1;
        int firstMatchHeight = -1;
        int mutualSearchDeepCount = BLOCKS_TO_RETRIEVE;
        while (true) {
            long peer1BlockId = getPeerBlockId(host1, stHeight);
            long peer2BlockId = getPeerBlockId(host2, stHeight);
            if ((peer1BlockId == peer2BlockId) && (peer2BlockId > 0)) {
                firstMatchHeight = stHeight;
                log.debug("found Mutual between '{}' ({}) vs '{}' ({}), match = {}", host1, height1, host2, height2, stHeight);
                break;
            } else {
                if (stHeight <= 0 || mutualSearchDeepCount <= 0) {
                    log.debug("NOT found Mutual between '{}' ({}) vs '{}' ({}), match = {}, mutualSearchDeepCount = {}",
                        host1, height1, host2, height2, stHeight, mutualSearchDeepCount);
                    return null; // errors getting common block, 0 is reached (no network or similar)
                }
                stHeight = Math.max(0, stHeight - step);
                step *= 2;
            }
            mutualSearchDeepCount--;
            log.trace("nextLoop '{}' ({} / id = {}) vs '{}' ({} / id = {}), stHeight = {}, step = {}, mutualSearchDeepCount = {}",
                host1, height1, peer1BlockId, host2, height2, peer2BlockId, stHeight, step, mutualSearchDeepCount);
        }
        Block block = null;
        if (firstMatchHeight != -1) {
            int tHeight = firstMatchHeight;
            while (tHeight <= minHeight) {
                long peer1BlockId = getPeerBlockId(host1, tHeight);
                long peer2BlockId = getPeerBlockId(host2, tHeight);
                if (peer2BlockId > 0 && peer1BlockId == peer2BlockId) {
                    block = getPeerBlock(host1, tHeight);
                    if (step == 1) {
                        break;
                    }
                    step = Math.max(step / 2, 1);
                    tHeight += step;
                } else {
                    tHeight -= step;
                }
            }
        }

        return block;
    }

    @Override
    public boolean addPeer(PeerInfo peer) {
        Objects.requireNonNull(peer);
        setDefaultPortIfNull(peer);
        String url = createUrl(peer);
        boolean result = false;
        if (!peers.contains(peer)) {
            result = true;
            peers.add(peer);
            peerApiUrls.add(url);
            log.info("Added new peer: {}", peer.getHost());
        }
        return result;
    }

    @Override
    public List<PeerInfo> getAllPeers() {
        return peers;
    }

    private void setDefaultPortIfNull(PeerInfo peerInfo) {
        if (peerInfo.getPort() == null) {
            peerInfo.setPort(port);
        }
    }

    private String createUrl(PeerInfo peerInfo) {
        return String.format(URL_FORMAT, peerInfo.getSchema(), peerInfo.getHost(), peerInfo.getPort());
    }

}
