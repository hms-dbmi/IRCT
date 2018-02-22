# IRCT-CL

## What is the IRCT?
As part of the NIH BD2K PIC-SURE Center of Excellence http://www.pic-sure.org the HMS Department of Biomedical informatics https://dbmi.hms.harvard.edu is developing an open-source infrastructure that will foster the incorporation of multiple heterogeneous patient level clinical, omics and environment datasets. This system embraces the idea of decentralized datasets of varying types, and the protocols used to access them while still providing a simple communication layer that can handle querying, joining, and computing on. The BD2K PIC-SURE RESTful API implementation is called Inter Resource Communication Tool (IRCT).

## What is the IRCT-CL?
The IRCT Communication Layer (IRCT-CL) provides a resource agnostic Representative State Transfer (RESTful) service. Users build queries by making a series of calls to the RESTful service that can span multiple datasets and resources.

## Whitelist Authorization Configuration
1. Create your own whitelist.json file based on the sample in /IRCT-API/src/main/resources/whitelist.json.sample, then put it in your local file system
2. In your wildfly configuration file `standalone.xml`, search for field `urn:jboss:domain:naming:2.0` then add one line after `<bindings>` for letting the
system know the file location: `<simple name="java:global/whitelist_config_file" value="<Client whitelist config file location>" />`,
modify the content in value section to the location of your local whitelist.json file.
3. For more details, please go to check the template in /IRCT-CL/src/main/resources/wildfly-configuration/standalone.xml. In this template, 
search for whitelist_config_file, there is a comment about details about this configuration

## Version Information

### 1.4
This release adds support for Joins by adding the Join Service. It removes the previous join service implemented in the system service. It also includes updates to the Select Clause JSON handler to support fields that are not associated with an operation.

### 1.3.2
No changes in this release

### 1.3.1
This release includes improved documentation.

### 1.3
This release includes a refactoring of the Query Service service. It adds better support for joins, as well as new support for subqueries, and sort predicates.

### 1.2.2
This release cleans up some development code.

### 1.2.1
This release fixes some minor bugs, and adds better documentation. It includes standardized error messages for system exceptions, and unknown URL requests.

### 1.2
This release supports more search for term functionality.

### 1.1
This release cleans up some code, as well as fixes some minor bugs.

### 1.0
This is the first full release of the IRCT Communication Library. It provides a RESTFull interface for the IRCT.

### 0.3
This release fixes bugs in the IRCT-RI 0.2 release

### 0.2
This is the prerelease version of the IRCT-CL it provides a proof of concept of the IRCT.
