# Apollo-tools
**!Attention! Repository is under construction!**

Apollo is being developed by the Apollo Foundation and supporting members of the community.
This repository contains different tools to work with Apollo blockchain.

The several utility tools are available inside **com.apollocurrency.aplwallet.apl.tools.ApolloTools**  

1. The 'height monitor'
The most frequently used tool, see in
**com.apollocurrency.aplwallet.apl.tools.impl.heightmon.HeightMonitor**
The configuration files used are in conf folder, see: peers.json, peers-1t.json, peers-2t.json, peers-3t.json 

2. RSA encryptor/decryptor (for update package url encryption, used by updater v1 release procedure)

3. Other less frequently used tools (Offline transaction signer, db compact tool, minting service, etc), see code.

## Necessary software and steps
1. Code is tested with **JDK 11** and targeted to that platform now. You should have it correctly installed (OpenJDK as example).
2. First you should Build and Install (into local maven repo) the main 'Apollo' repository because Tools has dependency on 'apl-conf' build by 'Apollo'
3. Then 