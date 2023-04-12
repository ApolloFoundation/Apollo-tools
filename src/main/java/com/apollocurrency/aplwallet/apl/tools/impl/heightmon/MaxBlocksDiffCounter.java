/*
 *  Copyright © 2018-2019 Apollo Foundation
 */

package com.apollocurrency.aplwallet.apl.tools.impl.heightmon;


import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
public class MaxBlocksDiffCounter {

    private final int period;
    private int value;
    private LocalDateTime createdDateTime;
    private Duration durationOnPeriod;

    public MaxBlocksDiffCounter(int period) {
        this.period = period;
        this.durationOnPeriod = Duration.ofMinutes(period);
        this.createdDateTime = LocalDateTime.now()
                .plus(this.durationOnPeriod);
        log.debug("Created for date-time : {}", this.createdDateTime);
    }

    public int update(int index, int currentBlockDiff) {
        int result = -1;
        LocalDateTime currentTime = LocalDateTime.now();
        log.trace("period = [{}], createdDateTime = '{}', currentTime = '{}' > ? {}",
            period, createdDateTime, currentTime, (currentTime.isAfter(this.createdDateTime)));
        if (currentTime.isAfter(this.createdDateTime)) {
            result = this.value;
            if (index == 0) {
                this.value = currentBlockDiff; // always assign new value to zero item
            } else {
                this.value = Math.max(value, currentBlockDiff); // assign max value to the rest of items
            }
        }
        log.info("MAX Blocks diff for last {} hours is '{}' blocks {}", period / 60, result, result != -1 ? "*" : "");
        return result;
    }

    public int getValue() {
        return value;
    }

    public int getPeriod() {
        return period;
    }

}
