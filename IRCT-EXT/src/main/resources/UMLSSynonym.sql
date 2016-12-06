set @umlsSynonymBeforeFindId = (select IF(max(id) is NULL,0, max(id)) from EventConverterImplementation) + 1;

insert into EventConverterImplementation(id, eventListener, name) values(@umlsSynonymBeforeFindId,'edu.harvard.hms.dbmi.bd2k.irct.findtools.event.find.UMLSSynonymBeforeFind','UMLS Synonym Before Find');

insert into event_parameters(id, name, value) values(@umlsSynonymBeforeFindId, 'jdbcDriverName', 'DRIVER');
insert into event_parameters(id, name, value) values(@umlsSynonymBeforeFindId, 'connectionString', 'CONNECTION STRING');
insert into event_parameters(id, name, value) values(@umlsSynonymBeforeFindId, 'username', 'USERNAME');
insert into event_parameters(id, name, value) values(@umlsSynonymBeforeFindId, 'password', 'PASSWORD');
insert into event_parameters(id, name, value) values(@umlsSynonymBeforeFindId, 'storedSynByPTProcedure', 'SYNONYM BY PT PROCEDURE');
insert into event_parameters(id, name, value) values(@umlsSynonymBeforeFindId, 'storedSynByPTSABProcedure', 'SYNONYM BY PT WITH SAB PROCEDURE');
insert into event_parameters(id, name, value) values(@umlsSynonymBeforeFindId, 'newTermColumn', 'RETURN COLUMN');