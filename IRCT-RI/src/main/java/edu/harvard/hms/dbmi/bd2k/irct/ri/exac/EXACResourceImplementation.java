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
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyType;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Path;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.PrimitiveDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.process.IRCTProcess;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.WhereClause;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.PathResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ProcessResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.QueryResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ResourceState;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Column;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.FileResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;

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
		for (Path rePath : getReturnEntity()) {
			returnEntityArray.add(rePath.toJson(depth));
		}
		returnJSON.add("returnEntity", returnEntityArray.build());
		returnJSON.add("editableReturnEntity", this.editableReturnEntity());

		return returnJSON.build();
	}

	@Override
	public List<Path> getPathRoot() {
		List<Path> roots = new ArrayList<Path>();

		Path variant = new Path();
		variant.setName("Variant");
		variant.setPui("variant");
		variant.setDataType(EXACDataType.VARIANT);
		roots.add(variant);

		Path gene = new Path();
		gene.setName("Gene");
		gene.setPui("gene");
		gene.setDataType(EXACDataType.GENE);
		roots.add(gene);

		Path transcript = new Path();
		transcript.setName("Transcript");
		transcript.setPui("transcript");
		transcript.setDataType(EXACDataType.TRANSCRIPT);
		roots.add(transcript);

		Path region = new Path();
		region.setName("Region");
		region.setPui("region");
		region.setDataType(EXACDataType.REGION);
		roots.add(region);
		
		return roots;
	}

	@Override
	public List<OntologyRelationship> relationships() {
		List<OntologyRelationship> relationships = new ArrayList<OntologyRelationship>();
		relationships.add(EXACOntologyRelationship.CHILD);
		relationships.add(EXACOntologyRelationship.PARENT);
		return relationships;
	}

	@Override
	public OntologyRelationship getRelationshipFromString(String relationship) {
		return EXACOntologyRelationship.valueOf(relationship);
	}

	@Override
	public List<Path> getPathRelationship(Path path,
			OntologyRelationship relationship)
			throws ResourceInterfaceException {
		List<Path> returns = new ArrayList<Path>();

		if (path.getPui().equals("variant")) {
			
//			/rest/variant/variant/<variant_str>
			Path variant = new Path();
			variant.setName("Variant");
			variant.setPui("variant/variant");
			variant.setDataType(EXACDataType.VARIANT);
			returns.add(variant);
			
//			/rest/variant/base_coverage/<variant_str>
			Path baseCoverage = new Path();
			baseCoverage.setName("Base Coverage");
			baseCoverage.setPui("variant/base_coverage");
			baseCoverage.setDataType(EXACDataType.VARIANT);
			returns.add(baseCoverage);
			
//			/rest/variant/consequences/<variant_str>
			Path consequences = new Path();
			consequences.setName("Consequences");
			consequences.setPui("variant/consequences");
			consequences.setDataType(EXACDataType.VARIANT);
			returns.add(consequences);
			
//			/rest/variant/any_covered/<variant_str>
			Path anyCovered = new Path();
			anyCovered.setName("Any Covered");
			anyCovered.setPui("variant/any_covered");
			anyCovered.setDataType(EXACDataType.VARIANT);
			returns.add(anyCovered);
			
//			/rest/variant/ordered_csqs/<variant_str>
			Path orderedCSQS = new Path();
			orderedCSQS.setName("Ordered CSQS");
			orderedCSQS.setPui("variant/ordered_csqs");
			orderedCSQS.setDataType(EXACDataType.VARIANT);
			returns.add(orderedCSQS);
			
//			/rest/variant/metrics/<variant_str>
			Path metrics = new Path();
			metrics.setName("Metrics");
			metrics.setPui("variant/metrics");
			metrics.setDataType(EXACDataType.VARIANT);
			returns.add(metrics);
			
		} else if (path.getPui().equals("gene")) {
			Path transcript = new Path();
			transcript.setName("Transcripts");
			transcript.setPui("gene/transcript");
			transcript.setDataType(EXACDataType.GENE);
			returns.add(transcript);

			Path geneVariants = new Path();
			geneVariants.setName("Variants in Gene");
			geneVariants.setPui("gene/variants_in_gene");
			geneVariants.setDataType(EXACDataType.GENE);
			returns.add(geneVariants);

			Path transcriptVariants = new Path();
			transcriptVariants.setName("Variants in Transcript");
			transcriptVariants.setPui("gene/variants_in_transcript");
			transcriptVariants.setDataType(EXACDataType.GENE);
			returns.add(transcriptVariants);

			Path transcriptGenes = new Path();
			transcriptGenes.setName("Transcripts in Gene");
			transcriptGenes.setPui("gene/transcripts_in_gene");
			transcriptGenes.setDataType(EXACDataType.GENE);
			returns.add(transcriptGenes);

			Path coverageStats = new Path();
			coverageStats.setName("Coverage Stats");
			coverageStats.setPui("gene/coverage_stats");
			coverageStats.setDataType(EXACDataType.GENE);
			returns.add(coverageStats);
		} else if (path.getPui().equals("transcript")) {
//			/rest/transcript/transcript/<transcript_id>
			Path transcript = new Path();
			transcript.setName("Transcript");
			transcript.setPui("transcript/transcript");
			transcript.setDataType(EXACDataType.TRANSCRIPT);
			returns.add(transcript);
			
//			/rest/transcript/variants_in_transcript/<transcript_id>
			Path variantsInTranscript = new Path();
			variantsInTranscript.setName("Variants in Transcript");
			variantsInTranscript.setPui("transcript/variants_in_transcript");
			variantsInTranscript.setDataType(EXACDataType.TRANSCRIPT);
			returns.add(variantsInTranscript);
			
//			/rest/transcript/coverage_stats/<transcript_id>
			Path coverageStats = new Path();
			coverageStats.setName("Coverage Stats");
			coverageStats.setPui("transcript/coverage_stats");
			coverageStats.setDataType(EXACDataType.TRANSCRIPT);
			returns.add(coverageStats);
			
//			/rest/transcript/gene/<transcript_id>
			Path gene = new Path();
			gene.setName("Gene");
			gene.setPui("transcript/gene");
			gene.setDataType(EXACDataType.VARIANT);
			returns.add(gene);
		} else if (path.getPui().equals("region")) {
//			/rest/region/genes_in_region/<region_id>
			Path genesInRegion = new Path();
			genesInRegion.setName("Genes in region");
			genesInRegion.setPui("region/genes_in_region");
			genesInRegion.setDataType(EXACDataType.REGION);
			returns.add(genesInRegion);
			
//			/rest/region/variants_in_region/<region_id>
			Path variantsInRegion = new Path();
			variantsInRegion.setName("Variants in region");
			variantsInRegion.setPui("region/variants_in_region");
			variantsInRegion.setDataType(EXACDataType.REGION);
			returns.add(variantsInRegion);
			
//			/rest/region/coverage_array/<region_id>
			Path coverageArray = new Path();
			coverageArray.setName("Coverage Array");
			coverageArray.setPui("region/coverage_array");
			coverageArray.setDataType(EXACDataType.REGION);
			returns.add(coverageArray);
		}

		return returns;
	}

	@Override
	public OntologyType getOntologyType() {
		return OntologyType.TREE;
	}

	@Override
	public Path getPathFromString(String path) {
		Path pathObj = new Path();
		pathObj.setPui(path);
		return pathObj;
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
				
				actionState.setResults(convertJsonToResultSet(results));
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
				
				actionState.setResults(convertJsonToResultSet(results));
				actionState.setComplete(true);
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (whereClause.getPredicateType().getName().equals("BYREGION")) {
			
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

	private ResultSet convertJsonToResultSet(JsonArray results) {
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
	public List<Path> getReturnEntity() {
		return new ArrayList<Path>();
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
