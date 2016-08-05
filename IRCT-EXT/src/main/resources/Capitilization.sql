set @capitilizationBeforeFindId = (select IF(max(id) is NULL,0, max(id)) from EventConverterImplementation) + 1;

insert into EventConverterImplementation(id, eventListener, name) values(@capitilizationBeforeFindId,'edu.harvard.hms.dbmi.bd2k.irct.findtools.event.find.CapitilizationBeforeFind','Capitilization Before Find');

insert into event_parameters(id, name, value) values(@capitilizationBeforeFindId, 'allCaps', 'true');
insert into event_parameters(id, name, value) values(@capitilizationBeforeFindId, 'allLowerCase', 'true');
insert into event_parameters(id, name, value) values(@capitilizationBeforeFindId, 'wordCapitilization', 'true');