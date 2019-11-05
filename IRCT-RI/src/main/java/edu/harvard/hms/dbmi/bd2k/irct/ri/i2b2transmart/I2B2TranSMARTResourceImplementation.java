/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2transmart;

import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.find.FindByPath;
import edu.harvard.hms.dbmi.bd2k.irct.model.find.FindInformationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Entity;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.SelectClause;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ResourceState;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultStatus;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.Column;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.ResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.I2B2XMLResourceImplementation;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import javax.json.*;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

/**
 * An implementation of a resource that communicates with the tranSMART
 * instance. It extends the i2b2 XML resource implementation.
 */
public class I2B2TranSMARTResourceImplementation extends
		I2B2XMLResourceImplementation {
	private String transmartURL;

	private Logger logger = Logger.getLogger(this.getClass());
	
	@Override
	public void setup(Map<String, String> parameters)
			throws ResourceInterfaceException {

		logger.debug("setup() start");

		if (!parameters.keySet().contains("resourceName")) {
			throw new ResourceInterfaceException("Missing `resourceName` parameter.");
		}
		if (!parameters.keySet().contains("resourceURL")) {
			throw new ResourceInterfaceException("Missing `resourceURL` parameter.");
		}
		if (!parameters.keySet().contains("transmartURL")) {
			throw new ResourceInterfaceException("Missing `transmartURL` parameter.");
		}
		if (!parameters.keySet().contains("domain")) {
			throw new ResourceInterfaceException("Missing `domain` parameter.");
		}
		logger.debug("setup() All mandatory parameters are there.");

		this.transmartURL = parameters.get("transmartURL");
		logger.debug("setup() `transmartURL` is now set to:"+this.transmartURL);

		super.setup(parameters);
	}

	@Override
	public List<Entity> getPathRelationship(Entity path,
			OntologyRelationship relationship, User user)
			throws ResourceInterfaceException {
		List<Entity> returns = super.getPathRelationship(path, relationship, user);

		java.util.logging.Logger.getGlobal().log(java.util.logging.Level.FINEST, "getPathRelationship() ");
		// Get the counts from the tranSMART server
		try {
			HttpClient client = createClient(user);
			String basePath = path.getPui();
			String[] pathComponents = basePath.split("/");

			if (pathComponents.length > 3) {
				String myPath = "\\";
				for (String pathComponent : Arrays.copyOfRange(pathComponents,
						3, pathComponents.length)) {
					myPath += "\\" + pathComponent;
				}
				basePath = pathComponents[0] + "/" + pathComponents[1] + "/"
						+ pathComponents[2];

				java.util.logging.Logger.getGlobal().log(java.util.logging.Level.FINEST, "getPathRelationship() URL:"+this.transmartURL
				+ "/chart/childConceptPatientCounts");
				HttpPost post = new HttpPost(this.transmartURL
						+ "/chart/childConceptPatientCounts");
				List<NameValuePair> formParameters = new ArrayList<NameValuePair>();
				formParameters.add(new BasicNameValuePair("charttype",
						"childconceptpatientcounts"));
				formParameters.add(new BasicNameValuePair("concept_key", myPath
						+ "\\"));
				formParameters.add(new BasicNameValuePair("concept_level", ""));

				post.setEntity(new UrlEncodedFormEntity(formParameters));
				java.util.logging.Logger.getGlobal().log(java.util.logging.Level.FINEST, "getPathRelationship() making call over HTTP");
				HttpResponse response = client.execute(post);

				JsonReader jsonReader = Json.createReader(response.getEntity()
						.getContent());
				JsonObject responseContent = jsonReader.readObject();

				java.util.logging.Logger.getGlobal().log(java.util.logging.Level.FINEST, "getPathRelationship() ResponseEntity:"
						+responseContent.toString());

				JsonObject counts = responseContent.getJsonObject("counts");

				for (Entity singleReturn : returns) {
					String singleReturnMyPath = convertPUItoI2B2Path(singleReturn
							.getPui());

					if (counts.containsKey(singleReturnMyPath)) {
						singleReturn.getCounts().put("count",
								counts.getInt(singleReturnMyPath));
					}
				}
			}
		} catch (Exception e) {
			java.util.logging.Logger.getGlobal().log(java.util.logging.Level.SEVERE, "getPathRelationship() Exception "+e.getMessage());
			e.printStackTrace();
		}

		return returns;
	}

	@Override
	public Result runQuery(User user, Query query, Result result)
			throws ResourceInterfaceException {
		
		result = super.runQuery(user, query, result);

		if (result.getResultStatus() != ResultStatus.ERROR) {
			String resultInstanceId = result.getResourceActionId();
			String resultId = resultInstanceId.split("\\|")[2];
			try {
				// Wait for it to be either ready or fail
				result = checkForResult(user, result);
				while ((result.getResultStatus() != ResultStatus.ERROR)
						&& (result.getResultStatus() != ResultStatus.COMPLETE)) {
					Thread.sleep(3000);
					result = checkForResult(user, result);
				}
				if (result.getResultStatus() == ResultStatus.ERROR) {
					return result;
				}
				result.setResultStatus(ResultStatus.RUNNING);

				// Gather Select Clauses
				Map<String, String> aliasMap = new HashMap<String, String>();

				for (SelectClause selectClause : query
						.getClausesOfType(SelectClause.class)) {
					String pui = selectClause.getParameter().getPui()
							.replaceAll("/" + this.resourceName + "/", "");

					String rawPUI = selectClause.getParameter().getPui();
					if (rawPUI.endsWith("*")) {
						//Get the base PUI
						String basePUI = rawPUI.substring(0, rawPUI.length() - 1);
						boolean compact = false;
						String subPUI = null;

						if(selectClause.getStringValues().containsKey("COMPACT") && selectClause.getStringValues().get("COMPACT").equalsIgnoreCase("true")) {
							compact = true;
						}
						if(selectClause.getStringValues().containsKey("REMOVEPREPEND") && selectClause.getStringValues().get("REMOVEPREPEND").equalsIgnoreCase("true")) {
							subPUI = basePUI.substring(0, basePUI.substring(0, basePUI.length() - 1).lastIndexOf("/"));
						}

						//Loop through all the children and add them to the aliasMap
						aliasMap.putAll(getAllChildrenAsAliasMap(basePUI, subPUI, compact, user));

					} else {
						pui = convertPUItoI2B2Path(selectClause.getParameter()
								.getPui());
						aliasMap.put(pui.replaceAll("%2[f,F]", "/") + "\\",
								selectClause.getAlias());
					}
				}

				// Run Additional Queries and Create Result Set
				result = runClinicalDataQuery(user, result, aliasMap, resultId);
				result.setResultStatus(ResultStatus.COMPLETE);

				// Set the status to complete
			} catch (InterruptedException | UnsupportedOperationException
					| IOException | ResultSetException | PersistableException | JsonException e) {
				result.setResultStatus(ResultStatus.ERROR);
				result.setMessage(e.getMessage());
			}
		}
		return result;
	}

	private Result runClinicalDataQuery(User user, Result result,
			Map<String, String> aliasMap, String resultId)
			throws ResultSetException, ClientProtocolException, IOException,
			PersistableException, JsonException {
		
		logger.debug("runClinicalDataQuery() starting");
				
		// Setup Resultset
		ResultSet rs = (ResultSet) result.getData();
		if (rs.getSize() == 0) {
			rs = createInitialDataset(result, aliasMap);
		}

		// Get additional fields to grab from alias
		List<String> additionalFields = new ArrayList<String>();
		for (String key : aliasMap.keySet()) {
			if (!key.startsWith("\\")) {
				additionalFields.add(key);
			}
		}

		String pivot = "PATIENT_NUM";

		// Setup initial fields
		Map<String, Long> entryMap = new HashMap<String, Long>();

		// Loop through the columns submitting and appending to the
		// rows every 10
		List<String> parameterList = new ArrayList<String>();
		
		// The list of all the concepts that we need to query individually from tranSMART
		List<String> conceptList = new ArrayList<String>();
		
		int counter = 0;
		String parameters = "";
		for (String param : aliasMap.keySet()) {
			logger.debug("runClinicalDataQuery() alias:"+aliasMap.get(param)+" as concept:"+param);
			conceptList.add(param);
			
			if (counter >= 10) {
				parameterList.add(parameters);
				counter = 0;
				parameters = "";
			}
			if (!parameters.equals("")) {
				parameters += "|";
			}
			parameters += param;
			counter++;
		}
		if (!parameters.equals("")) {
			parameterList.add(parameters);
		}
		logger.debug("runClinicalDataQuery() starting");

		for (String conceptPath : conceptList) {
			// Call the tranSMART API to get the dataset
			String url = this.transmartURL
					+ "/ClinicalData/retrieveClinicalData?rid="
					+ resultId
					+ "&gatherAllEncounterFacts=false"
					+ "&conceptPaths="
					+ URLEncoder.encode(URLDecoder.decode(conceptPath, "UTF-8"),
							"UTF-8");

			HttpClient client = createClient(user);
			HttpGet get = new HttpGet(url);
			logger.debug("runClinicalDataQuery() url:"+url);
			HttpResponse response = client.execute(get);

			// Parse the response from i2b2/tranSMART
			try {
				JsonParser parser = Json.createParser(response.getEntity()
						.getContent());

				convertJsonStreamToResultSet(rs, parser, aliasMap, pivot, entryMap,
						additionalFields);
			} catch (Exception e) {
				logger.error("runClinicalDataQuery() Exception parsing i2b2/tranSMART response: "+e.getMessage());
				result.setResultStatus(ResultStatus.ERROR);
				result.setMessage(e.getMessage());
			}

		}
		logger.debug("runClinicalDataQuery() Finished retrieving from source. Setting `data` field of `result` object.");
		result.setData(rs);
		logger.debug("runClinicalDataQuery() Finished");
		return result;
	}

	private ResultSet convertJsonStreamToResultSet(ResultSet rs,
			JsonParser parser, Map<String, String> aliasMap, String pivot,
			Map<String, Long> entryMap, List<String> additionalFields)
			throws ResultSetException, PersistableException, JsonException {

		while (parser.hasNext()) {
			JsonObject obj = convertStreamToObject(parser);

			if (!obj.containsKey(pivot)) {
				break;
			}
			String id = obj.getString(pivot);

			if (entryMap.containsKey(id)) {
				// Is already in the resultset
				rs.absolute(entryMap.get(id));
				rs.updateString(aliasMap.get(obj.getString("CONCEPT_PATH")),
						obj.getString("VALUE"));

			} else {
				// Is not in the resultset
				rs.appendRow();
				rs.updateString(pivot, id);
				// Add concept value
				rs.updateString(aliasMap.get(obj.getString("CONCEPT_PATH")),
						obj.getString("VALUE"));

				// Add fields
				for (String field : additionalFields) {
					if (obj.containsKey(field)) {
						rs.updateString(aliasMap.get(field),
								obj.getString(field));
					}
				}
				entryMap.put(id, rs.getRow());
			}

		}

		return rs;
	}

	private ResultSet createInitialDataset(Result result,
			Map<String, String> aliasMap) throws ResultSetException {
		ResultSet rs = (ResultSet) result.getData();

		// Set up the columns
		Column idColumn = new Column();
		idColumn.setName("PATIENT_NUM");
		idColumn.setDataType(PrimitiveDataType.STRING);
		rs.appendColumn(idColumn);

		for (String aliasKey : aliasMap.keySet()) {
			Column newColumn = new Column();
			if (aliasMap.get(aliasKey) == null) {
				newColumn.setName(aliasKey);
			} else {
				newColumn.setName(aliasMap.get(aliasKey));
			}
			newColumn.setDataType(PrimitiveDataType.STRING);

			rs.appendColumn(newColumn);
		}

		result.setData(rs);
		return rs;
	}

	private JsonObject convertStreamToObject(JsonParser parser) {
		JsonObjectBuilder build = Json.createObjectBuilder();
		String key = null;
		boolean endObj = false;
		while (parser.hasNext() && !endObj) {
			Event event = parser.next();

			switch (event) {
			case KEY_NAME:
				key = parser.getString();
				break;
			case VALUE_STRING:
				build.add(key, parser.getString());
				key = null;
				break;
			case VALUE_NUMBER:
				build.add(key, parser.getBigDecimal());
				key = null;
				break;
			case VALUE_TRUE:
				build.add(key, true);
				key = null;
				break;
			case VALUE_FALSE:
				build.add(key, false);
				key = null;
				break;
			case END_OBJECT:
				endObj = true;
				break;
			default:
			}
		}

		return build.build();
	}

	@Override
	public Result getResults(User user, Result result)
			throws ResourceInterfaceException {
		// This method only exists so the results for i2b2XML do not get called
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

	@Override
	public String getType() {
		return "i2b2/tranSMART";
	}

	@Override
	public List<Entity> find(Entity path,
			FindInformationInterface findInformation, User user)
			throws ResourceInterfaceException {
		List<Entity> returns = new ArrayList<Entity>();

		if (findInformation instanceof FindByPath) {
			FindByPath findPath = (FindByPath) findInformation;
			if (findInformation.getValues().containsKey("tmObservationOnly")) {
				returns = searchObservationOnly(findPath.getValues()
						.get("term"), findPath.getValues().get("strategy"),
						user, findPath.getValues().get("tmObservationOnly"));
			} else {
				returns = searchObservationOnly(findPath.getValues()
						.get("term"), findPath.getValues().get("strategy"),
						user, "FALSE");
			}
		} else {
			returns = super.find(path, findInformation, user);
		}
		return returns;
	}

	public List<Entity> searchObservationOnly(String searchTerm,
			String strategy, User user, String onlObs) {
		List<Entity> entities = new ArrayList<Entity>();

		try {
			URI uri = new URI(this.transmartURL.split("://")[0],
					this.transmartURL.split("://")[1].split("/")[0], "/"
							+ this.transmartURL.split("://")[1].split("/")[1]
							+ "/textSearch/findPaths", "onlyObs=" + onlObs
							+ "&term=" + searchTerm, null);

			HttpClient client = createClient(user);
			HttpGet get = new HttpGet(uri);
			HttpResponse response = client.execute(get);
			JsonReader reader = Json.createReader(response.getEntity()
					.getContent());
			JsonArray arrayResults = reader.readArray();

			for (JsonValue val : arrayResults) {
				JsonObject returnObject = (JsonObject) val;

				Entity returnedEntity = new Entity();
				returnedEntity
						.setPui("/"
								+ this.resourceName
								+ converti2b2Path(returnObject
										.getString("conceptPath")));

				if (!returnObject.isNull("text")) {
					returnedEntity.getAttributes().put("text",
							returnObject.getString("text"));
				}
				entities.add(returnedEntity);
			}

		} catch (URISyntaxException | JsonException | IOException e) {
			logger.error("searchObservationOnly() Exception: "+e.getMessage());
		}

		return entities;
	}
	
	protected void addAuthenticationHeader(User user, List<Header> defaultHeaders) {
		// TODO This should be enhanced, so that the user's Resource specific token gets retrieved!!!
		String token = user.getToken();
		defaultHeaders.add(new BasicHeader("Authorization", "Bearer "+token));
	}

}
