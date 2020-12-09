package com.apollocurrency.aplwallet.apl.tools;

import ch.qos.logback.core.PropertyDefinerBase;

import java.nio.file.Paths;

public class ToolsLogDirDefiner extends PropertyDefinerBase {
    @Override
    public String getPropertyValue() {
        return Paths.get(System.getProperty("java.io.tmpdir"), "apl-blockchain-tools-logs").toAbsolutePath().toString();
    }
}
