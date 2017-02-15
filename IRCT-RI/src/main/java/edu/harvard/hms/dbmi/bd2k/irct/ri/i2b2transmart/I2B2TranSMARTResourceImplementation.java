/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2transmart;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

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
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.I2B2XMLResourceImplementation;

/**
 * An implementation of a resource that communicates with the tranSMART
 * instance. It extends the i2b2 XML resource implementation.
 * 
 */
public class I2B2TranSMARTResourceImplementation extends
		I2B2XMLResourceImplementation {
	private String transmartURL;

	@Override
	public void setup(Map<String, String> parameters)
			throws ResourceInterfaceException {
		String[] strArray = { "resourceName", "resourceURL", "transmartURL",
				"domain" };
		if (!parameters.keySet().containsAll(Arrays.asList(strArray))) {
			throw new ResourceInterfaceException("Missing parameters");
		}

		this.transmartURL = parameters.get("transmartURL");

		super.setup(parameters);
	}

	@Override
	public List<Entity> getPathRelationship(Entity path,
			OntologyRelationship relationship, SecureSession session)
			throws ResourceInterfaceException {
		List<Entity> returns = super.getPathRelationship(path, relationship,
				session);

		// Get the counts from the tranSMART server
		try {
			HttpClient client = createClient(session);
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

				HttpPost post = new HttpPost(this.transmartURL
						+ "/chart/childConceptPatientCounts");
				List<NameValuePair> formParameters = new ArrayList<NameValuePair>();
				formParameters.add(new BasicNameValuePair("charttype",
						"childconceptpatientcounts"));
				formParameters.add(new BasicNameValuePair("concept_key", myPath
						+ "\\"));
				formParameters.add(new BasicNameValuePair("concept_level", ""));

				post.setEntity(new UrlEncodedFormEntity(formParameters));

				HttpResponse response = client.execute(post);

				JsonReader jsonReader = Json.createReader(response.getEntity()
						.getContent());

				JsonObject counts = jsonReader.readObject().getJsonObject(
						"counts");

				for (Entity singleReturn : returns) {
					String singleReturnMyPath = convertPUItoI2B2Path(singleReturn
							.getPui());

					if (counts.containsKey(singleReturnMyPath)) {
						singleReturn.getCounts().put("count",
								counts.getInt(singleReturnMyPath));
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return returns;
	}

	@Override
	public Result runQuery(SecureSession session, Query query, Result result)
			throws ResourceInterfaceException {
		result = super.runQuery(session, query, result);

		if (result.getResultStatus() != ResultStatus.ERROR) {
			String resultInstanceId = result.getResourceActionId();
			String resultId = resultInstanceId.split("\\|")[2];
			try {
				// Wait for it to be either ready or fail
				result = checkForResult(session, result);
				while ((result.getResultStatus() != ResultStatus.ERROR)
						&& (result.getResultStatus() != ResultStatus.COMPLETE)) {
					Thread.sleep(3000);
					result = checkForResult(session, result);
				}
				if (result.getResultStatus() == ResultStatus.ERROR) {
					return result;
				}
				result.setResultStatus(ResultStatus.RUNNING);

				String queryType = null;

				// Gather Select Clauses
				String gatherAllEncounterFacts = "false";
				Map<String, String> aliasMap = new HashMap<String, String>();

				for (SelectClause selectClause : query
						.getClausesOfType(SelectClause.class)) {
					String pui = selectClause.getParameter().getPui()
							.replaceAll("/" + this.resourceName + "/", "");

					if (pui.contains("/")) {
						queryType = "CLINICAL";
						pui = convertPUItoI2B2Path(selectClause.getParameter()
								.getPui());
					}

					aliasMap.put(pui.replaceAll("%2[f,F]", "/"), selectClause.getAlias());
				}

				// Run Additional Queries and Create Result Set
				if (queryType == null) {
					result.setResultStatus(ResultStatus.ERROR);
					result.setMessage("Unknown queryType");
					return result;
				}

				switch (queryType) {
				case "CLINICAL":
					result = runClinicalDataQuery(session, result, aliasMap,
							gatherAllEncounterFacts, resultId);
					result.setResultStatus(ResultStatus.COMPLETE);
					break;
				default:
					result.setResultStatus(ResultStatus.ERROR);
					result.setMessage("Unknown queryType");
				}

				// Set the status to complete
			} catch (InterruptedException | UnsupportedOperationException
					| IOException | ResultSetException | PersistableException e) {
				result.setResultStatus(ResultStatus.ERROR);
				result.setMessage(e.getMessage());
			}
		}
		return result;
	}

	private Result runClinicalDataQuery(SecureSession session, Result result,
			Map<String, String> aliasMap, String gatherAllEncounterFacts,
			String resultId) throws ResultSetException,
			ClientProtocolException, IOException, PersistableException {
		// Setup Resultset
		ResultSet rs = (ResultSet) result.getData();
		if (rs.getSize() == 0) {
			rs = createInitialDataset(result, aliasMap, gatherAllEncounterFacts);
		}

		// Get additional fields to grab from alias
		List<String> additionalFields = new ArrayList<String>();
		for (String key : aliasMap.keySet()) {
			if (!key.startsWith("\\")) {
				additionalFields.add(key);
			}
		}

		String pivot = "PATIENT_NUM";
		if (gatherAllEncounterFacts.equalsIgnoreCase("true")) {
			pivot = "ENCOUNTER_NUM";
			additionalFields.add("PATIENT_NUM");
		}

		//Setup initial fields
		Map<String, Long> entryMap = new HashMap<String, Long>();
		
		// Loop through the columns submitting and appending to the
		// rows every 10
		List<String> parameterList = new ArrayList<String>();
		int counter = 0;
		String parameters = "";
		for (String param : aliasMap.keySet()) {
			if (counter == 10) {
				parameterList.add(parameters);
				counter = 0;
				parameters = "";
			}
			if (!parameters.equals("")) {
				parameters += "|";
			}
			parameters += param;
		}
		if (!parameters.equals("")) {
			parameterList.add(parameters);
		}

		for (String parameter : parameterList) {
			// Call the tranSMART API to get the dataset
			String url = this.transmartURL
					+ "/ClinicalData/retrieveClinicalData?rid="
					+ resultId
					+ "&conceptPaths="
					+ URLEncoder.encode(URLDecoder.decode(parameter, "UTF-8"),
							"UTF-8") + "&gatherAllEncounterFacts="
					+ gatherAllEncounterFacts;
			System.out.println(url);
			HttpClient client = createClient(session);
			HttpGet get = new HttpGet(url);
			HttpResponse response = client.execute(get);

			JsonParser parser = Json.createParser(response.getEntity()
					.getContent());

			convertJsonStreamToResultSet(rs, parser,
					aliasMap, pivot, entryMap, additionalFields);
			
		}
		result.setData(rs);
		return result;
	}

	

	private ResultSet convertJsonStreamToResultSet(ResultSet rs,
			JsonParser parser, Map<String, String> aliasMap, String pivot,
			Map<String, Long> entryMap, List<String> additionalFields)
			throws ResultSetException, PersistableException {

		while (parser.hasNext()) {
			JsonObject obj = convertStreamToObject(parser);

			if(!obj.containsKey(pivot)) {
				break;
			}
			String id = obj.getString(pivot);

			if (entryMap.containsKey(id)) {
				// Is already in the resultset
				rs.absolute(entryMap.get(id));
				rs.updateString(aliasMap.get(obj.getString("CONCEPT_PATH")), obj.getString("VALUE"));
				
			} else {
				// Is not in the resultset
				rs.appendRow();
				rs.updateString(pivot, id);
				//Add concept value
				rs.updateString(aliasMap.get(obj.getString("CONCEPT_PATH")), obj.getString("VALUE"));
				
				//Add fields
				for (String field : additionalFields) {
					if (obj.containsKey(field)) {
						rs.updateString(aliasMap.get(field), obj.getString(field));
					}
				}
				entryMap.put(id, rs.getRow());
			}

		}

		return rs;
	}

	private ResultSet createInitialDataset(Result result,
			Map<String, String> aliasMap, String gatherAllEncounterFacts)
			throws ResultSetException {
		ResultSet rs = (ResultSet) result.getData();

		// Set up the columns
		Column idColumn = new Column();
		idColumn.setName("PATIENT_NUM");
		idColumn.setDataType(PrimitiveDataType.STRING);
		rs.appendColumn(idColumn);

		if (gatherAllEncounterFacts.equalsIgnoreCase("true")) {
			Column encounterColumn = new Column();
			encounterColumn.setName("ENCOUNTER_NUM");
			encounterColumn.setDataType(PrimitiveDataType.STRING);
			rs.appendColumn(encounterColumn);
		}

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
	public Result getResults(SecureSession session, Result result)
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
			FindInformationInterface findInformation, SecureSession session)
			throws ResourceInterfaceException {
		List<Entity> returns = new ArrayList<Entity>();

		if (findInformation instanceof FindByPath) {
			FindByPath findPath = (FindByPath) findInformation;
			if (findInformation.getValues().containsKey("tmObservationOnly")) {
				returns = searchObservationOnly(findPath.getValues()
						.get("term"), findPath.getValues().get("strategy"),
						session, findPath.getValues().get("tmObservationOnly"));
			} else {
				returns = searchObservationOnly(findPath.getValues()
						.get("term"), findPath.getValues().get("strategy"),
						session, "FALSE");
			}
		} else {
			returns = super.find(path, findInformation, session);
		}
		return returns;
	}

	public List<Entity> searchObservationOnly(String searchTerm,
			String strategy, SecureSession session, String onlObs) {
		List<Entity> entities = new ArrayList<Entity>();

		try {
			URI uri = new URI(this.transmartURL.split("://")[0],
					this.transmartURL.split("://")[1].split("/")[0], "/"
							+ this.transmartURL.split("://")[1].split("/")[1]
							+ "/textSearch/findPaths", "oblyObs=" + onlObs
							+ "&term=" + searchTerm, null);

			HttpClient client = createClient(session);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return entities;
	}

	private String convertPUItoI2B2Path(String pui) {
		String[] singleReturnPathComponents = pui.split("/");
		String singleReturnMyPath = "";
		for (String pathComponent : Arrays.copyOfRange(
				singleReturnPathComponents, 4,
				singleReturnPathComponents.length)) {
			singleReturnMyPath += "\\" + pathComponent;
		}

		singleReturnMyPath += "\\";

		return singleReturnMyPath;
	}
}
