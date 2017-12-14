INSERT INTO `DataConverterImplementation` (`id`, `dataConverter`, `format`, `resultDataType`)
VALUES
	(1, 'edu.harvard.hms.dbmi.bd2k.irct.ws.rs.resultconverter.JSONTabularDataConverter', 'JSON', 'TABULAR'),
	(2, 'edu.harvard.hms.dbmi.bd2k.irct.ws.rs.resultconverter.XMLTabularDataConverter', 'XML', 'TABULAR'),
	(3, 'edu.harvard.hms.dbmi.bd2k.irct.ws.rs.resultconverter.XSLXTabularDataConverter', 'XLSX', 'TABULAR'),
	(4, 'edu.harvard.hms.dbmi.bd2k.irct.ws.rs.resultconverter.CSVTabularDataConverter', 'CSV', 'TABULAR');
