package edu.harvard.hms.dbmi.bd2k.irct.ri.fileService;

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
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

public class FileServiceResourceImplementation implements
		QueryResourceImplementationInterface,
		PathResourceImplementationInterface {
	private String resourceName;
	private String baseDir;
	private ResourceState resourceState;
	private File baseFilePath;

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
			OntologyRelationship relationship, SecureSession session)
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
	public Result runQuery(SecureSession session, Query qep, Result result)
			throws ResourceInterfaceException {
		WhereClause wc = (WhereClause) qep.getClauses().get(0L);

		String[] pathComponents = wc.getField().getPui().split("/");
		String fileName = pathComponents[2];
		
		try {
			ResultSet rs = (ResultSet) result.getData();
			
			
			Reader in = new FileReader(this.baseDir + "/" + fileName);
			
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
			
			result.setData(rs);
			result.setResultStatus(ResultStatus.COMPLETE);
		} catch (Exception e) {
			result.setResultStatus(ResultStatus.ERROR);
			result.setMessage(e.getMessage());
			e.printStackTrace();
		}
		
		return result;
	}
	
	

	private ResultSet createInitialDataset(Result result, Set<String> headers) throws ResultSetException {
		ResultSet rs = (ResultSet) result.getData();
		for(String header : headers) {
			Column encounterColumn = new Column();
			encounterColumn.setName(header);
			encounterColumn.setDataType(PrimitiveDataType.STRING);
			rs.appendColumn(encounterColumn);
		}
		return rs;
	}

	@Override
	public Result getResults(SecureSession session, Result result)
			throws ResourceInterfaceException {
		return result;
	}
	
	
	@Override
	public List<Entity> find(Entity path,
			FindInformationInterface findInformation, SecureSession session)
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

}
