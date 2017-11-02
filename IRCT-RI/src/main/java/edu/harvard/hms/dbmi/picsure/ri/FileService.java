package edu.harvard.hms.dbmi.picsure.ri;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.log4j.Logger;

import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.find.FindInformationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Entity;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.WhereClause;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ResourceState;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.PathResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.QueryResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultStatus;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.Column;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.ResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;

public class FileService implements
		QueryResourceImplementationInterface,
		PathResourceImplementationInterface {
	
	private String resourceName;
	private String baseDir;
	private ResourceState resourceState;
	private File baseFilePath;
	
	private Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void setup(Map<String, String> parameters)
			throws ResourceInterfaceException {
		String[] strArray = { "resourceName", "baseDir" };
		if (!parameters.keySet().containsAll(Arrays.asList(strArray))) {
			throw new ResourceInterfaceException("Missing parameters");
		}
		this.resourceName = parameters.get("resourceName");
		this.baseDir = parameters.get("baseDir");
		this.baseFilePath = new File(this.baseDir);
		this.resourceState = ResourceState.READY;
	}

	@Override
	public List<Entity> getPathRelationship(Entity path,
			OntologyRelationship relationship, User user)
			throws ResourceInterfaceException {

		List<Entity> returns = new ArrayList<Entity>();

		String basePath = path.getPui();
		String[] pathComponents = basePath.split("/");

		if (pathComponents.length == 2) {
			File[] files = baseFilePath.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return pathname.isFile();
				}
			});

			for (File myFile : files) {
				Entity entity = new Entity();
				entity.setName(myFile.getName());
				entity.setDisplayName(myFile.getName());
				entity.setPui(basePath + "/" + myFile.getName());
				returns.add(entity);
			}
		}

		return returns;
	}

	@Override
	public Result runQuery(User user, Query qep, Result result)
			throws ResourceInterfaceException {
		logger.debug("runQuery() Starting");
		
		WhereClause wc = (WhereClause) qep.getClauses().get(0L);
		String[] pathComponents = wc.getField().getPui().split("/");
		String fileName = pathComponents[2];
		logger.debug("runQuery() fileName:"+fileName);
		
		try {
			ResultSet rs = (ResultSet) result.getData();
			Reader in = new FileReader(this.baseDir + "/" + fileName);
		    
			logger.debug("runQuery() create ApacheCSVParser, and read in file:" + this.baseDir + "/" + fileName);
			CSVParser parser = new CSVParser(in, CSVFormat.EXCEL.withHeader());
		    rs = createInitialDataset(result, parser.getHeaderMap().keySet());
			for (CSVRecord record : parser) {
				Map<String, String> line = record.toMap();
				rs.appendRow();
				for (String header : line.keySet()) {
					rs.updateString(header, record.get(header));
				}
			}
			parser.close();
			in.close();
			
			logger.debug("runQuery() setting data of `result` object");
			result.setData(rs);
			result.setResultStatus(ResultStatus.COMPLETE);
			logger.debug("runQuery() setting status of `result` object to COMPLETE");
			
		} catch (Exception e) {
			logger.error("runQuery() Exception:"+e.getMessage());
			
			result.setResultStatus(ResultStatus.ERROR);
			result.setMessage(e.getMessage());
		}
		logger.debug("runQuery() Finished.");
		return result;
	}
	
	

	private ResultSet createInitialDataset(Result result, Set<String> headers) throws ResultSetException {
		logger.debug("createInitialDataset() Starting");
		ResultSet rs = (ResultSet) result.getData();
		logger.debug("createInitialDataset() Setting "+headers.size()+" columns.");		
		for(String header : headers) {
			Column encounterColumn = new Column();
			encounterColumn.setName(header);
			encounterColumn.setDataType(PrimitiveDataType.STRING);
			logger.trace("createInitialDataset() Header "+header+" added.");
			rs.appendColumn(encounterColumn);
		}
		logger.debug("createInitialDataset() Finished");
		return rs;
	}

	@Override
	public Result getResults(User user, Result result)
			throws ResourceInterfaceException {
		logger.debug("createInitialDataset() Starting");
		return result;
	}
	
	
	@Override
	public List<Entity> find(Entity path,
			FindInformationInterface findInformation, User user)
			throws ResourceInterfaceException {
		return new ArrayList<Entity>();
	}

	@Override
	public String getType() {
		return "fileService";
	}

	@Override
	public ResourceState getState() {
		return this.resourceState;
	}

	@Override
	public ResultDataType getQueryDataType(Query query) {
		return ResultDataType.TABULAR;
	}

	@Override
	public Result runRawQuery(String queryString) throws ResourceInterfaceException {
		// TODO Auto-generated method stub
		return null;
	}

}
