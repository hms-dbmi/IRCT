-- SET THE RESOURCE PARAMETERS
set @resourceName = '{{scidbresourcename}}';
set @resourceURL = '{{scidbresourceurl}}';
set @resourceImplementingInterface = 'edu.harvard.hms.dbmi.bd2k.irct.ri.scidb.SciDBResourceImplementation';
set @resourceOntology = 'TREE';

-- SET THE RESOURCE VARIABLE
set @resourceId = (select IF(id is NULL,0,max(id)) from Resource) + 1;

-- SET THE RESOURCE PREDICATES
set @filterId = (select IF(id is NULL,0,max(id)) from PredicateType) + 1;

-- SET THE SELECT OPERATIONS
set @aggregateId = (select IF(id is NULL,0,max(id)) from SelectOperationType) + 1;
set @betweenId = @aggregateId + 1;
-- SET THE JOIN OPERATIONS
set @crossJoinID = (select IF(id is NULL,0,max(id)) from JoinType) + 1;

-- SET THE SORT OPERATION
set @sortId = (select IF(id is NULL,0,max(id)) from SortOperationType) + 1;

-- SET THE FIELDS
set @filter_OperatorId = (select IF(id is NULL,0,max(id)) from Field) + 1;
set @filter_ValueId = @filter_operatorId + 1;

set @aggregate_Function_Id = @filter_ValueId + 1;
set @aggregate_Dimension_Id = @aggregate_Function_Id + 1;

set @crossJoin_RIGHT_ID = @aggregate_Dimension_Id + 1;
set @crossJoin_DIMMENSION_Id = @crossJoin_RIGHT_ID + 1;
set @crossJoin_LEFTALIAS_ID = @crossJoin_DIMMENSION_Id + 1;
set @crossJoin_RIGHTALIAS_ID = @crossJoin_LEFTALIAS_ID + 1;

set @sort_Direction_Id = @crossJoin_RIGHTALIAS_ID + 1;

set @lowBound_BetweenId = @sort_Direction_Id + 1;
set @highBound_BetweenId = @lowBound_BetweenId + 1;




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

-- INSERT BETWEEN PREDICATE
insert into PredicateType(id, defaultPredicate, description, displayName, name) values(@betweenId, 0, 'Between', 'Between', 'BETWEEN');
insert into PredicateType_dataTypes(PredicateType_id, dataTypes) values(@betweenId, 'edu.harvard.hms.dbmi.bd2k.irct.ri.scidb.SciDBDataType:ATTRIBUTE');
insert into PredicateType_dataTypes(PredicateType_id, dataTypes) values(@betweenId, 'edu.harvard.hms.dbmi.bd2k.irct.ri.scidb.SciDBDataType:DIMENSION');

insert into Field(id, description, name, path, relationship, required) values(@lowBound_BetweenId, 'Lower Bound', 'Low Bound', 'LOWBOUNDS', null, 1);
insert into Field_dataTypes(Field_id, dataTypes) values(@lowBound_BetweenId, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into Field_dataTypes(Field_id, dataTypes) values(@lowBound_BetweenId, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:ARRAY');

insert into Field(id, description, name, path, relationship, required) values(@highBound_BetweenId, 'Higher Bound', 'High Bound', 'HIGHBOUNDS', null, 1);
insert into Field_dataTypes(Field_id, dataTypes) values(@highBound_BetweenId, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into Field_dataTypes(Field_id, dataTypes) values(@highBound_BetweenId, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:ARRAY');

insert into PredicateType_Field(PredicateType_id, fields_id) values(@betweenId, @lowBound_BetweenId);
insert into PredicateType_Field(PredicateType_id, fields_id) values(@betweenId, @highBound_BetweenId);

insert into Resource_PredicateType(Resource_Id, supportedPredicates_id) values(@resourceId, @betweenId);


-- INSERT Select Aggregate Operation
insert into SelectOperationType(id, name, displayName, description) values(@aggregateId, 'AGGREGATE', 'Aggregate', 'A set of aggregate functions that can be run');
insert into SelectOperationType_dataTypes(SelectOperationType_id, dataTypes) values(@aggregateId, 'edu.harvard.hms.dbmi.bd2k.irct.ri.scidb.SciDBDataType:ATTRIBUTE');
insert into SelectOperationType_dataTypes(SelectOperationType_id, dataTypes) values(@aggregateId, 'edu.harvard.hms.dbmi.bd2k.irct.ri.scidb.SciDBDataType:DIMENSION');

insert into Field(id, description, name, path, relationship, required) values(@aggregate_Function_Id, 'Aggregate Function', 'Function', 'FUNCTION', null, 1);
insert into Field_permittedValues(Field_Id, permittedValues) values(@aggregate_Function_Id, 'count');
insert into Field_permittedValues(Field_Id, permittedValues) values(@aggregate_Function_Id, 'approxdc');
insert into Field_permittedValues(Field_Id, permittedValues) values(@aggregate_Function_Id, 'avg');
insert into Field_permittedValues(Field_Id, permittedValues) values(@aggregate_Function_Id, 'var');
insert into Field_permittedValues(Field_Id, permittedValues) values(@aggregate_Function_Id, 'stdev');
insert into Field_permittedValues(Field_Id, permittedValues) values(@aggregate_Function_Id, 'mad');
insert into Field_permittedValues(Field_Id, permittedValues) values(@aggregate_Function_Id, 'min');
insert into Field_permittedValues(Field_Id, permittedValues) values(@aggregate_Function_Id, 'max');
insert into Field_permittedValues(Field_Id, permittedValues) values(@aggregate_Function_Id, 'median');
insert into Field_permittedValues(Field_Id, permittedValues) values(@aggregate_Function_Id, 'first_value');
insert into Field_permittedValues(Field_Id, permittedValues) values(@aggregate_Function_Id, 'last_value');
insert into Field_permittedValues(Field_Id, permittedValues) values(@aggregate_Function_Id, 'sum');
insert into Field_permittedValues(Field_Id, permittedValues) values(@aggregate_Function_Id, 'prod');
insert into SelectOperationType_Field(SelectOperationType_id, fields_id) values(@aggregateId, @aggregate_Function_Id);

insert into Field(id, description, name, path, relationship, required) values(@aggregate_Dimension_Id, 'Aggregate Dimension', 'Dimension', 'DIMENSION', null, 0);
insert into Field_dataTypes(Field_id, dataTypes) values(@aggregate_Dimension_Id, 'edu.harvard.hms.dbmi.bd2k.irct.ri.scidb.SciDBDataType:DIMENSION');
insert into SelectOperationType_Field(SelectOperationType_id, fields_id) values(@aggregateId, @aggregate_Dimension_Id);

insert into Resource_SelectOperationType(Resource_Id, supportedSelectOperations_id) values(@resourceId, @aggregateId);

-- INSERT THE Cross Join
insert into JoinType(id, description, displayName, name) values(@crossJoinID, 'Performs a cross-product join with equality predicates.', 'Cross Join', 'CROSSJOIN');
insert into JoinType_dataTypes(JoinType_id, dataTypes) values(@crossJoinID, 'edu.harvard.hms.dbmi.bd2k.irct.ri.scidb.SciDBDataType:ATTRIBUTE');
insert into JoinType_dataTypes(JoinType_id, dataTypes) values(@crossJoinID, 'edu.harvard.hms.dbmi.bd2k.irct.ri.scidb.SciDBDataType:DIMENSION');

insert into Field(id, description, name, path, relationship, required) values(@crossJoin_RIGHT_ID, 'Right Query', 'Right', 'RIGHT', null, 1);
insert into Field_dataTypes(Field_id, dataTypes) values(@crossJoin_RIGHT_ID, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:SUBQUERY');
insert into JoinType_Field(JoinType_id, fields_id) values(@crossJoinId, @crossJoin_RIGHT_ID);

insert into Field(id, description, name, path, relationship, required) values(@crossJoin_DIMMENSION_Id, 'DIMENSIONS', 'Dimensions', 'DIMENSIONS', null, 1);
insert into Field_dataTypes(Field_id, dataTypes) values(@crossJoin_DIMMENSION_Id, 'edu.harvard.hms.dbmi.bd2k.irct.ri.scidb.SciDBDataType:DIMENSION');
insert into Field_dataTypes(Field_id, dataTypes) values(@crossJoin_DIMMENSION_Id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:ARRAY');
insert into JoinType_Field(JoinType_id, fields_id) values(@crossJoinId, @crossJoin_DIMMENSION_Id);


insert into Field(id, description, name, path, relationship, required) values(@crossJoin_LEFTALIAS_ID, 'Left Alias', 'Left Alias', 'LEFT_ALIAS', null, 0);
insert into Field_dataTypes(Field_id, dataTypes) values(@crossJoin_LEFTALIAS_ID, 'edu.harvard.hms.dbmi.bd2k.irct.ri.scidb.SciDBDataType:DIMENSION');
insert into JoinType_Field(JoinType_id, fields_id) values(@crossJoinId, @crossJoin_LEFTALIAS_ID);

insert into Field(id, description, name, path, relationship, required) values(@crossJoin_RIGHTALIAS_ID, 'Right Alias', 'Right Alias', 'RIGHT_ALIAS', null, 0);
insert into Field_dataTypes(Field_id, dataTypes) values(@crossJoin_DIMMENSION_Id, 'edu.harvard.hms.dbmi.bd2k.irct.ri.scidb.SciDBDataType:DIMENSION');
insert into JoinType_Field(JoinType_id, fields_id) values(@crossJoinId, @crossJoin_RIGHTALIAS_ID);


insert into Resource_JoinType(Resource_Id, supportedJoins_id) values(@resourceId, @crossJoinID);

-- INSERT THE SORT OPERATION
insert into SortOperationType(id, description, displayName, name) values(@sortId, 'Sort', 'Sort', 'SORT');

insert into Field(id, description, name, path, relationship, required) values(@sort_Direction_Id, 'Direction', 'Direction', 'DIRECTION', null, 0);
insert into Field_permittedValues(Field_id, permittedValues) values(@sort_Direction_Id, 'DESC');
insert into Field_permittedValues(Field_id, permittedValues) values(@sort_Direction_Id, 'ASC');

insert into SortOperationType_Field(SortOperationType_id, fields_id) values(@sortId, @sort_Direction_Id);

insert into Resource_SortOperationType(Resource_id, supportedSortOperations_id) values(@resourceId, @sortId);
