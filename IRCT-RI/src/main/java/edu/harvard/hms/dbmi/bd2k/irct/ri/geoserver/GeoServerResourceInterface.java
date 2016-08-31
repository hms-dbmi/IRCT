package edu.harvard.hms.dbmi.bd2k.irct.ri.geoserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.find.FindInformationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Entity;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ResourceState;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.PathResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.QueryResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

public class GeoServerResourceInterface implements
		QueryResourceImplementationInterface, PathResourceImplementationInterface {

	private String resourceName;
	private String resourceURL;
	private ResourceState resourceState;

	@Override
	public void setup(Map<String, String> parameters)
			throws ResourceInterfaceException {
		String[] strArray = { "resourceName", "resourceURL" };
		if (!parameters.keySet().containsAll(Arrays.asList(strArray))) {
			throw new ResourceInterfaceException("Missing parameters");
		}

		this.resourceName = parameters.get("resourceName");
		this.resourceURL = parameters.get("resourceURL");

		this.resourceState = ResourceState.READY;
	}
	
	@Override
	public List<Entity> getPathRelationship(Entity path,
			OntologyRelationship relationship, SecureSession session)
			throws ResourceInterfaceException {
		// TODO Auto-generated method stub
		
		
		return null;
	}
	
	


	@Override
	public Result runQuery(SecureSession session,
			edu.harvard.hms.dbmi.bd2k.irct.model.query.Query qep, Result result)
			throws ResourceInterfaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result getResults(SecureSession session, Result result)
			throws ResourceInterfaceException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<Entity> find(Entity path,
			FindInformationInterface findInformation, SecureSession session)
			throws ResourceInterfaceException {
		return new ArrayList<Entity>();
	}

	@Override
	public ResultDataType getQueryDataType(
			edu.harvard.hms.dbmi.bd2k.irct.model.query.Query query) {
		return ResultDataType.TABULAR;
	}

	@Override
	public String getType() {
		return "GeoServer";
	}

	@Override
	public ResourceState getState() {
		return this.resourceState;
	}
}
