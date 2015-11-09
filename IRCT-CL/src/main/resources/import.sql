--LOAD JOINS
insert into JoinType(id, description, joinImplementation, name, requireFields, requireRelationships) values (1,  'Full Outer Join', 'edu.harvard.hms.dbmi.bd2k.irct.action.join.FullOuterJoin', 'Full Outer Join', 1, 0);
insert into JoinType(id, description, joinImplementation, name, requireFields, requireRelationships) values (2,  'Inner Join', 'edu.harvard.hms.dbmi.bd2k.irct.action.join.InnerJoin', 'Inner Join', 1, 0);
insert into JoinType(id, description, joinImplementation, name, requireFields, requireRelationships) values (3,  'Left Outer Join', 'edu.harvard.hms.dbmi.bd2k.irct.action.join.LeftOuterJoin', 'Left Outer Join', 1, 0);
insert into JoinType(id, description, joinImplementation, name, requireFields, requireRelationships) values (4,  'Right Outer Join', 'edu.harvard.hms.dbmi.bd2k.irct.action.join.RightOuterJoin', 'Right Outer Join', 1, 0);

--LOAD RESOURCES
insert into Resource(id, implementingInterface, name, ontologyType) values (1, 'edu.harvard.hms.dbmi.bd2k.irct.ri.fakeResource.FakeResourceImplementationDOB', 'Fake Resource DOB', 'FLAT');
insert into Resource(id, implementingInterface, name, ontologyType) values (2, 'edu.harvard.hms.dbmi.bd2k.irct.ri.fakeResource.FakeResourceImplementationName', 'Fake Resource Name', 'FLAT');
insert into Resource(id, implementingInterface, name, ontologyType) values (3, 'edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.I2B2XMLResourceImplementation', 'i2b2 Demo', 'TREE');
insert into Resource(id, implementingInterface, name, ontologyType) values (4, 'edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2transmart.I2B2TranSMARTResourceImplementation', 'tranSMART Demo', 'TREE');
insert into Resource(id, implementingInterface, name, ontologyType) values (5, 'edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2transmart.I2B2TranSMARTResourceImplementation', 'PICI SIMONS DEV', 'TREE');


--LOAD PARAMETERS
insert into Resource_Parameters(id, name, value) values (3, 'i2b2username', 'demo');
insert into Resource_Parameters(id, name, value) values (3, 'i2b2domain', 'i2b2demo');
insert into Resource_Parameters(id, name, value) values (3, 'i2b2password', 'demouser');
insert into Resource_Parameters(id, name, value) values (3, 'i2b2projectID', 'Demo');
insert into Resource_Parameters(id, name, value) values (3, 'serverName', 'i2b2demo');
insert into Resource_Parameters(id, name, value) values (3, 'ONTConnectionURL', 'http://192.168.56.101:9090/i2b2/services/OntologyService');
insert into Resource_Parameters(id, name, value) values (3, 'CRCConnectionURL', 'http://192.168.56.101:9090/i2b2/services/QueryToolService');
insert into Resource_Parameters(id, name, value) values (3, 'PMConnectionURL', 'http://192.168.56.101:9090/i2b2/services/PMService');

insert into Resource_Parameters(id, name, value) values (4, 'tranSMARTusername', 'admin');
insert into Resource_Parameters(id, name, value) values (4, 'tranSMARTpassword', 'SuperAdmin');
insert into Resource_Parameters(id, name, value) values (4, 'i2b2domain', 'i2b2demo');
insert into Resource_Parameters(id, name, value) values (4, 'i2b2username', 'Demo');
insert into Resource_Parameters(id, name, value) values (4, 'i2b2password', 'demouser');
insert into Resource_Parameters(id, name, value) values (4, 'i2b2projectID', 'Demo');
insert into Resource_Parameters(id, name, value) values (4, 'baseURL', 'https://avl-dev-app.dbmi.hms.harvard.edu/transmart');
insert into Resource_Parameters(id, name, value) values (4, 'serverName', 'avl-transmartDemo');
insert into Resource_Parameters(id, name, value) values (4, 'ONTConnectionURL', 'https://avl-dev-app.dbmi.hms.harvard.edu/transmart/proxy?url=http://localhost:9090/i2b2/services/OntologyService');
insert into Resource_Parameters(id, name, value) values (4, 'CRCConnectionURL', 'https://avl-dev-app.dbmi.hms.harvard.edu/transmart/proxy?url=http://localhost:9090/i2b2/services/QueryToolService');
insert into Resource_Parameters(id, name, value) values (4, 'PMConnectionURL', 'https://avl-dev-app.dbmi.hms.harvard.edu/transmart/proxy?url=http://localhost:9090/i2b2/services/PMService/getServices');

insert into Resource_Parameters(id, name, value) values (5, 'tranSMARTusername', 'je72');
insert into Resource_Parameters(id, name, value) values (5, 'tranSMARTpassword', 'UnHashed$$');
insert into Resource_Parameters(id, name, value) values (5, 'i2b2domain', 'i2b2demo');
insert into Resource_Parameters(id, name, value) values (5, 'i2b2username', 'Demo');
insert into Resource_Parameters(id, name, value) values (5, 'i2b2password', 'demouser');
insert into Resource_Parameters(id, name, value) values (5, 'i2b2projectID', 'Demo');
insert into Resource_Parameters(id, name, value) values (5, 'baseURL', 'https://pici-simons-dev-app.dbmi.hms.harvard.edu/transmart');
insert into Resource_Parameters(id, name, value) values (5, 'serverName', 'PICI-SIMONS-DEV');
insert into Resource_Parameters(id, name, value) values (5, 'ONTConnectionURL', 'https://pici-simons-dev-app.dbmi.hms.harvard.edu/transmart/proxy?url=http://localhost:9090/i2b2/rest/OntologyService');
insert into Resource_Parameters(id, name, value) values (5, 'CRCConnectionURL', 'https://pici-simons-dev-app.dbmi.hms.harvard.edu/transmart/proxy?url=http://localhost:9090/i2b2/rest/QueryToolService');
insert into Resource_Parameters(id, name, value) values (5, 'PMConnectionURL', 'https://pici-simons-dev-app.dbmi.hms.harvard.edu/transmart/proxy?url=http://localhost:9090/i2b2/services/PMService/getServices');

--LOAD i2b2 PREDICATES
insert PredicateType(id, name, description, implementingInterface, defaultPredicate) values(1, 'CONTAINS', 'CONTAINS', null, 1);
insert into Resource_PredicateType(resource_id, supportedPredicates_id) values(3, 1);

insert into PredicateType(id, name, description, implementingInterface, defaultPredicate) values(2, 'CONSTRAIN_MODIFIER', 'Constrain by modifier', null, 0);
insert into Resource_PredicateType(resource_id, supportedPredicates_id) values(3, 2);

insert into PredicateTypeValue(id, name, required) values(1, 'modifier_key', 1);
insert into PredicateType_values(id, name, values_id) values(2, 'modifier_key', 1);

insert into PredicateType(id, name, description, implementingInterface, defaultPredicate) values(3, 'CONSTRAIN_VALUE', 'Constrain by value', null, 0);
insert into Resource_PredicateType(resource_id, supportedPredicates_id) values(3, 3);

insert into PredicateTypeValue(id, name, required) values(2, 'value_operator', 1);
insert into PredicateTypeValue_permittedValues(PredicateTypeValue_id, permittedValues) values(2, "EQ");
insert into PredicateTypeValue_permittedValues(PredicateTypeValue_id, permittedValues) values(2, "NE");
insert into PredicateTypeValue_permittedValues(PredicateTypeValue_id, permittedValues) values(2, "GT");
insert into PredicateTypeValue_permittedValues(PredicateTypeValue_id, permittedValues) values(2, "GE");
insert into PredicateTypeValue_permittedValues(PredicateTypeValue_id, permittedValues) values(2, "LT");
insert into PredicateTypeValue_permittedValues(PredicateTypeValue_id, permittedValues) values(2, "LE");

insert into PredicateTypeValue(id, name, required) values(3, 'value_constraint', 1);
insert into PredicateTypeValue(id, name, required) values(4, 'value_unit_of_measure', 0);
insert into PredicateTypeValue(id, name, required) values(5, 'value_type', 1);
insert into PredicateTypeValue_permittedValues(PredicateTypeValue_id, permittedValues) values(5, "TEXT");
insert into PredicateTypeValue_permittedValues(PredicateTypeValue_id, permittedValues) values(5, "LARGETEX");
insert into PredicateTypeValue_permittedValues(PredicateTypeValue_id, permittedValues) values(5, "NUMBER");
insert into PredicateTypeValue_permittedValues(PredicateTypeValue_id, permittedValues) values(5, "FLAG");
insert into PredicateTypeValue_permittedValues(PredicateTypeValue_id, permittedValues) values(5, "MODIFIER");

insert into PredicateType_values(id, name, values_id) values(3, 'value_operator', 2);
insert into PredicateType_values(id, name, values_id) values(3, 'value_constraint', 3);
insert into PredicateType_values(id, name, values_id) values(3, 'value_unit_of_measure', 4);
insert into PredicateType_values(id, name, values_id) values(3, 'value_type', 5);

insert into PredicateType(id, name, description, implementingInterface, defaultPredicate) values(4, 'CONSTRAIN_DATE', 'Constrain by date', null, 0);
insert into Resource_PredicateType(resource_id, supportedPredicates_id) values(3, 4);

insert into PredicateTypeValue(id, name, required) values(6, 'value_from_inclusive', 1);
insert into PredicateTypeValue_permittedValues(PredicateTypeValue_id, permittedValues) values(6, "YES");
insert into PredicateTypeValue_permittedValues(PredicateTypeValue_id, permittedValues) values(6, "NO");

insert into PredicateTypeValue(id, name, required) values(7, 'value_from_time', 1);
insert into PredicateTypeValue_permittedValues(PredicateTypeValue_id, permittedValues) values(7, "START_DATE");
insert into PredicateTypeValue_permittedValues(PredicateTypeValue_id, permittedValues) values(7, "END_DATE");

insert into PredicateTypeValue(id, name, required) values(8, 'value_from_date', 1);
insert into PTV_SDT(id, supportedDataType) values(8, "DATE");

insert into PredicateTypeValue(id, name, required) values(9, 'value_to_inclusive', 1);
insert into PredicateTypeValue_permittedValues(PredicateTypeValue_id, permittedValues) values(9, "YES");
insert into PredicateTypeValue_permittedValues(PredicateTypeValue_id, permittedValues) values(9, "NO");

insert into PredicateTypeValue(id, name, required) values(10, 'value_to_time', 1);
insert into PredicateTypeValue_permittedValues(PredicateTypeValue_id, permittedValues) values(10, "START_DATE");
insert into PredicateTypeValue_permittedValues(PredicateTypeValue_id, permittedValues) values(10, "END_DATE");

insert into PredicateTypeValue(id, name, required) values(11, 'value_to_date', 1);
insert into PTV_SDT(id, supportedDataType) values(11, "DATE");

insert into PredicateType_values(id, name, values_id) values(4, 'value_from_inclusive', 6);
insert into PredicateType_values(id, name, values_id) values(4, 'value_from_time', 7);
insert into PredicateType_values(id, name, values_id) values(4, 'value_from_date', 8);
insert into PredicateType_values(id, name, values_id) values(4, 'value_to_inclusive', 9);
insert into PredicateType_values(id, name, values_id) values(4, 'value_to_time', 10);
insert into PredicateType_values(id, name, values_id) values(4, 'value_to_date', 11);

--LOAD i2b2/tranSMART PREDICATES

--CONTAINS
insert into PredicateType(id, name, description, implementingInterface, defaultPredicate) values(5, 'CONTAINS', 'CONTAINS', null, 1);
insert into Resource_PredicateType(resource_id, supportedPredicates_id) values(4, 5);

--CONSTRAINT MODIFIER
insert into PredicateType(id, name, description, implementingInterface, defaultPredicate) values(6, 'CONSTRAIN_MODIFIER', 'Constrain by modifier', null, 1);
insert into Resource_PredicateType(resource_id, supportedPredicates_id) values(4, 6);

insert into PredicateTypeValue(id, name, required) values(12, 'modifier_key', 1);
insert into PredicateType_values(id, name, values_id) values(6, 'modifier_key', 12);

--CONSTRAIN VALUE


-- LOAD i2b2/tranSMART PICI SIMONS PREDICATED

--CONTAINS
insert into PredicateType(id, name, description, implementingInterface, defaultPredicate) values(9, 'CONTAINS', 'CONTAINS', null, 1);
insert into Resource_PredicateType(resource_id, supportedPredicates_id) values(5, 9);
