# Apollo-tools

This repository contains code for ___apollo-tools___ component.
There are other components that are parts of Apollo:

1. [Apollo](https://github.com/ApolloFoundation/Apollo): Apollo blockchain node
2. [Apollo-web-ui](https://github.com/ApolloFoundation/Apollo-web-ui): Web wallet UI that is served by Apollo blockchain node and can be accersed by browser at http://localhost:7876
3. [Apollo-dektop](https://github.com/ApolloFoundation/Apollo-desktop) Desktop wallet UI. Apollo-web-ui must be installed tobe able to run Apollo desktop wallet.
4. [Apollo-bom-ext](https://github.com/ApolloFoundation/Apollo-bom-ext) This module required in compilation time oly. It contains BOM of all external libraries used by Apollo components. **

Apollo is being developed by the Apollo Foundation and supporting members of the community.
This repository contains different tools to work with Apollo blockchain.

The command line utili can

* sign transactions offline
* "mint" cyprocurency colored coins
* generate/verify public key
* export constants used in build time
* run blockchain height monitor

The 'height monitor' is the most frequently used tool, see in
The configuration files used are in conf folder, see: peers.json, peers-1t.json, peers-2t.json, peers-3t.json 

## Requirements

[Apollo blockchain node](https://github.com/ApolloFoundation/Apollo) should be up and running for 
most use cases, excep offline transaction signing.

Java 11 (JRE) is required to run ___apollo-tools___.


### Build instructions

Fisrt, you should build [Apollo](https://github.com/ApolloFoundation/Apollo): Apollo blockchain node.

Then go to cloned repository code and run
```
	./mvnw clean install
```

Final artefact will be assembled in ___tarfet___ directory with name similar to ___apollo-tools-1.47.14-NoOS-NoArch.zip___. Unzip it near to installed Apollo and run by scripts in ___ApolloWallet/apollo-tools/bin___ directory.

 

