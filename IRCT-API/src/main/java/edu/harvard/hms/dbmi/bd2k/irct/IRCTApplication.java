/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.harvard.hms.dbmi.bd2k.irct.dataconverter.ResultDataConverter;
import edu.harvard.hms.dbmi.bd2k.irct.event.EventConverterImplementation;
import edu.harvard.hms.dbmi.bd2k.irct.event.IRCTEventListener;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.join.IRCTJoin;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.DataConverterImplementation;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultDataType;
import edu.harvard.hms.dbmi.bd2k.irct.util.Utilities;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.*;
import javax.json.stream.JsonParsingException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.ProxySelector;
import java.util.*;

/**
 * Manages supported resources and join types for this instance of the IRCT
 * application
 */
@Startup
@Singleton
@ApplicationScoped
public class IRCTApplication {

	@javax.annotation.Resource(mappedName = "java:global/resultDataFolder")
	private String resultDataFolder = null;
	
	private String whitelistLocation;
	
	// key is the name string, value is a JsonArray for resources
	private Map<String, JsonArray> whitelist;
	private boolean whitelistEnabled = false;

	private Map<String, Resource> resources;
	private Map<String, IRCTJoin> supportedJoinTypes;
	private Map<ResultDataType, List<DataConverterImplementation>> resultDataConverters;

	// token introspection configuration parameters
	private String verify_user_method;
	public static final String VERIFY_METHOD_SESSION_FILETER="sessionFilter";
	public static final String VERIFY_METHOD_TOKEN_INTRO="tokenIntro";
	private String token_introspection_url;
	private String token_introspection_token;

	// client secret and userField related to sessionFilter
	private String clientSecret;

	// keep objectMapper final
	public static final ObjectMapper objectMapper = new ObjectMapper();

	private Logger logger = Logger.getLogger(this.getClass());

	@Inject
	private EntityManagerFactory objectEntityManager;

	@Inject
	private IRCTEventListener irctEventListener;

	private EntityManager oem;

	// check the example from Apache HttpClient official website:
	// http://hc.apache.org/httpcomponents-client-4.5.x/httpclient/examples/org/apache/http/examples/client/ClientMultiThreadedExecution.java
	public static final PoolingHttpClientConnectionManager HTTP_CLIENT_CONNECTION_MANAGER;

	// If want to use self sign certificate for https,
	// please follow the official httpclient example link:
	// https://hc.apache.org/httpcomponents-client-ga/httpclient/examples/org/apache/http/examples/client/ClientCustomSSL.java
	public static final CloseableHttpClient CLOSEABLE_HTTP_CLIENT;
	static {
		HTTP_CLIENT_CONNECTION_MANAGER = new PoolingHttpClientConnectionManager();
		HTTP_CLIENT_CONNECTION_MANAGER.setMaxTotal(100);
		CLOSEABLE_HTTP_CLIENT = HttpClients
				.custom()
				.setConnectionManager(HTTP_CLIENT_CONNECTION_MANAGER)
				.setRoutePlanner(
						new SystemDefaultRoutePlanner(ProxySelector
								.getDefault()))
				.build();
	}

	/**
	 * Initiates the IRCT Application and loading of the joins, resources, and
	 * predicates.
	 *
	 */
	@PostConstruct
	public void init() throws NamingException{
		logger.info("Starting IRCT Application");

		/********************************************************/
		/********************************************************/
		logger.info("Loading Token Introspection Conf");
		loadTokenIntrospection();
		logger.info("Finished loading token Introspection Conf");
		/********************************************************/
		/***********************Notice***************************/
		//loading client secret has logic dependent on loading token introspection
		//so please put loadTokenIntrospection() before loadClientSecret()
		/********************************************************/
		/********************************************************/
		logger.info("Loading client secret");
		loadClientSecret();
		logger.info("Finished loading client secret");
		/********************************************************/
		/********************************************************/

		this.oem = objectEntityManager.createEntityManager();
		this.oem.setFlushMode(FlushModeType.COMMIT);

		logger.info("Loading Data Converters");
		loadDataConverters();
		logger.info("Finished Data Converters");

		logger.info("Loading Event Listeners");
		loadIRCTEventListeners();
		logger.info("Finished Loading Event Listeners");

		logger.info("Loading Join Types");
		loadJoins();
		logger.info("Finished Loading Join Types");

		logger.info("Loading Resources");
		loadResources();
		logger.info("Finished Loading Resources");
		
		logger.info("Loading Whitelists");
		loadWhiteLists();
		logger.info("Finihsed loading whitelists");

		logger.info("Finished Starting IRCT Application");
	}

	public String getVersion() {
		String version = null;
		//log.log(Level.INFO, "getVersion() Starting");

		// try to load from maven properties first
		try {
			Properties p = new Properties();

			// This filename is generated by maven, hopefully.
			String runtimepropertiesFileName = "/META-INF/maven/edu.harvard.hms.dbmi.bd2k.irct/IRCT-API/pom.properties";
			InputStream is = IRCTApplication.class.getResourceAsStream(runtimepropertiesFileName);
			if (is != null) {
				p.load(is);
				version = p.getProperty("version", "");
			}
		} catch (Exception e) {
			logger.error("getVersion() Exception:" + e.getMessage());
		}

		if (version == null) {
			// we could not compute the version so use a blank
			version = "N/A";
		}

		return version;
	}

	/**
	 * Load all the Listeners
	 *
	 */
	private void loadIRCTEventListeners() {
		this.irctEventListener.init();
		CriteriaBuilder cb = oem.getCriteriaBuilder();
		CriteriaQuery<EventConverterImplementation> criteria = cb.createQuery(EventConverterImplementation.class);
		Root<EventConverterImplementation> load = criteria.from(EventConverterImplementation.class);
		criteria.select(load);
		List<EventConverterImplementation> allEventListeners = oem.createQuery(criteria).getResultList();

		for (EventConverterImplementation irctEvent : allEventListeners) {
			irctEventListener.registerListener(irctEvent);
		}

		logger.info("Loaded " + allEventListeners.size() + " IRCT Event listeners");
	}

	/**
	 * Load all the Output Data Converters
	 *
	 */
	private void loadDataConverters() {
		this.resultDataConverters = new HashMap<ResultDataType, List<DataConverterImplementation>>();
		CriteriaBuilder cb = oem.getCriteriaBuilder();
		CriteriaQuery<DataConverterImplementation> criteria = cb.createQuery(DataConverterImplementation.class);
		Root<DataConverterImplementation> load = criteria.from(DataConverterImplementation.class);
		criteria.select(load);
		List<DataConverterImplementation> allDCI = oem.createQuery(criteria).getResultList();
		for (DataConverterImplementation dci : allDCI) {
			if (this.resultDataConverters.containsKey(dci.getResultDataType())) {
				this.resultDataConverters.get(dci.getResultDataType()).add(dci);
			} else {
				List<DataConverterImplementation> dciList = new ArrayList<DataConverterImplementation>();
				dciList.add(dci);
				this.resultDataConverters.put(dci.getResultDataType(), dciList);
			}

		}

		logger.info("Loaded " + allDCI.size() + " result data converters");
	}

	/**
	 * Loads all the joins from the persistence manager
	 */
	private void loadJoins() {

		setSupportedJoinTypes(new HashMap<String, IRCTJoin>());
		// Run JPA Query to load the resources
		CriteriaBuilder cb = oem.getCriteriaBuilder();
		CriteriaQuery<IRCTJoin> criteria = cb.createQuery(IRCTJoin.class);
		Root<IRCTJoin> load = criteria.from(IRCTJoin.class);
		criteria.select(load);
		for (IRCTJoin jt : oem.createQuery(criteria).getResultList()) {
			this.supportedJoinTypes.put(jt.getName(), jt);
		}
		logger.info("Loaded " + this.supportedJoinTypes.size() + " joins");
	}

	/**
	 *
	 * Loads all the resources from the persistence manager
	 *
	 */
	private void loadResources() {
		logger.info("loadResources() Starting");
		setResources(new HashMap<String, Resource>());

		// Run JPQL to load the resources
		Query query = oem.createQuery("SELECT res FROM Resource res WHERE res.ontologyType=:arg1").setParameter("arg1", "TREE");
		@SuppressWarnings("unchecked")
		List<Resource> resourceList = query.getResultList();

		for (Resource resource : resourceList) {
			try {
				logger.info("loadResources() Setting up resource:"
									+ resource.getName()
									+" with id: "+resource.getId()
									+", "+resource.getClass().toString());
				resource.setup();
				this.resources.put(resource.getName(), resource);
				logger.info("loadResources() resource `"+resource.getName()+"` has been loaded");
			} catch (ResourceInterfaceException e) {
				logger.warn("loadResources() Exception: "+e.getMessage());
			}
		}
		logger.info("loadResources() Loaded " + this.resources.size() + " resources");
	}

	private void loadTokenIntrospection(){
		try {
			Context ctx = new InitialContext();
			verify_user_method = (String) ctx.lookup("global/verify_user_method");
			token_introspection_url = (String) ctx.lookup("global/token_introspection_url");
			token_introspection_token = (String) ctx.lookup("global/token_introspection_token");
			ctx.close();
		} catch (NamingException e) {
			verify_user_method = VERIFY_METHOD_SESSION_FILETER;
		}

		logger.info("verify_user_method setup as: " + verify_user_method);
	}

	private void loadClientSecret() throws NamingException{
		if (!VERIFY_METHOD_SESSION_FILETER.equals(verify_user_method)) {
			logger.info("System is not in "+ VERIFY_METHOD_SESSION_FILETER+ " mode, client secret is not loaded");
			return;
		}
		Context ctx = new InitialContext();
		clientSecret = (String) ctx.lookup("java:global/client_secret");
	}
	
	/**
	 * Adds a given resource to the IRCT application
	 *
	 * @param name
	 *            Resource name
	 * @param resource
	 *            Resource
	 */
	public void addResource(String name, Resource resource) {
		// Persist the new resource
		oem.persist(resource);

		this.resources.put(name, resource);
	}

	/**
	 * Removes a resource from the IRCT application
	 *
	 * @param name
	 *            Resource name
	 */
	public void removeResource(String name) {
		// Remove the resource from persistence manager
		oem.remove(this.resources.remove(name));
	}

	/**
	 * Returns true if the resource exists
	 *
	 * True if resource exists, false otherwise
	 *
	 * @param name
	 *            Resource name
	 * @return If resource exists
	 */
	public boolean doesResourceExist(String name) {
		return this.resources.containsKey(name);
	}

	/**
	 * Returns a map of the resources where the Resource name is the key, and
	 * the Resource itself is the value
	 *
	 * @return Resources
	 */
	public Map<String, Resource> getResources() {
		return resources;
	}

	/**
	 * Sets a map of the resources.
	 *
	 * @param resources
	 *            Resources
	 */
	public void setResources(Map<String, Resource> resources) {
		this.resources = resources;
	}

	/**
	 * Returns a map of the supported joins where the Join name is the key, and
	 * the JoinType itself is the value
	 *
	 * @return Supported join types
	 */
	public Map<String, IRCTJoin> getSupportedJoinTypes() {
		return supportedJoinTypes;
	}

	/**
	 * Sets a map of the supported join types
	 *
	 * @param supportedJoinTypes
	 *            Supported join types
	 */
	public void setSupportedJoinTypes(Map<String, IRCTJoin> supportedJoinTypes) {
		this.supportedJoinTypes = supportedJoinTypes;
	}

	/**
	 * Adds a join to the list of supported joins
	 *
	 * @param name
	 *            Join Name
	 * @param join
	 *            Join
	 */
	public void addJoin(String name, IRCTJoin join) {
		// Persist the join
		oem.persist(join);
		this.supportedJoinTypes.put(name, join);
	}

	/**
	 * @return the resultDataConverters
	 */
	public Map<ResultDataType, List<DataConverterImplementation>> getResultDataConverters() {
		return resultDataConverters;
	}

	/**
	 * @param resultDataConverters
	 *            the resultDataConverters to set
	 */
	public void setResultDataConverters(Map<ResultDataType, List<DataConverterImplementation>> resultDataConverters) {
		this.resultDataConverters = resultDataConverters;
	}

	/**
	 * Returns a dataconveter for a given datatype and format
	 *
	 * @param dataType
	 *            DataType
	 * @param format
	 *            Format
	 * @return DataConverter
	 */
	public ResultDataConverter getResultDataConverter(ResultDataType dataType, String format) {
		List<DataConverterImplementation> dciList = this.resultDataConverters.get(dataType);

		for (DataConverterImplementation dci : dciList) {
			if (dci.getFormat().equals(format)) {

				return dci.getDataConverter();
			}
		}
		return null;
	}

	/**
	 * Removes a join from the list of supported joins
	 *
	 * @param name
	 *            Join name
	 */
	public void removeJoin(String name) {
		// Removes the supported joins from the persistence manager
		oem.remove(this.supportedJoinTypes.remove(name));
	}

	/**
	 * Returns true if the join type is supported
	 *
	 * True if join type is supported, otherwise it false
	 *
	 * @param name
	 *            Resource name
	 * @return If resource exists
	 */
	public boolean doesJoinExists(String name) {
		return this.supportedJoinTypes.containsKey(name);
	}
	
	/**
	 *  Loads the white lists into application
	 *  
	 *  <p>Instead of using @javax.annotation.Resource to auto read the resource from JNDI,
	 *  which is kind of mandatory (throw a <code>NamingException</code> never be handled and will stop the load progress), 
	 *  here we read and handle the exception to not stop the progress, which kind of allowing optionally configure.
	 *  
	 *  <p>For exception handling: besides disabling the functionality - setting the field in configuration xml file 
	 *  to false or the location field is not in the list,
	 *  all other exceptions will end up not making any changes to the whitelist map, which means will never break 
	 *  the load progress.
	 *  @author yuzhang
	 */
	private void loadWhiteLists() {
		try {
			Context ctx = new InitialContext();
			whitelistEnabled = (Boolean) ctx.lookup("global/whitelist_enabled");
			whitelistLocation = (String) ctx.lookup("global/whitelist_config_file");
			ctx.close();
		} catch (NamingException e) {
			logger.debug("whitelist_config_file naming execption", e);
			whitelistEnabled = false;
		}
		
		if (!whitelistEnabled) {
			logger.info("Whitelist functionality is not enabled");
			return;
		}
		
		// to be able to support change the configuration white list Json file at runtime
		if (whitelist == null) {
			whitelist = new HashMap<>();
		}
		
		try (JsonReader reader = Json.createReader(
				new FileInputStream(whitelistLocation))) {
			logger.debug("starting to read whitelist file in: " + whitelistLocation);
			JsonArray jsonArray = reader.readArray();
			for (JsonValue value : jsonArray) {
				JsonObject valueObject = ((JsonObject)value);
				JsonArray resources = Json.createArrayBuilder().build();
				if (valueObject.containsKey(Utilities.Naming.Whitelist.JSON_RESOURCES))
					resources = valueObject.getJsonArray(Utilities.Naming.Whitelist.JSON_RESOURCES);
				
				if (valueObject.containsKey(Utilities.Naming.Whitelist.JSON_NAME)) {
					try {
						String name = valueObject.getString(Utilities.Naming.Whitelist.JSON_NAME);
						whitelist.put(name, resources);
						// change to Log4j
						// then change to debug
						logger.debug("Added one email from whitelist: " + name
								+ " with resources: " + resources.toString());
					} catch (ClassCastException | NullPointerException npe) {
						logger.error("The format of each object in whitelist array is not right. Please take a look into the sample whitelist json");
					}
					
				}
			}
		
		} catch (FileNotFoundException ex) {
			logger.error("Cannot find the whitelist file, please check your configuration file. "
					+ "Your file location: " + whitelistLocation);
		} catch (ClassCastException cce ) {
			// change this to Log4J would be great
			// I think another ticket is changing this to Log4j
			logger.error("The root layer of whitelist should be an array");
		} catch (JsonParsingException ex) {
			logger.error("Input whitelist file is not well formatted");
		}
	}

	/**
	 * Get the name of the result data folder
	 *
	 * @return Result Data Folder
	 */
	public String getResultDataFolder() {
		return resultDataFolder;
	}

	/**
	 * Sets the name of the result data folder
	 *
	 * @param resultDataFolder
	 *            Result Data Folder
	 */
	public void setResultDataFolder(String resultDataFolder) {
		this.resultDataFolder = resultDataFolder;
	}
	
	/**
	 * Getter of the white list
	 * @return
	 */
	public Map<String, JsonArray> getWhitelist() {
		return whitelist;
	}

	public boolean isWhitelistEnabled() {
		return whitelistEnabled;
	}

	public String getVerify_user_method() {
		return verify_user_method;
	}

	public String getToken_introspection_url() {
		return token_introspection_url;
	}

	public String getToken_introspection_token() {
		return token_introspection_token;
	}

	public String getClientSecret() {
		return clientSecret;
	}
}
