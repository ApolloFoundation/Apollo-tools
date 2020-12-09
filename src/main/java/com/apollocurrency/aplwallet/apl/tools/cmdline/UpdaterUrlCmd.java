/*
 * Copyright © 2018-2019 Apollo Foundation
 */
package com.apollocurrency.aplwallet.apl.tools.cmdline;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.util.ArrayList;
import java.util.List;

/**
 * @author alukin@gmail.com
 */
@Parameters(commandDescription = "Encrypt/Decrypt Updater URL. Default ot encrypt")
public class UpdaterUrlCmd {
    public static final String CMD = "updaterurl";
    @Parameter(names = {"--hex", "-x"}, description = "Treat input value for encryption as hexadecimal string")
    public boolean useHex = false;
    @Parameter(names = {"--utf", "-u"}, description = "Convert decrypted value to UTF-8 string")
    public boolean toUtf8 = false;
    @Parameter(names = {"--decrypt", "-d"}, description = "Decrypt. Default to encrypt")
    public boolean decrypt = false;
    @Parameter(names = {"--key", "-k"}, description = "Path to private key for encryption or to certificate for decryption", required = true)
    public String keyfile;
    @Parameter(names = {"--in", "-i"}, description = "Input file. Argument string id used if omitted", required = true)
    public String infile;
}
