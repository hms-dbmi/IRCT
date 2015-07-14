--LOAD JOINS
insert into JoinType(id, description, joinImplementation, name, requireFields, requireRelationships) values (1,  'Full Outer Join', 'edu.harvard.hms.dbmi.bd2k.irct.action.join.FullOuterJoin', 'Full Outer Join', 1, 0);
insert into JoinType(id, description, joinImplementation, name, requireFields, requireRelationships) values (2,  'Inner Join', 'edu.harvard.hms.dbmi.bd2k.irct.action.join.InnerJoin', 'Inner Join', 1, 0);
insert into JoinType(id, description, joinImplementation, name, requireFields, requireRelationships) values (3,  'Left Outer Join', 'edu.harvard.hms.dbmi.bd2k.irct.action.join.LeftOuterJoin', 'Left Outer Join', 1, 0);
insert into JoinType(id, description, joinImplementation, name, requireFields, requireRelationships) values (4,  'Right Outer Join', 'edu.harvard.hms.dbmi.bd2k.irct.action.join.RightOuterJoin', 'Right Outer Join', 1, 0);

--LOAD RESOURCES
insert into Resource(id, implementingInterface, name, ontologyType) values (1, 'edu.harvard.hms.dbmi.bd2k.irct.ri.fakeResource.FakeResourceImplementationDOB', 'Fake Resource DOB', 'FLAT');
insert into Resource(id, implementingInterface, name, ontologyType) values (2, 'edu.harvard.hms.dbmi.bd2k.irct.ri.fakeResource.FakeResourceImplementationName', 'Fake Resource Name', 'FLAT');
insert into Resource(id, implementingInterface, name, ontologyType) values (3, 'edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.I2B2XMLResourceImplementation', 'i2b2 Demo', 'TREE');


--LOAD PARAMETERS

insert into Resource_Parameters(id, name, value) values (3, 'username', 'demo');
insert into Resource_Parameters(id, name, value) values (3, 'domain', 'i2b2demo');
insert into Resource_Parameters(id, name, value) values (3, 'password', 'demouser');
insert into Resource_Parameters(id, name, value) values (3, 'ONTConnectionURL', 'http://192.168.56.101:9090/i2b2/services/OntologyService');
insert into Resource_Parameters(id, name, value) values (3, 'projectID', 'Demo');
insert into Resource_Parameters(id, name, value) values (3, 'serverName', 'i2b2demo');