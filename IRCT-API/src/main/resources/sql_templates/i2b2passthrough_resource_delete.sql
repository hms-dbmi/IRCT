set @resourceName = 'i2b2passthrough-i2b2-org';

set @resourceId = (SELECT id FROM Resource WHERE name = @resourceName);

DELETE FROM resource_parameter WHERE id = @resourceId;

DELETE FROM resource WHERE name = @resourceName;