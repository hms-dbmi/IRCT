package edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2transmart;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
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

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Path;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.ri.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.I2B2XMLResourceImplementation;

/**
 * An implementation of a resource that communicates with the tranSMART
 * instance. It extends the i2b2 XML resource implementation.
 * 
 */
public class I2B2TranSMARTResourceImplementation extends
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
			}
			return paths;
		} catch (KeyManagementException | NoSuchAlgorithmException
				| IOException e) {
			throw new ResourceInterfaceException(
					"Error logging into tranSMART server");
		}
	}

	@Override
	public ResultSet getResults(Long queryId) throws ResourceInterfaceException {

		return null;
	}

//	private ResultSet getColumns(Long queryId, String columnName,
//			String... paths) {
//		return null;
//	}

	@Override
	public Long runQuery(Query query) throws ResourceInterfaceException {
		try {
			super.setClient(login());
			Long runId = super.runQuery(query);

			return runId;
		} catch (KeyManagementException | NoSuchAlgorithmException
				| IOException e) {
			throw new ResourceInterfaceException(
					"Error logging into tranSMART server");
		}
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
}
