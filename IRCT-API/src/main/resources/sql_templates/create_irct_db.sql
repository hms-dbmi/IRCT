# ************************************************************
# Sequel Pro SQL dump
# Version 4541
#
# http://www.sequelpro.com/
# https://github.com/sequelpro/sequelpro
#
# Host: 192.168.99.100 (MySQL 5.7.22)
# Database: irct
# Generation Time: 2019-03-12 14:52:55 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Dump of table ClauseAbstract
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ClauseAbstract`;

CREATE TABLE `ClauseAbstract` (
  `DTYPE` varchar(31) NOT NULL,
  `id` bigint(20) NOT NULL,
  `alias` varchar(255) DEFAULT NULL,
  `logicalOperator` varchar(255) DEFAULT NULL,
  `operationType_id` bigint(20) DEFAULT NULL,
  `parameter_id` bigint(20) DEFAULT NULL,
  `field_id` bigint(20) DEFAULT NULL,
  `predicateType_id` bigint(20) DEFAULT NULL,
  `subQuery_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5eqha8o31lnrwoff9aadybgj` (`operationType_id`),
  KEY `FK28yo8d00ixkn9n07cu8l61t1p` (`parameter_id`),
  KEY `FKnusoe06dtgpem5c2xpgeleg98` (`field_id`),
  KEY `FKn6nww6uirpv2wckk7p21p613c` (`predicateType_id`),
  KEY `FKku48g8v2p78m29aejtxnolmyj` (`subQuery_id`),
  CONSTRAINT `FK28yo8d00ixkn9n07cu8l61t1p` FOREIGN KEY (`parameter_id`) REFERENCES `Entity` (`id`),
  CONSTRAINT `FK5eqha8o31lnrwoff9aadybgj` FOREIGN KEY (`operationType_id`) REFERENCES `SelectOperationType` (`id`),
  CONSTRAINT `FKku48g8v2p78m29aejtxnolmyj` FOREIGN KEY (`subQuery_id`) REFERENCES `SubQuery` (`id`),
  CONSTRAINT `FKn6nww6uirpv2wckk7p21p613c` FOREIGN KEY (`predicateType_id`) REFERENCES `PredicateType` (`id`),
  CONSTRAINT `FKnusoe06dtgpem5c2xpgeleg98` FOREIGN KEY (`field_id`) REFERENCES `Entity` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table DataConverterImplementation
# ------------------------------------------------------------

DROP TABLE IF EXISTS `DataConverterImplementation`;

CREATE TABLE `DataConverterImplementation` (
  `id` bigint(20) NOT NULL,
  `dataConverter` varchar(255) DEFAULT NULL,
  `format` varchar(255) DEFAULT NULL,
  `resultDataType` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

LOCK TABLES `DataConverterImplementation` WRITE;
/*!40000 ALTER TABLE `DataConverterImplementation` DISABLE KEYS */;

INSERT INTO `DataConverterImplementation` (`id`, `dataConverter`, `format`, `resultDataType`)
VALUES
	(1,'edu.harvard.hms.dbmi.bd2k.irct.ws.rs.resultconverter.JSONTabularDataConverter','JSON','TABULAR'),
	(2,'edu.harvard.hms.dbmi.bd2k.irct.ws.rs.resultconverter.XMLTabularDataConverter','XML','TABULAR'),
	(3,'edu.harvard.hms.dbmi.bd2k.irct.ws.rs.resultconverter.XSLXTabularDataConverter','XLSX','TABULAR'),
	(4,'edu.harvard.hms.dbmi.bd2k.irct.ws.rs.resultconverter.CSVTabularDataConverter','CSV','TABULAR');

/*!40000 ALTER TABLE `DataConverterImplementation` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table Entity
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Entity`;

CREATE TABLE `Entity` (
  `id` bigint(20) NOT NULL,
  `dataType` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `displayName` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `ontology` varchar(255) DEFAULT NULL,
  `ontologyId` varchar(255) DEFAULT NULL,
  `pui` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table event_parameters
# ------------------------------------------------------------

DROP TABLE IF EXISTS `event_parameters`;

CREATE TABLE `event_parameters` (
  `id` bigint(20) NOT NULL,
  `value` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`,`name`),
  CONSTRAINT `FKq44jyx3w5oyxqbxrmawfjrofy` FOREIGN KEY (`id`) REFERENCES `EventConverterImplementation` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table EventConverterImplementation
# ------------------------------------------------------------

DROP TABLE IF EXISTS `EventConverterImplementation`;

CREATE TABLE `EventConverterImplementation` (
  `id` bigint(20) NOT NULL,
  `eventListener` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table Field
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Field`;

CREATE TABLE `Field` (
  `id` bigint(20) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `path` varchar(255) DEFAULT NULL,
  `relationship` varchar(255) DEFAULT NULL,
  `required` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

LOCK TABLES `Field` WRITE;
/*!40000 ALTER TABLE `Field` DISABLE KEYS */;

INSERT INTO `Field` (`id`, `description`, `name`, `path`, `relationship`, `required`)
VALUES
	(1,'By Encounter','By Encounter','ENCOUNTER',NULL,b'0'),
	(2,'Constrain by a modifier of this entity','Modifier','MODIFIER_KEY','edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.I2B2OntologyRelationship:MODIFIER',b'1'),
	(3,'By Encounter','By Encounter','ENCOUNTER',NULL,b'0'),
	(4,'Operator','Operator','OPERATOR',NULL,b'1'),
	(5,'Constraint','Constraint','CONSTRAINT',NULL,b'1'),
	(6,'Unit of Measure','Unit of Measure','UNIT_OF_MEASURE',NULL,b'0'),
	(7,'By Encounter','By Encounter','ENCOUNTER',NULL,b'0'),
	(8,'Inclusive From Date','From Inclusive','FROM_INCLUSIVE',NULL,b'1'),
	(9,'From Date Start or End','From Time','FROM_TIME',NULL,b'1'),
	(10,'From Date','From Date','FROM_DATE',NULL,b'1'),
	(11,'Inclusive To Date','To Inclusive','TO_INCLUSIVE',NULL,b'1'),
	(12,'To Date Start or End','To Time','TO_TIME',NULL,b'1'),
	(13,'To Date','To Date','TO_DATE',NULL,b'1'),
	(14,'By Encounter','By Encounter','ENCOUNTER',NULL,b'0');

/*!40000 ALTER TABLE `Field` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table Field_dataTypes
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Field_dataTypes`;

CREATE TABLE `Field_dataTypes` (
  `Field_id` bigint(20) NOT NULL,
  `dataTypes` varchar(255) DEFAULT NULL,
  KEY `FKjilu9ox5c6ud3ju87qyjlwchg` (`Field_id`),
  CONSTRAINT `FKjilu9ox5c6ud3ju87qyjlwchg` FOREIGN KEY (`Field_id`) REFERENCES `Field` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

LOCK TABLES `Field_dataTypes` WRITE;
/*!40000 ALTER TABLE `Field_dataTypes` DISABLE KEYS */;

INSERT INTO `Field_dataTypes` (`Field_id`, `dataTypes`)
VALUES
	(5,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING'),
	(5,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:INTEGER'),
	(5,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:FLOAT'),
	(6,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING'),
	(10,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:DATE'),
	(13,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:DATE');

/*!40000 ALTER TABLE `Field_dataTypes` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table Field_permittedValues
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Field_permittedValues`;

CREATE TABLE `Field_permittedValues` (
  `Field_id` bigint(20) NOT NULL,
  `permittedValues` varchar(255) DEFAULT NULL,
  KEY `FK2uhi59su80u87juu34dhx4xna` (`Field_id`),
  CONSTRAINT `FK2uhi59su80u87juu34dhx4xna` FOREIGN KEY (`Field_id`) REFERENCES `Field` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

LOCK TABLES `Field_permittedValues` WRITE;
/*!40000 ALTER TABLE `Field_permittedValues` DISABLE KEYS */;

INSERT INTO `Field_permittedValues` (`Field_id`, `permittedValues`)
VALUES
	(1,'YES'),
	(1,'NO'),
	(3,'YES'),
	(3,'NO'),
	(4,'EQ'),
	(4,'NE'),
	(4,'GT'),
	(4,'GE'),
	(4,'LT'),
	(4,'LE'),
	(4,'BETWEEN'),
	(4,'LIKE[exact]'),
	(4,'LIKE[begin]'),
	(4,'LIKE[end]'),
	(4,'LIKE[contains]'),
	(7,'YES'),
	(7,'NO'),
	(8,'YES'),
	(8,'NO'),
	(9,'START_DATE'),
	(9,'END_DATE'),
	(11,'YES'),
	(11,'NO'),
	(12,'START_DATE'),
	(12,'END_DATE'),
	(14,'YES'),
	(14,'NO');

/*!40000 ALTER TABLE `Field_permittedValues` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table hibernate_sequence
# ------------------------------------------------------------

DROP TABLE IF EXISTS `hibernate_sequence`;

CREATE TABLE `hibernate_sequence` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

LOCK TABLES `hibernate_sequence` WRITE;
/*!40000 ALTER TABLE `hibernate_sequence` DISABLE KEYS */;

INSERT INTO `hibernate_sequence` (`next_val`)
VALUES
	(1),
	(1),
	(1),
	(1),
	(1),
	(1),
	(1),
	(1),
	(1),
	(1),
	(1),
	(1),
	(1);

/*!40000 ALTER TABLE `hibernate_sequence` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table IRCTJoin
# ------------------------------------------------------------

DROP TABLE IF EXISTS `IRCTJoin`;

CREATE TABLE `IRCTJoin` (
  `id` bigint(20) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `displayName` varchar(255) DEFAULT NULL,
  `joinImplementation` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table IRCTJoin_Field
# ------------------------------------------------------------

DROP TABLE IF EXISTS `IRCTJoin_Field`;

CREATE TABLE `IRCTJoin_Field` (
  `IRCTJoin_id` bigint(20) NOT NULL,
  `fields_id` bigint(20) NOT NULL,
  UNIQUE KEY `UK_orlrsolso57j4yhd7ijm589or` (`fields_id`),
  KEY `FKhjdt5iqllvygy8w7834cdoqq4` (`IRCTJoin_id`),
  CONSTRAINT `FKehsf3povmv2ctyiyioci1pam` FOREIGN KEY (`fields_id`) REFERENCES `Field` (`id`),
  CONSTRAINT `FKhjdt5iqllvygy8w7834cdoqq4` FOREIGN KEY (`IRCTJoin_id`) REFERENCES `IRCTJoin` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table IRCTProcess
# ------------------------------------------------------------

DROP TABLE IF EXISTS `IRCTProcess`;

CREATE TABLE `IRCTProcess` (
  `id` bigint(20) NOT NULL,
  `processType_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK949gn1qarf04ms9jsd3nqtqxm` (`processType_id`),
  CONSTRAINT `FK949gn1qarf04ms9jsd3nqtqxm` FOREIGN KEY (`processType_id`) REFERENCES `ProcessType` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table IRCTProcess_Resource
# ------------------------------------------------------------

DROP TABLE IF EXISTS `IRCTProcess_Resource`;

CREATE TABLE `IRCTProcess_Resource` (
  `IRCTProcess_id` bigint(20) NOT NULL,
  `resources_id` bigint(20) NOT NULL,
  UNIQUE KEY `UK_df7pqcdqs7wllnmy9ndnel84e` (`resources_id`),
  KEY `FKtfmxwvpi4t1q8n8uy92s2krvp` (`IRCTProcess_id`),
  CONSTRAINT `FK370wrr1s8emg9bt4x3x74guqj` FOREIGN KEY (`resources_id`) REFERENCES `Resource` (`id`),
  CONSTRAINT `FKtfmxwvpi4t1q8n8uy92s2krvp` FOREIGN KEY (`IRCTProcess_id`) REFERENCES `IRCTProcess` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table JoinType
# ------------------------------------------------------------

DROP TABLE IF EXISTS `JoinType`;

CREATE TABLE `JoinType` (
  `id` bigint(20) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `displayName` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table JoinType_dataTypes
# ------------------------------------------------------------

DROP TABLE IF EXISTS `JoinType_dataTypes`;

CREATE TABLE `JoinType_dataTypes` (
  `JoinType_id` bigint(20) NOT NULL,
  `dataTypes` varchar(255) DEFAULT NULL,
  KEY `FKqir34toa3et6dmq2x8m6m4cwa` (`JoinType_id`),
  CONSTRAINT `FKqir34toa3et6dmq2x8m6m4cwa` FOREIGN KEY (`JoinType_id`) REFERENCES `JoinType` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table JoinType_Field
# ------------------------------------------------------------

DROP TABLE IF EXISTS `JoinType_Field`;

CREATE TABLE `JoinType_Field` (
  `JoinType_id` bigint(20) NOT NULL,
  `fields_id` bigint(20) NOT NULL,
  UNIQUE KEY `UK_johv0xfh1rh26rvkodur4c9bj` (`fields_id`),
  KEY `FKo5bhevdoxdg60r6wpaoi8r2ix` (`JoinType_id`),
  CONSTRAINT `FK562hh9a6if0i0hbg2fvfcrwji` FOREIGN KEY (`fields_id`) REFERENCES `Field` (`id`),
  CONSTRAINT `FKo5bhevdoxdg60r6wpaoi8r2ix` FOREIGN KEY (`JoinType_id`) REFERENCES `JoinType` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table PredicateType
# ------------------------------------------------------------

DROP TABLE IF EXISTS `PredicateType`;

CREATE TABLE `PredicateType` (
  `id` bigint(20) NOT NULL,
  `defaultPredicate` bit(1) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `displayName` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

LOCK TABLES `PredicateType` WRITE;
/*!40000 ALTER TABLE `PredicateType` DISABLE KEYS */;

INSERT INTO `PredicateType` (`id`, `defaultPredicate`, `description`, `displayName`, `name`)
VALUES
	(1,b'1','Contains value','Contains','CONTAINS'),
	(2,b'0','Constrain by Modifier','Constrain by Modifier','CONSTRAIN_MODIFIER'),
	(3,b'0','Constrains by Value','Constrain by Value','CONSTRAIN_VALUE'),
	(4,b'0','Constrains by Date','Constrain by Date','CONSTRAIN_DATE');

/*!40000 ALTER TABLE `PredicateType` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table PredicateType_dataTypes
# ------------------------------------------------------------

DROP TABLE IF EXISTS `PredicateType_dataTypes`;

CREATE TABLE `PredicateType_dataTypes` (
  `PredicateType_id` bigint(20) NOT NULL,
  `dataTypes` varchar(255) DEFAULT NULL,
  KEY `FK72uk4oyraqht9giy6dgen1kla` (`PredicateType_id`),
  CONSTRAINT `FK72uk4oyraqht9giy6dgen1kla` FOREIGN KEY (`PredicateType_id`) REFERENCES `PredicateType` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

LOCK TABLES `PredicateType_dataTypes` WRITE;
/*!40000 ALTER TABLE `PredicateType_dataTypes` DISABLE KEYS */;

INSERT INTO `PredicateType_dataTypes` (`PredicateType_id`, `dataTypes`)
VALUES
	(1,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING'),
	(1,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:INTEGER'),
	(1,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:FLOAT'),
	(2,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING'),
	(2,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:INTEGER'),
	(2,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:FLOAT'),
	(3,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING'),
	(3,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:INTEGER'),
	(3,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:FLOAT');

/*!40000 ALTER TABLE `PredicateType_dataTypes` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table PredicateType_Field
# ------------------------------------------------------------

DROP TABLE IF EXISTS `PredicateType_Field`;

CREATE TABLE `PredicateType_Field` (
  `PredicateType_id` bigint(20) NOT NULL,
  `fields_id` bigint(20) NOT NULL,
  UNIQUE KEY `UK_bv0wdi2f2axdmcyhf2tl8p75c` (`fields_id`),
  KEY `FKca2rupjbc9fktk0nh8jrwsa6c` (`PredicateType_id`),
  CONSTRAINT `FKca2rupjbc9fktk0nh8jrwsa6c` FOREIGN KEY (`PredicateType_id`) REFERENCES `PredicateType` (`id`),
  CONSTRAINT `FKm8n78jwv4qemmeqhdcxdfv2kk` FOREIGN KEY (`fields_id`) REFERENCES `Field` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

LOCK TABLES `PredicateType_Field` WRITE;
/*!40000 ALTER TABLE `PredicateType_Field` DISABLE KEYS */;

INSERT INTO `PredicateType_Field` (`PredicateType_id`, `fields_id`)
VALUES
	(1,1),
	(2,2),
	(2,3),
	(3,4),
	(3,5),
	(3,6),
	(3,7),
	(4,8),
	(4,9),
	(4,10),
	(4,11),
	(4,12),
	(4,13),
	(4,14);

/*!40000 ALTER TABLE `PredicateType_Field` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table PredicateType_paths
# ------------------------------------------------------------

DROP TABLE IF EXISTS `PredicateType_paths`;

CREATE TABLE `PredicateType_paths` (
  `PredicateType_id` bigint(20) NOT NULL,
  `paths` varchar(255) DEFAULT NULL,
  KEY `FKdnior8uksqlvybjv9yvnxvwil` (`PredicateType_id`),
  CONSTRAINT `FKdnior8uksqlvybjv9yvnxvwil` FOREIGN KEY (`PredicateType_id`) REFERENCES `PredicateType` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table process_values
# ------------------------------------------------------------

DROP TABLE IF EXISTS `process_values`;

CREATE TABLE `process_values` (
  `PROCESS_VALUE` bigint(20) NOT NULL,
  `process_id` varchar(255) NOT NULL,
  PRIMARY KEY (`PROCESS_VALUE`,`process_id`),
  CONSTRAINT `FK6p6y7qa7v42djvuv1xkqxyaig` FOREIGN KEY (`PROCESS_VALUE`) REFERENCES `IRCTProcess` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table ProcessType
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ProcessType`;

CREATE TABLE `ProcessType` (
  `id` bigint(20) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `displayName` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table ProcessType_Fields
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ProcessType_Fields`;

CREATE TABLE `ProcessType_Fields` (
  `ProcessType_id` bigint(20) NOT NULL,
  `fields_id` bigint(20) NOT NULL,
  UNIQUE KEY `UK_gsn9bq7e6fqpw92pchyw74da3` (`fields_id`),
  KEY `FK9oapjwen6caugd668rf2liv9v` (`ProcessType_id`),
  CONSTRAINT `FK9oapjwen6caugd668rf2liv9v` FOREIGN KEY (`ProcessType_id`) REFERENCES `ProcessType` (`id`),
  CONSTRAINT `FKquesy9bph8snl6nnr5kgh06n5` FOREIGN KEY (`fields_id`) REFERENCES `Field` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table ProcessType_Returns
# ------------------------------------------------------------

DROP TABLE IF EXISTS `ProcessType_Returns`;

CREATE TABLE `ProcessType_Returns` (
  `ProcessType_id` bigint(20) NOT NULL,
  `returns_id` bigint(20) NOT NULL,
  UNIQUE KEY `UK_rqnq3dqh729conwmd03ku43rq` (`returns_id`),
  KEY `FKpea3l4r2yku9kr0yahl077w0k` (`ProcessType_id`),
  CONSTRAINT `FKj8kwvoklfi5jan1pvhduru34o` FOREIGN KEY (`returns_id`) REFERENCES `Field` (`id`),
  CONSTRAINT `FKpea3l4r2yku9kr0yahl077w0k` FOREIGN KEY (`ProcessType_id`) REFERENCES `ProcessType` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table Query
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Query`;

CREATE TABLE `Query` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table Query_ClauseAbstract
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Query_ClauseAbstract`;

CREATE TABLE `Query_ClauseAbstract` (
  `Query_id` bigint(20) NOT NULL,
  `clauses_id` bigint(20) NOT NULL,
  `clauses_KEY` bigint(20) NOT NULL,
  PRIMARY KEY (`Query_id`,`clauses_KEY`),
  KEY `FKc48uba4a9sq3wgnvbth09vf7s` (`clauses_id`),
  CONSTRAINT `FKc48uba4a9sq3wgnvbth09vf7s` FOREIGN KEY (`clauses_id`) REFERENCES `ClauseAbstract` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table Query_Resource
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Query_Resource`;

CREATE TABLE `Query_Resource` (
  `Query_id` bigint(20) NOT NULL,
  `resources_id` bigint(20) NOT NULL,
  PRIMARY KEY (`Query_id`,`resources_id`),
  KEY `FKbophovty49c00fs93k9f3uvxa` (`resources_id`),
  CONSTRAINT `FKbophovty49c00fs93k9f3uvxa` FOREIGN KEY (`resources_id`) REFERENCES `Resource` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table Query_SubQuery
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Query_SubQuery`;

CREATE TABLE `Query_SubQuery` (
  `Query_id` bigint(20) NOT NULL,
  `subQueries_id` bigint(20) NOT NULL,
  `subQueries_KEY` varchar(255) NOT NULL,
  PRIMARY KEY (`Query_id`,`subQueries_KEY`),
  KEY `FK2q00vf1fyv6c4qk5eamxh3cue` (`subQueries_id`),
  CONSTRAINT `FK2q00vf1fyv6c4qk5eamxh3cue` FOREIGN KEY (`subQueries_id`) REFERENCES `SubQuery` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table Resource
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Resource`;

CREATE TABLE `Resource` (
  `id` bigint(20) NOT NULL,
  `implementingInterface` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `ontologyType` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_t51o07lcts4qhxgwwujbg51u5` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

LOCK TABLES `Resource` WRITE;
/*!40000 ALTER TABLE `Resource` DISABLE KEYS */;

INSERT INTO `Resource` (`id`, `implementingInterface`, `name`, `ontologyType`)
VALUES
	(1,'edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.I2B2XMLResourceImplementation','i2b2-wildfly-default','TREE');

/*!40000 ALTER TABLE `Resource` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table Resource_dataTypes
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Resource_dataTypes`;

CREATE TABLE `Resource_dataTypes` (
  `Resource_id` bigint(20) NOT NULL,
  `dataTypes` varchar(255) DEFAULT NULL,
  KEY `FK72swwhsf77fspfktwr24o0utq` (`Resource_id`),
  CONSTRAINT `FK72swwhsf77fspfktwr24o0utq` FOREIGN KEY (`Resource_id`) REFERENCES `Resource` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

LOCK TABLES `Resource_dataTypes` WRITE;
/*!40000 ALTER TABLE `Resource_dataTypes` DISABLE KEYS */;

INSERT INTO `Resource_dataTypes` (`Resource_id`, `dataTypes`)
VALUES
	(1,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:DATETIME'),
	(1,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:DATE'),
	(1,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:INTEGER'),
	(1,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING'),
	(1,'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:FLOAT');

/*!40000 ALTER TABLE `Resource_dataTypes` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table Resource_Field
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Resource_Field`;

CREATE TABLE `Resource_Field` (
  `Resource_id` bigint(20) NOT NULL,
  `supportedSelectFields_id` bigint(20) NOT NULL,
  UNIQUE KEY `UK_70l160yqla8q85duhbdmwi0s9` (`supportedSelectFields_id`),
  KEY `FKleberky5f01jk01qifhm6lkav` (`Resource_id`),
  CONSTRAINT `FKleberky5f01jk01qifhm6lkav` FOREIGN KEY (`Resource_id`) REFERENCES `Resource` (`id`),
  CONSTRAINT `FKlu97ayxqdtelpc0yi135d6j4o` FOREIGN KEY (`supportedSelectFields_id`) REFERENCES `Field` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table Resource_JoinType
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Resource_JoinType`;

CREATE TABLE `Resource_JoinType` (
  `Resource_id` bigint(20) NOT NULL,
  `supportedJoins_id` bigint(20) NOT NULL,
  UNIQUE KEY `UK_63fa3sacx4lhuax720sytnl5s` (`supportedJoins_id`),
  KEY `FK8n2r56itfjnre2p29dixasxr0` (`Resource_id`),
  CONSTRAINT `FK8n2r56itfjnre2p29dixasxr0` FOREIGN KEY (`Resource_id`) REFERENCES `Resource` (`id`),
  CONSTRAINT `FKt1ww7av4deh9g20aryo912a2u` FOREIGN KEY (`supportedJoins_id`) REFERENCES `JoinType` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table Resource_LogicalOperator
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Resource_LogicalOperator`;

CREATE TABLE `Resource_LogicalOperator` (
  `id` bigint(20) NOT NULL,
  `logicalOperator` varchar(255) NOT NULL,
  KEY `FK1a1lbrn4k8t2ocxl842vbltl5` (`id`),
  CONSTRAINT `FK1a1lbrn4k8t2ocxl842vbltl5` FOREIGN KEY (`id`) REFERENCES `Resource` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

LOCK TABLES `Resource_LogicalOperator` WRITE;
/*!40000 ALTER TABLE `Resource_LogicalOperator` DISABLE KEYS */;

INSERT INTO `Resource_LogicalOperator` (`id`, `logicalOperator`)
VALUES
	(1,'AND'),
	(1,'OR'),
	(1,'NOT');

/*!40000 ALTER TABLE `Resource_LogicalOperator` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table resource_parameters
# ------------------------------------------------------------

DROP TABLE IF EXISTS `resource_parameters`;

CREATE TABLE `resource_parameters` (
  `id` bigint(20) NOT NULL,
  `value` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`,`name`),
  CONSTRAINT `FK1d2tewavn8d3kt64x3yqm9ws1` FOREIGN KEY (`id`) REFERENCES `Resource` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

LOCK TABLES `resource_parameters` WRITE;
/*!40000 ALTER TABLE `resource_parameters` DISABLE KEYS */;

INSERT INTO `resource_parameters` (`id`, `value`, `name`)
VALUES
	(1,NULL,'clientId'),
	(1,'i2b2demo','domain'),
	(1,'true','ignoreCertificate'),
	(1,NULL,'namespace'),
	(1,'demouser','password'),
	(1,'i2b2-wildfly-default','resourceName'),
	(1,'http://i2b2-wildfly:9090/i2b2/services/','resourceURL'),
	(1,NULL,'transmartURL'),
	(1,'demo','username');

/*!40000 ALTER TABLE `resource_parameters` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table Resource_PredicateType
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Resource_PredicateType`;

CREATE TABLE `Resource_PredicateType` (
  `Resource_id` bigint(20) NOT NULL,
  `supportedPredicates_id` bigint(20) NOT NULL,
  UNIQUE KEY `UK_y5xm5d8miayjkgfggcmfs579` (`supportedPredicates_id`),
  KEY `FKe2nesi3ipljqh6u43399iv03b` (`Resource_id`),
  CONSTRAINT `FKe2nesi3ipljqh6u43399iv03b` FOREIGN KEY (`Resource_id`) REFERENCES `Resource` (`id`),
  CONSTRAINT `FKpilakkbccxwmu3jsophxumtfc` FOREIGN KEY (`supportedPredicates_id`) REFERENCES `PredicateType` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

LOCK TABLES `Resource_PredicateType` WRITE;
/*!40000 ALTER TABLE `Resource_PredicateType` DISABLE KEYS */;

INSERT INTO `Resource_PredicateType` (`Resource_id`, `supportedPredicates_id`)
VALUES
	(1,1),
	(1,2),
	(1,3),
	(1,4);

/*!40000 ALTER TABLE `Resource_PredicateType` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table Resource_ProcessType
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Resource_ProcessType`;

CREATE TABLE `Resource_ProcessType` (
  `Resource_id` bigint(20) NOT NULL,
  `supportedProcesses_id` bigint(20) NOT NULL,
  UNIQUE KEY `UK_h0wxwkho8gklx70a5r3pumpn1` (`supportedProcesses_id`),
  KEY `FKfsfhhkh9ywmwm5excorlu5pb6` (`Resource_id`),
  CONSTRAINT `FK62k8fgcp1tu02ya421w788sq9` FOREIGN KEY (`supportedProcesses_id`) REFERENCES `ProcessType` (`id`),
  CONSTRAINT `FKfsfhhkh9ywmwm5excorlu5pb6` FOREIGN KEY (`Resource_id`) REFERENCES `Resource` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table Resource_relationships
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Resource_relationships`;

CREATE TABLE `Resource_relationships` (
  `Resource_id` bigint(20) NOT NULL,
  `relationships` varchar(255) DEFAULT NULL,
  KEY `FKjmast2nvmxna8bcn3sloyjaee` (`Resource_id`),
  CONSTRAINT `FKjmast2nvmxna8bcn3sloyjaee` FOREIGN KEY (`Resource_id`) REFERENCES `Resource` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

LOCK TABLES `Resource_relationships` WRITE;
/*!40000 ALTER TABLE `Resource_relationships` DISABLE KEYS */;

INSERT INTO `Resource_relationships` (`Resource_id`, `relationships`)
VALUES
	(1,'edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.I2B2OntologyRelationship:PARENT'),
	(1,'edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.I2B2OntologyRelationship:CHILD'),
	(1,'edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.I2B2OntologyRelationship:SIBLING'),
	(1,'edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.I2B2OntologyRelationship:MODIFIER'),
	(1,'edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.I2B2OntologyRelationship:TERM');

/*!40000 ALTER TABLE `Resource_relationships` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table Resource_SelectOperationType
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Resource_SelectOperationType`;

CREATE TABLE `Resource_SelectOperationType` (
  `Resource_id` bigint(20) NOT NULL,
  `supportedSelectOperations_id` bigint(20) NOT NULL,
  UNIQUE KEY `UK_b8nhnn6k3d9chfer56h6s1cfc` (`supportedSelectOperations_id`),
  KEY `FK2imb4iun7egiyqm33jd1gbggx` (`Resource_id`),
  CONSTRAINT `FK24xo1dvt33evlyuogifdrtcwu` FOREIGN KEY (`supportedSelectOperations_id`) REFERENCES `SelectOperationType` (`id`),
  CONSTRAINT `FK2imb4iun7egiyqm33jd1gbggx` FOREIGN KEY (`Resource_id`) REFERENCES `Resource` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table Resource_SortOperationType
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Resource_SortOperationType`;

CREATE TABLE `Resource_SortOperationType` (
  `Resource_id` bigint(20) NOT NULL,
  `supportedSortOperations_id` bigint(20) NOT NULL,
  UNIQUE KEY `UK_2hnp58jxtvftv7o9hnqaopol1` (`supportedSortOperations_id`),
  KEY `FKggm5xv7n6foba635mlob3rywq` (`Resource_id`),
  CONSTRAINT `FK80vvgdlmu4ktd915y1cmogul1` FOREIGN KEY (`supportedSortOperations_id`) REFERENCES `SortOperationType` (`id`),
  CONSTRAINT `FKggm5xv7n6foba635mlob3rywq` FOREIGN KEY (`Resource_id`) REFERENCES `Resource` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table Resource_VisualizationType
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Resource_VisualizationType`;

CREATE TABLE `Resource_VisualizationType` (
  `Resource_id` bigint(20) NOT NULL,
  `supportedVisualizations_id` bigint(20) NOT NULL,
  UNIQUE KEY `UK_6a2kcf3dy8lmqqll1n47j7ol3` (`supportedVisualizations_id`),
  KEY `FKpb7n2sud10h5289cbj3jx1w19` (`Resource_id`),
  CONSTRAINT `FKpb7n2sud10h5289cbj3jx1w19` FOREIGN KEY (`Resource_id`) REFERENCES `Resource` (`id`),
  CONSTRAINT `FKrcvbeo3oqytb5r38vt7fgehk3` FOREIGN KEY (`supportedVisualizations_id`) REFERENCES `VisualizationType` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table resSeq
# ------------------------------------------------------------

DROP TABLE IF EXISTS `resSeq`;

CREATE TABLE `resSeq` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

LOCK TABLES `resSeq` WRITE;
/*!40000 ALTER TABLE `resSeq` DISABLE KEYS */;

INSERT INTO `resSeq` (`next_val`)
VALUES
	(1);

/*!40000 ALTER TABLE `resSeq` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table Result
# ------------------------------------------------------------

DROP TABLE IF EXISTS `Result`;

CREATE TABLE `Result` (
  `id` bigint(20) NOT NULL,
  `data` varchar(255) DEFAULT NULL,
  `dataType` varchar(255) DEFAULT NULL,
  `endTime` datetime DEFAULT NULL,
  `jobType` varchar(255) DEFAULT NULL,
  `message` varchar(255) DEFAULT NULL,
  `resourceActionId` varchar(255) DEFAULT NULL,
  `resultSetLocation` varchar(255) DEFAULT NULL,
  `resultStatus` varchar(255) DEFAULT NULL,
  `startTime` datetime DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKdift6qnfkveefqophnmckasq6` (`user_id`),
  CONSTRAINT `FKdift6qnfkveefqophnmckasq6` FOREIGN KEY (`user_id`) REFERENCES `User` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table select_values
# ------------------------------------------------------------

DROP TABLE IF EXISTS `select_values`;

CREATE TABLE `select_values` (
  `select_id` bigint(20) NOT NULL,
  `value` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`select_id`,`name`),
  CONSTRAINT `FKa10qr2s2ixfmn6qdhl2ik3dfu` FOREIGN KEY (`select_id`) REFERENCES `ClauseAbstract` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table SelectOperationType
# ------------------------------------------------------------

DROP TABLE IF EXISTS `SelectOperationType`;

CREATE TABLE `SelectOperationType` (
  `id` bigint(20) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `displayName` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table SelectOperationType_dataTypes
# ------------------------------------------------------------

DROP TABLE IF EXISTS `SelectOperationType_dataTypes`;

CREATE TABLE `SelectOperationType_dataTypes` (
  `SelectOperationType_id` bigint(20) NOT NULL,
  `dataTypes` varchar(255) DEFAULT NULL,
  KEY `FKp8pi3uxn2kbf0ah9goq03nosc` (`SelectOperationType_id`),
  CONSTRAINT `FKp8pi3uxn2kbf0ah9goq03nosc` FOREIGN KEY (`SelectOperationType_id`) REFERENCES `SelectOperationType` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table SelectOperationType_Field
# ------------------------------------------------------------

DROP TABLE IF EXISTS `SelectOperationType_Field`;

CREATE TABLE `SelectOperationType_Field` (
  `SelectOperationType_id` bigint(20) NOT NULL,
  `fields_id` bigint(20) NOT NULL,
  UNIQUE KEY `UK_3gmwhf2m72dwb9a86hj58pebp` (`fields_id`),
  KEY `FKd0qylvdlhj9252q5qg49gcaic` (`SelectOperationType_id`),
  CONSTRAINT `FKd0qylvdlhj9252q5qg49gcaic` FOREIGN KEY (`SelectOperationType_id`) REFERENCES `SelectOperationType` (`id`),
  CONSTRAINT `FKg43981fpntrojrnsnec01mu5j` FOREIGN KEY (`fields_id`) REFERENCES `Field` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table SelectOperationType_paths
# ------------------------------------------------------------

DROP TABLE IF EXISTS `SelectOperationType_paths`;

CREATE TABLE `SelectOperationType_paths` (
  `SelectOperationType_id` bigint(20) NOT NULL,
  `paths` varchar(255) DEFAULT NULL,
  KEY `FK3fjn9jqy7ciiwdcfpj4r6mo7s` (`SelectOperationType_id`),
  CONSTRAINT `FK3fjn9jqy7ciiwdcfpj4r6mo7s` FOREIGN KEY (`SelectOperationType_id`) REFERENCES `SelectOperationType` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table SortOperationType
# ------------------------------------------------------------

DROP TABLE IF EXISTS `SortOperationType`;

CREATE TABLE `SortOperationType` (
  `id` bigint(20) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `displayName` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table SortOperationType_dataTypes
# ------------------------------------------------------------

DROP TABLE IF EXISTS `SortOperationType_dataTypes`;

CREATE TABLE `SortOperationType_dataTypes` (
  `SortOperationType_id` bigint(20) NOT NULL,
  `dataTypes` varchar(255) DEFAULT NULL,
  KEY `FKfhhb9ks4wrhkdwxvo7nfvgdwy` (`SortOperationType_id`),
  CONSTRAINT `FKfhhb9ks4wrhkdwxvo7nfvgdwy` FOREIGN KEY (`SortOperationType_id`) REFERENCES `SortOperationType` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table SortOperationType_Field
# ------------------------------------------------------------

DROP TABLE IF EXISTS `SortOperationType_Field`;

CREATE TABLE `SortOperationType_Field` (
  `SortOperationType_id` bigint(20) NOT NULL,
  `fields_id` bigint(20) NOT NULL,
  UNIQUE KEY `UK_h39sgo99sd79w6s03mjtuu9kt` (`fields_id`),
  KEY `FK1tars0boxctrr0hpfon6clvqm` (`SortOperationType_id`),
  CONSTRAINT `FK1tars0boxctrr0hpfon6clvqm` FOREIGN KEY (`SortOperationType_id`) REFERENCES `SortOperationType` (`id`),
  CONSTRAINT `FKtjlcoxkwwivo4dfgu1eksr0q1` FOREIGN KEY (`fields_id`) REFERENCES `Field` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table SortOperationType_paths
# ------------------------------------------------------------

DROP TABLE IF EXISTS `SortOperationType_paths`;

CREATE TABLE `SortOperationType_paths` (
  `SortOperationType_id` bigint(20) NOT NULL,
  `paths` varchar(255) DEFAULT NULL,
  KEY `FKobvsa3h5j7o78atb2cnc5nlhh` (`SortOperationType_id`),
  CONSTRAINT `FKobvsa3h5j7o78atb2cnc5nlhh` FOREIGN KEY (`SortOperationType_id`) REFERENCES `SortOperationType` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table SubQuery
# ------------------------------------------------------------

DROP TABLE IF EXISTS `SubQuery`;

CREATE TABLE `SubQuery` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `parent_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table User
# ------------------------------------------------------------

DROP TABLE IF EXISTS `User`;

CREATE TABLE `User` (
  `id` bigint(20) NOT NULL,
  `accessKey` varchar(255) DEFAULT NULL,
  `token` varchar(16384) DEFAULT NULL,
  `userId` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table userSeq
# ------------------------------------------------------------

DROP TABLE IF EXISTS `userSeq`;

CREATE TABLE `userSeq` (
  `next_val` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

LOCK TABLES `userSeq` WRITE;
/*!40000 ALTER TABLE `userSeq` DISABLE KEYS */;

INSERT INTO `userSeq` (`next_val`)
VALUES
	(1);

/*!40000 ALTER TABLE `userSeq` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table VisualizationType
# ------------------------------------------------------------

DROP TABLE IF EXISTS `VisualizationType`;

CREATE TABLE `VisualizationType` (
  `id` bigint(20) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `displayName` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `returns` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table VisualizationType_Field
# ------------------------------------------------------------

DROP TABLE IF EXISTS `VisualizationType_Field`;

CREATE TABLE `VisualizationType_Field` (
  `VisualizationType_id` bigint(20) NOT NULL,
  `fields_id` bigint(20) NOT NULL,
  UNIQUE KEY `UK_jhyo7o9m5brbv9driabvbgv1d` (`fields_id`),
  KEY `FKqiexdpnre5ytr3mwtr0vwsext` (`VisualizationType_id`),
  CONSTRAINT `FKfsr4iakshvcwtco1xjthkwido` FOREIGN KEY (`fields_id`) REFERENCES `Field` (`id`),
  CONSTRAINT `FKqiexdpnre5ytr3mwtr0vwsext` FOREIGN KEY (`VisualizationType_id`) REFERENCES `VisualizationType` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table where_values
# ------------------------------------------------------------

DROP TABLE IF EXISTS `where_values`;

CREATE TABLE `where_values` (
  `where_id` bigint(20) NOT NULL,
  `value` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`where_id`,`name`),
  CONSTRAINT `FK8ysfbt3qeue2xatidf87cpoqo` FOREIGN KEY (`where_id`) REFERENCES `ClauseAbstract` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;




/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
