-- SET THE RESOURCE PARAMETERS
set @resourceName = '{{scidbresourcename}}';
set @resourceURL = '{{scidbresourceurl}}';
set @resourceImplementingInterface = 'edu.harvard.hms.dbmi.bd2k.irct.ri.scidb.SciDBResourceImplementation';
set @resourceOntology = 'TREE';

-- SET THE RESOURCE VARIABLE
set @resourceId = (select IF(id is NULL,0,max(id)) from Resource) + 1;

-- SET THE RESOURCE PREDICATES

set @filterId = (select IF(id is NULL,0,max(id)) from PredicateType) + 1;

-- SET THE FIELDS
set @filter_OperatorId = (select IF(id is NULL,0,max(id)) from Field) + 1;
set @filter_ValueId = @filter_operatorId + 1;

-- INSERT THE RESOURCE
insert into Resource(id, implementingInterface, name, ontologyType) values(@resourceId, @resourceImplementingInterface, @resourceName, @resourceOntology);

-- INSERT THE RESOURCE PARAMERTERS
insert into resource_parameters(id, name, value) values(@resourceId, 'resourceName', @resourceName);
insert into resource_parameters(id, name, value) values(@resourceId, 'resourceURL', @resourceURL);

-- INSERT THE RESOURCE DATATYPES
insert into Resource_dataTypes(Resource_id, datatypes) values(@resourceId, 'edu.harvard.hms.dbmi.bd2k.irct.ri.scidb.SciDBDataType:ARRAY');
insert into Resource_dataTypes(Resource_id, datatypes) values(@resourceId, 'edu.harvard.hms.dbmi.bd2k.irct.ri.scidb.SciDBDataType:ATTRIBUTE');
insert into Resource_dataTypes(Resource_id, datatypes) values(@resourceId, 'edu.harvard.hms.dbmi.bd2k.irct.ri.scidb.SciDBDataType:DIMENSION');
insert into Resource_dataTypes(Resource_id, datatypes) values(@resourceId, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into Resource_dataTypes(Resource_id, datatypes) values(@resourceId, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:INTEGER');
insert into Resource_dataTypes(Resource_id, datatypes) values(@resourceId, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:FLOAT');
insert into Resource_dataTypes(Resource_id, datatypes) values(@resourceId, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:DOUBLE');

-- INSERT THE RESOURCE RELATIONSHIPS
-- NONE


-- INSERT FILTER PREDICATE
insert into PredicateType(id, defaultPredicate, description, displayName, name) values(@filterId, 0, 'Filter', 'Filter by Value', 'FILTER');
insert into PredicateType_dataTypes(PredicateType_id, dataTypes) values(@filterId, 'edu.harvard.hms.dbmi.bd2k.irct.ri.scidb.SciDBDataType:ATTRIBUTE');
insert into PredicateType_dataTypes(PredicateType_id, dataTypes) values(@filterId, 'edu.harvard.hms.dbmi.bd2k.irct.ri.scidb.SciDBDataType:DIMENSION');

insert into Field(id, description, name, path, relationship, required) values(@filter_OperatorId, 'Operator', 'Operator', 'OPERATOR', null, 1);
insert into Field_permittedValues(Field_Id, permittedValues) values(@filter_OperatorId, 'EQ');
insert into Field_permittedValues(Field_Id, permittedValues) values(@filter_OperatorId, 'NE');
insert into Field_permittedValues(Field_Id, permittedValues) values(@filter_OperatorId, 'GT');
insert into Field_permittedValues(Field_Id, permittedValues) values(@filter_OperatorId, 'GE');
insert into Field_permittedValues(Field_Id, permittedValues) values(@filter_OperatorId, 'LT');
insert into Field_permittedValues(Field_Id, permittedValues) values(@filter_OperatorId, 'LE');

insert into Field(id, description, name, path, relationship, required) values(@filter_ValueId, 'Value', 'Value', 'VALUE', null, 1);
insert into Field_dataTypes(Field_id, dataTypes) values(@filter_ValueId, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into Field_dataTypes(Field_id, dataTypes) values(@filter_ValueId, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:INTEGER');
insert into Field_dataTypes(Field_id, dataTypes) values(@filter_ValueId, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:FLOAT');
insert into Field_dataTypes(Field_id, dataTypes) values(@filter_ValueId, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:DOUBLE');

insert into PredicateType_Field(PredicateType_id, fields_id) values(@filterId, @filter_OperatorId);
insert into PredicateType_Field(PredicateType_id, fields_id) values(@filterId, @filter_ValueId);

insert into Resource_PredicateType(Resource_Id, supportedPredicates_id) values(@resourceId, @filterId);