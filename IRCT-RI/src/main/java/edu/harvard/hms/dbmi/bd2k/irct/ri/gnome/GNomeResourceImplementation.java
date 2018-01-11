/**
 *
 */
package edu.harvard.hms.dbmi.bd2k.irct.ri.gnome;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.WhereClause;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ResourceState;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.QueryResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultStatus;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;
import org.apache.log4j.Logger;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class GNomeResourceImplementation implements
		QueryResourceImplementationInterface {

	Logger logger = Logger.getLogger(getClass());

	private String resourceName;
	private String resourceRootURL;
	private String gnomeUserName;
	private String gnomePassword;

	private String token;

	private ResourceState resourceState;

	private static final String AUTH_URL = "/auth/auth.cgi";

	/*
	 * (non-Javadoc)
	 *
	 * @see edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.
	 * ResourceImplementationInterface#setup(java.util.Map)
	 */
	@Override
	public void setup(Map<String, String> parameters)
			throws ResourceInterfaceException{
		logger.debug("setup for " + resourceName +
				" Starting...");

		String errorString = "";
		this.resourceName = parameters.get("resourceName");
		if (this.resourceName == null) {
			logger.error( "setup() `resourceName` parameter is missing.");
			errorString += " resourceName";
		}

		this.resourceRootURL = parameters.get("resourceRootURL");
		if (this.resourceRootURL == null) {
			logger.error( "setup() `rootURL` parameter is missing.");
			errorString += " resourceRootURL";
		}

		this.gnomeUserName = parameters.get("gnomeUserName");
		if (this.gnomeUserName == null) {
			logger.error( "setup() `gnomeUserName` parameter is missing.");
			errorString += " gnomeUserName";
		}

		this.gnomePassword = parameters.get("gnomePassword");
		if (this.gnomePassword == null) {
			logger.error( "setup() `gnomePassword` parameter is missing.");
			errorString += " gnomePassword";
		}

		if (!errorString.isEmpty()) {
			throw new ResourceInterfaceException("GNome Interface setup() is missing:" + errorString);
		}

		retrieveToken();

		logger.debug( "setup for " + resourceName +
				" Finished. " + resourceName +
						" is in READY state.");
		resourceState = ResourceState.READY;
	}

	@Override
	public String getType() {
		return null;
	}

	private boolean isTokenExists(){
		return token!=null && !token.isEmpty();
	}

	private void retrieveToken() {
		HttpURLConnection con;
		try {
			URL url = new URL(resourceRootURL + AUTH_URL);

			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");

			con.setRequestProperty("Authorization", "Basic " +
							DatatypeConverter.printBase64Binary((gnomeUserName+":"+gnomePassword)
							.getBytes()));

			// the response body is in a Json format with a field "token"
			token = (String)new ObjectMapper()
					.readValue(con.getInputStream(), Map.class)
					.get("token");

			if (token != null && !token.isEmpty())
				logger.info("gNome token has been retrieved correctly");
			else
				logger.warn("gNome token has NOT been retrieved correctly");

		} catch (IOException ex ){
			logger.warn("Cannot retrieve GNome token with URL: " + resourceRootURL + AUTH_URL);
		} catch (IllegalStateException | IllegalArgumentException ex) {
			logger.error("adding basic auth to request header failed: " + ex.getMessage());
		}
	}


	@Override
	public Result runQuery(User user, Query query, Result result) throws ResourceInterfaceException {

		if (!isTokenExists()) {
			retrieveToken();
			if (!isTokenExists()) {
				result.setResultStatus(ResultStatus.ERROR);
				result.setMessage("Cannot retrieve a token from gNome");
				return result;
			}
		}

		List<WhereClause> whereClauses = query.getClausesOfType(WhereClause.class);
		String queryId = "NOTSET";

		for (WhereClause whereClause : whereClauses) {
			Map<String, String> queries = whereClause.getStringValues();
		}







		throw new NotImplementedException();
	}

	@Override
	public Result getResults(User user, Result result) throws ResourceInterfaceException {
		throw new NotImplementedException();
	}

	@Override
	public ResourceState getState() {
		throw new NotImplementedException();
	}

	@Override
	public ResultDataType getQueryDataType(Query query) {
		throw new NotImplementedException();
	}
}
