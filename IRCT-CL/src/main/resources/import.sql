--LOAD JOINS
insert into JoinType(id, description, joinImplementation, name, requireFields, requireRelationships) values (1,  'Full Outer Join', 'edu.harvard.hms.dbmi.bd2k.irct.action.join.FullOuterJoin', 'Full Outer Join', 1, 0);
insert into JoinType(id, description, joinImplementation, name, requireFields, requireRelationships) values (2,  'Inner Join', 'edu.harvard.hms.dbmi.bd2k.irct.action.join.InnerJoin', 'Inner Join', 1, 0);
insert into JoinType(id, description, joinImplementation, name, requireFields, requireRelationships) values (3,  'Left Outer Join', 'edu.harvard.hms.dbmi.bd2k.irct.action.join.LeftOuterJoin', 'Left Outer Join', 1, 0);
insert into JoinType(id, description, joinImplementation, name, requireFields, requireRelationships) values (4,  'Right Outer Join', 'edu.harvard.hms.dbmi.bd2k.irct.action.join.RightOuterJoin', 'Right Outer Join', 1, 0);

--LOAD RESOURCES
insert into Resource(id, implementingInterface, name, ontologyType) values (1, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.FakeResourceImplementationDOB', 'Fake Resource DOB', 'FLAT');
insert into Resource(id, implementingInterface, name, ontologyType) values (2, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.FakeResourceImplementationName', 'Fake Resource Name', 'FLAT');