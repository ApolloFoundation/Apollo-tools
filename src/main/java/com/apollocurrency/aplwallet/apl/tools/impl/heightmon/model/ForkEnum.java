package com.apollocurrency.aplwallet.apl.tools.impl.heightmon.model;

/**
 * Status that should be reported by Nagios plugin to Nagios monitoring system.
 * Possible values :
 *  - OK = 0 - service is appeared to be functioning properly, no forks
 *  - WARNING = 1 - fork is above minimal "warning" threshold value or did not appear to be working properly
 *  - CRITICAL = 2 - fork is above maximum "critical" threshold value and needs very deep manual fixes
 */
public enum ForkEnum {
    OK(0),
    WARNING(1),
    CRITICAL(2);

    private final int value;

    ForkEnum(int value) {
        this.value = value;
    }

}
