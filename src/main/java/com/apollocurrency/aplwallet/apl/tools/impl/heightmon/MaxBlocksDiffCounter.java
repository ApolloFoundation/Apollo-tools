/*
 *  Copyright © 2018-2019 Apollo Foundation
 */

package com.apollocurrency.aplwallet.apl.tools.impl.heightmon;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class MaxBlocksDiffCounter {
    private static final Logger log = LoggerFactory.getLogger(MaxBlocksDiffCounter.class);

    private final int period;
    private int value;
//    private long lastResetTime;
    private LocalDateTime lastUpdateTime;
    private Duration durationOnPeriod;

    public MaxBlocksDiffCounter(int period) {
        this.period = period;
//        this.lastResetTime = System.currentTimeMillis() / (1000 * 60 * 60) * period;
        this.durationOnPeriod = Duration.ofMinutes(period);
//        this.durationOnPeriod = Duration.ofHours(period);
        this.lastUpdateTime =
            LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.systemDefault())
                .plus(this.durationOnPeriod);
        log.debug("Created for date-time : {}", this.lastUpdateTime);
    }

    public boolean update(int currentBlockDiff) {
        boolean result = false;
//        long currentTime = System.currentTimeMillis() / (1000 * 60 * 60);
        LocalDateTime currentTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.systemDefault());
        LocalDateTime timeDifference = this.lastUpdateTime.minus(durationOnPeriod);
        log.debug("currentTime = '{}', lastResetTime = '{}' (time-diff = {}), period = '{}' >= ? {}",
            currentTime, lastUpdateTime, timeDifference, period, (this.lastUpdateTime.isEqual(currentTime) || this.lastUpdateTime.isAfter(currentTime)));
        if (this.lastUpdateTime.isEqual(currentTime) || this.lastUpdateTime.isAfter(currentTime)) {
            lastUpdateTime = currentTime;
            value = Math.max(value, currentBlockDiff);
            result = true;
        }
        log.info("MAX Blocks diff for last {}h is {} blocks {}", period, value, result ? "*" : "");
        return result;
    }

    public int getValue() {
        return value;
    }

    public int getPeriod() {
        return period;
    }

}
