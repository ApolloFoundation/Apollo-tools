/*
 *  Copyright © 2018-2019 Apollo Foundation
 */

package com.apollocurrency.aplwallet.apl.tools.impl.heightmon.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PeersConfig {
    private static final int DEFAULT_PORT = 7876;
    private List<PeerInfo> peersInfo;
    private int defaultPort;
    private int warningLevel = 720; //number of blocks that forms fork
    private int criticalLevel = 21000; // apl.maxRollback Apollo constant

    @JsonCreator
    public PeersConfig(@JsonProperty("peersInfo") List<PeerInfo> peersInfo,
                       @JsonProperty("defaultPort") int defaultPort,
                       @JsonProperty("warningLevel") int warningLevel,
                       @JsonProperty("criticalLevel") int criticalLevel) {
        this.peersInfo = peersInfo;
        this.defaultPort = defaultPort;
        this.warningLevel = warningLevel;
        this.criticalLevel = criticalLevel;
    }

    public PeersConfig(@JsonProperty("peersInfo") List<PeerInfo> peersInfo,
                       @JsonProperty("warningLevel") int warningLevel,
                       @JsonProperty("criticalLevel") int criticalLevel
                       ) {
        this(peersInfo, DEFAULT_PORT, warningLevel, criticalLevel);
    }

    public List<PeerInfo> getPeersInfo() {
        return peersInfo;
    }

    public void setPeersInfo(List<PeerInfo> peersInfo) {
        this.peersInfo = peersInfo;
    }

    public int getDefaultPort() {
        return defaultPort;
    }

    public void setDefaultPort(int defaultPort) {
        this.defaultPort = defaultPort;
    }
}
