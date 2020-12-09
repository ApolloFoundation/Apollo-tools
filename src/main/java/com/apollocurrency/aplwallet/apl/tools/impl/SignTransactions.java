/*
 *  Copyright © 2018-2019 Apollo Foundation
 */

package com.apollocurrency.aplwallet.apl.tools.impl;

import com.apollocurrency.aplwallet.api.dto.TransactionDTO;
import com.apollocurrency.aplwallet.apl.crypto.Convert;
import com.apollocurrency.aplwallet.apl.util.JSON;
import com.apollocurrency.aplwallet.apl.util.env.PosixExitCodes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class SignTransactions {
    private final TxBuilder builder;
    public SignTransactions(long genesisCreatorId) {
        this.builder = new TxBuilder(genesisCreatorId);
    }

    public int sign(String unsignedFN, String signedFN) {
        try {
            File unsigned = new File(unsignedFN);
            if (!unsigned.exists()) {
                System.out.println("File not found: " + unsigned.getAbsolutePath());
                return PosixExitCodes.EX_IOERR.exitCode();
            }
            File signed = new File(signedFN);
            if (signed.exists()) {
                System.out.println("File already exists: " + signed.getAbsolutePath());
                return PosixExitCodes.EX_IOERR.exitCode();
            }

            byte[] keySeed = KeySeedUtil.readKeySeed().getKeySeed();
            int n = 0;
            if (Files.exists(signed.toPath())) {
                Files.delete(signed.toPath());
            }
            Files.createFile(signed.toPath());
            List<String> unsignedTransactions = Files.readAllLines(unsigned.toPath());

            for (String unsignedTransaction : unsignedTransactions) {

                Files.write(signed.toPath(), builder.buildAndSign(Convert.parseHexString(unsignedTransaction), keySeed).getCopyTxBytes(), StandardOpenOption.APPEND);
                Files.write(signed.toPath(), System.lineSeparator().getBytes(), StandardOpenOption.APPEND);
                n += 1;
            }
            System.out.println("Signed " + n + " transactions");
        } catch (Exception e) {
            e.printStackTrace();
            return PosixExitCodes.EX_IOERR.exitCode();
        }
        return PosixExitCodes.OK.exitCode();
    }

    public int signJson(String unsignedFN, String signedFN) {
        try {
            File unsigned = new File(unsignedFN);
            if (!unsigned.exists()) {
                System.out.println("File not found: " + unsigned.getAbsolutePath());
                return PosixExitCodes.EX_IOERR.exitCode();
            }
            File signed = new File(signedFN);
            if (signed.exists()) {
                System.out.println("File already exists: " + signed.getAbsolutePath());
                return PosixExitCodes.EX_IOERR.exitCode();
            }
            byte[] keySeed = KeySeedUtil.readKeySeed().getKeySeed();
            try (BufferedReader reader = new BufferedReader(new FileReader(unsigned))) {
                TransactionDTO transactionDTO = JSON.getMapper().readValue(reader, TransactionDTO.class);
                Files.write(signed.toPath(), builder.buildAndSign(transactionDTO, keySeed).getCopyTxBytes(), StandardOpenOption.CREATE);
                System.out.println("Signed transaction JSON saved as: " + signed.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return PosixExitCodes.EX_IOERR.exitCode();
        }
        return PosixExitCodes.OK.exitCode();
    }
}
