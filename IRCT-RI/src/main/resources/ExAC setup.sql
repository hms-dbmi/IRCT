-- Set the resource parameters
set @resourceName = '{{exacresourcename.msg}}';
set @resourceURL = '{{exacresourceurl.msg}}';
set @resourceImplementingInterface = 'edu.harvard.hms.dbmi.bd2k.irct.ri.exac.EXACResourceImplementation';
set @resourceOntology = 'TREE';

-- Set the resource variables
set @resourceId = (select IF(id is NULL,0,max(id)) from Resource) + 1;

-- Set the resource predicates
set @BYENSEMBLID = (select IF(id is NULL,0,max(id)) from PredicateType) + 1;
set @BYQUERYID = @BYENSEMBLID + 1;
set @BYREGIONID = @BYENSEMBLID + 2;
set @BYVARIANTID = @BYENSEMBLID + 3;

-- Set the Fields
set @ENSEMBLID_FIELD_ID = (select IF(id is NULL,0,max(id)) from Field) + 1;
set @QUERY_FIELD_ID = @ENSEMBLID_FIELD_ID + 1;
set @CHROMOSOME_FIELD_ID = @ENSEMBLID_FIELD_ID + 2;
set @START_FIELD_ID = @ENSEMBLID_FIELD_ID + 3;
set @STOP_FIELD_ID = @ENSEMBLID_FIELD_ID + 4;
set @VARIANT_CHROMOSONE_FIELD_ID = @ENSEMBLID_FIELD_ID + 5;
set @VARIANT_POSITION_FIELD_ID = @ENSEMBLID_FIELD_ID + 6;
set @VARIANT_REFERENCE_FIELD_ID = @ENSEMBLID_FIELD_ID + 7;
set @VARIANT_VARIANT_FIELD_ID = @ENSEMBLID_FIELD_ID + 8;


-- set the process id
set @calculateRarityId = (select IF(id is NULL,0,max(id)) from ProcessType) + 1;

-- Set the Process Parameter Ids
set @calculateRarityId_ResultSet = @VARIANT_VARIANT_FIELD_ID  + 1;
set @calculateRarityId_Chromosome = @calculateRarityId_ResultSet + 1;
set @calculateRarityId_Position = @calculateRarityId_Chromosome + 1;
set @calculateRarityId_Reference = @calculateRarityId_Position + 1;
set @calculateRarityId_Variant = @calculateRarityId_Reference + 1;


-- INSERT THE RESOURCE
insert into Resource(id, implementingInterface, name, ontologyType) values(@resourceId, @resourceImplementingInterface, @resourceName, @resourceOntology);

-- INSERT THE RESOURCE PARAMERTERS
insert into resource_parameters(id, name, value) values(@resourceId, 'resourceName', @resourceName);
insert into resource_parameters(id, name, value) values(@resourceId, 'resourceURL', @resourceURL);

-- INSERT RESOURCE DATATYEPS
insert into Resource_dataTypes(Resource_id, datatypes) values(@resourceId, 'edu.harvard.hms.dbmi.bd2k.irct.ri.exac.EXACDataType:VARIANT');
insert into Resource_dataTypes(Resource_id, datatypes) values(@resourceId, 'edu.harvard.hms.dbmi.bd2k.irct.ri.exac.EXACDataType:GENE');
insert into Resource_dataTypes(Resource_id, datatypes) values(@resourceId, 'edu.harvard.hms.dbmi.bd2k.irct.ri.exac.EXACDataType:TRANSCRIPT');
insert into Resource_dataTypes(Resource_id, datatypes) values(@resourceId, 'edu.harvard.hms.dbmi.bd2k.irct.ri.exac.EXACDataType:REGION');
insert into Resource_dataTypes(Resource_id, datatypes) values(@resourceId, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into Resource_dataTypes(Resource_id, datatypes) values(@resourceId, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:INTEGER');

-- INSERT RESOURCE RELATIONSHIPS
insert into Resource_relationships(Resource_id, relationships) values(@resourceId, 'edu.harvard.hms.dbmi.bd2k.irct.ri.exac.EXACOntologyRelationship:PARENT');
insert into Resource_relationships(Resource_id, relationships) values(@resourceId, 'edu.harvard.hms.dbmi.bd2k.irct.ri.exac.EXACOntologyRelationship:CHILD');


-- BY ENSEMBL PREDICATE
insert into PredicateType(id, defaultPredicate, description, displayName, name) values(@BYENSEMBLID, 0, 'Look up by Ensembl Id', 'Ensembl ID', 'ENSEMBL');
insert into PredicateType_dataTypes(PredicateType_id, dataTypes) values(@BYENSEMBLID, 'edu.harvard.hms.dbmi.bd2k.irct.ri.exac.EXACDataType:GENE');
insert into PredicateType_dataTypes(PredicateType_id, dataTypes) values(@BYENSEMBLID, 'edu.harvard.hms.dbmi.bd2k.irct.ri.exac.EXACDataType:TRANSCRIPT');

insert into Field(id, description, name, path, relationship, required) values(@ENSEMBLID_FIELD_ID, 'Ensembl Id', 'Ensembl Id', 'ENSEMBLID', null, 1);
insert into Field_dataTypes(Field_id, dataTypes) values(@ENSEMBLID_FIELD_ID, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into PredicateType_Field(PredicateType_id, fields_id) values(@BYENSEMBLID, @ENSEMBLID_FIELD_ID);

insert into Resource_PredicateType(Resource_Id, supportedPredicates_id) values(@resourceId, @BYENSEMBLID);


-- BY QUERY PREDICATE
insert into PredicateType(id, defaultPredicate, description, displayName, name) values(@BYQUERYID, 1, 'Look up by a query string', 'Query', 'QUERY');
insert into PredicateType_dataTypes(PredicateType_id, dataTypes) values(@BYQUERYID, 'edu.harvard.hms.dbmi.bd2k.irct.ri.exac.EXACDataType:TRANSCRIPT');
insert into PredicateType_dataTypes(PredicateType_id, dataTypes) values(@BYQUERYID, 'edu.harvard.hms.dbmi.bd2k.irct.ri.exac.EXACDataType:VARIANT');
insert into PredicateType_dataTypes(PredicateType_id, dataTypes) values(@BYQUERYID, 'edu.harvard.hms.dbmi.bd2k.irct.ri.exac.EXACDataType:GENE');
insert into PredicateType_dataTypes(PredicateType_id, dataTypes) values(@BYQUERYID, 'edu.harvard.hms.dbmi.bd2k.irct.ri.exac.EXACDataType:TRANSCRIPT');
insert into PredicateType_dataTypes(PredicateType_id, dataTypes) values(@BYQUERYID, 'edu.harvard.hms.dbmi.bd2k.irct.ri.exac.EXACDataType:REGION');
insert into PredicateType_dataTypes(PredicateType_id, dataTypes) values(@BYQUERYID, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');

insert into Field(id, description, name, path, relationship, required) values(@QUERY_FIELD_ID, 'Query String', 'Query String', 'QUERY', null, 1);
insert into Field_dataTypes(Field_id, dataTypes) values(@QUERY_FIELD_ID, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into PredicateType_Field(PredicateType_id, fields_id) values(@BYQUERYID, @QUERY_FIELD_ID);

insert into Resource_PredicateType(Resource_Id, supportedPredicates_id) values(@resourceId, @BYQUERYID);


-- BY REGION PREDICATE
insert into PredicateType(id, defaultPredicate, description, displayName, name) values(@BYREGIONID, 0, 'Look up by region', 'Region', 'REGION');
insert into PredicateType_dataTypes(PredicateType_id, dataTypes) values(@BYREGIONID, 'edu.harvard.hms.dbmi.bd2k.irct.ri.exac.EXACDataType:REGION');

insert into Field(id, description, name, path, relationship, required) values(@CHROMOSOME_FIELD_ID, 'Chromosome', 'Chromosome', 'CHROMOSOME', null, 1);
insert into Field_dataTypes(Field_id, dataTypes) values(@CHROMOSOME_FIELD_ID, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into PredicateType_Field(PredicateType_id, fields_id) values(@BYREGIONID, @CHROMOSOME_FIELD_ID);

insert into Field(id, description, name, path, relationship, required) values(@START_FIELD_ID, 'Start Position', 'Start', 'START', null, 1);
insert into Field_dataTypes(Field_id, dataTypes) values(@START_FIELD_ID, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into PredicateType_Field(PredicateType_id, fields_id) values(@BYREGIONID, @START_FIELD_ID);

insert into Field(id, description, name, path, relationship, required) values(@STOP_FIELD_ID, 'Stop Position', 'Stop', 'STOP', null, 0);
insert into Field_dataTypes(Field_id, dataTypes) values(@STOP_FIELD_ID, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into PredicateType_Field(PredicateType_id, fields_id) values(@BYREGIONID, @STOP_FIELD_ID);

insert into Resource_PredicateType(Resource_Id, supportedPredicates_id) values(@resourceId, @BYREGIONID);

-- BY VARIANT PREDICATE
insert into PredicateType(id, defaultPredicate, description, displayName, name) values(@BYVARIANTID, 0, 'Look up by variant', 'Variant', 'VARIANT');
insert into PredicateType_dataTypes(PredicateType_id, dataTypes) values(@BYVARIANTID, 'edu.harvard.hms.dbmi.bd2k.irct.ri.exac.EXACDataType:VARIANT');


insert into Field(id, description, name, path, relationship, required) values(@VARIANT_CHROMOSONE_FIELD_ID, 'Chromosome', 'Chromosome', 'CHROMOSOME', null, 1);
insert into Field_dataTypes(Field_id, dataTypes) values(@VARIANT_CHROMOSONE_FIELD_ID, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into PredicateType_Field(PredicateType_id, fields_id) values(@BYVARIANTID, @VARIANT_CHROMOSONE_FIELD_ID);

insert into Field(id, description, name, path, relationship, required) values(@VARIANT_POSITION_FIELD_ID, 'Variant Position', 'Position', 'POSITION', null, 1);
insert into Field_dataTypes(Field_id, dataTypes) values(@VARIANT_POSITION_FIELD_ID, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into PredicateType_Field(PredicateType_id, fields_id) values(@BYVARIANTID, @VARIANT_POSITION_FIELD_ID);

insert into Field(id, description, name, path, relationship, required) values(@VARIANT_REFERENCE_FIELD_ID, 'Varaint Reference', 'Reference', 'REFERENCE', null, 1);
insert into Field_dataTypes(Field_id, dataTypes) values(@VARIANT_REFERENCE_FIELD_ID, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into PredicateType_Field(PredicateType_id, fields_id) values(@BYVARIANTID, @VARIANT_REFERENCE_FIELD_ID);

insert into Field(id, description, name, path, relationship, required) values(@VARIANT_VARIANT_FIELD_ID, 'Variant', 'Variant', 'VARIANT', null, 1);
insert into Field_dataTypes(Field_id, dataTypes) values(@VARIANT_VARIANT_FIELD_ID, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into PredicateType_Field(PredicateType_id, fields_id) values(@BYVARIANTID, @VARIANT_VARIANT_FIELD_ID);

insert into Resource_PredicateType(Resource_Id, supportedPredicates_id) values(@resourceId, @BYVARIANTID);



-- Process
-- Rarity
insert into ProcessType(id, name, displayName, description) values(@calculateRarityId, 'RARITY', 'Calculate Rarity', 'Calculate the rarity of a variant against the ExAC database');

-- ResultSet
insert into Field(id, path, name, description, relationship, required) values(@calculateRarityId_ResultSet, 'RESULTSET', 'Result', 'Result Set', null, 1);
insert into Field_dataTypes(Field_id, dataTypes) values(@calculateRarityId_ResultSet, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:RESULTSET');
insert into ProcessType_Fields(ProcessType_id, fields_id) values(@calculateRarityId, @calculateRarityId_ResultSet);

-- Chromosome Column
insert into Field(id, path, name, description, relationship, required) values(@calculateRarityId_Chromosome, 'CHROMOSOME', 'Chromosome', 'Chromosome Column', null, 1);
insert into Field_dataTypes(Field_id, dataTypes) values(@calculateRarityId_Chromosome, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:COLUMN');
insert into ProcessType_Fields(ProcessType_id, fields_id) values(@calculateRarityId, @calculateRarityId_Chromosome);

-- Position Column
insert into Field(id, path, name, description, relationship, required) values(@calculateRarityId_Position, 'POSITION', 'Position', 'Position Column', null, 1);
insert into Field_dataTypes(Field_id, dataTypes) values(@calculateRarityId_Position, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:COLUMN');
insert into ProcessType_Fields(ProcessType_id, fields_id) values(@calculateRarityId, @calculateRarityId_Position);

-- Reference Column
insert into Field(id, path, name, description, relationship, required) values(@calculateRarityId_Reference, 'REFERENCE', 'Reference', 'Reference Column', null, 1);
insert into Field_dataTypes(Field_id, dataTypes) values(@calculateRarityId_Reference, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:COLUMN');
insert into ProcessType_Fields(ProcessType_id, fields_id) values(@calculateRarityId, @calculateRarityId_Reference);

-- Variant Column
insert into Field(id, path, name, description, relationship, required) values(@calculateRarityId_Variant, 'VARIANT', 'Variant', 'Variant Column', null, 1);
insert into Field_dataTypes(Field_id, dataTypes) values(@calculateRarityId_Variant, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:COLUMN');
insert into ProcessType_Fields(ProcessType_id, fields_id) values(@calculateRarityId, @calculateRarityId_Variant);

insert into Resource_ProcessType(Resource_id, supportedProcesses_id) values(@resourceId, @calculateRarityId);