/**
 * 
 */
package edu.harvard.hms.dbmi.bd2k.irct.ri.scidb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;

import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.find.FindInformationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Entity;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.model.process.IRCTProcess;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.ClauseAbstract;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.SelectClause;
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
import edu.harvard.hms.dbmi.bd2k.irct.security.SecurityUtility;
import edu.harvard.hms.dbmi.scidb.SciDB;
import edu.harvard.hms.dbmi.scidb.SciDBArray;
import edu.harvard.hms.dbmi.scidb.SciDBAttribute;
import edu.harvard.hms.dbmi.scidb.SciDBCommand;
import edu.harvard.hms.dbmi.scidb.SciDBDimension;
import edu.harvard.hms.dbmi.scidb.SciDBFilterFactory;
import edu.harvard.hms.dbmi.scidb.SciDBFunction;
import edu.harvard.hms.dbmi.scidb.SciDBListElement;
import edu.harvard.hms.dbmi.scidb.SciDBOperation;
import edu.harvard.hms.dbmi.scidb.exception.NotConnectedException;

/**
 * @author Jeremy R. Easton-Marks
 *
 */
public class SciDBResourceImplementation implements
		PathResourceImplementationInterface,
		QueryResourceImplementationInterface,
		ProcessResourceImplementationInterface {

	private String resourceName;
	private String clientId;
	private String namespace;
	private boolean ignoreCertificate;
	private String resourceURL;

	private ResourceState resourceState;

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.
	 * ResourceImplementationInterface#setup(java.util.Map)
	 */
	@Override
	public void setup(Map<String, String> parameters)
			throws ResourceInterfaceException {
		String[] strArray = { "resourceName", "resourceURL" };
		if (!parameters.keySet().containsAll(Arrays.asList(strArray))) {
			throw new ResourceInterfaceException("Missing parameters");
		}

		this.resourceName = parameters.get("resourceName");
		this.resourceURL = parameters.get("resourceURL");
		this.clientId = parameters.get("clientId");
		this.namespace = parameters.get("namespace");
		String certificateString = parameters.get("ignoreCertificate");

		if (certificateString != null && certificateString.equals("true")) {
			this.ignoreCertificate = true;
		} else {
			this.ignoreCertificate = false;
		}

		resourceState = ResourceState.READY;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.
	 * PathResourceImplementationInterface
	 * #getPathRelationship(edu.harvard.hms.dbmi
	 * .bd2k.irct.model.ontology.Entity,
	 * edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship,
	 * edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession)
	 */
	@Override
	public List<Entity> getPathRelationship(Entity path,
			OntologyRelationship relationship, SecureSession session)
			throws ResourceInterfaceException {
		List<Entity> entities = new ArrayList<Entity>();
		// Build
		HttpClient client = createClient(session);
		String basePath = path.getPui();
		String[] pathComponents = basePath.split("/");
		CSVParser parser = null;

		SciDB sciDB = new SciDB();
		sciDB.connect(client, this.resourceURL);

		try {
			if (pathComponents.length == 2) {
				sciDB.executeQuery(sciDB.list(SciDBListElement.ARRAYS), "csv");
				parser = new CSVParser(
						new InputStreamReader(sciDB.readLines()),
						CSVFormat.DEFAULT);
				for (CSVRecord csvRecord : parser) {
					String name = csvRecord.get(0);
					name = name.substring(1, name.length() - 1);
					Entity entity = new Entity();
					entity.setName(name);
					entity.setDisplayName(name);
					entity.setPui(basePath + "/" + name);
					entity.setDataType(SciDBDataType.ARRAY);
					entities.add(entity);
				}

			} else if (pathComponents.length == 3) {
				sciDB.executeQuery(
						sciDB.show(new SciDBArray(pathComponents[2])), "csv");
				String line = inputStreamToString(sciDB.readLines());
				SciDBArray array = SciDBArray.fromAFLResponseString(line);

				for (String attributeName : array.getAttributes().keySet()) {
					Entity entity = new Entity();
					entity.setPui(basePath + "/" + attributeName);
					entity.setName(attributeName);
					entity.setDisplayName(attributeName);
					entity.setDataType(SciDBDataType.ATTRIBUTE);

					SciDBAttribute attribute = array.getAttributes().get(
							attributeName);
					entity.getAttributes().put("nullable",
							Boolean.toString(attribute.isNullable()));
					entity.getAttributes().put("defaultValue",
							attribute.getDefaultValue());
					entity.getAttributes().put("compressionType",
							attribute.getCompressionType());

					entities.add(entity);
				}
				for (String dimensionName : array.getDimensions().keySet()) {
					Entity entity = new Entity();
					entity.setPui(basePath + "/" + dimensionName);
					entity.setName(dimensionName);
					entity.setDisplayName(dimensionName);
					entity.setDataType(SciDBDataType.DIMENSION);

					SciDBDimension dimension = array.getDimensions().get(
							dimensionName);
					entity.getAttributes().put("lowValue",
							dimension.getLowValue());
					entity.getAttributes().put("highValue",
							dimension.getHighValue());
					entity.getAttributes().put("chunkLength",
							dimension.getChunkLength());
					entity.getAttributes().put("chunkOverlap",
							dimension.getChunkOverlap());
					entities.add(entity);
				}

			} else {
				sciDB.close();
				throw new ResourceInterfaceException(relationship.toString()
						+ " not supported for this path " + basePath);
			}
		} catch (NotConnectedException | IOException e) {
			e.printStackTrace();
		} finally {
			if (parser != null) {
				try {
					parser.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		sciDB.close();
		return entities;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.
	 * QueryResourceImplementationInterface
	 * #runQuery(edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession,
	 * edu.harvard.hms.dbmi.bd2k.irct.model.query.Query,
	 * edu.harvard.hms.dbmi.bd2k.irct.model.result.Result)
	 */
	@Override
	public Result runQuery(SecureSession session, Query query, Result result)
			throws ResourceInterfaceException {
		HttpClient client = createClient(session);
		result.setResultStatus(ResultStatus.CREATED);

		SciDB sciDB = new SciDB();
		sciDB.connect(client, this.resourceURL);
		SciDBCommand whereOperation = null;
		try {
			List<String> selects = new ArrayList<String>();
			String arrayName = "";
			for (ClauseAbstract clause : query.getClauses().values()) {
				if (clause instanceof WhereClause) {
					if (((WhereClause) clause).getPredicateType().getName()
							.equals("FILTER")) {
						if (whereOperation == null) {
							arrayName = ((WhereClause) clause).getField()
									.getPui().split("/")[2];
							whereOperation = sciDB
									.filter(new SciDBArray(arrayName),
											createSciDBFilterOperation((WhereClause) clause));
						} else {
							whereOperation = sciDB
									.filter(whereOperation,
											createSciDBFilterOperation((WhereClause) clause));
						}
					}
				} else if (clause instanceof SelectClause) {
					String[] pathComponents = ((SelectClause) clause)
							.getParameter().getPui().split("/");
					if (pathComponents.length == 4) {
						selects.add(pathComponents[3]);
					}
				}
			}

			String queryId = "";
			if (selects.isEmpty()) {
				queryId = sciDB.executeQuery((SciDBOperation) whereOperation);
			} else {
				queryId = sciDB.executeQuery(
						sciDB.project(whereOperation,
								selects.toArray(new String[] {})), "dcsv");
			}
			if (queryId.contains("Exception")) {
				result.setResultStatus(ResultStatus.ERROR);
				result.setMessage(queryId);
				sciDB.close();
			} else {
				result.setResourceActionId(sciDB.getSessionId() + "|" + queryId);
				result.setResultStatus(ResultStatus.RUNNING);
			}
		} catch (NotConnectedException e) {
			e.printStackTrace();
			result.setResultStatus(ResultStatus.ERROR);
			result.setMessage(e.getMessage());
			sciDB.close();
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.
	 * QueryResourceImplementationInterface
	 * #getResults(edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession,
	 * edu.harvard.hms.dbmi.bd2k.irct.model.result.Result)
	 */
	@Override
	public Result getResults(SecureSession session, Result result)
			throws ResourceInterfaceException {
		if (result.getResultStatus() == ResultStatus.COMPLETE
				|| result.getResultStatus() == ResultStatus.ERROR) {
			return result;
		}

		HttpClient client = createClient(session);
		SciDB sciDB = new SciDB();
		sciDB.connect(client, this.resourceURL);
		try {
			BufferedReader in = new BufferedReader(
					new InputStreamReader(sciDB.readLines(result
							.getResourceActionId().split("\\|")[0])));
			String line = null;

			boolean firstLine = true;
			FileResultSet rs = (FileResultSet) result.getData();
			while ((line = in.readLine()) != null) {
				if (firstLine) {
					rs = createColumns(result, line);
					rs.first();
					firstLine = false;
				} else {
					rs.appendRow();
					line = line.replaceAll("\\{", "").replaceAll("\\} ", ",");
					String[] lineData = line.split(",");
					for (int datai = 0; datai < lineData.length; datai++) {
						rs.updateString(datai, lineData[datai]);
					}
					if (rs.getRow() % (rs.getMaxPending() - 1) == 0) {
						rs.merge();
					}
				}
			}

			result.setData(rs);

			result.setResultStatus(ResultStatus.COMPLETE);
		} catch (NotConnectedException | IOException | ResultSetException
				| PersistableException e) {
			e.printStackTrace();
			result.setResultStatus(ResultStatus.ERROR);
			result.setMessage(e.getMessage());
		}
		sciDB.close();
		return result;
	}

	private FileResultSet createColumns(Result result, String headerLine)
			throws ResultSetException {
		FileResultSet rs = (FileResultSet) result.getData();

		headerLine = headerLine.replaceAll("\\{", "").replaceAll("\\} ", ",");
		String[] columnNames = headerLine.split(",");
		for (String columnName : columnNames) {
			Column newColumn = new Column();
			newColumn.setName(columnName.trim());
			newColumn.setDataType(PrimitiveDataType.STRING);
			rs.appendColumn(newColumn);
		}
		return rs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.
	 * ProcessResourceImplementationInterface
	 * #runProcess(edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession,
	 * edu.harvard.hms.dbmi.bd2k.irct.model.process.IRCTProcess,
	 * edu.harvard.hms.dbmi.bd2k.irct.model.result.Result)
	 */
	@Override
	public Result runProcess(SecureSession session, IRCTProcess process,
			Result result) throws ResourceInterfaceException {
		HttpClient client = createClient(session);
		SciDB sciDB = new SciDB();
		sciDB.connect(client, this.resourceURL);

		sciDB.close();
		return result;
	}

	private SciDBCommand createSciDBFilterOperation(WhereClause whereClause) {
		String value = whereClause.getStringValues().get("VALUE");

		if (!isNumeric(value)) {
			value = "'" + value + "'";
		}

		String operator = whereClause.getStringValues().get("OPERATOR");

		String[] pathComponents = whereClause.getField().getPui().split("/");
		String field = pathComponents[3];

		SciDBFunction returnFunction = null;
		switch (operator) {
		case "LT":
			returnFunction = SciDBFilterFactory.lessThan(field, value);
			break;
		case "LE":
			returnFunction = SciDBFilterFactory.lessThanEqual(field, value);
			break;
		case "GT":
			returnFunction = SciDBFilterFactory.greaterThan(field, value);
			break;
		case "GE=":
			returnFunction = SciDBFilterFactory.greaterThanEqual(field, value);
			break;
		case "EQ":
			returnFunction = SciDBFilterFactory.equal(field, value);
			break;
		case "NE":
			returnFunction = SciDBFilterFactory.notEqual(field, value);
			break;

		}
		return returnFunction;
	}

	private boolean isNumeric(String s) {
		return s.matches("[-+]?\\d*\\.?\\d+");
	}

	/**
	 * CREATES A CLIENT
	 * 
	 * @param token
	 * @return
	 */
	protected HttpClient createClient(SecureSession session) {
		// SSL WRAPAROUND
		HttpClientBuilder returns = null;

		if (ignoreCertificate) {
			try {
				// CLIENT CONNECTION
				returns = ignoreCertificate();
			} catch (NoSuchAlgorithmException | KeyManagementException e) {
				e.printStackTrace();
			}
		} else {
			returns = HttpClientBuilder.create();
		}

		List<Header> defaultHeaders = new ArrayList<Header>();

		String token = session.getToken().toString();
		if (this.clientId != null) {
			token = SecurityUtility.delegateToken(this.namespace,
					this.clientId, session);
		}

		if (session != null) {
			defaultHeaders.add(new BasicHeader("Authorization", token));
		}
		defaultHeaders.add(new BasicHeader("Content-Type",
				"application/x-www-form-urlencoded"));
		returns.setDefaultHeaders(defaultHeaders);

		return returns.build();
	}

	private HttpClientBuilder ignoreCertificate()
			throws NoSuchAlgorithmException, KeyManagementException {
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

		SSLContext sslContext;

		sslContext = SSLContext.getInstance("SSL");
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

		return HttpClients.custom().setConnectionManager(cm);
	}

	private static String inputStreamToString(InputStream inputStream)
			throws IOException {
		StringWriter writer = new StringWriter();
		IOUtils.copy(inputStream, writer, Charset.defaultCharset());
		inputStream.close();
		return writer.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.
	 * ResourceImplementationInterface#getType()
	 */
	@Override
	public String getType() {
		return "sciDB";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.
	 * QueryResourceImplementationInterface#getState()
	 */
	@Override
	public ResourceState getState() {
		return resourceState;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.
	 * QueryResourceImplementationInterface
	 * #getQueryDataType(edu.harvard.hms.dbmi.bd2k.irct.model.query.Query)
	 */
	@Override
	public ResultDataType getQueryDataType(Query query) {
		return ResultDataType.TABULAR;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.
	 * ProcessResourceImplementationInterface
	 * #getProcessDataType(edu.harvard.hms.
	 * dbmi.bd2k.irct.model.process.IRCTProcess)
	 */
	@Override
	public ResultDataType getProcessDataType(IRCTProcess process) {
		return ResultDataType.TABULAR;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.
	 * PathResourceImplementationInterface
	 * #find(edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Entity,
	 * edu.harvard.hms.dbmi.bd2k.irct.model.find.FindInformationInterface,
	 * edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession)
	 */
	@Override
	public List<Entity> find(Entity path,
			FindInformationInterface findInformation, SecureSession session)
			throws ResourceInterfaceException {
		return new ArrayList<Entity>();
	}
}
