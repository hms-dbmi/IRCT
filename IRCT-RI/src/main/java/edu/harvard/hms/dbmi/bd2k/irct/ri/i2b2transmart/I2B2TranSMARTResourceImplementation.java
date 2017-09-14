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

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

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
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.I2B2OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.I2B2XMLResourceImplementation;

/**
 * An implementation of a resource that communicates with the tranSMART
 * instance. It extends the i2b2 XML resource implementation.
 *
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
			throw new ResourceInterfaceException("Missing ```resourceName``` parameter.");
		}
		if (!parameters.keySet().contains("resourceURL")) {
			throw new ResourceInterfaceException("Missing ```resourceURL``` parameter.");
		}
		if (!parameters.keySet().contains("transmartURL")) {
			throw new ResourceInterfaceException("Missing ```transmartURL``` parameter.");
		}
		if (!parameters.keySet().contains("domain")) {
			throw new ResourceInterfaceException("Missing ```domain``` parameter.");
		}
		logger.debug("setup() All mandatory parameters are there.");

		this.transmartURL = parameters.get("transmartURL");
		logger.debug("setup() ```transmartURL``` is now set to:"+this.transmartURL);

		super.setup(parameters);
	}

	@Override
	public List<Entity> getPathRelationship(Entity path,
			OntologyRelationship relationship, SecureSession session)
			throws ResourceInterfaceException {
		List<Entity> returns = super.getPathRelationship(path, relationship,
				session);

		logger.debug("getPathRelationship() Starting...");
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


				HttpPost post = new HttpPost(this.transmartURL + "/chart/childConceptPatientCounts");
				List<NameValuePair> formParameters = new ArrayList<NameValuePair>();
				formParameters.add(new BasicNameValuePair("charttype","childconceptpatientcounts"));
				formParameters.add(new BasicNameValuePair("concept_key", myPath + "\\"));
				formParameters.add(new BasicNameValuePair("concept_level", ""));
				post.setEntity(new UrlEncodedFormEntity(formParameters));
				logger.debug("getPathRelationship() POST method to "+post.getURI().toString());

				HttpResponse response = client.execute(post);

				JsonReader jsonReader = Json.createReader(response.getEntity()
						.getContent());
				JsonObject responseContent = jsonReader.readObject();
				logger.debug("getPathRelationship() ResponseEntity:" +responseContent.toString());

				JsonObject counts = responseContent.getJsonObject("counts");
				logger.debug("getPathRelationship() got `counts` object.");

				logger.debug("getPathRelationship() now iterate through "+returns.size()+" entities in `returns`");
				// TO-DO: I don't understand WHY this is here?!!!!
				for (Entity singleReturn : returns) {
					String singleReturnMyPath = convertPUItoI2B2Path(singleReturn
							.getPui());
					logger.trace("getPathRelationship() `singleReturnMyPath` is set to '"+singleReturnMyPath+"'");

					if (counts.containsKey(singleReturnMyPath)) {
						singleReturn.getCounts().put("count",
								counts.getInt(singleReturnMyPath));
					}
				}
			}
		} catch (Exception e) {
			logger.error("getPathRelationship() Exception "+e.getMessage());
		}
		logger.debug("getPathRelationship() Finished. Returning `List<Entity>`");
		return returns;
	}

	@Override
	public Result runQuery(SecureSession session, Query query, Result result)
			throws ResourceInterfaceException {
		logger.debug("runQuery() Starting ...");

		logger.debug("runQuery() super.runQuery()");
		result = super.runQuery(session, query, result);
		logger.debug("runQuery() completed super.runQuery()");

		if (result.getResultStatus() != ResultStatus.ERROR) {
			logger.debug("runQuery() result is NOT in ERROR status.");

			String resultInstanceId = result.getResourceActionId();
			logger.debug("runQuery() `resultInstanceId` is "+resultInstanceId);

			String resultId = resultInstanceId.split("\\|")[2];
			logger.debug("runQuery() `resultId` is "+resultId+" from "+resultInstanceId);

			try {
				// Wait for it to be either ready or fail
				logger.trace("runQuery() calling checkForResult()");
				result = checkForResult(session, result);

				while ((result.getResultStatus() != ResultStatus.ERROR)
						&& (result.getResultStatus() != ResultStatus.COMPLETE)) {
					logger.trace("runQuery() Sleeping for 3000 millisec");
					Thread.sleep(3000);
					logger.trace("runQuery() calling checkForResult() again");
					result = checkForResult(session, result);
				}
				if (result.getResultStatus() == ResultStatus.ERROR) {
					logger.trace("runQuery() returning `result` with 'ERROR' status. Missing message?");
					return result;
				}
				// TO-DO: How can we be in RUNNING status? The loop above ends only if
				//        it is in ERROR or COMPLETE? Why are we resetting the status
				//        here to RUNNING?
				logger.debug("runQuery() setting result to RUNNING status (but why?)");
				result.setResultStatus(ResultStatus.RUNNING);

				// Gather Select Clauses
				logger.debug("runQuery() gathering SELECT clauses");
				Map<String, String> aliasMap = new HashMap<String, String>();

				for (SelectClause selectClause : query
						.getClausesOfType(SelectClause.class)) {

					logger.trace("runQuery() `selectClause` got one");

					String pui = selectClause.getParameter().getPui()
							.replaceAll("/" + this.resourceName + "/", "");
					logger.trace("runQuery() `pui` is now "+pui);

					String rawPUI = selectClause.getParameter().getPui();
					if (rawPUI.endsWith("*")) {
						logger.trace("runQuery() `rawPUI` ends with '*'");

						//Get the base PUI
						String basePUI = rawPUI.substring(0, rawPUI.length() - 1);
						logger.trace("runQuery() `basePUI` is now '"+basePUI+"'");
						boolean compact = false;
						String subPUI = null;

						if(selectClause.getStringValues().containsKey("COMPACT") && selectClause.getStringValues().get("COMPACT").equalsIgnoreCase("true")) {
							logger.trace("runQuery() this is a 'compact' field");
							compact = true;
						}
						if(selectClause.getStringValues().containsKey("REMOVEPREPEND") && selectClause.getStringValues().get("REMOVEPREPEND").equalsIgnoreCase("true")) {
							logger.trace("runQuery() 'REMOVEPREPEND' has been specified on this field.");

							subPUI = basePUI.substring(0, basePUI.substring(0, basePUI.length() - 1).lastIndexOf("/"));
							logger.trace("runQuery() set `subPUI` to '"+subPUI+"' value");
						}

						//Loop through all the children and add them to the aliasMap
						logger.trace("runQuery() adding this whole mess to `aliasMap`");
						aliasMap.putAll(getAllChildrenAsAliasMap(basePUI, subPUI, compact, session));

					} else {
						logger.trace("runQuery() get pui from converting it from i2b2Path");
						pui = convertPUItoI2B2Path(selectClause.getParameter()
								.getPui());
						logger.trace("runQuery() now `pui` is '"+pui+"' and adding it to aliasMap");
						aliasMap.put(pui.replaceAll("%2[f,F]", "/") + "\\",
								selectClause.getAlias());
					}
				}

				// Run Additional Queries and Create Result Set
				logger.debug("runQuery() additional queries, calling runClinicalDataQuery()");
				result = runClinicalDataQuery(session, result, aliasMap, resultId);

				logger.debug("runQuery() set `result` status to 'COMPLETE'");
				result.setResultStatus(ResultStatus.COMPLETE);

				// Set the status to complete
			} catch (InterruptedException | UnsupportedOperationException
					| IOException | ResultSetException | PersistableException | JsonException e) {
				e.printStackTrace();

				logger.error("runQuery() OtherException:"+e.getMessage()+":"+e.toString());

				result.setResultStatus(ResultStatus.ERROR);
				result.setMessage("runQuery() OtherException:"+e.getMessage());
			} catch (Exception e) {
				logger.error("runQuery() Exception:"+e.getMessage()+":"+e.toString());
				result.setResultStatus(ResultStatus.ERROR);
				result.setMessage("runQuery() Exception:"+e.getMessage());
			}
		} else {
			logger.debug("runQuery() result is already in ERROR status!");
		}
		logger.debug("runQuery() Finished. Returning `result`");
		return result;
	}

	private Map<String, String> getAllChildrenAsAliasMap(String basePUI, String subPUI, boolean compact, SecureSession session) throws ResourceInterfaceException {
		logger.debug("getAllChildrenAsAliasMap() Starting ...");
		Map<String, String> returns = new HashMap<String, String>();

		Entity baseEntity = new Entity(basePUI);
		for(Entity entity : super.getPathRelationship(baseEntity, I2B2OntologyRelationship.CHILD, session)) {
			logger.trace("getAllChildrenAsAliasMap() get `entity` children of "+baseEntity.getName());

			if(entity.getAttributes().containsKey("visualattributes")) {
				logger.trace("getAllChildrenAsAliasMap() check the visualattribuets");
				String visualAttributes = entity.getAttributes().get("visualattributes");

				if(visualAttributes.startsWith("C") || visualAttributes.startsWith("F")) {
					logger.trace("getAllChildrenAsAliasMap() visualattributes call for going into this node");
					returns.putAll(getAllChildrenAsAliasMap(entity.getPui(), subPUI, compact, session));
				} else if (visualAttributes.startsWith("L")) {
					logger.trace("getAllChildrenAsAliasMap() `visualAttributes` is L. call convertPUItoI2B2Path()");
					String pui = convertPUItoI2B2Path(entity.getPui()).replaceAll("%2[f,F]", "/")  + "\\";
					String alias =  pui;
					if(compact) {
						alias = basePUI;
					}
					if(subPUI != null) {
						alias = alias.replaceAll(subPUI, "");
					}
					if(alias.endsWith("/")) {
						alias = alias.substring(0, alias.length() - 1);
					}
					logger.trace("getAllChildrenAsAliasMap() after convertsion, '"+pui+"' is mapped to '"+alias+"'");
					returns.put(pui, alias);
				}
			} else {
				logger.trace("getAllChildrenAsAliasMap() there are no visualattributes for `entity`:"+entity.getName());
			}
		}
		logger.debug("getAllChildrenAsAliasMap() Finished. Returning `Map<String,String>`");
		return returns;
	}

	private Result runClinicalDataQuery(SecureSession session, Result result,
			Map<String, String> aliasMap, String resultId)
			throws ResultSetException, ClientProtocolException, IOException,
			PersistableException, JsonException {
		logger.debug("runClinicalDataQuery() Starting ...");

		// Setup Resultset
		ResultSet rs = (ResultSet) result.getData();
		if (rs.getSize() == 0) {
			logger.debug("runClinicalDataQuery() calling createInitialDataset()");
			rs = createInitialDataset(result, aliasMap);
		}

		// Get additional fields to grab from alias
		List<String> additionalFields = new ArrayList<String>();
		for (String key : aliasMap.keySet()) {
			logger.debug("runClinicalDataQuery() aliasMap.key:"+key);
			if (!key.startsWith("\\")) {
				logger.debug("runClinicalDataQuery() add it to additionalFields");
				additionalFields.add(key);
			}
		}


		String pivot = "PATIENT_NUM";

		// Setup initial fields
		Map<String, Long> entryMap = new HashMap<String, Long>();

		// Loop through the columns submitting and appending to the
		// rows every 10
		// WTF is this??????? (question by Gabe)


		List<String> parameterList = new ArrayList<String>();
		int counter = 0;
		String parameters = "";
		for (String param : aliasMap.keySet()) {
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
			logger.debug("runClinicalDataQuery() adding `parameters` as ["+parameters+"] to `parameterList`");
			parameterList.add(parameters);
		}

		for (String parameter : parameterList) {
			// Call the tranSMART API to get the dataset
			logger.debug("runClinicalDataQuery() calling transmart API....");

			String url = this.transmartURL
					+ "/ClinicalData/retrieveClinicalData?rid="
					+ resultId
					+ "&conceptPaths="
					+ URLEncoder.encode(URLDecoder.decode(parameter, "UTF-8"),
							"UTF-8");

			HttpClient client = createClient(session);
			HttpGet get = new HttpGet(url);
			logger.info("runClinicalDataQuery() url:"+url);
			HttpResponse response = client.execute(get);

			logger.trace("runClinicalDataQuery() creating JsonParser from Entity");
			JsonParser parser = Json.createParser(response.getEntity().getContent());

			logger.debug("runClinicalDataQuery() calling convertJsonStreamToResultSet()");
			convertJsonStreamToResultSet(rs, parser, aliasMap, pivot, entryMap,
					additionalFields);
			logger.debug("runClinicalDataQuery() `ResultSet` is created from `JsonStream`");

		}
		logger.debug("runClinicalDataQuery() Setting data of `result` object.");
		result.setData(rs);

		logger.debug("runClinicalDataQuery() Finished. Returning `result`");
		return result;
	}

	private ResultSet convertJsonStreamToResultSet(ResultSet rs,
			JsonParser parser, Map<String, String> aliasMap, String pivot,
			Map<String, Long> entryMap, List<String> additionalFields)
			throws ResultSetException, PersistableException, JsonException {

		logger.debug("convertJsonStreamToResultSet() Starting...");
		logger.debug("convertJsonStreamToResultSet() entryMap:"+StringUtils.join( entryMap.keySet().toArray(), ","));

		while (parser.hasNext()) {
			JsonObject obj = convertStreamToObject(parser);

			if (!obj.containsKey(pivot)) {
				logger.debug("convertJsonStreamToResultSet() already has `pivot`:"+pivot);
				break;
			}
			String id = obj.getString(pivot);

			logger.debug("convertJsonStreamToResultSet() set `id` to "+id);

			if (entryMap.containsKey(id)) {
				logger.debug("convertJsonStreamToResultSet() "+id+" is in `entryMap`");
				rs.absolute(entryMap.get(id));
				rs.updateString(aliasMap.get(obj.getString("CONCEPT_PATH")),
						obj.getString("VALUE"));

			} else {
				logger.debug("convertJsonStreamToResultSet() "+id+" is not in `entryMap`.");

				logger.debug("convertJsonStreamToResultSet() add a new row");
				rs.appendRow();
				logger.trace("convertJsonStreamToResultSet() calling updateString("+pivot+","+id+")");
				rs.updateString(pivot, id);

				// Add concept value
				logger.trace("convertJsonStreamToResultSet() calling updateString() for CONCEPT_PATH/VALUE");
				rs.updateString(aliasMap.get(obj.getString("CONCEPT_PATH")),
						obj.getString("VALUE"));

				// Add fields
				logger.trace("convertJsonStreamToResultSet() adding `additionalFields` from `aliasMap`"+StringUtils.join(additionalFields, ","));
				for (String field : additionalFields) {
					logger.trace("convertJsonStreamToResultSet() additional field:"+field);
					if (obj.containsKey(field)) {
						rs.updateString(aliasMap.get(field), obj.getString(field));
					} else {
						logger.trace("convertJsonStreamToResultSet() additional field:"+field+" not in key list.");
					}
				}
				if (obj.getString("PATIENT_IDE")!=null) {
					rs.updateString("PATIENT_IDE", obj.getString("PATIENT_IDE"));
				}
				logger.trace("convertJsonStreamToResultSet() adding `id`:"+id+" to `entryMap`");
				entryMap.put(id, rs.getRow());
			}

		}
		logger.trace("convertJsonStreamToResultSet() Finished. Returning result");
		return rs;
	}

	private ResultSet createInitialDataset(Result result,
			Map<String, String> aliasMap) throws ResultSetException {
		logger.debug("createInitialDataset() Starting");

		ResultSet rs = (ResultSet) result.getData();

		// Set up the columns
		logger.debug("createInitialDataset() creating PATIENT_NUM column");
		Column column = new Column();
		column.setName("PATIENT_NUM");
		column.setDataType(PrimitiveDataType.STRING);
		rs.appendColumn(column);

		for (String aliasKey : aliasMap.keySet()) {
			Column newColumn = new Column();
			if (aliasMap.get(aliasKey) == null) {
				newColumn.setName(aliasKey);
			} else {
				newColumn.setName(aliasMap.get(aliasKey));
			}
			newColumn.setDataType(PrimitiveDataType.STRING);
			logger.debug("createInitialDataset() adding new column:"+newColumn.getName());
			rs.appendColumn(newColumn);
		}

		// Adding mapped PATIENT_IDE

		Column newColumn2 = new Column();
		newColumn2.setName("PATIENT_IDE");
		newColumn2.setDataType(PrimitiveDataType.STRING);
		rs.appendColumn(newColumn2);
		logger.debug("createInitialDataset() adding new column:"+newColumn2.getName());

		logger.debug("createInitialDataset() calling setData()");
		result.setData(rs);
		logger.debug("createInitialDataset() Finished");
		return rs;
	}

	private JsonObject convertStreamToObject(JsonParser parser) {
		logger.debug("convertStreamToObject() Starting...");

		JsonObjectBuilder build = Json.createObjectBuilder();
		String key = null;
		boolean endObj = false;
		while (parser.hasNext() && !endObj) {
			Event event = parser.next();
			switch (event) {
			case KEY_NAME:
				key = parser.getString();
				logger.trace("convertStreamToObject() key:"+key);

				break;
			case VALUE_STRING:
				logger.trace("convertStreamToObject() value(STRING):"+parser.getString());
				build.add(key, parser.getString());
				logger.trace("convertStreamToObject() key is reset");
				key = null;
				break;
			case VALUE_NUMBER:
				logger.trace("convertStreamToObject() value(NUMBER):"+parser.getBigDecimal());
				build.add(key, parser.getBigDecimal());
				logger.trace("convertStreamToObject() key is reset");
				key = null;
				break;
			case VALUE_TRUE:
				logger.trace("convertStreamToObject() value(BOOLEAN):true");
				build.add(key, true);
				logger.trace("convertStreamToObject() key is reset");
				key = null;
				break;
			case VALUE_FALSE:
				logger.trace("convertStreamToObject() value(BOOLEAN):false");
				build.add(key, false);
				logger.trace("convertStreamToObject() key is reset");
				key = null;
				break;
			case END_OBJECT:
				logger.trace("convertStreamToObject() end object");
				endObj = true;
				break;
			default:
				logger.trace("convertStreamToObject() unknown `event` type:"+event.toString());
			}
		}
		logger.debug("convertStreamToObject() Finished. Returning `JsonObject`");
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
		logger.debug("searchObservationOnly() Starting...");
		try {
			URI uri = new URI(this.transmartURL.split("://")[0],
					this.transmartURL.split("://")[1].split("/")[0], "/"
							+ this.transmartURL.split("://")[1].split("/")[1]
							+ "/textSearch/findPaths", "oblyObs=" + onlObs
							+ "&term=" + searchTerm, null);

			HttpClient client = createClient(session);
			HttpGet get = new HttpGet(uri);
			logger.debug("searchObservationOnly() calling `uri`:"+uri.toString());
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
		logger.debug("searchObservationOnly() Finished. Returning `entities`");
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

		return singleReturnMyPath;
	}
}
