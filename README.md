# Apollo-tools

This repository contains code for ___apollo-tools___ component.
There are other components that are parts of Apollo:

1. [Apollo](https://github.com/ApolloFoundation/Apollo): Apollo blockchain node
2. [Apollo-web-ui](https://github.com/ApolloFoundation/Apollo-web-ui): Web wallet UI that is served by Apollo blockchain node and can be accersed by browser at http://localhost:7876
3. [Apollo-dektop](https://github.com/ApolloFoundation/Apollo-desktop) Desktop wallet UI. Apollo-web-ui must be installed tobe able to run Apollo desktop wallet.
4. [Apollo-bom-ext](https://github.com/ApolloFoundation/Apollo-bom-ext) This module required in compilation time oly. It contains BOM of all external libraries used by Apollo components. **

Apollo is being developed by the Apollo Foundation and supporting members of the community.
This repository contains different tools to work with Apollo blockchain.

The command line utility can

 1. __Height monitor__, used to detect forks on apollo blockchain networks and monitor overall node state.
 The configuration files used are in conf folder, see: peers.json, peers-1t.json, peers-2t.json, peers-3t.json 
 
 2. __RSA encryptor/decryptor__ (for update package url encryption, used by updater v1 release procedure)
 
 3. __Offline transaction signer__ - special utility, which performs signing of transactions locally without apollo node connection
 
 4. __Minter__ - generate new MINTABLE currency units by solving cryptographic puzzle (PoW)
 

The 'height monitor' is the most frequently used tool, see in
The configuration files used are in conf folder, see: peers.json, peers-1t.json, peers-2t.json, peers-3t.json 

 ## Getting started with Apollo tools
 1. Code is tested with **JDK 11** and targeted to that platform now. 
 You should have it correctly installed (OpenJDK as example). Check your java version before proceeding: ```java --version```
 2. First you should build and install apollo tools: `./mvnw clean install`
 3. Then use scripts located under `bin/` folder to launch desired tool or simply launch the `apollo-tools` jar under `target` directory to get comprehensive cmd args usage
 4. Tool-specific configuration locates under `conf` directory, feel free to modify it for personal needs
  ### Height monitor
  Designed for network monitoring and finding forks, shard differences and slow or stuck peers
  #### Prerequisites
  1. Installed **JDK 11** and project built
  #### Required steps
  1. Choose network to monitor, available configs: mainnet, testnet 1, testnet 2, testnet 3, tap network; Or create your own config for another blockchain network using `conf/peers.json` as a peers config example.
  2. Launch the following command:
  ```
    cd path/to/Apollo-tools-dir
    ./bin/start-hm-mainnet.sh  -- Height monitor for mainnet
    OR
    ./bin/start-hm-testnet-1.sh  -- Height monitor for testnet 1
    OR
    ./bin/start-hm-testnet-2.sh  -- Height monitor for testnet 2
    OR 
    ./bin/start-hm-testnet-3.sh  -- Height monitor for testnet 3
    OR
    ./bin/apl-stat-height-monitor.sh "/absolute/path/to/your/network/peers" -- Height monitor for your custom network
  ```
  ### Updater URL encryptor/decryptor
  The tool for updater url double encryption/decryption. See `bin/apl-rsadecrypt.sh` and `bin/apl-rsaencrypt.sh` for parameters details.
  #### Example
  __Encrypt Update Package URL (first iteration)__

  Encrypting the following URL `https://s3.org/ApolloUpdate-1.51.0-Linux.jar` for Updater
  * Build the Apollo-tools project (alternatively you may use binary distributions to avoid build from sources)
  ```console
  mvn clean install 
   ```
  * For Windows, substitute `C:\path\to\url` with absolute path to file with URL to encrypt and `C:\path\to\key`
with absolute path to private key to encrypt with, then execute
  ```console
   bin\apl-rsaencrypt.sh --in C:\path\to\url --key C:\path\to\key
   ```
  * For Linux/MacOS, substitute `/path/to/url` with absolute path to file with URL to encrypt and `/path/to/key`
    with absolute path to private key to encrypt with, then execute
  ```console
  ./bin/apl-rsaencrypt.sh --in /path/to/url --key /path/to/key
  ```
  * Check the output and copy the encrypted hexadecimal string under `Your encrypted message in hexadecimal format` line
and save to any convenient file
  * Verify that URL is correctly encrypted:

__for Windows__
```console
bin/apl-rsadecrypt.bat --in /path/to/encryptedURL --key /path/to/cert --utf
```
__for Linux/MacOS__

```console
./bin/apl-rsadecrypt.sh --in /path/to/encryptedURL --key /path/to/cert --utf
```
, where substitute `/path/to/encryptedURL` with absolute path to file received after encryption where encrypted URL is
located and `/path/to/cert` with absolute path to the certificate to decrypt 
(which contains public key cryptographically linked with private key used for encryption)

Verify that message under `Result message is` the same as you encrypted, if so then encryption procedure is OK

__Encrypt Update Package URL (second iteration)__

This encryption procedure is like the previous but as an input URL you will use an encrypted URL output from previous
encryption step - first iteration (typically this output must be received from another person)

Encrypting the following encrypted URL `9c907ce247b10af3a8429ef....` (1024 symbols total) for Updater
* Build the Apollo-tools project (alternatively you may use binary distributions to avoid build from sources)
  ```console
  mvn clean install 
   ```
* For Windows, substitute `C:\path\to\url` with absolute path to file with encrypted hexadecimal URL to encrypt and `C:\path\to\key`
  with absolute path to private key to encrypt with, then execute
  ```console
   bin\apl-rsaencrypt.sh --hex --in C:\path\to\url --key C:\path\to\key
   ```
* For Linux/MacOS, aubstitute `/path/to/url` with absolute path to file with encrypted hexadecimal URL to encrypt and `/path/to/key`
  with absolute path to private key to encrypt with, then execute
  ```console
  ./bin/apl-rsaencrypt.sh --hex --in /path/to/url --key /path/to/key
  ```
* Check the output and copy the encrypted hexadecimal string under `Your encrypted message in hexadecimal format` line
  and save to any convenient file - this is your complete Update Package Encrypted URL which consist of two parts (lines)
  with length of 1024 symbols (512 bytes) for each
* Verify that encryption was correct:

__for Windows__
```console
bin/apl-rsadecrypt.bat --in /path/to/encryptedURL --key /path/to/cert
```
__for Linux/MacOS__

```console
./bin/apl-rsadecrypt.sh --in /path/to/encryptedURL --key /path/to/cert
```
, where substitute `/path/to/encryptedURL` with absolute path to file received after encryption where encrypted URL is
located (two lines with 1024 symbols for each) and `/path/to/cert` with absolute path to the certificate to decrypt
(which contains public key cryptographically linked with private key used for encryption)

Verify that output in line ` Your decrypted message in hexadecimal format:` is the same as was input for encryption for
the second Update URL encryption URL (output from first iteration). 

Also you can decrypt it again using another 
certificate to receive the original URL (use --utf key as in `first encryption iteration` verification routine)


  ### Offline transaction signer
  Accept list of unsigned transactions from the input file and sign one by one and then write to the specified output file
  Usage: `./bin/apl-sign.sh --input "/path/to/your/unsigned/txs"`
  ### Currency Minter
  Currency minter is designed to mint new units of mintable currencies. 
  #### Prerequisites:
  1. Launched Apollo node (locally or with an exposed API accessible from minting node)
  2. Created **MINTABLE** currency (totalSupply > currentSupply)
  3. Installed **JDK 11** and project built
  #### Required steps
  1. Specify in the `conf/apl-mint.properties` file host with Apollo API publicly exposed and accessible from your Currency Minter host (use the following properties: `apl.apiServerPort`, `apl.mint.useHttps`, `apl.mint.serverAddress`). For the Apollo node launched locally with default settings, no action required.
  2. Specify in the `conf/apl-mint.properties` file under the `apl.mint.currencyCode` property code of the currency to mint
  3. Depending on your hardware resources, set the `apl.mint.unitsPerMint` property to the number of units to mint per Mint Iteration. 
  It is needed to balance between profit from frequent minting solutions and apl amount required to cover minting fees per found solution. 
  Higher value set here increases total minting difficulty.
  4. Specify the number of CPU threads allocated to minter under `apl.mint.threadPoolSize` property. For better performance - set here number of CPUs physical cores.
  5. Launch from command line: 
     ```
     cd path/to/Apollo-tools-dir
     ./bin/apl-mint.sh  -- Linux 
     OR
     ./bin/apl-mint.bat -- Windows
 
     ```
 6. Type your Apollo's account secretPhrase into launched command prompt or for vault wallet: use exported secretKey
 7. All done
 
 #### Further steps
 1. Explore  rest of the properties for your needs under `conf/apl-mint.properties`
 2. Use your independent config for minter: add to startup script the following argument: -c "/absolute/path/to/your/config": `./bin/apl-mint.sh -c "/home/user/apollo/mint.config"`

