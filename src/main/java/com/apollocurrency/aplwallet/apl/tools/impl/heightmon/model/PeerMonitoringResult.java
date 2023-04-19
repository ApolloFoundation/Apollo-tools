/*
 *  Copyright © 2018-2019 Apollo Foundation
 */

package com.apollocurrency.aplwallet.apl.tools.impl.heightmon.model;

import com.apollocurrency.aplwallet.apl.tools.impl.heightmon.Block;
import com.apollocurrency.aplwallet.apl.util.Version;

import java.util.List;
import java.util.Map;

public class PeerMonitoringResult {
    private String peerUrl;
    private List<ShardDTO> shards;
    private int height;
    private Version version;
    private Map<String, Block> peerMutualBlocks;
    private boolean isLiveHost = false;
    private boolean isDownloading = false;

    public PeerMonitoringResult(String peerUrl,
                                List<ShardDTO> shards,
                                int height,  // main host
                                Version version,
                                Map<String, Block> peerMutualBlocks,
                                boolean isLiveHost,
                                int height2,  // second comparing host
                                int criticalHeightValue) {
        this.peerUrl = peerUrl;
        this.shards = shards;
        this.height = height;
        this.version = version;
        this.peerMutualBlocks = peerMutualBlocks;
        this.isLiveHost = isLiveHost;
        if (isLiveHost && this.peerMutualBlocks != null
            && this.peerMutualBlocks.values().stream().findFirst().isPresent()
            && height2 > 0) {
            // we evaluate peer downloading BH if it has difference between 'mutual block height' bigger then 'critical' config value
            this.isDownloading =
                Math.abs(this.peerMutualBlocks.values().stream().findFirst().get().getHeight() - height2) > criticalHeightValue;
        }
    }

    public Map<String, Block> getPeerMutualBlocks() {
        return peerMutualBlocks;
    }

    public void setPeerMutualBlocks(Map<String, Block> peerMutualBlocks) {
        this.peerMutualBlocks = peerMutualBlocks;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public List<ShardDTO> getShards() {
        return shards;
    }

    public void setShards(List<ShardDTO> shards) {
        this.shards = shards;
    }

    public boolean isLiveHost() {
        return isLiveHost;
    }

    public boolean isDownloading() {
        return isDownloading;
    }

    public void setDownloading(boolean downloading) {
        isDownloading = downloading;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PeerMonitoringResult{");
        sb.append("peerUrl='").append(peerUrl).append('\'');
        sb.append(", isLiveHost=").append(isLiveHost);
        sb.append(", this.isDownloading=").append(isDownloading);
        sb.append(", height=").append(height);
        sb.append(", version=").append(version);
        sb.append(", shards=").append(shards);
        sb.append(", peerMutualBlocks=").append(peerMutualBlocks);
        sb.append('}');
        return sb.toString();
    }
}
