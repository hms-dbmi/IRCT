-- SET THE RESOURCE PARAMETERS
set @resourceName = '{{gnome_resource_name}}';
set @resourceURL = '{{gnome_resource_url}}';
set @gnomeUserName = '{{gnome_user_name}}';
set @gnomePassword = '{{gnome_password}}';
set @resourceImplementingInterface = 'edu.harvard.hms.dbmi.bd2k.irct.ri.gnome.GNome.GNomeResourceImplementation';
set @resourceOntology = 'TREE';

-- SET THE RESOURCE VARIABLE
set @resourceId = (select NULLIF(max(id), 0) from Resource) + 1;

-- INSERT THE RESOURCE
insert into `Resource`(`id`, `implementingInterface`, `name`, `ontologyType`) values(@resourceId, @resourceImplementingInterface, @resourceName, @resourceOntology);

-- INSERT THE RESOURCE PARAMERTERS
insert into `resource_parameters`(`id`, `name`, `value`) values(@resourceId, 'resourceName', @resourceName);
insert into `resource_parameters`(`id`, `name`, `value`) values(@resourceId, 'resourceURL', @resourceURL);
insert into `resource_parameters`(`id`, `name`, `value`) values(@resourceId, 'gnomeUserName', @gnomeUserName);
insert into `resource_parameters`(`id`, `name`, `value`) values(@resourceId, 'gnomePassword', @gnomePassword);
