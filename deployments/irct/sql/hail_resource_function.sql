CREATE OR REPLACE FUNCTION add_hail_resource(
  resourceName varchar,
  resourceURL varchar
) RETURNS VOID AS $$


DECLARE
  -- SET THE RESOURCE VARIABLES
  resourceId integer := (select COALESCE(max(id), 1) from IRCT_Resource) + 1;
  --resourceName varchar := ( COALESCE(resourceName, '') = '', '{{ resourceName }}', resourceName);
  --resourceURL varchar := ( COALESCE(resourceURL, '') = '', '{{ resourceURL}}', resourceURL);

  -- SET THE RESOURCE PREDICATES
  predicatetype_filter_id integer := (select COALESCE(max(id), 1) from IRCT_PredicateType) + 1;

  -- SET THE FIELDS
  gene_field_id integer := (select COALESCE(max(id), 0) from IRCT_Field) + 1;
  sign_field_id integer := gene_field_id + 1;
  subject_field_id integer := sign_field_id + 1;

BEGIN
  -- INSERT THE RESOURCE VARIABLE
  insert into IRCT_Resource(id, implementingInterface, name, ontologyType) values
    (resourceId, 'edu.harvard.hms.dbmi.bd2k.picsure.ri.LivyHAIL', resourceName, 'TREE');

  -- INSERT THE RESOURCE PARAMETERS
  insert into IRCT_resource_parameters(id, name, value) values(resourceId, 'resourceName', resourceName);
  insert into IRCT_resource_parameters(id, name, value) values(resourceId, 'resourceURL', resourceURL);

  -- INSERT RESOURCE DATA TYPES
  ---- insert into PredicateType_dataTypes(PredicateType_id, dataTypes) values (predicate_type_id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
  --insert into PredicateType_dataTypes(PredicateType_id, dataTypes) values
  --  (predicatetype_filter_id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');

  -- INSERT RESOURCE RELATIONSHIPS

  -- INSERT RESOURCE LogicalOperators


  -- FILTER predicate
  insert into IRCT_PredicateType(id, defaultPredicate, description, displayName, name) values
    (predicatetype_filter_id, false, 'Execute filter predicate', 'filter', 'filter');
  insert into IRCT_Resource_PredicateType(Resource_Id, supportedPredicates_id) values
    (resourceId, predicatetype_filter_id);

  -- INSERT FIELDS
  insert into IRCT_Field(id, description, name, path, required, relationship) values
    (gene_field_id, 'Gene', 'Gene', 'gene',false,NULL);
  insert into IRCT_Field_dataTypes(Field_id, dataTypes)
    values(gene_field_id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
  insert into IRCT_PredicateType_Field(PredicateType_id, fields_id) values
    (predicatetype_filter_id, gene_field_id);

  insert into IRCT_Field(id, description, name, path, required, relationship) values
    (sign_field_id, 'Significance', 'Significance', 'significance',false,NULL);
  insert into IRCT_Field_dataTypes(Field_id, dataTypes)
    values(sign_field_id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
  insert into IRCT_PredicateType_Field(PredicateType_id, fields_id) values
    (predicatetype_filter_id, sign_field_id);

  insert into IRCT_Field(id, description, name, path, required, relationship) values
    (subject_field_id, 'Subject ID', 'Subject ID', 'subject_id',false,NULL);
  insert into IRCT_Field_dataTypes(Field_id, dataTypes)
    values(subject_field_id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
  insert into IRCT_PredicateType_Field(PredicateType_id, fields_id) values
    (predicatetype_filter_id, subject_field_id);

END;
$$ LANGUAGE plpgsql
