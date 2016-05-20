/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.ri.exac;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Entity;
import edu.harvard.hms.dbmi.bd2k.irct.model.process.IRCTProcess;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.ClauseAbstract;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.WhereClause;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ResourceState;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.PathResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.ProcessResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.QueryResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultStatus;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.Column;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.FileResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.ResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

public class EXACResourceImplementation implements
		QueryResourceImplementationInterface,
		PathResourceImplementationInterface,
		ProcessResourceImplementationInterface {

	private ResourceState resourceState;
	private String resourceName;
	private String resourceURL;
	private String[] exacColumns = { "allele_count", "allele_freq", "allele_num",
			"alt", "chrom", "filter", "hom_count", "pop_acs.African",
			"pop_acs.East Asian", "pop_acs.European (Finnish)",
			"pop_acs.European (Non-Finnish)", "pop_acs.Latino",
			"pop_acs.Other", "pop_acs.South Asian", "pop_ans.African",
			"pop_ans.East Asian", "pop_ans.European (Finnish)",
			"pop_ans.European (Non-Finnish)", "pop_ans.Latino",
			"pop_ans.Other", "pop_ans.South Asian", "pop_homs.African",
			"pop_homs.East Asian", "pop_homs.European (Finnish)",
			"pop_homs.European (Non-Finnish)", "pop_homs.Latino",
			"pop_homs.Other", "pop_homs.South Asian", "pos",
			"quality_metrics.BaseQRankSum",
			"quality_metrics.ClippingRankSum", "quality_metrics.DP",
			"quality_metrics.FS", "quality_metrics.InbreedingCoeff",
			"quality_metrics.MQ", "quality_metrics.MQRankSum",
			"quality_metrics.QD", "quality_metrics.ReadPosRankSum",
			"quality_metrics.VQSLOD", "ref", "rsid", "site_quality",
			"variant_id", "xpos", "xstart", "xstop" };

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
		List<Entity> returns = new ArrayList<Entity>();
		String resourcePath = getResourcePathFromPUI(path.getPui());

		if (resourcePath.equals("/")) {
			// Variant
			returns.add(createEntity("Variant", "/variant",
					EXACDataType.VARIANT));
			// Gene
			returns.add(createEntity("Gene", "/gene", EXACDataType.GENE));
			// Transcript
			returns.add(createEntity("Transcript", "/transcript",
					EXACDataType.TRANSCRIPT));
			// Region
			returns.add(createEntity("Region", "/region", EXACDataType.REGION));
		} else if (resourcePath.equals("/variant")) {
			// Base Coverage
			returns.add(createEntity("Base Coverage", resourcePath + "/"
					+ "base_coverage", EXACDataType.VARIANT));
			// Consequences
			returns.add(createEntity("Consequences", resourcePath + "/"
					+ "consequences", EXACDataType.VARIANT));
			// Any Covered
			returns.add(createEntity("Any Covered", resourcePath + "/"
					+ "any_covered", EXACDataType.VARIANT));
			// Ordered CSQS
			returns.add(createEntity("Ordered CSQS", resourcePath + "/"
					+ "ordered_csqs", EXACDataType.VARIANT));
			// Metrics
			returns.add(createEntity("Metrics", resourcePath + "/" + "metrics",
					EXACDataType.VARIANT));
		} else if (resourcePath.equals("/gene")) {
			returns.add(createEntity("Transcripts", resourcePath + "/"
					+ "transcript", EXACDataType.GENE));
			returns.add(createEntity("Variants in Gene", resourcePath + "/"
					+ "variants_in_gene", EXACDataType.GENE));
			returns.add(createEntity("Variants in Transcript", resourcePath
					+ "/" + "variants_in_transcript", EXACDataType.GENE));
			returns.add(createEntity("Transcripts in Gene", resourcePath + "/"
					+ "transcripts_in_gene", EXACDataType.GENE));
			returns.add(createEntity("Coverage Stats", resourcePath + "/"
					+ "coverage_stats", EXACDataType.GENE));
		} else if (resourcePath.equals("/transcript")) {
			returns.add(createEntity("Variants in Transcript", resourcePath
					+ "/" + "variants_in_transcript", EXACDataType.TRANSCRIPT));
			returns.add(createEntity("Coverage Stats", resourcePath + "/"
					+ "coverage_stats", EXACDataType.TRANSCRIPT));
			returns.add(createEntity("Gene", resourcePath + "/" + "gene",
					EXACDataType.TRANSCRIPT));
		} else if (resourcePath.equals("/region")) {
			returns.add(createEntity("Genes in region", resourcePath + "/"
					+ "genes_in_region", EXACDataType.REGION));
			returns.add(createEntity("Variants in region", resourcePath + "/"
					+ "variants_in_region", EXACDataType.REGION));
			returns.add(createEntity("Coverage Array", resourcePath + "/"
					+ "coverage_array", EXACDataType.REGION));
		}

		return returns;
	}

	@Override
	public Result runQuery(SecureSession session, Query qep, Result result)
			throws ResourceInterfaceException {
		// TODO Auto-generated method stub
		HttpClient client = createClient(session);
		result.setResultStatus(ResultStatus.CREATED);

		// Check Clause
		if (qep.getClauses().size() != 1) {
			result.setResultStatus(ResultStatus.ERROR);
			result.setMessage("Wrong number of clauses");
			return result;
		}
		ClauseAbstract clause = qep.getClauses().values().iterator().next();

		if (!(clause instanceof WhereClause)) {
			result.setResultStatus(ResultStatus.ERROR);
			result.setMessage("Clause is not a where Clause");
			return result;
		}

		WhereClause whereClause = (WhereClause) clause;
		// Create Query
		String urlString = null;

		// BY ENSEMBL
		if (whereClause.getPredicateType().getName().equals("ENSEMBL")) {
			urlString = resourceURL + "/rest"
					+ getResourcePathFromPUI(whereClause.getField().getPui())
					+ "/" + whereClause.getStringValues().get("ENSEMBLID");

		} else if (whereClause.getPredicateType().getName().equals("QUERY")) {
			// BY QUERY
			String[] resourcePath = getResourcePathFromPUI(
					whereClause.getField().getPui()).split("/");
			String service = "";
			if (resourcePath.length >= 3) {
				service = "&service=" + resourcePath[2];
			}

			urlString = resourceURL + "/rest/awesome?query="
					+ whereClause.getStringValues().get("QUERY") + service;
		} else if (whereClause.getPredicateType().getName().equals("REGION")) {
			// BY REGION
			urlString = resourceURL + "/rest"
					+ getResourcePathFromPUI(whereClause.getField().getPui())
					+ "/" + whereClause.getStringValues().get("CHROMOSOME")
					+ "-" + whereClause.getStringValues().get("START");
			if (whereClause.getStringValues().containsKey("STOP")) {
				urlString += "-" + whereClause.getStringValues().get("STOP");
			}

		} else if (whereClause.getPredicateType().getName().equals("VARIANT")) {
			// BY VARIANT
			urlString = resourceURL + "/rest"
					+ getResourcePathFromPUI(whereClause.getField().getPui())
					+ "/" + whereClause.getStringValues().get("CHROMOSOME")
					+ "-" + whereClause.getStringValues().get("POSITION") + "-"
					+ whereClause.getStringValues().get("REFERENCE") + "-"
					+ whereClause.getStringValues().get("VARIANT");
		}
		// Run Query
		if (urlString == null) {
			result.setResultStatus(ResultStatus.ERROR);
			result.setMessage("Unknown predicate");
			return result;
		}

		HttpGet get = new HttpGet(urlString);
		try {
			HttpResponse response = client.execute(get);
			JsonReader reader = Json.createReader(response.getEntity()
					.getContent());
			JsonStructure results = reader.read();
			if (results.getValueType().equals(ValueType.ARRAY)) {
				result = convertJsonArrayToResultSet((JsonArray) results,
						result);
			} else {
				result = convertJsonObjectToResultSet((JsonObject) results,
						result);
			}
			reader.close();
			result.setResultStatus(ResultStatus.COMPLETE);
		} catch (IOException e) {
			result.setResultStatus(ResultStatus.ERROR);
			result.setMessage(e.getMessage());
		}

		// Format Results

		return result;
	}

	@Override
	public Result runProcess(SecureSession session, IRCTProcess process,
			Result result) throws ResourceInterfaceException {
		HttpClient client = createClient(session);
		try {
			ResultSet resultSetField = (ResultSet) process.getObjectValues()
					.get("RESULTSET");
			String chromosomeColumn = process.getStringValues().get(
					"CHROMOSOME");
			String positionColumn = process.getStringValues().get("POSITION");
			String referenceColumn = process.getStringValues().get("REFERENCE");
			String variantColumn = process.getStringValues().get("VARIANT");

			ResultSet rs = createResultSet(result, resultSetField);
			
			// Move to First
			resultSetField.first();
			// Loop through all rows and get the data needed for the bulk
			// request
			resultSetField.beforeFirst();
			JsonArrayBuilder jsonArray = Json.createArrayBuilder();
			while (resultSetField.next()) {

				String queryString = resultSetField.getString(chromosomeColumn);
				queryString += "-" + resultSetField.getString(positionColumn);
				queryString += "-" + resultSetField.getString(referenceColumn);
				queryString += "-" + resultSetField.getString(variantColumn);

				// Run the Bulk request(s)
				jsonArray.add(queryString);

			}

			HttpPost post = new HttpPost(this.resourceURL + "/rest/bulk/variant");

			// Set Header
			try {
				post.setEntity(new StringEntity(jsonArray.build().toString()));
				HttpResponse response = client.execute(post);
				JsonReader reader = Json.createReader(response.getEntity()
						.getContent());
				JsonObject responseObject = reader.readObject();
				
				//Merge the results back into the result set
				resultSetField.beforeFirst();
				rs.first();
				while (resultSetField.next()) {
					rs.appendRow();
					//Copy the original data over
					for(Column column : resultSetField.getColumns()) {
						rs.updateString(column.getName(), resultSetField.getString(column.getName()));
					}
					//Add the new data if it exists
					String queryString = resultSetField.getString(chromosomeColumn);
					queryString += "-" + resultSetField.getString(positionColumn);
					queryString += "-" + resultSetField.getString(referenceColumn);
					queryString += "-" + resultSetField.getString(variantColumn);
					
					if(responseObject.containsKey(queryString)) {
						JsonObject varObject = responseObject.getJsonObject(queryString).getJsonObject("variant");
						for(String newColumnString : this.exacColumns) {
							String value = getValue(varObject, newColumnString);
							if(value != null) {
								rs.updateString(newColumnString, value.toString());
							}
						}
					}
				}
				
				result.setData(rs);
				result.setResultStatus(ResultStatus.COMPLETE);
			} catch (IOException | PersistableException e) {
				e.printStackTrace();
				result.setResultStatus(ResultStatus.ERROR);
				result.setMessage(e.getMessage());
			}

		} catch (ResultSetException e) {
			e.printStackTrace();
			result.setResultStatus(ResultStatus.ERROR);
			result.setMessage(e.getMessage());
		}

		return result;
	}

//	private JsonValue getValueFromJson(JsonObject varObject, String newColumnString) {
////		String[] columnPath = newColumnString.split("\\.");
//		
//		if(!newColumnString.contains(".") && (varObject.containsKey(newColumnString))) {
//			return varObject.get(newColumnString);
//		}
//		return null;
//	}
	
	

	private ResultSet createResultSet(Result result, ResultSet resultSetField) throws ResultSetException {
		ResultSet rs = (ResultSet) result.getData();
		
		for(Column column : resultSetField.getColumns()) {
			rs.appendColumn(column);
		}
		
		for(String newColumnString : this.exacColumns) {
			Column newColumn = new Column();
			newColumn.setName(newColumnString);
			newColumn.setDataType(PrimitiveDataType.STRING);
			rs.appendColumn(newColumn);
		}
		
		return rs;
	}

	@Override
	public Result getResults(SecureSession session, Result result)
			throws ResourceInterfaceException {
		if (result.getResultStatus() != ResultStatus.COMPLETE) {
			result.setResultStatus(ResultStatus.ERROR);
		}
		return result;
	}

	@Override
	public List<Entity> searchPaths(Entity path, String searchTerm,
			SecureSession session) throws ResourceInterfaceException {
		return new ArrayList<Entity>();
	}

	@Override
	public List<Entity> searchOntology(Entity path, String ontologyType,
			String ontologyTerm, SecureSession session)
			throws ResourceInterfaceException {
		return new ArrayList<Entity>();
	}

	@Override
	public String getType() {
		return "exac";
	}

	@Override
	public ResourceState getState() {
		return this.resourceState;
	}

	@Override
	public ResultDataType getQueryDataType(Query query) {
		// Check Clause
		if (query.getClauses().size() != 1) {
			return null;
		}
		ClauseAbstract clause = query.getClauses().values().iterator().next();

		if (!(clause instanceof WhereClause)) {
			return null;
		}

		WhereClause whereClause = (WhereClause) clause;
		String resourcePath = getResourcePathFromPUI(whereClause.getField()
				.getPui());

		if (resourcePath.split("/").length >= 2) {
			return ResultDataType.TABULAR;
		}

		return ResultDataType.JSON;
	}

	@Override
	public ResultDataType getProcessDataType(IRCTProcess pep) {
		return ResultDataType.TABULAR;
	}

	private Result convertJsonObjectToResultSet(JsonObject exacJSONResults,
			Result result) {
		// TODO: BUILD OUT
		return result;
	}

	private Result convertJsonArrayToResultSet(JsonArray exacJSONResults,
			Result result) {

		FileResultSet mrs = (FileResultSet) result.getData();
		try {
			if (exacJSONResults.size() == 0) {
				result.setData(mrs);
				return result;
			}
			// Setup columns
			JsonObject columnObject = (JsonObject) exacJSONResults.get(0);

			List<String> fields = getNames("", columnObject);
			for (String field : fields) {
				Column column = new Column();
				column.setName(field);
				column.setDataType(PrimitiveDataType.STRING);
				mrs.appendColumn(column);
			}

			// Add data
			for (JsonValue val : exacJSONResults) {
				JsonObject obj = (JsonObject) val;
				mrs.appendRow();
				for (String field : fields) {
					mrs.updateString(field, getValue(obj, field));
				}
			}
		} catch (ResultSetException | PersistableException e) {
			e.printStackTrace();
		}

		result.setData(mrs);
		return result;
	}

	private String getValue(JsonObject obj, String field) {
		if (field.contains(".")) {
			String thisField = field.split("\\.")[0];
			String remaining = field.replaceFirst(thisField + ".", "");
			return getValue(obj.getJsonObject(thisField), remaining);
		}
		if (obj.containsKey(field)) {
			ValueType vt = obj.get(field).getValueType();
			if (vt == ValueType.NUMBER) {
				return obj.getJsonNumber(field).toString();
			} else if (vt == ValueType.TRUE) {
				return "TRUE";
			} else if (vt == ValueType.FALSE) {
				return "FALSE";
			}
			return obj.getString(field);
		}
		return "";
	}

	private List<String> getNames(String prefix, JsonObject obj) {
		List<String> returns = new ArrayList<String>();
		for (String field : obj.keySet()) {
			if (obj.get(field).getValueType() == ValueType.OBJECT) {
				if (prefix.equals("")) {
					returns.addAll(getNames(field, obj.getJsonObject(field)));
				} else {
					returns.addAll(getNames(prefix + "." + field,
							obj.getJsonObject(field)));
				}
			} else if (obj.get(field).getValueType() != ValueType.ARRAY) {
				if (prefix.equals("")) {
					returns.add(field);
				} else {
					returns.add(prefix + "." + field);
				}
			}
		}

		return returns;
	}

	private String getResourcePathFromPUI(String pui) {
		String[] pathComponents = pui.split("/");

		if (pathComponents.length <= 2) {
			return "/";
		}
		String myPath = "";

		for (String pathComponent : Arrays.copyOfRange(pathComponents, 2,
				pathComponents.length)) {
			myPath += "/" + pathComponent;
		}

		return myPath;
	}

	private HttpClient createClient(SecureSession session) {
		HttpClientBuilder returns = HttpClientBuilder.create();
		return returns.build();
	};

	private Entity createEntity(String name, String resourceString,
			EXACDataType type) {

		Entity newEntity = new Entity();
		newEntity.setName(name);
		newEntity.setDisplayName(name);
		newEntity.setPui("/" + this.resourceName + resourceString);
		newEntity.setDataType(type);

		return newEntity;
	}
}
