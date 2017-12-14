-- This is a start for altering tables to maintain referencial integrity, but
-- this is outside of the EntityManager

ALTER TABLE field_datatypes DROP FOREIGN KEY `FKjilu9ox5c6ud3ju87qyjlwchg`;  
ALTER TABLE field_datatypes ADD CONSTRAINT `FKjilu9ox5c6ud3ju87qyjlwchg` FOREIGN KEY (`Field_id`) REFERENCES `Field` (`id`) ON DELETE CASCADE;

ALTER TABLE field_permittedvalues DROP FOREIGN KEY `FK2uhi59su80u87juu34dhx4xna`;
ALTER TABLE field_permittedvalues ADD CONSTRAINT `FK2uhi59su80u87juu34dhx4xna` FOREIGN KEY (`Field_id`) REFERENCES `Field` (`id`)

ALTER TABLE predicatetype_field DROP FOREIGN KEY `FKm8n78jwv4qemmeqhdcxdfv2kk`;
ALTER TABLE predicatetype_field ADD CONSTRAINT `FKm8n78jwv4qemmeqhdcxdfv2kk` FOREIGN KEY (`fields_id`) REFERENCES `Field` (`id`) ON DELETE CASCADE;