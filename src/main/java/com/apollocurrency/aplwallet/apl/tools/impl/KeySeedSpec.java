package com.apollocurrency.aplwallet.apl.tools.impl;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KeySeedSpec {
    private boolean isVault;
    private byte[] keySeed;
}
