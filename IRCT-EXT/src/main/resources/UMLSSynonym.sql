set @umlsSynonymBeforeFindId = (select IF(max(id) is NULL,0, max(id)) from EventConverterImplementation) + 1;

insert into EventConverterImplementation(id, eventListener, name) values(@umlsSynonymBeforeFindId,'edu.harvard.hms.dbmi.bd2k.irct.findtools.event.find.UMLSSynonymBeforeFind','UMLS Synonym Before Find');

insert into event_parameters(id, name, value) values(@umlsSynonymBeforeFindId, 'jndi', 'java:jboss/datasources/umls');