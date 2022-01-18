/*
 *  Copyright © 2018-2019 Apollo Foundation
 */

package com.apollocurrency.aplwallet.apl.tools.impl;

import com.apollocurrency.aplwallet.api.dto.TransactionDTO;
import com.apollocurrency.aplwallet.apl.core.app.AplException;
import com.apollocurrency.aplwallet.apl.core.entity.blockchain.Transaction;
import com.apollocurrency.aplwallet.apl.crypto.Convert;
import com.apollocurrency.aplwallet.apl.util.FileUtils;
import com.apollocurrency.aplwallet.apl.util.JSON;
import com.apollocurrency.aplwallet.apl.util.env.PosixExitCodes;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
                .map(e -> builder.sign(e, keySeed))
                .peek(e-> log.debug("Signed tx {} as {}", Convert.toHexString(e.getUnsignedBytes()), Convert.toHexString(e.getCopyTxBytes())))
                .map(e-> Convert.toHexString(e.getCopyTxBytes()))
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
        return txs.stream().map(Convert::parseHexString).map(e -> {
            try {
                return builder.build(e);
            } catch (AplException.NotValidException notValidException) {
                throw new RuntimeException(notValidException);
            }
        }).collect(Collectors.toList());
    }
}
