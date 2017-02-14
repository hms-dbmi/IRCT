# IRCT-RI

## What is the IRCT?
As part of the NIH BD2K PIC-SURE Center of Excellence http://www.pic-sure.org the HMS Department of Biomedical informatics https://dbmi.hms.harvard.edu is developing an open-source infrastructure that will foster the incorporation of multiple heterogeneous patient level clinical, omics and environment datasets. This system embraces the idea of decentralized datasets of varying types, and the protocols used to access them while still providing a simple communication layer that can handle querying, joining, and computing on. The BD2K PIC-SURE RESTful API implementation is called Inter Resource Communication Tool (IRCT).

## What is the IRCT-RI?
The IRCT Resource Interface (IRCT-RI) provides a way of connecting tht IRCT application to different resources for querying. Included in the project are interfaces to both i2b2, i2b2/tranSMART, and the ExAC browser.

You can read more about some of the current Resource Implementations here: https://github.com/hms-dbmi/IRCT-RI/wiki

## Version Information

### 1.3.1
This release includes improved documentation, and an update on the SciDB.sql file. The i2b2/tranSMART resource interface has been refactored to better support '/' characters.

### 1.3
This release includes improvements in the SciDB resource interface that includes improved

### 1.2.2
This release includes the first implementation of the SciDB resource interface

### 1.2.1
Includes updated JavaDocs

### 1.2
The resource interfaces were refactored to support new search for term functionality.

### 1.1
This release includes minor improvements in the i2b2, and i2b2/tranSMART resource interfaces, as well as the sql scripts needed to add these resource interfaces to an IRCT instance.

### 1.0
This is the first full release of the IRCT Resource Interface library. It includes interfaces for i2b2, i2b2/tranSMART, and ExAC. It allows for secure interactions with the resource.

### 0.3
This release fixes bugs in the IRCT-RI 0.2 release

### 0.2
This is the prerelease release of the IRCT-RI it provides a proof of concept of the IRCT.
