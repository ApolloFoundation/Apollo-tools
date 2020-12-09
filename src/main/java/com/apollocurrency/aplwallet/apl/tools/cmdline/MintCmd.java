/*
 * Copyright © 2018-2019 Apollo Foundation
 */
package com.apollocurrency.aplwallet.apl.tools.cmdline;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;


@Parameters(commandDescription = "Start minting service to generate new currency units by performing PoW and sending minting transactions with proper PoW nonce to blockchain")
public class MintCmd {
    public static final String CMD = "mint";
    @Parameter(names = {"--config-file", "-c"}, description = "Absolute path to properties config file, where all Minter Service parameters are located")
    public String configFile = "conf/apl-mint.properties";
}
