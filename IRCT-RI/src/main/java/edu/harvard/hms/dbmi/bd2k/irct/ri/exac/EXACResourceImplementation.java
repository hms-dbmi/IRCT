/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.ri.exac;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;

import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.action.ActionState;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Entity;
import edu.harvard.hms.dbmi.bd2k.irct.model.process.IRCTProcess;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.WhereClause;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ResourceState;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.PathResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.ProcessResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.QueryResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Column;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.FileResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

public class EXACResourceImplementation implements
		QueryResourceImplementationInterface,
		PathResourceImplementationInterface,
		ProcessResourceImplementationInterface {
	private String serverURL;
	private HttpClient client;

	@Override
	public void setup(Map<String, String> parameters) {
		setClient(HttpClientBuilder.create()
				.setRedirectStrategy(new LaxRedirectStrategy()).build());
		setServerURL(parameters.get("serverURL"));
	}

	@Override
	public String getType() {
		return "exAC";
	}

	@Override
	public JsonObject toJson() {
		return toJson(1);
	}

	@Override
	public JsonObject toJson(int depth) {
		depth--;
		JsonObjectBuilder returnJSON = Json.createObjectBuilder();
		returnJSON.add("type", this.getType());

		JsonArrayBuilder returnEntityArray = Json.createArrayBuilder();
		for (Entity rePath : getReturnEntity()) {
			returnEntityArray.add(rePath.toJson(depth));
		}
		returnJSON.add("returnEntity", returnEntityArray.build());
		returnJSON.add("editableReturnEntity", this.editableReturnEntity());

		return returnJSON.build();
	}


	@Override
	public List<Entity> getPathRelationship(Entity path,
			OntologyRelationship relationship, SecureSession session)
			throws ResourceInterfaceException {
		List<Entity> returns = new ArrayList<Entity>();

		if (path.getPui().equals("variant")) {
			
//			/rest/variant/variant/<variant_str>
			Entity variant = new Entity();
			variant.setName("Variant");
			variant.setPui("variant/variant");
			variant.setDataType(EXACDataType.VARIANT);
			returns.add(variant);
			
//			/rest/variant/base_coverage/<variant_str>
			Entity baseCoverage = new Entity();
			baseCoverage.setName("Base Coverage");
			baseCoverage.setPui("variant/base_coverage");
			baseCoverage.setDataType(EXACDataType.VARIANT);
			returns.add(baseCoverage);
			
//			/rest/variant/consequences/<variant_str>
			Entity consequences = new Entity();
			consequences.setName("Consequences");
			consequences.setPui("variant/consequences");
			consequences.setDataType(EXACDataType.VARIANT);
			returns.add(consequences);
			
//			/rest/variant/any_covered/<variant_str>
			Entity anyCovered = new Entity();
			anyCovered.setName("Any Covered");
			anyCovered.setPui("variant/any_covered");
			anyCovered.setDataType(EXACDataType.VARIANT);
			returns.add(anyCovered);
			
//			/rest/variant/ordered_csqs/<variant_str>
			Entity orderedCSQS = new Entity();
			orderedCSQS.setName("Ordered CSQS");
			orderedCSQS.setPui("variant/ordered_csqs");
			orderedCSQS.setDataType(EXACDataType.VARIANT);
			returns.add(orderedCSQS);
			
//			/rest/variant/metrics/<variant_str>
			Entity metrics = new Entity();
			metrics.setName("Metrics");
			metrics.setPui("variant/metrics");
			metrics.setDataType(EXACDataType.VARIANT);
			returns.add(metrics);
			
		} else if (path.getPui().equals("gene")) {
			Entity transcript = new Entity();
			transcript.setName("Transcripts");
			transcript.setPui("gene/transcript");
			transcript.setDataType(EXACDataType.GENE);
			returns.add(transcript);

			Entity geneVariants = new Entity();
			geneVariants.setName("Variants in Gene");
			geneVariants.setPui("gene/variants_in_gene");
			geneVariants.setDataType(EXACDataType.GENE);
			returns.add(geneVariants);

			Entity transcriptVariants = new Entity();
			transcriptVariants.setName("Variants in Transcript");
			transcriptVariants.setPui("gene/variants_in_transcript");
			transcriptVariants.setDataType(EXACDataType.GENE);
			returns.add(transcriptVariants);

			Entity transcriptGenes = new Entity();
			transcriptGenes.setName("Transcripts in Gene");
			transcriptGenes.setPui("gene/transcripts_in_gene");
			transcriptGenes.setDataType(EXACDataType.GENE);
			returns.add(transcriptGenes);

			Entity coverageStats = new Entity();
			coverageStats.setName("Coverage Stats");
			coverageStats.setPui("gene/coverage_stats");
			coverageStats.setDataType(EXACDataType.GENE);
			returns.add(coverageStats);
		} else if (path.getPui().equals("transcript")) {
//			/rest/transcript/transcript/<transcript_id>
			Entity transcript = new Entity();
			transcript.setName("Transcript");
			transcript.setPui("transcript/transcript");
			transcript.setDataType(EXACDataType.TRANSCRIPT);
			returns.add(transcript);
			
//			/rest/transcript/variants_in_transcript/<transcript_id>
			Entity variantsInTranscript = new Entity();
			variantsInTranscript.setName("Variants in Transcript");
			variantsInTranscript.setPui("transcript/variants_in_transcript");
			variantsInTranscript.setDataType(EXACDataType.TRANSCRIPT);
			returns.add(variantsInTranscript);
			
//			/rest/transcript/coverage_stats/<transcript_id>
			Entity coverageStats = new Entity();
			coverageStats.setName("Coverage Stats");
			coverageStats.setPui("transcript/coverage_stats");
			coverageStats.setDataType(EXACDataType.TRANSCRIPT);
			returns.add(coverageStats);
			
//			/rest/transcript/gene/<transcript_id>
			Entity gene = new Entity();
			gene.setName("Gene");
			gene.setPui("transcript/gene");
			gene.setDataType(EXACDataType.VARIANT);
			returns.add(gene);
		} else if (path.getPui().equals("region")) {
//			/rest/region/genes_in_region/<region_id>
			Entity genesInRegion = new Entity();
			genesInRegion.setName("Genes in region");
			genesInRegion.setPui("region/genes_in_region");
			genesInRegion.setDataType(EXACDataType.REGION);
			returns.add(genesInRegion);
			
//			/rest/region/variants_in_region/<region_id>
			Entity variantsInRegion = new Entity();
			variantsInRegion.setName("Variants in region");
			variantsInRegion.setPui("region/variants_in_region");
			variantsInRegion.setDataType(EXACDataType.REGION);
			returns.add(variantsInRegion);
			
//			/rest/region/coverage_array/<region_id>
			Entity coverageArray = new Entity();
			coverageArray.setName("Coverage Array");
			coverageArray.setPui("region/coverage_array");
			coverageArray.setDataType(EXACDataType.REGION);
			returns.add(coverageArray);
		}

		return returns;
	}


	@Override
	public ActionState runQuery(Query qep) throws ResourceInterfaceException {
		ActionState actionState = new ActionState();
		
		
		WhereClause whereClause = (WhereClause) qep.getClauses()
				.values().iterator().next();

		if (whereClause.getPredicateType().getName().equals("BYQUERY")) {
			String service = whereClause.getField().getPui().split("/")[1];
			HttpGet get = new HttpGet(getServerURL() + "/rest/awesome?query="
					+ whereClause.getValues().get("query") + "&service="
					+ service);

			try {
				HttpResponse response = client.execute(get);
				JsonReader reader = Json.createReader(response.getEntity()
						.getContent());
				JsonArray results = reader.readArray();
				
				actionState.setResults(convertJsonArrayToResultSet(results));
				actionState.setComplete(true);
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (whereClause.getPredicateType().getName().equals("BYENSEMBL")) {
			HttpGet get = new HttpGet(getServerURL() + "/rest/" + whereClause.getField().getPui() + "/" + whereClause.getValues().get("ensembl"));

			try {
				HttpResponse response = client.execute(get);
				JsonReader reader = Json.createReader(response.getEntity()
						.getContent());
				JsonArray results = reader.readArray();
				
				actionState.setResults(convertJsonArrayToResultSet(results));
				actionState.setComplete(true);
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (whereClause.getPredicateType().getName().equals("BYREGION")) {
			String service = getServerURL() + "/rest/region/" + whereClause.getValues().get("chromosome") + "-" + whereClause.getValues().get("start");
			if (whereClause.getValues().containsKey("stop")) {
				service += "-" + whereClause.getValues().get("stop");
			}
			HttpGet get = new HttpGet(service);
			
			try {
				HttpResponse response = client.execute(get);
				JsonReader reader = Json.createReader(response.getEntity()
						.getContent());
				JsonArray results = reader.readArray();
				
				actionState.setResults(convertJsonArrayToResultSet(results));
				actionState.setComplete(true);
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return actionState;
	}
	
	@Override
	public ActionState runProcess(IRCTProcess pep) {
		ActionState actionState = new ActionState();
		
		ResultSet rs = pep.getResultSets().get("ResultSet");
		String chromColumn = pep.getValues().get("Chromosome");
		String startColumn = pep.getValues().get("Start");
		String refColumn = pep.getValues().get("Reference");
		String altColumn = pep.getValues().get("Alternative");
		
		
		
		try {
			Set<String> columns = new HashSet<String>();
			for(Column col : rs.getColumns()) {
				columns.add(col.getName());
			}
			
			List<Map<String, String>> rawData = new ArrayList<Map<String, String>>();
			
			rs.beforeFirst();
			while(rs.next()) {
				Map<String, String> entry = new HashMap<String, String>();
				
				String chrom = rs.getString(chromColumn);
				String start = rs.getString(startColumn);
				String ref = rs.getString(refColumn);
				String alt = rs.getString(altColumn);
				
				
				for(Column column : rs.getColumns()) {
					entry.put(column.getName(), rs.getString(column.getName()));
				}

				if(!alt.equals("-") && !ref.equals("-")) {
					String url = getServerURL() + "/rest/variant/variant/" + chrom + "-" + start + "-" + ref + "-" + alt;
					System.out.println(url);
				
					HttpGet get = new HttpGet(url);
				
					HttpResponse response = client.execute(get);
					JsonReader reader = Json.createReader(response.getEntity().getContent());
					JsonObject varObj = reader.readObject();
					
					List<String> fields = getNames("", varObj);
					columns.addAll(fields);
					
					
					for(String field : fields) {
						entry.put(field, getValue(varObj, field));
					}
					rawData.add(entry);
					
					reader.close();
				}
			}
			
			FileResultSet mrs = new FileResultSet();
			for(String field : columns) {
				Column column = new Column();
				column.setName(field);
				column.setDataType(PrimitiveDataType.STRING);
				mrs.appendColumn(column);
			}
			
			for(Map<String, String> entry : rawData) {
				mrs.appendRow();
				for(String field : columns) {
					String value = entry.get(field);
					if(value == null) {
						value = "";
					}
					mrs.updateString(field, value);
				}
			}
			actionState.setResults(mrs);
			actionState.setComplete(true);
			
		} catch (ResultSetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PersistableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return actionState;
	}

	@Override
	public ResultSet getResults(ActionState actionState) throws ResourceInterfaceException {
		return null;
	}
	
	@Override
	public List<Entity> searchPaths(Entity path, String searchTerm, SecureSession session)
			throws ResourceInterfaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Entity> searchOntology(Entity path, String ontologyType,
			String ontologyTerm, SecureSession session) throws ResourceInterfaceException {
		// TODO Auto-generated method stub
		return null;
	}
	
	private ResultSet convertJsonArrayToResultSet(JsonArray results) {
		FileResultSet mrs = new FileResultSet();

		try {
			if (results.size() == 0) {
				return mrs;
			}
			// Setup columns
			JsonObject columnObject = (JsonObject) results.get(0);
			
			List<String> fields = getNames("", columnObject);
			for(String field : fields) {
				Column column = new Column();
				column.setName(field);
				column.setDataType(PrimitiveDataType.STRING);
				mrs.appendColumn(column);
			}
			
			// Add data
			for(JsonValue val : results) {
				JsonObject obj = (JsonObject) val;
				mrs.appendRow();
				for(String field : fields) {
//					JsonValue jv = getValue(obj, field);
//					if(jv == null) {
//						mrs.updateString(field, "");
//					} else if(jv.getValueType() == ValueType.NUMBER) {
//						mrs.updateLong(field, ((JsonNumber) jv).longValue());
//					} else if(jv.getValueType() == ValueType.STRING) {
//						mrs.updateString(field, ((JsonString) jv).getString());
//					} else {
//						mrs.updateString(field, jv.toString());
//					}
					mrs.updateString(field, getValue(obj, field));
				}
			}
		} catch (ResultSetException | PersistableException e) {
			e.printStackTrace();
		}

		return mrs;
	}
	
	private String getValue(JsonObject obj, String field) {
		if(field.contains(".")) {
			String thisField = field.split("\\.")[0];
			String remaining = field.replaceFirst(thisField + ".", "");
			return getValue(obj.getJsonObject(thisField), remaining);
		}
		if(obj.containsKey(field)) {
			ValueType vt = obj.get(field).getValueType();
			if(vt == ValueType.NUMBER) {
				return obj.getJsonNumber(field).toString();
			}
			return obj.getString(field);
//			return obj.get(field);
		}
		return "";
	}

	private List<String> getNames(String prefix, JsonObject obj) {
		List<String> returns = new ArrayList<String>();
		for(String field : obj.keySet()) {
			if(obj.get(field).getValueType() == ValueType.OBJECT) {
				if(prefix.equals("")) {
					returns.addAll(getNames(field, obj.getJsonObject(field)));
				} else {
					returns.addAll(getNames(prefix + "." + field, obj.getJsonObject(field)));
				}
			} else if(obj.get(field).getValueType() != ValueType.ARRAY){
				if(prefix.equals("")) {
					returns.add(field);
				} else {
					returns.add(prefix + "." + field);
				}
			}
		}
		
		return returns;
	}

	@Override
	public ResourceState getState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Entity> getReturnEntity() {
		return new ArrayList<Entity>();
	}

	@Override
	public Boolean editableReturnEntity() {
		return false;
	}

	/**
	 * @return the client
	 */
	public HttpClient getClient() {
		return client;
	}

	/**
	 * @param client
	 *            the client to set
	 */
	public void setClient(HttpClient client) {
		this.client = client;
	}

	/**
	 * @return the serverURL
	 */
	public String getServerURL() {
		return serverURL;
	}

	/**
	 * @param serverURL
	 *            the serverURL to set
	 */
	public void setServerURL(String serverURL) {
		this.serverURL = serverURL;
	}
}
