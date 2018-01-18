set @resourceName = 'hail-dev';

set @resourceImplementingInterface = 'edu.harvard.hms.dbmi.bd2k.picsure.ri.HAIL';
set @resourceURL = 'https://hail-dev.hms.harvard.edu:10001';

-- SET THE RESOURCE VARIABLE
set @resourceId = (select NULLIF(max(id), 1) from Resource) + 1;
insert into Resource(id, implementingInterface, name, ontologyType) VALUES
  (@resourceId, @resourceImplementingInterface, @resourceName, 'TREE');

-- INSERT THE RESOURCE PARAMERTERS
insert into resource_parameters(id, name, value) values(@resourceId, 'resourceName', @resourceName);
insert into resource_parameters(id, name, value) values(@resourceId, 'resourceURL', @resourceURL);
	
-- SET THE RESOURCE PREDICATES
set @predicatetype_SUMMARY = (select max(id) from PredicateType) + 1;
insert into PredicateType(id, defaultPredicate, description, displayName, name) VALUES
  (@predicatetype_SUMMARY, 0, 'Summarize function', 'summary', 'SUMMARY');
	
-- SET THE FIELDS
set @field_STUDY = (select max(id) from Field) + 1;
insert into Field(id, description, name, path, required, relationship) VALUES
  (@field_STUDY, 'study to get data from', 'STUDY', 'study',0,NULL );
set @field_TABLE = (select max(id) from Field) + 1;
insert into Field(id, description, name, path, required, relationship) VALUES
  (@field_TABLE, 'table name to import it to', 'TABLE', 'table',0,NULL );

-- Add the field to the PREDICATE
insert into PredicateType_Field(PredicateType_id, fields_id) VALUES
  (@predicatetype_SUMMARY, @field_STUDY),
  (@predicatetype_SUMMARY, @field_TABLE);

-- Finally add the PREDICATE to the RESOURCE
insert into Resource_PredicateType(Resource_Id, supportedPredicates_id) VALUES
  (@resourceId, @predicatetype_SUMMARY);
