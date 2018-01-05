/**
 *
 */
package edu.harvard.hms.dbmi.bd2k.irct.ri.gnome;

import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.find.FindInformationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Entity;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.model.process.IRCTProcess;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.*;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Field;
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
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;
import edu.harvard.hms.dbmi.scidb.*;
import edu.harvard.hms.dbmi.scidb.exception.NotConnectedException;
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
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GNomeResourceImplementation implements
		PathResourceImplementationInterface,
		QueryResourceImplementationInterface,
		ProcessResourceImplementationInterface {

	Logger logger = Logger.getLogger(getClass());

	private String resourceName;
	private String rootURL;
	private String username;
	private String password;

	private String token;

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
		logger.debug("setup for " + resourceName +
				" Starting...");

		this.resourceName = parameters.get("resourceName");
		if (this.resourceName == null) {
			logger.error( "setup() `resourceName` parameter is missing.");
			throw new RuntimeException("Missing `resourceName` parameter.");
		}

		this.rootURL = parameters.get("rootURL");
		if (this.rootURL == null) {
			logger.error( "setup() `rootURL` parameter is missing.");
			throw new RuntimeException("Missing `rootURL` parameter.");

		}

		this.username = parameters.get("gnomeUserName");
		if (this.username == null) {
			logger.error( "setup() `gnomeUserName` parameter is missing.");
			throw new RuntimeException("Missing `gnomeUserName` parameter.");
		}

		this.password = parameters.get("gnomePassword");
		if (this.password == null) {
			logger.error( "setup() `gnomePassword` parameter is missing.");
			throw new RuntimeException("Missing `gnomePassword` parameter.");
		}



		logger.debug( "setup for " + resourceName +
				" Finished. " + resourceName +
						" is in READY state.");
		resourceState = ResourceState.READY;
	}

	private void getConnection(){

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
			OntologyRelationship relationship, User user)
			throws ResourceInterfaceException {

		logger.debug( "getPathRelationship() Starting...");

		List<Entity> entities = new ArrayList<Entity>();

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
	public Result runQuery(User user, Query query, Result result)
			throws ResourceInterfaceException {
		logger.log(Level.INFO, "runQuery() Starting");

		// Setup SciDB connection
		HttpClient client = createClient();
		SciDB sciDB = new SciDB(this.username, this.password);
		logger.debug("runQuery() connecting to resource "+this.rootURL);
		sciDB.connect(client, this.rootURL);
		if (sciDB.getSessionId()==null) {
			logger.error("runQuery() Could not create SciDB session while connecting.");
			result.setResultStatus(ResultStatus.ERROR);
			result.setMessage("Could not create SciDB session while connecting.");
			return result;
		}
		result.setResultStatus(ResultStatus.CREATED);

		List<WhereClause> whereClauses = query.getClausesOfType(WhereClause.class);
		String queryId = "NOTSET";
		// Execute AFL queries from the fields portion of the WHERE clause
		for (WhereClause whereClause : whereClauses) {
			Map<String, String> queries = whereClause.getStringValues();

			for(String queryString: queries.values()) {
				logger.debug("runQuery() executing queryString:"+queryString);
				try {
					queryId = sciDB.executeAflQuery(queryString);

					if (queryId != null && queryId.contains("Exception")) {
						// This is an error, and we should handle it as such.
						logger.error("runQuery() SciDB Exception:"+queryId);

						result.setResultStatus(ResultStatus.ERROR);
						// Now this is a guess and a risk, but hopefully
						// not a big one. If in doubt, turn on DEBUG level
						// logging, re-run the query and check the logfiles.
						int errormsg_linecount = queryId.split("\n").length;
						switch (errormsg_linecount) {
						case 0:
							result.setMessage("SciDB Exception:"+queryId);
							break;
						case 1:
						case 2:
							result.setMessage("SciDB Exception:"+queryId.split("\n")[0]);
							break;
						default:
							result.setMessage("SciDB "+queryId.split("\n")[errormsg_linecount-3]);

						}
						logger.error("runQuery() returning ERROR result.");
						return result;
					}
					result.setResultStatus(ResultStatus.RUNNING);
					result.setMessage("SciDB Query id:"+queryId+" for PIC-SURE queryId:"+query.getId());
				} catch (Exception e) {
					logger.error( "runQuery() Exception:"+e.getMessage());
					result.setResultStatus(ResultStatus.ERROR);
					result.setMessage(e.getMessage());
				}
			}
		}
		logger.debug( "runQuery() completed all queries");
		result.setResourceActionId(sciDB.getSessionId() + "|" + queryId);

		logger.debug("runQuery() returning `result` with status "+result.getResultStatus().toString());
		return result;
	}

	private SciDBCommand createQuery(SciDB sciDB, Query query) {
		logger.debug( "createQuery() ");

		SciDBCommand command = null;
		// Parse all subqueries first
		Map<String, SciDBCommand> subQueryCommands = new HashMap<String, SciDBCommand>();

		for (String subQueryID : query.getSubQueries().keySet()) {
			subQueryCommands.put(subQueryID, createQuery(sciDB, query.getSubQuery(subQueryID)));
		}

		// Parse all join clauses
		logger.debug( "createQuery() Parse all join clauses");
		List<JoinClause> joinClauses = query.getClausesOfType(JoinClause.class);
		for (JoinClause joinClause : joinClauses) {
			command = addJoinOperation(sciDB, command, subQueryCommands, joinClause);
		}

		// Parse all where clauses second
		logger.debug( "createQuery() Parse all where clauses");
		List<WhereClause> whereClauses = query
				.getClausesOfType(WhereClause.class);
		for (WhereClause whereClause : whereClauses) {
			command = addWhereOperation(sciDB, command, subQueryCommands, whereClause);
		}

		// Parse all sort clauses
		logger.debug( "createQuery() Parse all sort clauses");
		List<SortClause> sortClauses = query.getClausesOfType(SortClause.class);
		for(SortClause sortClause : sortClauses) {
			command = addSortOperation(sciDB, command, subQueryCommands, sortClause);
		}

		// Parse all select clauses
		logger.debug( "createQuery() Parse all select clauses");
		List<SelectClause> selectClauses = query
				.getClausesOfType(SelectClause.class);
		List<String> selects = new ArrayList<String>();
		for (SelectClause selectClause : selectClauses) {
			if (selectClause.getOperationType() != null) {
				command = addSelectOperation(sciDB, command, subQueryCommands, selectClause);
			} else {
				String[] pathComponents = selectClause.getParameter().getPui()
						.split("/");
				if (pathComponents.length == 4) {
					selects.add(pathComponents[2] + "." + pathComponents[3]);
				} else if (pathComponents.length == 3) {
					selects.add(pathComponents[2]);
				}
			}
		}
		if (!selects.isEmpty()) {
			command = sciDB.project(command, selects.toArray(new String[] {}));
		}
		logger.debug( "createQuery() Returning command: "+command.toString()+" or "+command.toAFLQueryString());
		return command;

	}

	private SciDBCommand addWhereOperation(SciDB sciDB,
			SciDBCommand whereOperation, Map<String, SciDBCommand> subQueryCommands, WhereClause whereClause) {

		logger.debug( "addWhereOperation() Starting...");

		String predicateName = whereClause.getPredicateType().getName();
		switch (predicateName) {
		case "AFL":
			List<Field> fields = whereClause.getPredicateType().getFields();
			for(Field field: fields) {
				logger.debug( "addWhereOperation() field:"+ field.getName()+" path:"+field.getPath());
			}
			whereOperation = new SciDBArray("scidblist");
			break;
		default:
			throw new RuntimeException("Unsupported PREDICATE operation.");
		}
		logger.debug( "addWhereOperation() Returning ```whereOperation``` as "+whereOperation.toAFLQueryString());
		return whereOperation;
	}

	private SciDBCommand addSelectOperation(SciDB sciDB,
			SciDBCommand selectOperation,
			Map<String, SciDBCommand> subQueryCommands, SelectClause selectClause) {
		logger.debug( "addSelectOperation() Starting ...");
		String operationName = selectClause.getOperationType().getName();

		switch (operationName) {
		case "AGGREGATE":
			String aggregateFunction = selectClause.getStringValues().get(
					"FUNCTION");
			String alias = selectClause.getAlias();
			if (aggregateFunction.equalsIgnoreCase("COUNT")) {
				if (selectClause.getStringValues().containsKey("DIMENSION")) {
					String dimension = selectClause.getStringValues().get(
							"DIMENSION");
					String[] dimensions = dimension.split("/");

					if (dimensions.length == 4) {
						dimension = dimensions[3];
					}
					selectOperation = sciDB.aggregate(selectOperation,
							SciDBAggregateFactory.count(), dimension, alias);
				} else if ((selectClause.getObjectValues() != null) && (selectClause.getObjectValues().containsKey(
						"DIMENSION"))) {

					String[] dimensions = (String[]) selectClause
							.getObjectValues().get("DIMENSION");

					String dimensionString = "";
					for (Object dim : dimensions) {
						dimensionString += dim + ",";
					}

					dimensionString = dimensionString.substring(0,
							dimensionString.length() - 1);
					selectOperation = sciDB.aggregate(selectOperation,
							SciDBAggregateFactory.count(), dimensionString,
							alias);
				} else {
					selectOperation = sciDB.aggregate(selectOperation,
							SciDBAggregateFactory.count(), null, alias);
				}
			}

		}
		logger.debug( "addSelectOperation() Returning ```select``` operatrion as "+selectOperation.toAFLQueryString());
		return selectOperation;
	}

	private SciDBCommand addJoinOperation(SciDB sciDB,
			SciDBCommand joinOperation, Map<String, SciDBCommand> subQueryCommands, JoinClause joinClause) {
		String joinName = joinClause.getJoinType().getName();

		switch (joinName) {
		case "CROSSJOIN":
			Query right = (Query) joinClause.getObjectValues().get("RIGHT");
			String rightAlias = joinClause.getStringValues().get("RIGHT_ALIAS");
			String leftAlias = joinClause.getStringValues().get("LEFT_ALIAS");
			if(joinClause.getStringValues().containsKey("DIMENSIONS")) {
				String rightDimension = joinClause.getStringValues().get("DIMENSIONS");
				SciDBCommand rightCommand = createQuery(sciDB, right);
				String[] components = joinClause.getField().getPui().split("/");
				joinOperation = sciDB.crossJoin(new SciDBArray(components[2]),
						rightCommand, components[2] + "." + components[3],
						rightDimension);

			} else if(joinClause.getObjectValues().containsKey("DIMENSIONS")) {
				String[] dimensions = (String[]) joinClause.getObjectValues().get("DIMENSIONS");
				String[] components = joinClause.getField().getPui().split("/");
				SciDBCommand rightCommand = createQuery(sciDB, right);

				SciDBCommand leftCommand;
				if(subQueryCommands.containsKey(components[2])) {
					leftCommand = subQueryCommands.get(components[2]);
				} else {
					leftCommand = new SciDBArray(components[2]);
				}
				joinOperation = sciDB.crossJoin(leftCommand, leftAlias, rightCommand, rightAlias, dimensions);

			}
		}
		return joinOperation;
	}

	private SciDBCommand addSortOperation(SciDB sciDB,
			SciDBCommand sortOperation,
			Map<String, SciDBCommand> subQueryCommands, SortClause sortClause) {
		String sortName = sortClause.getOperationType().getName();

		switch (sortName) {
		case "SORT":
			String field = null;
			String[] components = sortClause.getParameter().getPui().split("/");

			if(components.length == 3) {
				field = components[2];
			} else if (components.length == 4) {
				if(subQueryCommands.containsKey(components[2])) {
					sortOperation = subQueryCommands.get(components[2]);
				} else {
;					sortOperation = new SciDBArray(components[2]);
				}
				field = components[3];
			}

			String direction = sortClause.getStringValues().get("DIRECTION");

			if(field == null && direction == null) {
				sortOperation = sciDB.sort(sortOperation);
			} else if (field != null && direction == null) {
				sortOperation = sciDB.sort(sortOperation, field);
			} else if(field != null && direction != null) {
				sortOperation = sciDB.sort(sortOperation, field, direction);
			}
			break;
		}
		return sortOperation;
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
	public Result getResults(User user, Result result)
			throws ResourceInterfaceException {
		logger.debug( "getResults() Starting ...");

		if (result.getResultStatus() == ResultStatus.COMPLETE
				|| result.getResultStatus() == ResultStatus.ERROR) {
			logger.debug( "getResults() `ResultStatus` is COMPLETE or ERROR, so returning immediately.");
			return result;
		}
		logger.debug( "getResults() `ResultStatus` is :"+result.getResultStatus());
		HttpClient client = createClient();
		SciDB sciDB = new SciDB(this.username, this.password);
		sciDB.connect(client, this.rootURL);
		logger.debug( "getResults() connecting to "+this.rootURL);

		try {
			BufferedReader in = new BufferedReader(
					new InputStreamReader(sciDB.readLines(result
							.getResourceActionId().split("\\|")[0])));
			String line = null;

			boolean firstLine = true;
			FileResultSet rs = (FileResultSet) result.getData();
			logger.debug( "getResults() reading output from SciDB query response");

			while ((line = in.readLine()) != null) {
				if (firstLine) {
					rs = createColumns(result, line);
					rs.first();
					firstLine = false;
				} else {
					rs.appendRow();
					line = line.replaceAll("\\{", "").replaceAll("\\} ", ",");

					CSVParser parser = CSVParser.parse(line,
							CSVFormat.DEFAULT.withQuote('\''));

					CSVRecord record = parser.getRecords().get(0);

					for (int datai = 0; datai < rs.getColumns().length; datai++) {
						rs.updateString(datai, record.get(datai));
					}
					if (rs.getRow() % (rs.getMaxPending() - 1) == 0) {
						rs.merge();
					}
				}
			}
			logger.debug( "getResults() setting data for resultId:"+result.getId());

			logger.debug( "getResults() `FileResultSet` size:"+rs.getSize());
			logger.debug( "getResults() `FileResultSet` closed?:"+rs.isClosed());
			logger.debug( "getResults() `FileResultSet` persisted?:"+rs.isPersisted());

			result.setData(rs);
			result.setResultStatus(ResultStatus.COMPLETE);
		} catch (NotConnectedException | IOException | ResultSetException | PersistableException e) {
			logger.error("getResults() Exception reading response from SciDB connection. "+e.getClass().toString()+"/"+e.getMessage());
			result.setResultStatus(ResultStatus.ERROR);
			result.setMessage((e.getMessage()==null?"Error getting SciDB response.":e.getMessage()));
			e.printStackTrace();
		}
		logger.debug( "getResults() closing SciDB connection.");
		sciDB.close();
		logger.debug( "getResults() Finished, returning result with "+result.getResultStatus().toString());
		return result;
	}

	private FileResultSet createColumns(Result result, String headerLine)
			throws ResultSetException {
		logger.debug( "createColumns() Starting...");

		FileResultSet rs = (FileResultSet) result.getData();

		headerLine = headerLine.replaceAll("\\{", "").replaceAll("\\} ", ",");
		String[] columnNames = headerLine.split(",");
		for (String columnName : columnNames) {
			Column newColumn = new Column();
			newColumn.setName(columnName.trim());
			newColumn.setDataType(PrimitiveDataType.STRING);
			rs.appendColumn(newColumn);
		}
		logger.debug( "createColumns() Finished.");
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
	public Result runProcess(User user, IRCTProcess process,
			Result result) throws ResourceInterfaceException {
		logger.debug( "runProcess() Starting...");

		HttpClient client = createClient();
		SciDB sciDB = new SciDB(this.username, this.password);
		sciDB.connect(client, this.rootURL);
		sciDB.close();

		logger.debug( "runProcess() Finished.");
		return result;
	}

	private SciDBCommand createSciDBFilterOperation(WhereClause whereClause) {
		logger.debug( "createSciDBFilterOperation() Starting...");

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
		logger.debug( "createSciDBFilterOperation() Finished.");
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
	protected HttpClient createClient() {
		logger.debug( "createClient() Starting...");

		// SSL WRAPAROUND
		HttpClientBuilder returns = HttpClientBuilder.create();

		if (1==1) {
			logger.debug( "createClient() Ignoring certificate errors.");
			try {
				// CLIENT CONNECTION
				returns = ignoreCertificate();
			} catch (NoSuchAlgorithmException | KeyManagementException e) {
				logger.error( "createClient() Failed to ignore certificate errors:"+e.getMessage());
				e.printStackTrace();
			}
		}
		//else {
		//	returns = HttpClientBuilder.create();
		//}

		List<Header> defaultHeaders = new ArrayList<Header>();
		logger.error( "createClient() Set default header Content-type.");
		defaultHeaders.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
		returns.setDefaultHeaders(defaultHeaders);

		logger.debug( "createClient() Finished. Returning HttpClientBuilder instance.");
		return returns.build();
	}

	private HttpClientBuilder ignoreCertificate()
			throws NoSuchAlgorithmException, KeyManagementException {
		logger.debug( "ignoreCertificate() Starting...");

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

		logger.debug( "ignoreCertificate() Finished.");
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
			FindInformationInterface findInformation, User user)
			throws ResourceInterfaceException {
		return new ArrayList<Entity>();
	}
}
