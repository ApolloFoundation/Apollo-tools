package com.apollocurrency.aplwallet.apl.tools.impl.mint;

import com.apollocurrency.aplwallet.apl.tools.impl.ConfDirLocator;
import com.apollocurrency.aplwallet.apl.util.injectable.PropertiesHolder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

@Slf4j
public class MintWorkerRunner {
    private volatile MintWorker mintWorker;
    private final String confPath;

    public MintWorkerRunner(String confPath) {
        this.confPath = confPath;
    }

    public void startMinting() {
        Path confPath = Paths.get(this.confPath);
        Properties properties = new Properties();
        if (!confPath.isAbsolute()) {
            confPath = ConfDirLocator.getBinDir().resolve(confPath);
        }
        try {
            properties.load(Files.newInputStream(confPath));
        } catch (IOException e) {
            log.error("Unable to read mint service config", e);
            return;
        }
        PropertiesHolder holder = new PropertiesHolder();
        holder.init(properties);
        log.info("Mint config loaded from {}", confPath);
        mintWorker = new MintWorker(holder);
        log.info("Starting minting service...");
        mintWorker.run();
    }




    public void stopMinting() {
        mintWorker.stop();
    }
}
