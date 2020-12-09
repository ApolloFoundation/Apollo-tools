package com.apollocurrency.aplwallet.apl.tools.impl;

import com.apollocurrency.aplwallet.apl.crypto.Convert;
import com.apollocurrency.aplwallet.apl.crypto.Crypto;
import com.apollocurrency.aplwallet.apl.util.StringUtils;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;

public class KeySeedUtil {
    private KeySeedUtil() {}

    public static KeySeedSpec readKeySeed() {
        String secretPhraseString;
        byte[] secret = null;
        Console console = System.console();
        boolean isVault = false;
        if (console == null) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
                System.out.println("Enter secretPhrase, if you have secretKey press enter:");
                secretPhraseString = in.readLine();
                if (StringUtils.isBlank(secretPhraseString)) {
                    System.out.println("Enter secret key in hexadecimal format: ");
                    String s = in.readLine();
                    secret = Convert.parseHexString(s);
                    isVault = true;
                } else {
                    secret = Convert.toBytes(secretPhraseString);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            secretPhraseString = new String(console.readPassword("Secret phrase, skip if you have secretKey : "));
            if (StringUtils.isBlank(secretPhraseString)) {
                String s = new String(console.readPassword("Enter secretKey in hexadecimal format: "));
                secret = Convert.parseHexString(s);
                isVault = true;
            } else {
                secret = Convert.toBytes(secretPhraseString);
            }
        }
        return new KeySeedSpec(isVault, Crypto.getKeySeed(secret));
    }
}
