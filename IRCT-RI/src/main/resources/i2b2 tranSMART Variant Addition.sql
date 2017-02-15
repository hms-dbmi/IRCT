set @resourceId = null;

set @variantId = (select IF(id is NULL,0,max(id)) from SortOperationType) + 1;
set @geneSymbol_Id = (select IF(id is NULL,0,max(id)) from Field) + 1;

-- INSERT THE SORT OPERATION
insert into SelectOperationType(id, description, displayName, name) values(@variantId, 'Variant', 'Variant', 'VARIANT');

insert into Field(id, description, name, path, relationship, required) values(@geneSymbol_Id, 'Gene Symbol', 'Gene Symbol', 'GENESYMBOL', null, 0);
insert into Field_dataTypes(Field_id, dataTypes) values(@geneSymbol_Id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');

insert into SelectOperationType_Field(SelectOperationType_id, fields_id) values(@variantId, @geneSymbol_Id);

insert into Resource_SelectOperationType(Resource_id, supportedSelectOperations_id) values(@resourceId, @variantId);
