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
set @predicate_type_id = (select NULLIF(max(id), 0) from PredicateType) + 1;
insert into PredicateType(id, defaultPredicate, description, displayName, name) values(@predicate_type_id, 0, 'Contains', 'Contains Values', 'CONTAINS');
insert into PredicateType_dataTypes(PredicateType_id, dataTypes) values(@predicate_type_id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into Resource_PredicateType(Resource_Id, supportedPredicates_id) values(@resourceId, @predicate_type_id);

-- SET THE FIELDS
set @field_id = (select NULLIF(max(id), 0) from Field) + 1;
insert into Field(id, description, name, path, relationship, required) values(@field_id, '1st group of samples for comparison', 'project_type_A', 'project_type_A', null, 1);
insert into Field_dataTypes(Field_id, dataTypes) values(@field_id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into PredicateType_Field(PredicateType_id, fields_id) values (@predicate_type_id, @field_id);

set @field_id = (select NULLIF(max(id), 0) from Field) + 1;
insert into Field(id, description, name, path, relationship, required) values(@field_id, '2st group of samples for comparison', 'project_type_B', 'project_type_B', null, 1);
insert into Field_dataTypes(Field_id, dataTypes) values(@field_id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into PredicateType_Field(PredicateType_id, fields_id) values (@predicate_type_id, @field_id);

set @field_id = (select NULLIF(max(id), 0) from Field) + 1;
insert into Field(id, description, name, path, relationship, required) values(@field_id, 'Threshold for p-value', 'pvalue', 'pvalue', null, 0);
insert into Field_dataTypes(Field_id, dataTypes) values(@field_id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into PredicateType_Field(PredicateType_id, fields_id) values (@predicate_type_id, @field_id);

set @field_id = (select NULLIF(max(id), 0) from Field) + 1;
insert into Field(id, description, name, path, relationship, required) values(@field_id, 'Minimum # of samples in 1st group with variants', 'Ncase', 'Ncase', null, 0);
insert into Field_dataTypes(Field_id, dataTypes) values(@field_id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into PredicateType_Field(PredicateType_id, fields_id) values (@predicate_type_id, @field_id);

set @field_id = (select NULLIF(max(id), 0) from Field) + 1;
insert into Field(id, description, name, path, relationship, required) values(@field_id, 'Maximum # of samples in 2nd group with variants', 'Ncontrol', 'Ncontrol', null, 0);
insert into Field_dataTypes(Field_id, dataTypes) values(@field_id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into PredicateType_Field(PredicateType_id, fields_id) values (@predicate_type_id, @field_id);

set @field_id = (select NULLIF(max(id), 0) from Field) + 1;
insert into Field(id, description, name, path, relationship, required) values(@field_id, 'Source population to calculate allele frequency', 'ancestry', 'ancestry', null, 0);
insert into Field_dataTypes(Field_id, dataTypes) values(@field_id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'ALL');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'AFR');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'ASN');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'EUR');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'MIXfs');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'MIXfe');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'MIXse');
insert into PredicateType_Field(PredicateType_id, fields_id) values (@predicate_type_id, @field_id);

set @field_id = (select NULLIF(max(id), 0) from Field) + 1;
insert into Field(id, description, name, path, relationship, required) values(@field_id, 'Maximum allele frequency for variants', 'allele_freq', 'allele_freq', null, 0);
insert into Field_dataTypes(Field_id, dataTypes) values(@field_id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'af_common');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'af_less_common');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'af_rare');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'af_na');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'af_novel');
insert into PredicateType_Field(PredicateType_id, fields_id) values (@predicate_type_id, @field_id);

set @field_id = (select NULLIF(max(id), 0) from Field) + 1;
insert into Field(id, description, name, path, relationship, required) values(@field_id, 'Keyword #1 for phenotype', 'Pheno_pri', 'Pheno_pri', null, 0);
insert into Field_dataTypes(Field_id, dataTypes) values(@field_id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into PredicateType_Field(PredicateType_id, fields_id) values (@predicate_type_id, @field_id);

set @field_id = (select NULLIF(max(id), 0) from Field) + 1;
insert into Field(id, description, name, path, relationship, required) values(@field_id, 'Keyword #2 for phenotype', 'Pheno_sec', 'Pheno_sec', null, 0);
insert into Field_dataTypes(Field_id, dataTypes) values(@field_id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into PredicateType_Field(PredicateType_id, fields_id) values (@predicate_type_id, @field_id);

set @field_id = (select NULLIF(max(id), 0) from Field) + 1;
insert into Field(id, description, name, path, relationship, required) values(@field_id, 'Keyword #3 for phenotype', 'Pheno_ter', 'Pheno_ter', null, 0);
insert into Field_dataTypes(Field_id, dataTypes) values(@field_id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into PredicateType_Field(PredicateType_id, fields_id) values (@predicate_type_id, @field_id);

set @field_id = (select NULLIF(max(id), 0) from Field) + 1;
insert into Field(id, description, name, path, relationship, required) values(@field_id, 'Threshold for genes selected by phenotype keywords', 'Pheno_g_rank', 'Pheno_g_rank', null, 0);
insert into Field_dataTypes(Field_id, dataTypes) values(@field_id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into PredicateType_Field(PredicateType_id, fields_id) values (@predicate_type_id, @field_id);

set @field_id = (select NULLIF(max(id), 0) from Field) + 1;
insert into Field(id, description, name, path, relationship, required) values(@field_id, 'Gene annotations to be used for impact prediction', 'gene_model', 'gene_model', null, 0);
insert into Field_dataTypes(Field_id, dataTypes) values(@field_id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'refGene');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'ensGene');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'ccdsGene');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'knownGene');
insert into PredicateType_Field(PredicateType_id, fields_id) values (@predicate_type_id, @field_id);

set @field_id = (select NULLIF(max(id), 0) from Field) + 1;
insert into Field(id, description, name, path, relationship, required) values(@field_id, 'Computational prediction for variant’s impact on gene', 'gene_impact', 'gene_impact', null, 0);
insert into Field_dataTypes(Field_id, dataTypes) values(@field_id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'nonsynonymous');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'LoF');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'LoF_extended');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'disrupt');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'in-frame-deletion');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'in-frame-insertion');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'missense');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'misstart');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'nonsense');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'nonstop');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'synonymous');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'any');
insert into PredicateType_Field(PredicateType_id, fields_id) values (@predicate_type_id, @field_id);

set @field_id = (select NULLIF(max(id), 0) from Field) + 1;
insert into Field(id, description, name, path, relationship, required) values(@field_id, 'Prediction for variant’s impact on protein using SIFT', 'protein_impact_SIFT', 'protein_impact_SIFT', null, 0);
insert into Field_dataTypes(Field_id, dataTypes) values(@field_id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'deleterious');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'neutral');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'any');
insert into PredicateType_Field(PredicateType_id, fields_id) values (@predicate_type_id, @field_id);

set @field_id = (select NULLIF(max(id), 0) from Field) + 1;
insert into Field(id, description, name, path, relationship, required) values(@field_id, 'Prediction for variant’s impact on protein using PolyPhen2', 'protein_impact_PPH2', 'protein_impact_PPH2', null, 0);
insert into Field_dataTypes(Field_id, dataTypes) values(@field_id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'deleterious');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'neutral');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'any');
insert into PredicateType_Field(PredicateType_id, fields_id) values (@predicate_type_id, @field_id);

set @field_id = (select NULLIF(max(id), 0) from Field) + 1;
insert into Field(id, description, name, path, relationship, required) values(@field_id, 'Prediction for variant’s impact on protein using Condel', 'Protein_impact_Condel', 'Protein_impact_Condel', null, 0);
insert into Field_dataTypes(Field_id, dataTypes) values(@field_id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'deleterious');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'neutral');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'any');
insert into PredicateType_Field(PredicateType_id, fields_id) values (@predicate_type_id, @field_id);

set @field_id = (select NULLIF(max(id), 0) from Field) + 1;
insert into Field(id, description, name, path, relationship, required) values(@field_id, 'Whether to select variants reported in HGMD database', 'hgmd', 'hgmd', null, 0);
insert into Field_dataTypes(Field_id, dataTypes) values(@field_id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'yes');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'no');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'any');
insert into PredicateType_Field(PredicateType_id, fields_id) values (@predicate_type_id, @field_id);

set @field_id = (select NULLIF(max(id), 0) from Field) + 1;
insert into Field(id, description, name, path, relationship, required) values(@field_id, 'Whether to select variants reported in GWAS catalog', 'gwas_catalog', 'gwas_catalog', null, 0);
insert into Field_dataTypes(Field_id, dataTypes) values(@field_id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'yes');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'no');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'any');
insert into PredicateType_Field(PredicateType_id, fields_id) values (@predicate_type_id, @field_id);

set @field_id = (select NULLIF(max(id), 0) from Field) + 1;
insert into Field(id, description, name, path, relationship, required) values(@field_id, 'Whether to select variants located on known protein domains', 'protein_domain', 'protein_domain', null, 0);
insert into Field_dataTypes(Field_id, dataTypes) values(@field_id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'yes');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'no');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'any');
insert into PredicateType_Field(PredicateType_id, fields_id) values (@predicate_type_id, @field_id);

set @field_id = (select NULLIF(max(id), 0) from Field) + 1;
insert into Field(id, description, name, path, relationship, required) values(@field_id, 'Whether to select variants located on transcription factor binding site (TFBS)', 'tfbs', 'tfbs', null, 0);
insert into Field_dataTypes(Field_id, dataTypes) values(@field_id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'yes');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'no');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'any');
insert into PredicateType_Field(PredicateType_id, fields_id) values (@predicate_type_id, @field_id);

set @field_id = (select NULLIF(max(id), 0) from Field) + 1;
insert into Field(id, description, name, path, relationship, required) values(@field_id, 'Select variants located on genomic positions with minimum conservation score', 'GERP_score', 'GERP_score', null, 0);
insert into Field_dataTypes(Field_id, dataTypes) values(@field_id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into PredicateType_Field(PredicateType_id, fields_id) values (@predicate_type_id, @field_id);

set @field_id = (select NULLIF(max(id), 0) from Field) + 1;
insert into Field(id, description, name, path, relationship, required) values(@field_id, 'Select variants located on genomic positions with maximum overlapping % with low sequence complexity', 'RMSK_percent', 'RMSK_percent', null, 0);
insert into Field_dataTypes(Field_id, dataTypes) values(@field_id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into PredicateType_Field(PredicateType_id, fields_id) values (@predicate_type_id, @field_id);

set @field_id = (select NULLIF(max(id), 0) from Field) + 1;
insert into Field(id, description, name, path, relationship, required) values(@field_id, 'Minimum variant quality score', 'score', 'score', null, 0);
insert into Field_dataTypes(Field_id, dataTypes) values(@field_id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into PredicateType_Field(PredicateType_id, fields_id) values (@predicate_type_id, @field_id);

set @field_id = (select NULLIF(max(id), 0) from Field) + 1;
insert into Field(id, description, name, path, relationship, required) values(@field_id, 'Select variants by zygosity', 'genotype', 'genotype', null, 0);
insert into Field_dataTypes(Field_id, dataTypes) values(@field_id, 'edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType:STRING');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'any');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'heterozygous');
insert into Field_permittedValues(Field_Id, permittedValues) values(@field_id, 'homozygous');
insert into PredicateType_Field(PredicateType_id, fields_id) values (@predicate_type_id, @field_id);


