-- SET THE RESOURCE PARAMETERS
set @resourceName = '{{gnome_resource_name}}';
set @resourceURL = '{{gnome_resource_url}}';
set @gnomeUserName = '{{gnome_user_name}}';
set @gnomePassword = '{{gnome_password}}';
set @resourceImplementingInterface = 'edu.harvard.hms.dbmi.bd2k.irct.ri.gnome.GNomeResourceImplementation';
set @resourceOntology = 'TREE';

-- SET THE RESOURCE VARIABLE
set @resourceId = (select NULLIF(max(id), 0) from Resource) + 1;

-- INSERT THE RESOURCE
insert into `Resource`(`id`, `implementingInterface`, `name`, `ontologyType`) values(@resourceId, @resourceImplementingInterface, @resourceName, @resourceOntology);

-- INSERT THE RESOURCE PARAMERTERS
insert into `resource_parameters`(`id`, `name`, `value`) values(@resourceId, 'resourceName', @resourceName);
insert into `resource_parameters`(`id`, `name`, `value`) values(@resourceId, 'resourceRootURL', @resourceURL);
insert into `resource_parameters`(`id`, `name`, `value`) values(@resourceId, 'gnomeUserName', @gnomeUserName);
insert into `resource_parameters`(`id`, `name`, `value`) values(@resourceId, 'gnomePassword', @gnomePassword);

-- INSERT THE RESOURCE DATATYPES
insert into Resource_dataTypes(Resource_id, datatypes) values(@resourceId, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');

-- INSERT FILTER PREDICATE
set @predicate_type_id = (select max(id) from PredicateType) + 1;
insert into PredicateType(id, defaultPredicate, description, displayName, name) values(@predicate_type_id, 0, 'Contains', 'Contains Values', 'CONTAINS');
insert into PredicateType_dataTypes(PredicateType_id, dataTypes) values(@predicate_type_id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into Resource_PredicateType(Resource_Id, supportedPredicates_id) values(@resourceId, @predicate_type_id);

-- SET THE FIELDS
set @field_id = (select max(id) from Field) + 1;
insert into Field(id, description, name, path, relationship, required) values(@field_id, '1st group of samples for comparison', 'project_type_A', 'project_type_A', null, 1);
insert into Field_dataTypes(Field_id, dataTypes) values(@field_id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into PredicateType_Field(PredicateType_id, fields_id) values (@predicate_type_id, @field_id);

set @field_id = (select max(id) from Field) + 1;
insert into Field(id, description, name, path, relationship, required) values(@field_id, '2st group of samples for comparison', 'project_type_B', 'project_type_B', null, 1);
insert into Field_dataTypes(Field_id, dataTypes) values(@field_id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into PredicateType_Field(PredicateType_id, fields_id) values (@predicate_type_id, @field_id);