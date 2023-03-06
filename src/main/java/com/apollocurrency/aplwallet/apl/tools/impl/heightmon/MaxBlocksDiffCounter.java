/*
 *  Copyright © 2018-2019 Apollo Foundation
 */

package com.apollocurrency.aplwallet.apl.tools.impl.heightmon;


import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
public class MaxBlocksDiffCounter {

    private final int period;
    private int value;
    private LocalDateTime createdDateTime;
    private Duration durationOnPeriod;

    public MaxBlocksDiffCounter(int period) {
        this.period = period;
//        this.durationOnPeriod = Duration.ofHours(period);
        this.durationOnPeriod = Duration.ofMinutes(period);
        this.createdDateTime =
            LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.systemDefault())
                .plus(this.durationOnPeriod);
        log.debug("Created for date-time : {}", this.createdDateTime);
    }

    public int update(int index, int currentBlockDiff) {
        int result = -1;
        LocalDateTime currentTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.systemDefault());
        log.debug("period = [{}], createdDateTime = '{}', currentTime = '{}' > ? {}",
            period, createdDateTime, currentTime, (currentTime.isAfter(this.createdDateTime)));
        if (currentTime.isAfter(this.createdDateTime)) {
//            value = Math.max(value, currentBlockDiff);
            result = this.value;
            if (index == 0) {
                this.value = currentBlockDiff;
            } else {
                this.value = Math.max(value, currentBlockDiff);
            }
        }
        log.info("MAX Blocks diff for last {}h is {} blocks {}", period, this.value, result != -1 ? "*" : "");
        return result;
    }

    public int getValue() {
        return value;
    }

    public int getPeriod() {
        return period;
    }

}
