/*
 *  Copyright © 2018-2019 Apollo Foundation
 */

package com.apollocurrency.aplwallet.apl.tools.impl.heightmon;

import com.apollocurrency.aplwallet.apl.tools.impl.heightmon.model.HeightMonitorConfig;
import com.apollocurrency.aplwallet.apl.tools.impl.heightmon.model.NetworkStats;
import com.apollocurrency.aplwallet.apl.tools.impl.heightmon.model.PeerDiffStat;
import com.apollocurrency.aplwallet.apl.tools.impl.heightmon.model.PeerInfo;
import com.apollocurrency.aplwallet.apl.tools.impl.heightmon.model.PeerMonitoringResult;
import com.apollocurrency.aplwallet.apl.tools.impl.heightmon.model.ShardDTO;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.inject.Singleton;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Singleton
@NoArgsConstructor
public class HeightMonitorServiceImpl implements HeightMonitorService {
    private static final List<Integer> DEFAULT_PERIODS = List.of(0, 1, 2, 3, 4, 5, 6, 8, 10, 12);
    private final AtomicReference<NetworkStats> lastStats = new AtomicReference<>(new NetworkStats(DEFAULT_PERIODS.size()));
    private List<MaxBlocksDiffCounter> maxBlocksDiffCounters;
    private HeightMonitorConfig config;
    private FetchHostResultService fetchHostResultService; // service to fetch response from hosts
    private boolean skipNotRespondingHost; // skip from processing and showing 'not live' hosts in final result

    public void init() {
        log.debug("Init HeightMonitorService...");
        this.fetchHostResultService = new FetchHostResultServiceImpl(this.config);
    }

    @Override
    public void setUp(HeightMonitorConfig config) {
        this.config = config;
        this.maxBlocksDiffCounters = createMaxBlocksDiffCounters(config.getMaxBlocksDiffPeriods() == null ? DEFAULT_PERIODS : config.getMaxBlocksDiffPeriods());
        this.config.setMaxBlocksDiffPeriods(config.getMaxBlocksDiffPeriods() == null ? DEFAULT_PERIODS : config.getMaxBlocksDiffPeriods());
        this.skipNotRespondingHost = this.config.getPeersConfig().isSkipNotRespondingHost();
        init();
    }

    private List<MaxBlocksDiffCounter> createMaxBlocksDiffCounters(List<Integer> maxBlocksDiffPeriods) {
        return maxBlocksDiffPeriods.stream().map(MaxBlocksDiffCounter::new).toList();
    }

    @Override
    public NetworkStats getLastStats() {
        return lastStats.get();
    }

    @Override
    public HeightMonitorConfig getConfig() {
        return this.config;
    }

    @Override
    public NetworkStats updateStats() {
        long start = System.currentTimeMillis();
        log.info("=========================================== : started at {}", new Date(start));
        Map<String, PeerMonitoringResult> peerBlocks = this.fetchHostResultService.getPeersMonitoringResults();
        NetworkStats networkStats = new NetworkStats(this.maxBlocksDiffCounters.size());
        List<PeerInfo> allPeers = this.fetchHostResultService.getAllPeers();
        for (PeerInfo peer : allPeers) {
            PeerMonitoringResult result = peerBlocks.get(peer.getHost());
            if (result != null) {
                List<String> shardList = result.getShards().stream().map(this::getShardHashFormatted).toList();
                log.info(String.format("%-16.16s - %8d - %s", peer.getHost(), result.getHeight(), String.join("->", shardList)));
                networkStats.getPeerHeight().put(peer.getHost(), result.getHeight());
                networkStats.getPeerShards().put(peer.getHost(), shardList);
            }
        }
        log.info(String.format("%7.7s %7.7s %-16.16s %-16.16s %9.9s %7.7s %7.7s %8.8s %8.8s %-13.13s %-13.13s %20.20s", "diff1", "diff2", "peer1", "peer2", "milestone", "height1", "height2", "version1", "version2", "shard1", "shard2", "shard-status"));
        int currentMaxBlocksDiff = -1;
        for (int i = 0; i < allPeers.size(); i++) {
            String host1 = allPeers.get(i).getHost();
            PeerMonitoringResult targetMonitoringResult = peerBlocks.get(host1);
            if (this.skipNotRespondingHost && !targetMonitoringResult.isLiveHost()) {
                continue;
            }
            for (int j = i + 1; j < allPeers.size(); j++) {
                String host2 = allPeers.get(j).getHost();
                PeerMonitoringResult comparedMonitoringResult = peerBlocks.get(host2);
                if (this.skipNotRespondingHost && !comparedMonitoringResult.isLiveHost()) {
                    continue;
                }
                Block lastMutualBlock = targetMonitoringResult.getPeerMutualBlocks().get(
                    this.fetchHostResultService.getPeerApiUrls().get(j));
                int lastHeight = targetMonitoringResult.getHeight();
                int blocksDiff1 = getBlockDiff(lastMutualBlock, lastHeight);
                int blocksDiff2 = getBlockDiff(lastMutualBlock, comparedMonitoringResult.getHeight());
                int milestoneHeight = getMilestoneHeight(lastMutualBlock);
                String shardsStatus = getShardsStatus(targetMonitoringResult, comparedMonitoringResult);
                String shard1 = getShardOrNothing(targetMonitoringResult);
                String shard2 = getShardOrNothing(comparedMonitoringResult);
                if (!targetMonitoringResult.isDownloading()) {
                    // do not count downloading peer
                    currentMaxBlocksDiff = Math.max(blocksDiff1, currentMaxBlocksDiff);
                }
                log.info(String.format("%7d %7d %-16.16s %-16.16s %9d %7d %7d %8.8s %8.8s %-13.13s %-13.13s %20.20s",
                    blocksDiff1, blocksDiff2, host1, host2, milestoneHeight, lastHeight,
                    comparedMonitoringResult.getHeight(), targetMonitoringResult.getVersion(),
                    comparedMonitoringResult.getVersion(), shard1, shard2, shardsStatus));
                networkStats.getPeerDiffStats().add(new PeerDiffStat(
                    blocksDiff1, blocksDiff2, host1, host2, milestoneHeight, lastHeight,
                    comparedMonitoringResult.getHeight(), targetMonitoringResult.getVersion(),
                    comparedMonitoringResult.getVersion(), shard1, shard2, shardsStatus));
            }
        }
        log.info("======== Current max diff {} =========", currentMaxBlocksDiff);
        networkStats.setCurrentMaxDiff(currentMaxBlocksDiff);
        for (int i = 0; i < maxBlocksDiffCounters.size(); i++) {
            MaxBlocksDiffCounter maxBlocksDiffCounter = maxBlocksDiffCounters.get(i);
            // replace 'currentMaxBlocksDiff' with value from maxBlocksDiffCounter after update()
            currentMaxBlocksDiff = maxBlocksDiffCounter.update(i, currentMaxBlocksDiff);
            if (currentMaxBlocksDiff >= 0) {
                networkStats.getDiffForTime().put(maxBlocksDiffCounter.getPeriod(), currentMaxBlocksDiff);
                // check reset condition
                if (i == maxBlocksDiffCounters.size() - 1 && networkStats.getDiffForTime().get(maxBlocksDiffCounter.getPeriod()) > 0) {
                    // reset MAP
                    networkStats.getDiffForTime().clear();
                    // recreate 'maxBlocksDiffCounters'
                    this.maxBlocksDiffCounters = createMaxBlocksDiffCounters(config.getMaxBlocksDiffPeriods());
                }
            } else {
                networkStats.getDiffForTime().putIfAbsent(maxBlocksDiffCounter.getPeriod(), currentMaxBlocksDiff);
            }
        }
        lastStats.set(networkStats);
        log.info("=========================================== : finished in {} sec", (System.currentTimeMillis() - start) / 1_000);
        log.trace("DUMP = {}", networkStats.getDiffForTime());
        return networkStats;
    }

    private String getShardOrNothing(PeerMonitoringResult targetMonitoringResult) {
        List<ShardDTO> shards = targetMonitoringResult.getShards();
        if (shards.isEmpty()) {
            return "---";
        } else {
            return getShardHashFormatted(shards.get(0));
        }
    }

    private String getShardHashFormatted(ShardDTO shardDTO) {
        String prunableZipHash = shardDTO.getPrunableZipHash();
        String coreZipHash = shardDTO.getCoreZipHash();
        if (coreZipHash != null) {
            return coreZipHash.substring(0, 6) + (prunableZipHash != null ? ":" + prunableZipHash.substring(0, 6) : "");
        } else {
            return "??????";
        }
    }

    private String getShardsStatus(PeerMonitoringResult targetMonitoringResult, PeerMonitoringResult comparedMonitoringResult) {
        if (!targetMonitoringResult.isLiveHost()) {
            return "peer1=OFF-LINE?";
        }
        List<ShardDTO> targetShards = targetMonitoringResult.getShards();
        List<ShardDTO> comparedShards = comparedMonitoringResult.getShards();
        StringBuilder status = new StringBuilder("OK");
        int comparedCounter = comparedShards.size() - 1;
        int targetCounter = targetShards.size() - 1;
        while (targetCounter >= 0 && comparedCounter >= 0 && status.toString().equals("OK")) {
            ShardDTO comparedShard = comparedShards.get(comparedCounter);
            ShardDTO targetShard = targetShards.get(targetCounter);
            if (comparedShard.getShardId() > targetShard.getShardId()) {
                targetCounter--;
                continue;
            }
            if (comparedShard.getShardId() < targetShard.getShardId()) {
                comparedCounter--;
                continue;
            }
            if (targetShard.getShardHeight() != comparedShard.getShardHeight()) {
                status = new StringBuilder("HEIGHT DIFF FROM " + targetShards.get(targetCounter).getShardId());
            } else if (!targetShard.getCoreZipHash().equalsIgnoreCase(comparedShard.getCoreZipHash())) {
                status = new StringBuilder("CORE DIFF FROM " + targetShards.get(targetCounter).getShardId());
            } else if (!Objects.equals(targetShard.getPrunableZipHash(), comparedShard.getPrunableZipHash())) {
                status = new StringBuilder("PRUN DIFF FROM " + targetShards.get(targetCounter).getShardId());
            }
            targetCounter--;
            comparedCounter--;
        }
        log.trace("Trg = {}", targetMonitoringResult);
        if (targetMonitoringResult.isDownloading()) {
            status.append("/DOWNLOADING");
        }
        return status.toString();
    }

    private int getMilestoneHeight(Block lastMutualBlock) {
        if (lastMutualBlock != null) {
            return lastMutualBlock.getHeight();
        } else {
            return -1;
        }
    }

    private int getBlockDiff(Block lastMutualBlock, int lastHeight) {
        int blocksDiff = -1;
        if (lastMutualBlock != null) {
            int mutualBlockHeight = lastMutualBlock.getHeight();
            blocksDiff = Math.abs(lastHeight - mutualBlockHeight);
        }
        return blocksDiff;
    }

}

