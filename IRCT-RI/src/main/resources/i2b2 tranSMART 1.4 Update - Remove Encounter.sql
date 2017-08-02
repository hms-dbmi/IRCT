-- Delete fields that are no longer needed in where clauses
delete PredicateType_Field from PredicateType_Field
	inner join PredicateType on PredicateType_Field.PredicateType_id=PredicateType.id 
	where PredicateType.name="CONTAINS";
    
delete from Field where path="ENOUTER";

delete PredicateType_Field from Field
	inner join PredicateType_Field on PredicateType_Field.fields_id=Field.id
    inner join PredicateType on PredicateType_Field.PredicateType_id=PredicateType.id
    where Field.path="ENOUTER";

delete PredicateType_Field from Field
	inner join PredicateType_Field on PredicateType_Field.fields_id=Field.id
    inner join PredicateType on PredicateType_Field.PredicateType_id=PredicateType.id
    where Field.path="ENOUNTER";
    
delete Field_permittedValues from Field
	inner join Field_permittedValues on Field_permittedValues.Field_id = Field.id
    where Field.path="ENOUNTER";
    
delete from Field where path="ENOUNTER";
