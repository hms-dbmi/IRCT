set @resourceName = 'hail-dev';

set @resourceURL = 'http://ec2-54-84-78-84.compute-1.amazonaws.com:10001/';

-- SET THE RESOURCE VARIABLE
set @resourceId = (select IFNULL(max(id), 1) from Resource) + 1;
insert into Resource(id, implementingInterface, name, ontologyType) VALUES
  (@resourceId, 'edu.harvard.hms.dbmi.bd2k.picsure.ri.HAIL', @resourceName, 'TREE');

-- INSERT THE RESOURCE PARAMERTERS
insert into resource_parameters(id, name, value) values(@resourceId, 'resourceName', @resourceName);
insert into resource_parameters(id, name, value) values(@resourceId, 'resourceURL', @resourceURL);
	
-- SET THE RESOURCE PREDICATES
-- set @predicatetype_SUMMARY = (select IFNULL(max(id), 1) from PredicateType) + 1;
-- insert into PredicateType(id, defaultPredicate, description, displayName, name) VALUES
--   (@predicatetype_SUMMARY, 0, 'Summarize function', 'summary', 'SUMMARY');

-- insert into PredicateType_dataTypes(PredicateType_id, dataTypes) values
--   (@predicate_type_id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
-- insert into Resource_PredicateType(Resource_Id, supportedPredicates_id) VALUES
--   (@resourceId, @predicatetype_SUMMARY);

-- SET THE FIELDS
-- set @field_id = (select NULLIF(max(id), 0) from Field) + 1;
-- insert into Field(id, description, name, path, required, relationship) VALUES
--  (@field_id, 'study to get data from', 'STUDY', 'study',0,NULL );
-- insert into PredicateType_Field(PredicateType_id, fields_id) values (@predicatetype_SUMMARY, @field_id);

-- set @field_id = (select NULLIF(max(id), 0) from Field) + 1;
-- insert into Field(id, description, name, path, required, relationship) VALUES
--  (@field_id, 'table name to import it to', 'TABLE', 'table',0,NULL );
-- insert into PredicateType_Field(PredicateType_id, fields_id) values (@predicatetype_SUMMARY, @field_id);

-- *********************** TEMPLATE PREDICATE ***********************
set @predicatetype_TEMPLATE = (select IFNULL(max(id), 1) from PredicateType) + 1;
insert into PredicateType(id, defaultPredicate, description, displayName, name) VALUES
  (@predicatetype_TEMPLATE, 0, 'Execute a predefined template', 'template', 'TEMPLATE');

insert into PredicateType_dataTypes(PredicateType_id, dataTypes) values
  (@predicatetype_TEMPLATE, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into Resource_PredicateType(Resource_Id, supportedPredicates_id) VALUES
  (@resourceId, @predicatetype_TEMPLATE);

set @field_id = (select NULLIF(max(id), 0) from Field) + 1;
insert into Field(id, description, name, path, required, relationship) VALUES
  (@field_id, 'Value Of Interest', 'ValOfInt', 'value_of_interest',0,NULL );
insert into PredicateType_Field(PredicateType_id, fields_id) values (@predicatetype_TEMPLATE, @field_id);

set @field_id = (select NULLIF(max(id), 0) from Field) + 1;
insert into Field(id, description, name, path, required, relationship) VALUES
  (@field_id, 'Filter Samples Expressions', 'FltrSamplExpr', 'filter_samples_expr',0,NULL );
insert into PredicateType_Field(PredicateType_id, fields_id) values (@predicatetype_TEMPLATE, @field_id);





