package com.apollocurrency.aplwallet.apl.tools.impl;

import com.apollocurrency.aplwallet.apl.tools.ApolloTools;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfDirLocator {
   public static Path getBinDir() {
       URI res = Paths.get("").toUri();
       try {
           res = ApolloTools.class.getProtectionDomain().getCodeSource().getLocation().toURI();
       } catch (URISyntaxException var2) {
       }

       Path createdBinPath = Paths.get(res).getParent().toAbsolutePath();
       if (createdBinPath.endsWith("target")) {
           createdBinPath = createdBinPath.getParent();
       }
       return createdBinPath;
   }
}
