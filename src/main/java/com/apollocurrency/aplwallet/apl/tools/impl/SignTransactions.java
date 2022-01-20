/*
 *  Copyright © 2018-2022 Apollo Foundation
 */

package com.apollocurrency.aplwallet.apl.tools.impl;

import com.apollocurrency.aplwallet.api.dto.TransactionDTO;
import com.apollocurrency.aplwallet.apl.core.model.Transaction;
import com.apollocurrency.aplwallet.apl.crypto.Convert;
import com.apollocurrency.aplwallet.apl.util.JSON;
import com.apollocurrency.aplwallet.apl.util.env.PosixExitCodes;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class SignTransactions {
    private final TxBuilder builder;
    public SignTransactions(long genesisCreatorId) {
        this.builder = new TxBuilder(genesisCreatorId);
    }

    public int signBytes(String unsignedFN, String signedFN) {
        return sign(unsignedFN, signedFN, this::readHexadecimalTransactions);
    }

    public int signJson(String unsignedFN, String signedFN) {
        return sign(unsignedFN, signedFN, this::readJsonTransactions);
    }
    public int sign(String unsignedFN, String signedFN, Function<Path, List<Transaction>> txReader) {
        try {
            Path unsignedFilePath = Paths.get(unsignedFN);
            if (!Files.exists(unsignedFilePath)) {
                log.error("File not found: {}", unsignedFilePath.toAbsolutePath());
                return PosixExitCodes.EX_IOERR.exitCode();
            }
            Path signedFilePath = Paths.get(signedFN);
            byte[] keySeed = KeySeedUtil.readKeySeed().getKeySeed();

            List<String> signedTransactions = txReader.apply(unsignedFilePath).stream()
                .map(e -> builder.buildAndSign(builder.toUnsignedBytes(e).array(), keySeed))
                .peek(e-> log.debug("Signed tx {} as {}", Convert.toHexString(builder.toUnsignedBytes(e).array()),
                    Convert.toHexString(builder.toBytes(e).array())))
                .map(e->  Convert.toHexString(builder.toBytes(e).array()))
                .collect(Collectors.toList());
            Files.createDirectories(signedFilePath.getParent());
            Files.write(signedFilePath, signedTransactions);
            log.info("Written {} signed txs to {}", signedTransactions.size(), signedFilePath);
            return PosixExitCodes.OK.exitCode();
        } catch (Exception e) {
            log.error("Error during signing transactions", e);
            return PosixExitCodes.EX_IOERR.exitCode();
        }
    }


    List<Transaction> readJsonTransactions(Path filePath) {
        List<TransactionDTO> txs;
        try {
            txs = JSON.getMapper().readValue(filePath.toFile(), new TypeReference<>() {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return txs.stream().map(builder::dtoToTx).collect(Collectors.toList());
    }

    List<Transaction> readHexadecimalTransactions(Path filePath) {
        List<String> txs;
        try {
            txs = Files.readAllLines(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return txs.stream().map(Convert::parseHexString).map(builder::build).collect(Collectors.toList());
    }
}
