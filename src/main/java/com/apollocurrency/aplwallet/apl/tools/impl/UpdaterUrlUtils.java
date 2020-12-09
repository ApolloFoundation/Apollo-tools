/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.apollocurrency.aplwallet.apl.tools.impl;

import com.apollocurrency.aplwallet.apl.crypto.Convert;
import com.apollocurrency.aplwallet.apl.tools.ApolloTools;
import com.apollocurrency.aplwallet.apl.updater.UpdaterUtil;
import com.apollocurrency.aplwallet.apl.updater.decryption.RSAUtil;
import com.apollocurrency.aplwallet.apl.util.DoubleByteArrayTuple;
import com.apollocurrency.aplwallet.apl.util.env.PosixExitCodes;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPrivateKey;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Utilites for updater URL encryption/decryption
 *
 * @author al
 */
@Slf4j
public class UpdaterUrlUtils {

    /**
     * RSA Encrypt message by using open private key. Message can be in hexadecimal or utf8 form.
     * Support encryption of already encrypted by this method message (isHexadecimal should be true)
     * Output encryption results, exceptions and so on to standard out (console)
     * @param pkPathString path to private key file (unencrypted)
     * @param  messageString message to encrypt
     * @param isHexadecimal whether message is in hexadecimal format or not
     * @return exit code 0 when encryption is successful, otherwise code is not 0
     */
    public static int encrypt(String pkPathString, String messageString, boolean isHexadecimal) {
        boolean isSecondEncryption = false;

        Objects.requireNonNull(pkPathString);
        Objects.requireNonNull(messageString);

        log.info("Got private key path {}", pkPathString);
        log.info("Got message: '{}'", messageString);
        log.info("Got isHexadecimal: {}", isHexadecimal);

        byte[] messageBytes;
        if (isHexadecimal) {
            messageBytes = Convert.parseHexString(messageString);
        } else {
            messageBytes = messageString.getBytes();
        }
        RSAPrivateKey privateKey;
        try {
            privateKey = (RSAPrivateKey) RSAUtil.getPrivateKey(pkPathString);
        } catch (IOException | GeneralSecurityException | URISyntaxException ex) {
            log.error("Error reading private key", ex);
            return PosixExitCodes.EX_IOERR.exitCode();
        }
        if (messageBytes.length > RSAUtil.keyLength(privateKey)) {
            log.error("Cannot encrypt message with size: {}", messageBytes.length);
            return PosixExitCodes.EX_DATAERR.exitCode();
        }
        if (messageBytes.length == RSAUtil.keyLength(privateKey)) {
            isSecondEncryption = true;
            log.debug("Second encryption will be performed");
        } else if (messageBytes.length > RSAUtil.maxEncryptionLength(privateKey)) {
            log.error("Message size is greater than {} bytes. Cannot encrypt.", RSAUtil.maxEncryptionLength(privateKey));
            return PosixExitCodes.EX_DATAERR.exitCode();
        }
        String result;
        if (isSecondEncryption) {
            DoubleByteArrayTuple splittedEncryptedBytes;
            try {
                splittedEncryptedBytes = RSAUtil.secondEncrypt(privateKey, messageBytes);
            } catch (GeneralSecurityException ex) {
                log.error("Security exception.", ex);
                return PosixExitCodes.EX_PROTOCOL.exitCode();
            }
            result = splittedEncryptedBytes.toString();
        } else {
            byte[] encryptedBytes;
            try {
                encryptedBytes = RSAUtil.encrypt(privateKey, messageBytes);
            } catch (GeneralSecurityException ex) {
                log.error("Security exception.", ex);
                return PosixExitCodes.EX_PROTOCOL.exitCode();
            }
            result = Convert.toHexString(encryptedBytes);
        }

        log.info("Your encrypted message in hexadecimal format: {}", result);
        return PosixExitCodes.OK.exitCode();
    }

    /**
     * Decrypt encrypted message using RSA algorithm and X509 certificate's public key.
     * Result message can be converted to UTF-8 string (isConvertToString=true).
     * Can decrypt double encrypted messages (message should consist of two parts separated by ',', ';' or space, tab, etc)
     * For double decryption two certificates required (should be specified in certificatePathString separated by ',', ';', etc)
     * Output results to standard out (console) with errors if any.
     * @param certificatePathString absolute path to X509 certificate, which key is used to decrypt message
     * @param encryptedMessageString hexadecimal encrypted message string, may contain two encrypted parts separated by ',', ';', etc
     * @param isConvertToString whether covert result decrypted message to utf-8 string or leave it in hexadecimal format
     * @return posix code 0, when decryption is successful
     */
    public static int decrypt(String certificatePathString, String encryptedMessageString, boolean isConvertToString) {

        Objects.requireNonNull(certificatePathString);
        Objects.requireNonNull(encryptedMessageString);

        log.info("Got public key path {}", certificatePathString);
        log.info("Got encrypted message: '{}'", encryptedMessageString);
        log.info("Convert to string: {}", isConvertToString);
        String[] split = encryptedMessageString.split("([;,]+)|(\\s+)");
        boolean isSplittedMessage = split.length == 2;
        if (split.length > 2 || split.length == 0) {
            log.error("Invalid message string");
            return PosixExitCodes.EX_DATAERR.exitCode();
        }
        String[] certPathSplit = certificatePathString.split("([;,]+)|(\\s+)");
        if (certPathSplit.length > 2 || certPathSplit.length == 0) {
            log.error("Invalid certificate string");
            return PosixExitCodes.EX_DATAERR.exitCode();
        }

        boolean isDoubleDecryptionRequired = certPathSplit.length == 2;

        byte[] result;
        PublicKey publicKey1;
        try {
            publicKey1 = readCertificate(certPathSplit[0]).getPublicKey();
        } catch (CertificateException | IOException ex) {
            log.error("Security exception.", ex);
            return PosixExitCodes.EX_PROTOCOL.exitCode();
        }

        PublicKey publicKey2;
        try {
            if (isSplittedMessage) {
                byte[] firstMessagePart = Convert.parseHexString(split[0]);
                byte[] secondMessagePart = Convert.parseHexString(split[1]);
                DoubleByteArrayTuple encryptedBytes = new DoubleByteArrayTuple(firstMessagePart, secondMessagePart);
                if (isDoubleDecryptionRequired) {
                    publicKey2 = readCertificate(certPathSplit[1]).getPublicKey();
                    result = RSAUtil.doubleDecrypt(publicKey1, publicKey2, encryptedBytes);
                } else {
                    result = RSAUtil.firstDecrypt(publicKey1, encryptedBytes);
                }
            } else {
                result = RSAUtil.decrypt(publicKey1, Convert.parseHexString(split[0]));
            }
        } catch (IOException | GeneralSecurityException ex) {
            log.error("Error while decryption", ex);
            return PosixExitCodes.EX_DATAERR.exitCode();
        }

        log.info("Your decrypted message in hexadecimal format:{}", Convert.toHexString(result));

        if (isConvertToString) {
            log.info("Result message is: {}", new String(result));
        }
        return PosixExitCodes.OK.exitCode();
    }

    private static Certificate readCertificate(String path) throws CertificateException, IOException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        Path certPath = Paths.get(path);
        return cf.generateCertificate(Files.newInputStream(certPath));
    }

}
