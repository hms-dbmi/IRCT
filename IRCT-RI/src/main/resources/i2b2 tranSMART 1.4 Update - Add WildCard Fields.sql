-- Add new fields to support WildCards with i2b2/tranSMART
set @resourceId = <RESOURCE_ID>;

-- Set the field ids
set @compactFieldId = (select IF(id is NULL,0,max(id)) from Field) + 1;
set @removePrependFieldId = @compactFieldId + 1;

-- Insert the COMPACT field
insert into Field(id, description, name, path, relationship, required) values(@compactFieldId, 'Compact the wildcard columns down to the parent level', 'Compact', 'COMPACT', null, 0);
insert into Field_permittedValues(Field_Id, permittedValues) values(@compactFieldId, 'TRUE');
insert into Field_permittedValues(Field_Id, permittedValues) values(@compactFieldId, 'FALSE');

insert into Resource_field(Resource_id, supportedSelectFields_id) values(@resourceId, @compactFieldId);


-- Insert the REMOVEPREPEND field
insert into Field(id, description, name, path, relationship, required) values(@removePrependFieldId, 'Removes the prepend of the column names', 'Remove Prepend', 'REMOVEPREPEND', null, 0);
insert into Field_permittedValues(Field_Id, permittedValues) values(@removePrependFieldId, 'TRUE');
insert into Field_permittedValues(Field_Id, permittedValues) values(@removePrependFieldId, 'FALSE');

insert into Resource_field(Resource_id, supportedSelectFields_id) values(@resourceId, @removePrependFieldId);