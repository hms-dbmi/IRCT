set @resourceName = '<RESOURCE_NAME>';
set @resourceName = 'i2b2-nhanes';
SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM `PredicateType_dataTypes` WHERE PredicateType_id IN (SELECT supportedPredicates_id FROM resource_predicatetype WHERE resource_id IN (SELECT id FROM resource WHERE NAME = @resourceName));
DELETE FROM predicatetype_field WHERE predicatetype_id IN (SELECT supportedPredicates_id FROM resource_predicatetype WHERE resource_id IN (SELECT id FROM resource WHERE name = @resourceName));
DELETE FROM predicatetype_paths WHERE predicatetype_id IN (SELECT supportedPredicates_id FROM resource_predicatetype WHERE resource_id IN (SELECT id FROM resource WHERE name = @resourceName));
DELETE FROM predicatetype WHERE id IN (SELECT supportedPredicates_id FROM resource_predicatetype WHERE resource_id IN (SELECT id FROM resource WHERE name = @resourceName));

DELETE FROM resource_predicatetype WHERE resource_id IN (SELECT id FROM resource WHERE  NAME = @resourceName);  
DELETE FROM resource_field WHERE resource_id IN (SELECT id FROM resource WHERE  NAME = @resourceName);
DELETE FROM resource_relationships WHERE resource_id IN (SELECT id FROM resource WHERE  NAME = @resourceName);                                      
DELETE FROM resource_logicaloperator WHERE id IN (SELECT id FROM   resource WHERE  NAME = @resourceName); 
DELETE FROM resource_datatypes WHERE resource_id IN (SELECT id FROM   resource WHERE  NAME = @resourceName); 
DELETE FROM resource_parameters WHERE id IN (SELECT id FROM resource WHERE  NAME = @resourceName);

DELETE FROM resource WHERE name = @resourceName;

SET FOREIGN_KEY_CHECKS = 1;
                                                            