# IRCT-EXT

## What is the IRCT?
As part of the NIH BD2K PIC-SURE Center of Excellence http://www.pic-sure.org the HMS Department of Biomedical informatics https://dbmi.hms.harvard.edu is developing an open-source infrastructure that will foster the incorporation of multiple heterogeneous patient level clinical, omics and environment datasets. This system embraces the idea of decentralized datasets of varying types, and the protocols used to access them while still providing a simple communication layer that can handle querying, joining, and computing on. The BD2K PIC-SURE RESTful API implementation is called Inter Resource Communication Tool (IRCT).

## What is the IRCT-EXT?
The IRCT Extension (IRCT-EXT) provides a way of adding additional functionality to an IRCT instance. Administrators can add any number of additional features without having to make code changes.

You can read more about some of the extensions here: https://github.com/hms-dbmi/IRCT-EXT/wiki

## Version Information

### 1.3.1
This release includes improved documentation, as well as an update to the sql file for adding result data converters to a new IRCT installation.

### 1.3
Minor formatting changes included in this release.

### 1.2.2
This release includes performance improvements in the CSV result data converter. It includes bug fixes that caused files to remain open after the IRCT was done using them.

### 1.2.1
This release includes some minor bug fixes with logging, as well as saving the results in AWS S3. It also includes better documentation.

### 1.2
This release includes new event listeners that expand the search functionality when enabled. These listeners allow for the capitalization of the search terms, and finding UMLS synonyms. Minor code cleanup was also included in this release.

### 1.1
This is the initial release of the IRCT Extensions. It has been tagged as release version 1.1 so it synchs better with the other components of the IRCT (CL, RI, and API). It contains the result data converters that previously were located in the API. Included this release are the event listers for monitoring, and saving and retrieving results to AWS S3.
