/**
 *
 */
package edu.harvard.hms.dbmi.bd2k.irct.ri.gnome;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.WhereClause;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ResourceState;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.QueryResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultStatus;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.Column;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.FileResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
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

	private void retrieveToken(){
		String urlString = resourceRootURL + AUTH_URL;

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpGet httpGet = new HttpGet(urlString);
		httpGet.addHeader("Authorization", "Basic " +
				DatatypeConverter.printBase64Binary((gnomeUserName+":"+gnomePassword)
						.getBytes()));
		CloseableHttpResponse response = null;

		try {
			response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();


			// the response body is in a Json format with a field "token"
			token = new ObjectMapper().readTree(entity
					.getContent())
					.get("token")
					.textValue();

			EntityUtils.consume(entity);

			if (token != null && !token.isEmpty())
				logger.info("gNome token has been retrieved correctly");
			else
				logger.warn("gNome token has NOT been retrieved correctly with URL: " + urlString);

		} catch (IOException ex ){
			logger.error("IOException when retrieving token from gNome with url:" + urlString +
					" with exception message: " + ex.getMessage());
		} finally {
			try {
				if (response != null)
					response.close();
			} catch (IOException ex) {
				logger.error("GNOME - IOExcpetion when closing http response: " + ex.getMessage());
			}

		}
	}


	@Override
	public Result runQuery(User user, Query query, Result result) {

		if (!isTokenExists()) {
			retrieveToken();
			if (!isTokenExists()) {
				result.setResultStatus(ResultStatus.ERROR);
				result.setMessage("Cannot retrieve a token from gNome");
				return result;
			}

		}

		List<WhereClause> whereClauses = query.getClausesOfType(WhereClause.class);

		result.setResultStatus(ResultStatus.CREATED);

		for (WhereClause whereClause : whereClauses) {

			// http request
			String urlString = resourceRootURL + "/" + whereClause.getField().getPui().split("/")[2];

			ObjectMapper objectMapper = new ObjectMapper();
			ObjectNode objectNode = objectMapper.createObjectNode();
			objectNode.put("token",token);

			Map<String, String> queries = whereClause.getStringValues();
			for (String key : queries.keySet()){
				objectNode.put(key, queries.get(key));
			}

			CloseableHttpClient client = HttpClientBuilder.create().build();
			HttpPost post = new HttpPost(urlString);
			try {
				post.setEntity(new StringEntity(objectMapper
						.writeValueAsString(objectNode), ContentType.APPLICATION_JSON));
			} catch (JsonProcessingException ex) {
				logger.error("gNome - Error when generating Json post body: " + ex.getMessage());
			}

			CloseableHttpResponse response = null;

			// http response
			try {
				result.setResultStatus(ResultStatus.RUNNING);

				response = client.execute(post);
				HttpEntity entity = response.getEntity();

				// parsing data
				parseData(result, objectMapper
						.readTree(entity
						.getContent()));

				result.setResultStatus(ResultStatus.COMPLETE);

				EntityUtils.consume(entity);

			} catch (PersistableException ex) {
				result.setResultStatus(ResultStatus.ERROR);
				logger.error("Persistable error: " + ex.getMessage() );
			} catch (ResultSetException ex) {
				result.setResultStatus(ResultStatus.ERROR);
				logger.error("Cannot append row: " + ex.getMessage());
			} catch (JsonParseException ex){
				result.setResultStatus(ResultStatus.ERROR);
				result.setMessage("Cannot parse gnome response as a JsonNode");
				logger.error("Cannot parse response as a JsonNode: " + ex.getMessage());
			} catch (IOException ex ){
				result.setResultStatus(ResultStatus.ERROR);
				result.setMessage("Cannot execute Post request to gnome");
				logger.error("IOException: Cannot cannot execute POST with URL: " + urlString);
			} finally {
				try {
					if (response != null)
						response.close();
				} catch (IOException ex) {
					logger.error("IOException when closing http response instance: " + ex.getMessage());
				}
			}


		}

		return result;
	}

	private void parseData(Result result, JsonNode responseJsonNode)
			throws PersistableException, ResultSetException{
		FileResultSet frs = (FileResultSet) result.getData();

		String responseStatus = responseJsonNode.get("status").textValue();

		JsonNode matrixNode = responseJsonNode.get("matrix");
		if (responseStatus.equalsIgnoreCase("success")){
			if (!matrixNode.getNodeType().equals(JsonNodeType.ARRAY)
					|| !matrixNode.get(0).getNodeType().equals(JsonNodeType.ARRAY)){
				String errorMessage = "Cannot parse response JSON from gnome: expecting an 2D array";
				result.setMessage(errorMessage);
				throw new PersistableException(errorMessage);
			}

			// append columns
			for (JsonNode innerJsonNode : matrixNode.get(0)){
				if (!innerJsonNode.getNodeType().equals(JsonNodeType.STRING)){
					String errorMessage = "Cannot parse response JSON from gnome: expecting a String in header array";
					result.setMessage(errorMessage);
					throw new PersistableException(errorMessage);
				}

				// how can I know what datatype it is for now?... just set it primitive string...
				frs.appendColumn(new Column(innerJsonNode.textValue(), PrimitiveDataType.STRING));
			}

			// append rows
			for (int i = 1; i < matrixNode.size(); i++){
				JsonNode jsonNode = matrixNode.get(i);
				if (!jsonNode.getNodeType().equals(JsonNodeType.ARRAY)){
					String errorMessage = "Cannot parse response JSON from gnome: expecting an 2D array";
					result.setMessage(errorMessage);
					throw new PersistableException(errorMessage);
				}

				frs.appendRow();

				for (int j = 0; j<jsonNode.size(); j++){
					// column datatype could be reset here by checking the json NodeType,
					// but no PrimitiveDataType.NUMBER implemented yet, can't efficiently separate
					// integer, double, just store everything as STRING for now
					frs.updateString(frs.getColumn(j).getName(),
							jsonNode.get(j).asText());
				}

			}
		} else {
			frs.appendColumn(new Column("status", PrimitiveDataType.STRING));
			frs.appendColumn(new Column("message", PrimitiveDataType.STRING));

			frs.appendRow();
			frs.updateString("status", responseStatus);
			frs.updateString("message", responseJsonNode.get("message").textValue());
		}

		result.setData(frs);
	}

	@Override
	public Result getResults(User user, Result result) {
		logger.debug( "getResults() Starting ...");

		return result;
	}

	@Override
	public ResourceState getState() {
		return resourceState;
	}

	@Override
	public ResultDataType getQueryDataType(Query query) {
		return ResultDataType.TABULAR;
	}
}
