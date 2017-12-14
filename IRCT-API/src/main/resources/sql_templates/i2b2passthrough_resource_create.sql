set @resourceName = 'i2b2passthrough-i2b2-org';
set @resourceURL = 'http://services.i2b2.org:9090/i2b2/services/';
set @resourceImplementingInterface = 'edu.harvard.hms.dbmi.bd2k.picsure.ri.I2B2Passthrough';
set @resourceOntology = 'TREE';

-- SET THE RESOURCE VARIABLE
set @resourceId = (SELECT IFNULL(max(id), 1) FROM Resource) + 1;

insert into Resource(id, implementingInterface, name, ontologyType) values(@resourceId, 
	@resourceImplementingInterface, 
	@resourceName, 
	'TREE'
);

INSERT INTO `resource_parameters` (`id`, `value`, `name`)
VALUES
	(@resourceId, @resourceName, 'resourceName'),
	(@resourceId, @resourceURL, 'resourceURL');
