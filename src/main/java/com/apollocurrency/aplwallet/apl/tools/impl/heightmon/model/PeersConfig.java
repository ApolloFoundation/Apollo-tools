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
    private final int warningLevel; //number of blocks that considered as a fork
    private final int criticalLevel; // apl.maxRollback Apollo constant
    private final boolean skipNotRespondingHost; // skip not responding host from processing
    private final int jettyServerPort;

    @JsonCreator
    public PeersConfig(@JsonProperty("peersInfo") List<PeerInfo> peersInfo,
                       @JsonProperty("defaultPort") int defaultPort,
                       @JsonProperty("warningLevel") int warningLevel,
                       @JsonProperty("criticalLevel") int criticalLevel,
                       @JsonProperty("skipNotRespondingHost") boolean skipNotRespondingHost,
                       @JsonProperty("jettyServerPort") int jettyServerPort) {
        this.peersInfo = peersInfo;
        this.defaultPort = defaultPort;
        this.warningLevel = warningLevel;
        this.criticalLevel = criticalLevel;
        this.skipNotRespondingHost = skipNotRespondingHost;
        this.jettyServerPort = jettyServerPort;
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

    public boolean isSkipNotRespondingHost() {
        return skipNotRespondingHost;
    }

    public int getJettyServerPort() {
        return jettyServerPort;
    }
}
