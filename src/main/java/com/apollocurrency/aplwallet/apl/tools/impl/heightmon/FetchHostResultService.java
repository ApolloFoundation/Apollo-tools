package com.apollocurrency.aplwallet.apl.tools.impl.heightmon;

import com.apollocurrency.aplwallet.apl.tools.impl.heightmon.model.PeerInfo;
import com.apollocurrency.aplwallet.apl.tools.impl.heightmon.model.PeerMonitoringResult;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

/**
 * Service to loop over host's list and gather Node's information by using HttpClient
 */
public interface FetchHostResultService {
    Map<String, PeerMonitoringResult> getPeersMonitoringResults();
    List<String> getPeerApiUrls();
    boolean addPeer(PeerInfo peerInfo) throws UnknownHostException;
    List<PeerInfo> getAllPeers();
}
