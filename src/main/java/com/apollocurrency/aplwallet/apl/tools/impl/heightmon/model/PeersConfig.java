/*
 *  Copyright © 2018-2019 Apollo Foundation
 */

package com.apollocurrency.aplwallet.apl.tools.impl.heightmon.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PeersConfig {
    private final List<PeerInfo> peersInfo;
    private final int defaultPort;
    private final int warningLevel; //number of blocks that forms fork
    private final int criticalLevel; // apl.maxRollback Apollo constant

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

    public List<PeerInfo> getPeersInfo() {
        return peersInfo;
    }

    public int getDefaultPort() {
        return defaultPort;
    }

    public int getWarningLevel() {
        return warningLevel;
    }

    public int getCriticalLevel() {
        return criticalLevel;
    }
}
