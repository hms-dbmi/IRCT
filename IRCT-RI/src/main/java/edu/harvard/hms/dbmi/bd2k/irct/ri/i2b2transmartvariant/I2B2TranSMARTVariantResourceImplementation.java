/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2transmartvariant;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;

import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.action.ActionState;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Path;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.PrimitiveDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.ClauseAbstract;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.SelectClause;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.WhereClause;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Column;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.FileResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.I2B2XMLResourceImplementation;

/**
 * An implementation of a resource that communicates with the tranSMART
 * instance. It extends the i2b2 XML resource implementation.
 * 
 */
public class I2B2TranSMARTVariantResourceImplementation extends
		I2B2XMLResourceImplementation {
	private String tranSMARTuserName;
	private String tranSMARTpassword;
	private String baseURL;

	@Override
	public void setup(Map<String, String> parameters) {
		this.tranSMARTuserName = parameters.get("tranSMARTusername");
		this.tranSMARTpassword = parameters.get("tranSMARTpassword");
		this.baseURL = parameters.get("baseURL");

		super.setup(parameters);

	}

	@Override
	public String getType() {
		return "i2b2/tranSMART";
	}

	@Override
	public List<Path> getPathRelationship(Path path,
			OntologyRelationship relationship)
			throws ResourceInterfaceException {
		try {
			HttpClient client = login();
			super.setClient(client);
			List<Path> paths = super.getPathRelationship(path, relationship);
			String self = path.getPui()
					.replaceAll(super.getServerName() + "/", "")
					.replace('/', '\\');
			if (!self.equals(getServerName())) {
				HttpPost post = new HttpPost(this.baseURL
						+ "/chart/childConceptPatientCounts");
				List<NameValuePair> formParameters = new ArrayList<NameValuePair>();
				formParameters.add(new BasicNameValuePair("charttype",
						"childconceptpatientcounts"));
				formParameters.add(new BasicNameValuePair("concept_key", self));
				formParameters.add(new BasicNameValuePair("concept_level", ""));
				post.setEntity(new UrlEncodedFormEntity(formParameters));
				HttpResponse response = client.execute(post);

				JsonReader jsonReader = Json.createReader(response.getEntity()
						.getContent());
				JsonObject counts = jsonReader.readObject().getJsonObject(
						"counts");

				for (Path singlePath : paths) {
					String i2b2Path = singlePath.getPui()
							.replaceAll(getServerName() + "/", "")
							.replace('/', '\\').substring(2);
					i2b2Path = i2b2Path.substring(i2b2Path.indexOf("\\"));
					if (counts.containsKey(i2b2Path)) {
						singlePath.getCounts().put("count",
								counts.getInt(i2b2Path));
					}
				}
				jsonReader.close();
			}
			return paths;
		} catch (KeyManagementException | NoSuchAlgorithmException
				| IOException e) {
			throw new ResourceInterfaceException(
					"Error logging into tranSMART server");
		}
	}

	@Override
	public ResultSet getResults(ActionState as)
			throws ResourceInterfaceException {
		return null;
	}

	@Override
	public ActionState runQuery(Query query) throws ResourceInterfaceException {
		String gatherAllEncounterFacts = "false";
		try {
			super.setClient(login());
			ActionState actionState = super.runQuery(query);
			// Get results and save them locally
			Map<String, String> selects = new HashMap<String, String>();
			String parameters = "";
			for (ClauseAbstract clause : query.getClauses().values()) {
				if (clause instanceof SelectClause) {
					SelectClause selectClaues = (SelectClause) clause;
					String pui = selectClaues.getParameter().getPui()
							.replaceAll(getServerName() + "/", "");
					pui = pui.substring(pui.indexOf("/", 3));
					if (!parameters.equals("")) {
						parameters += "|";
					}
					parameters += pui;
					selects.put(pui, selectClaues.getParameter().getName());
				}
				if (clause instanceof WhereClause) {
					WhereClause whereClause = (WhereClause) clause;
					String encounter = whereClause.getValues().get("encounter");
					if((encounter != null) && (encounter.equalsIgnoreCase("yes"))) {
						gatherAllEncounterFacts = "true";
					}
				}
			}

			if (parameters.equals("")) {
				actionState.setResults(super.getResults(actionState));
				actionState.setComplete(true);
			} else {
				System.out.println(this.baseURL
						+ "/ClinicalData/retrieveClinicalData?rid="
						+ actionState.getResourceId() + "&conceptPaths="
						+ URLEncoder.encode(parameters, "UTF-8") + "&gatherAllEncounterFacts=" + gatherAllEncounterFacts);
				HttpGet get = new HttpGet(this.baseURL
						+ "/ClinicalData/retrieveClinicalData?rid="
						+ actionState.getResourceId() + "&conceptPaths="
						+ URLEncoder.encode(parameters, "UTF-8") + "&gatherAllEncounterFacts=" + gatherAllEncounterFacts);
				HttpResponse response = super.getClient().execute(get);
				JsonReader reader = Json.createReader(response.getEntity()
						.getContent());
				JsonArray results = reader.readArray();
				if(gatherAllEncounterFacts.equalsIgnoreCase("true")) {
					actionState.setResults(convertJsonToPivotResultSetonEncounter(results, true));	
				} else {
					actionState.setResults(convertJsonToPivotResultSetonEncounter(results, false));
				}
				reader.close();
				actionState.setComplete(true);
			}

			return actionState;
		} catch (KeyManagementException | NoSuchAlgorithmException
				| IOException e) {
			throw new ResourceInterfaceException(
					"Error logging into tranSMART server");
		}
	}

	private ResultSet convertJsonToPivotResultSetonEncounter(JsonArray results, boolean onEncounter) {
		FileResultSet mrs = new FileResultSet();

		Pattern pattern = Pattern.compile("\\\\[0-9][0-9]_(?!.*\\\\[0-9][0-9]_*)");
		
		Set<String> columns = new HashSet<String>();
		columns.add("PATIENT_NUM");
		if(onEncounter) {
			columns.add("ENCOUNTER_NUM");
		}
		
		Map<String, Map<String, String>> rawData = new HashMap<String, Map<String, String>>();

		if (results.size() == 0) {
			return mrs;
		}

		for (JsonValue val : results) {
			JsonObject obj = (JsonObject) val;
			String pivotString = obj.getString("PATIENT_NUM");
			if(onEncounter) {
				pivotString = obj.getString("ENCOUNTER_NUM");
			}
			
			if(!rawData.containsKey(pivotString)) {
				Map<String, String> entryMap = new HashMap<String, String>();
				entryMap.put("PATIENT_NUM", obj.getString("PATIENT_NUM"));
				if(onEncounter) {
					entryMap.put("ENCOUNTER_NUM", obj.getString("ENCOUNTER_NUM"));
					if(obj.containsKey("RACE_CD")) {
						entryMap.put("RACE_CD", obj.getString("RACE_CD"));
						columns.add("RACE_CD");
					}
				}
				rawData.put(pivotString, entryMap);
			}
			
			String columnString = obj.getString("CONCEPT_PATH");
			if(onEncounter) {
				Matcher matcher = pattern.matcher(columnString);
				if(matcher.find()) {
					int end = columnString.indexOf("\\", matcher.end());
					columnString = columnString.substring(0, end);
				}
			}
			columns.add(columnString);			
			
			rawData.get(pivotString).put(columnString, obj.getString("VALUE"));
		}
		
		try {
		
			for(String column : columns) {
				Column field = new Column();
				field.setName(column);
				field.setDataType(PrimitiveDataType.STRING);
				mrs.appendColumn(field);
			}
			
			for(String encounterId : rawData.keySet()) {
				mrs.appendRow();
				Map<String, String> pivotTable = rawData.get(encounterId);
				
				for(String column : columns) {
					String value = "";
					if(pivotTable.containsKey(column)) {
						value = pivotTable.get(column);	
					}
					mrs.updateString(column, value);
				}
			}
			
		} catch (ResultSetException | PersistableException e) {
			e.printStackTrace();
		}
		
		
		return mrs;
	}

	private HttpClient login() throws NoSuchAlgorithmException,
			KeyManagementException, ClientProtocolException, IOException {
		// SSL WRAPAROUND
		System.setProperty("jsse.enableSNIExtension", "false");

		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(
					java.security.cert.X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(
					java.security.cert.X509Certificate[] certs, String authType) {
			}
		} };

		SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sslContext
				.getSocketFactory());

		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
				sslContext, NoopHostnameVerifier.INSTANCE);

		Registry<ConnectionSocketFactory> r = RegistryBuilder
				.<ConnectionSocketFactory> create().register("https", sslsf)
				.build();

		HttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(
				r);

		// CLIENT CONNECTION
		BasicCookieStore cookieStore = new BasicCookieStore();
		HttpClient client = HttpClients.custom().setConnectionManager(cm)
				.setDefaultCookieStore(cookieStore).build();

		HttpPost loginHost = new HttpPost(baseURL + "/j_spring_security_check");

		List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
		urlParameters.add(new BasicNameValuePair("j_username",
				this.tranSMARTuserName));
		urlParameters.add(new BasicNameValuePair("j_password",
				this.tranSMARTpassword));
		loginHost.setEntity(new UrlEncodedFormEntity(urlParameters));

		client.execute(loginHost);

		return client;
	}

	@Override
	public Boolean editableReturnEntity() {
		return true;
	}
}
