package com.apollocurrency.aplwallet.apl.tools.impl.heightmon.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Composed Fork Status with 'max block diff' value(s) included
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ForkStatus {
    /**
     * Common fork status
     */
    ForkEnum status = ForkEnum.OK;
    /**
     * Maximum fork difference found at latest time
     */
    int maxDiff = -1;
}
