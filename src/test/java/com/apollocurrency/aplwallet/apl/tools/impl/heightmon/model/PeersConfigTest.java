package com.apollocurrency.aplwallet.apl.tools.impl.heightmon.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

class PeersConfigTest {
    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @SneakyThrows
    @ParameterizedTest
    @ValueSource(strings = { "peers.json", "peers-1t.json", "peers-2t.json", "peers-3t.json", "peers-15t.json", "peers-tap.json" })
    void readConfig(String peerFile) {
        String root = new File(".").getAbsolutePath();
        Path configFolder = Path.of(root, "/conf");
        PeersConfig peersConfig = objectMapper.readValue(configFolder.resolve(peerFile).toFile(), PeersConfig.class);
        assertNotNull(peersConfig);
        assertNotNull(peersConfig.getPeersInfo());
        assertTrue(peersConfig.getPeersInfo().size() > 0);
    }
}