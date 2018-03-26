/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2;

import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.find.FindByOntology;
import edu.harvard.hms.dbmi.bd2k.irct.model.find.FindByPath;
import edu.harvard.hms.dbmi.bd2k.irct.model.find.FindInformationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.DataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Entity;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.ClauseAbstract;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.SelectClause;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.WhereClause;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.LogicalOperator;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ResourceState;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.PathResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.QueryResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultStatus;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.Column;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.FileResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;
import edu.harvard.hms.dbmi.i2b2.api.crc.CRCCell;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.pdo.*;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.psm.ConstrainDateTimeType;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.psm.ConstrainDateType;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.psm.ConstrainOperatorType;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.psm.ConstrainValueType;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.psm.InclusiveType;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.psm.*;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.psm.ItemType;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.psm.PanelType;
import edu.harvard.hms.dbmi.i2b2.api.exception.I2B2InterfaceException;
import edu.harvard.hms.dbmi.i2b2.api.ont.ONTCell;
import edu.harvard.hms.dbmi.i2b2.api.ont.xml.ConceptType;
import edu.harvard.hms.dbmi.i2b2.api.ont.xml.ConceptsType;
import edu.harvard.hms.dbmi.i2b2.api.ont.xml.ModifierType;
import edu.harvard.hms.dbmi.i2b2.api.ont.xml.ModifiersType;
import edu.harvard.hms.dbmi.i2b2.api.pm.PMCell;
import edu.harvard.hms.dbmi.i2b2.api.pm.xml.ConfigureType;
import edu.harvard.hms.dbmi.i2b2.api.pm.xml.ProjectType;
import org.apache.http.Header;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * A resource implementation of a resource that communicates with the i2b2
 * servers via XML
 */
public class I2B2XMLResourceImplementation
		implements QueryResourceImplementationInterface, PathResourceImplementationInterface {

	Logger logger = Logger.getLogger(this.getClass());

	protected String resourceName;
	protected String resourceURL;
	protected String domain;
	protected String clientId;
	protected String namespace;
	protected boolean useProxy;
	protected boolean ignoreCertificate;
	protected String proxyURL;
	protected String userName;
	protected String password;
	protected CRCCell crcCell;
	protected PMCell pmCell;
	protected ONTCell ontCell;

	protected ResourceState resourceState;

	@Override
	public void setup(Map<String, String> parameters) throws ResourceInterfaceException {

		if (!parameters.keySet().contains("resourceName")) {
			throw new ResourceInterfaceException("Missing ```resourceName``` parameter. It is mandatory");
		}

		if (!parameters.keySet().contains("resourceURL")) {
			throw new ResourceInterfaceException("Missing ```resourceURL``` parameter. It is mandatory.");
		}

		if (!parameters.keySet().contains("domain")) {
			throw new ResourceInterfaceException("Missing ```domain``` parameter. It is mandatory");
		}

		this.resourceName = parameters.get("resourceName");
		logger.debug("setup() resourceName:" + (this.resourceName != null ? this.resourceName : "NULL"));

		this.resourceURL = parameters.get("resourceURL");
		logger.debug("setup() resourceURL:" + (this.resourceName != null ? this.resourceURL : "NULL"));

		this.domain = parameters.get("domain");
		logger.debug("setup() domain:" + (this.resourceName != null ? this.domain : "NULL"));

		this.clientId = parameters.get("clientId");
		logger.debug("setup() clientId:" + (this.clientId != null ? this.clientId : "NULL"));

		this.namespace = parameters.get("namespace");
		logger.debug("setup() namespace:" + (this.namespace != null ? this.namespace : "NULL"));

		this.proxyURL = parameters.get("proxyURL");
		logger.debug("setup() proxyURL:" + (this.proxyURL != null ? this.proxyURL : "NULL"));

		String certificateString = parameters.get("ignoreCertificate");
		logger.debug("certificateString:" + (certificateString != null ? certificateString : "NULL"));

		if (this.proxyURL == null) {
			this.useProxy = false;
			this.userName = parameters.get("username");
			this.password = parameters.get("password");
			logger.debug("setup() Since no proxyURL has been specified. using username/password [" + this.userName + "/"
					+ this.password + "]");
		} else {
			logger.debug("setup() Using proxyURL to connect to i2b2??? We shall see ;)");
			this.useProxy = true;
		}

		if (certificateString != null && certificateString.equals("true")) {
			this.ignoreCertificate = true;
		} else {
			this.ignoreCertificate = false;
		}
		logger.debug("setup() ```ignoreCeriticate``` is "+ (this.ignoreCertificate ? "TRUE" : "FALSE"));

		// Setup Cells
		logger.debug("setup() Setting up CRCCell");
		crcCell = new CRCCell();
		logger.debug("setup() Setting up ONTCell");
		ontCell = new ONTCell();
		logger.debug("setup() Setting up PMCell");
		pmCell = new PMCell();
		logger.debug("setup() finished setting up everything. Zoom-zoom...");
		resourceState = ResourceState.READY;
	}

	@Override
	public String getType() {
		return "i2b2XML";
	}

	@Override
	public List<Entity> getPathRelationship(Entity path, OntologyRelationship relationship, User user)
			throws ResourceInterfaceException {
		logger.debug("getPathRelationship() Starting");
		logger.debug("getPathRelationship() path:" + (path==null?"NULL":path.getName() + path.getDisplayName() + path.getDescription()));
		logger.debug("getPathRelationship() relationship:" + (relationship == null?"NULL":relationship.getName()));

		List<Entity> entities = new ArrayList<Entity>();
		// Build
		HttpClient client = createClient(user);
		String basePath = path.getPui();
		String[] pathComponents = basePath.split("/");

		try {
			if (relationship == I2B2OntologyRelationship.CHILD) {
				logger.debug("getPathRelationship() I2B2OntologyRelationship.CHILD, with "+pathComponents.length+" components.");

				// If first then get projects
				if (pathComponents.length == 2) {
					logger.debug("getPathRelationship() creating PMCell.");
					pmCell = createPMCell();

					ConfigureType configureType = pmCell.getUserConfiguration(client, null,
							new String[] { "undefined" });
					for (ProjectType pt : configureType.getUser().getProject()) {
						logger.debug("getPathRelationship() ProjectType:"+pt.getName()+" "+pt.getDescription());

						Entity entity = new Entity();
						if (pt.getPath() == null) {
							entity.setPui(path.getPui() + "/" + URLEncoder.encode(pt.getName(), "UTF-8"));
						} else {
							entity.setPui(path.getPui() + URLEncoder.encode(pt.getPath(), "UTF-8"));
						}
						entity.setDisplayName(pt.getName());
						entity.setName(pt.getId());
						entity.setDescription(pt.getDescription());
						entities.add(entity);
					}

				} else {
					ontCell = createOntCell(pathComponents[2]);
					ConceptsType conceptsType = null;
					if (pathComponents.length == 3) {
						// If beyond second then get ontology categories
						conceptsType = ontCell.getCategories(client, false, false, true, "core");
					} else {
						// If second then get categories
						String myPath = "\\";
						for (String pathComponent : Arrays.copyOfRange(pathComponents, 3, pathComponents.length)) {
							myPath += "\\" + pathComponent;
						}
						basePath = pathComponents[0] + "/" + pathComponents[1] + "/" + pathComponents[2];

						conceptsType = ontCell.getChildren(client, myPath, false, true, false, -1, "core");

					}
					// Convert ConceptsType to Entities
					entities = convertConceptsTypeToEntities(basePath, conceptsType);
				}

			} else if (relationship == I2B2OntologyRelationship.MODIFIER) {
				logger.debug("getPathRelationship() I2B2OntologyRelationship.MODIFIER, with basePath(pui):"+(basePath==null?"NULL":basePath));

				String resourcePath = getResourcePathFromPUI(basePath);
				logger.debug("getPathRelationship() resourcePath:"+resourcePath);
				if (resourcePath == null) {
					return entities;
				}

				if (resourcePath.lastIndexOf('\\') != resourcePath.length() - 1) {
					resourcePath += '\\';
				}
				ontCell = createOntCell(pathComponents[2]);
				ModifiersType modifiersType = ontCell.getModifiers(client, false, false, null, -1, resourcePath, false,
						null);
				entities = convertModifiersTypeToEntities(basePath, modifiersType);
			} else if (relationship == I2B2OntologyRelationship.TERM) {
				String resourcePath = getResourcePathFromPUI(basePath);

				if (resourcePath == null) {
					return entities;
				}

				if (resourcePath.lastIndexOf('\\') != resourcePath.length() - 1) {
					resourcePath += '\\';
				}
				ontCell = createOntCell(pathComponents[2]);

				ConceptsType conceptsType = null;

				conceptsType = ontCell.getTermInfo(client, true, resourcePath, true, -1, true, "core");
				entities = convertConceptsTypeToEntities(basePath, conceptsType);
			} else {
				throw new ResourceInterfaceException(relationship.toString() + " not supported by this resource");
			}
		} catch (Exception e) {
			logger.error("getPathRelationship() Exception:", e);
			throw new ResourceInterfaceException(e.getMessage());
		}
		return entities;
	}

	@Override
	public List<Entity> find(Entity path, FindInformationInterface findInformation, User user)
			throws ResourceInterfaceException {
		List<Entity> returns = new ArrayList<Entity>();

		if (findInformation instanceof FindByPath) {
			returns = searchPaths(path, ((FindByPath) findInformation).getValues().get("term"),
					((FindByPath) findInformation).getValues().get("strategy"), user);
		} else if (findInformation instanceof FindByOntology) {
			String ontologyTerm = ((FindByOntology) findInformation).getValues().get("ontologyTerm");
			String ontologyType = ((FindByOntology) findInformation).getValues().get("ontologyType");
			returns = searchOntology(path, ontologyType, ontologyTerm, user);
		}

		return returns;
	}

	public List<Entity> searchPaths(Entity path, String searchTerm, String strategy, User user)
			throws ResourceInterfaceException {

		List<Entity> entities = new ArrayList<Entity>();
		HttpClient client = createClient(user);
		try {

			if ((path == null) || (path.getPui().split("/").length <= 2)) {
				pmCell = createPMCell();
				ConfigureType configureType = pmCell.getUserConfiguration(client, null, new String[] { "undefined" });
				for (ProjectType pt : configureType.getUser().getProject()) {
					for (ConceptType category : getCategories(client, pt.getId()).getConcept()) {

						String categoryName = converti2b2Path(category.getKey()).split("/")[1];

						entities.addAll(convertConceptsTypeToEntities("/" + this.resourceName + "/" + pt.getId(),
								runNameSearch(client, pt.getId(), categoryName, strategy, searchTerm)));
					}
				}
			} else {
				String[] pathComponents = path.getPui().split("/");
				if (pathComponents.length == 3) {
					// Get All Categories
					for (ConceptType category : getCategories(client, pathComponents[2]).getConcept()) {
						String categoryName = converti2b2Path(category.getKey()).split("/")[1];
						entities.addAll(convertConceptsTypeToEntities("/" + this.resourceName + "/" + pathComponents[2],
								runNameSearch(client, pathComponents[2], categoryName, strategy, searchTerm)));
					}
				} else {
					// Run request
					entities.addAll(convertConceptsTypeToEntities("/" + this.resourceName + "/" + pathComponents[2],
							runNameSearch(client, pathComponents[2], pathComponents[3], strategy, searchTerm)));
				}
			}
		} catch (JAXBException | UnsupportedOperationException | I2B2InterfaceException | IOException e) {
			throw new ResourceInterfaceException(e.getMessage());
		}
		return entities;
	}

	public List<Entity> searchOntology(Entity path, String ontologyType, String ontologyTerm, User user)
			throws ResourceInterfaceException {
		List<Entity> entities = new ArrayList<Entity>();
		HttpClient client = createClient(user);
		try {

			if ((path == null) || (path.getPui().split("/").length <= 2)) {
				pmCell = createPMCell();
				ConfigureType configureType = pmCell.getUserConfiguration(client, null, new String[] { "undefined" });
				for (ProjectType pt : configureType.getUser().getProject()) {
					entities.addAll(convertConceptsTypeToEntities("/" + this.resourceName + "/" + pt.getId(),
							runCategorySearch(client, pt.getId(), null, ontologyType, ontologyTerm)));
				}
			} else {
				String[] pathComponents = path.getPui().split("/");
				if (pathComponents.length == 3) {
					// Get All Categories
					entities.addAll(convertConceptsTypeToEntities("/" + this.resourceName + "/" + pathComponents[2],
							runCategorySearch(client, pathComponents[2], null, ontologyType, ontologyTerm)));
				} else {
					// Run request
					entities.addAll(convertConceptsTypeToEntities("/" + this.resourceName + "/" + pathComponents[2],
							runCategorySearch(client, pathComponents[2], pathComponents[3], ontologyType,
									ontologyTerm)));
				}
			}
		} catch (JAXBException | UnsupportedOperationException | I2B2InterfaceException | IOException e) {
			throw new ResourceInterfaceException(e.getMessage());
		}
		return entities;
	}

	@Override
	public Result runQuery(User user, Query query, Result result) throws ResourceInterfaceException {

		if (query.getMetaData()!= null
				&& !query.getMetaData().isEmpty())
			result.getMetaData().putAll(query.getMetaData());

		// Initial setup
		HttpClient client = createClient(user);
		result.setResultStatus(ResultStatus.CREATED);
		String projectId = "";

        // gather select clauses
        // I don't care about the performance here!!
        // and actually this gathering select clauses block is from I2B2TranSMARTResourceImplementation.java
        // It is working which is the only thing that I know
        Map<String, String> aliasMap = new LinkedHashMap<>();
        for (SelectClause selectClause : query
                .getClausesOfType(SelectClause.class)) {
            String pui = selectClause.getParameter().getPui()
                    .replaceAll("/" + this.resourceName + "/", "");

            String rawPUI = selectClause.getParameter().getPui();
            if (rawPUI.endsWith("*")) {
                //Get the base PUI
                String basePUI = rawPUI.substring(0, rawPUI.length() - 1);
                boolean compact = false;
                String subPUI = null;

                if(selectClause.getStringValues().containsKey("COMPACT") && selectClause.getStringValues().get("COMPACT").equalsIgnoreCase("true")) {
                    compact = true;
                }
                if(selectClause.getStringValues().containsKey("REMOVEPREPEND") && selectClause.getStringValues().get("REMOVEPREPEND").equalsIgnoreCase("true")) {
                    subPUI = basePUI.substring(0, basePUI.substring(0, basePUI.length() - 1).lastIndexOf("/"));
                }

                //Loop through all the children and add them to the aliasMap
                aliasMap.putAll(getAllChildrenAsAliasMap(basePUI, subPUI, compact, user));

            } else {
                pui = getPathFromString(selectClause.getParameter()
                        .getPui());
                aliasMap.put(pui,
                        selectClause.getAlias());
            }
        }
        // The blob above is from TransmartResourceImplementation

		if (aliasMap.size() != 0)
        	result.getMetaData().put("aliasMap", aliasMap); // pass it down to the getResult() to retrieve selected data

		// Create the query
		ArrayList<PanelType> panels = new ArrayList<PanelType>();
		int panelCount = 1;

		try {
			PanelType currentPanel = createPanel(panelCount);

			for (ClauseAbstract clause : query.getClauses().values()) {
				if (clause instanceof WhereClause) {
					// Get the projectId if it isn't already set
					if (projectId.equals("")) {
						String[] pathComponents = ((WhereClause) clause).getField().getPui().split("/");
						projectId = pathComponents[2];
					}
					WhereClause whereClause = (WhereClause) clause;
					ItemType itemType = createItemTypeFromWhereClause(whereClause);

					// FIRST
					if (panels.isEmpty() && currentPanel.getItem().isEmpty()) {
						currentPanel.getItem().add(itemType);
					} else if (whereClause.getLogicalOperator() == LogicalOperator.AND) {
						panels.add(currentPanel);
						currentPanel = createPanel(panelCount++);
						currentPanel.getItem().add(itemType);
					} else if (whereClause.getLogicalOperator() == LogicalOperator.OR) {
						currentPanel.getItem().add(itemType);
					} else if (whereClause.getLogicalOperator() == LogicalOperator.NOT) {
						panels.add(currentPanel);
						currentPanel = createPanel(panelCount++);
						currentPanel.getItem().add(itemType);
						currentPanel.setInvert(1);
						panels.add(currentPanel);
						currentPanel = createPanel(panelCount++);
					}
				}
			}
			if (currentPanel.getItem().size() != 0) {
				panels.add(currentPanel);
			}
		} catch (DatatypeConfigurationException e) {
			result.setResultStatus(ResultStatus.ERROR);
			result.setMessage("runQuery() DatatypeConfigurationException:"+e.getMessage());
		} catch (Exception e) {
			result.setResultStatus(ResultStatus.ERROR);
			result.setMessage("runQuery() Exception:"+e.getMessage());
		}

		ResultOutputOptionListType roolt = new ResultOutputOptionListType();
		ResultOutputOptionType root = new ResultOutputOptionType();
		root.setPriorityIndex(10);
		root.setName("PATIENTSET");
		roolt.getResultOutput().add(root);

		try {
			crcCell = createCRCCell(projectId, user.getName());
			MasterInstanceResultResponseType mirrt = crcCell.runQueryInstanceFromQueryDefinition(client, null, null,
					"IRCT", null, "ANY", 0, roolt, panels.toArray(new PanelType[panels.size()]));

			String resultId = mirrt.getQueryResultInstance().get(0).getResultInstanceId();
			String queryId = mirrt.getQueryResultInstance().get(0).getQueryInstanceId();
			result.setResourceActionId(projectId + "|" + queryId + "|" + resultId);
			result.setResultStatus(ResultStatus.RUNNING);
		} catch (JAXBException | IOException | I2B2InterfaceException e) {
			logger.error(getType()+".runQuery() "+e.getMessage()+" "+e);
			e.printStackTrace();
			result.setResultStatus(ResultStatus.ERROR);
			result.setMessage(getType()+".runQuery() OtherException: "+e.getMessage());
		}

		return result;
	}

	@Override
	public Result getResults(User user, Result result) throws ResourceInterfaceException {
		logger.debug("getResults() Starting...");
		try {
			result = checkForResult(user, result);

			if (result.getResultStatus() != ResultStatus.COMPLETE) {
				logger.debug("getResults() Result is not yet complete. Returning.");
				return result;
			} else {
				logger.debug("getResults() Current `ResultStatus` is "+
						(result.getResultStatus()==null?"NULL":result.getResultStatus().toString()));
			}

			// if only_count is enabled,
			// do not retrieve all the patient data set
			if ( !result.getMetaData().isEmpty()
					&& result.getMetaData().containsKey("only_count")) {
				return result;
			}

			// after checking i2b2's result status
			// go to retrieve data
			result.setResultStatus(ResultStatus.RUNNING);
			logger.debug("getResults() Changed `ResultStatus` back to running.");

			HttpClient client = createClient(user);
			String resultInstanceId = result.getResourceActionId();
			String resultId = resultInstanceId.split("\\|")[2];

			// Get PDO List
			logger.debug("getResults() getting PDOFromInputList with "+
					"resultInstanceId:"+(resultInstanceId==null?"NULL":resultInstanceId)+
					" and resultId:"+(resultId==null?"NULL":resultId));

			PatientDataResponseType pdrt = null;
			if (result.getMetaData().containsKey("aliasMap"))
				pdrt = crcCell.getPDOfromInputList(client, resultId, 0, 100000, false, false, false,
					null, result.getMetaData());
			else
				pdrt = crcCell.getPDOfromInputList(client, resultId, 0, 100000, false, false, false,
						OutputOptionSelectType.USING_INPUT_LIST);

			convertPatientDataResponseTypeToResultSet(pdrt, result);

			logger.debug("getResults() calling *convertPatientSetToResultSet*");
			result = convertPatientSetToResultSet(pdrt, result);

			logger.debug("getResults() Setting ```ResultStatus``` to COMPLETE.");
			result.setResultStatus(ResultStatus.COMPLETE);
		} catch (JAXBException | I2B2InterfaceException | IOException | ResultSetException | PersistableException e) {
			logger.error("getResults() OtherException");

			result.setMessage("getResults() OtherException:"+e.getMessage());
			result.setResultStatus(ResultStatus.ERROR);
		}
		return result;
	}

	/**
	 * Checks to see if the result is available
	 *
	 * @param result
	 *            Result
	 * @return Result
	 */
	protected Result checkForResult(User user, Result result) {
		logger.debug("checkForResult() Starting...");

		HttpClient client = createClient(user);

		// If resourceActionId is null, we cannot move forward. This means (as of now 2017-08-25)
		// that perhaps HTTP could not communicate? Note sure, but without transaction tracking,
		// we don't have much of way to track the source of the error. Only that it is not set :(
		String resultInstanceId = result.getResourceActionId();
		if (resultInstanceId==null) {
			result.setResultStatus(ResultStatus.ERROR);
			result.setMessage("Result is not available, because ActionId is NULL. Cryptic error for now.");
			return result;
		}
		logger.debug("checkForResult() resultInstanceId:"+(resultInstanceId!=null?resultInstanceId:"NULL"));

		String[] resultInstanceIdArray = resultInstanceId.split("\\|");
		String projectId = resultInstanceIdArray[0];
		logger.debug("checkForResult() projectId:"+(projectId!=null?projectId:"NULL"));

		String queryId = resultInstanceIdArray[1];
		logger.debug("checkForResult() queryId:"+(queryId!=null?queryId:"NULL"));

		try {
			logger.debug("checkForResult() creating `CRCCell`");
			CRCCell crcCell = createCRCCell(projectId, user.getName());

			// Is Query Master List Complete?
			InstanceResponseType instanceResponse = crcCell.getQueryInstanceListFromQueryId(client, queryId);

			String queryinstancelistStatus = null;
			// Determine the preliminary query status
			if (instanceResponse.getQueryInstance().size()>0) {
                queryinstancelistStatus = instanceResponse.getQueryInstance().get(0).getQueryStatusType().getName();
            } else {
			    if (instanceResponse.getStatus().getCondition().size()>0) {
			        // As an alternative, if there is no `QueryInstance` coming back, use the `Status` element, to determine the remote i2b2 query status
                    queryinstancelistStatus = instanceResponse.getStatus().getCondition().get(0).getValue();
                } else {
                    result.setResultStatus(ResultStatus.ERROR);
                    result.setMessage("Could not determine the remote query status.");
                    return result;
                }
            }

            switch (queryinstancelistStatus) {
                case "RUNNING":
                    result.setResultStatus(ResultStatus.RUNNING);
                    return result;
                case "ERROR":
                case "INCOMPLETE":
                    result.setResultStatus(ResultStatus.ERROR);
                    result.setMessage("queryinstancelistStatus:"+(queryinstancelistStatus != null ? queryinstancelistStatus : "NULL"));
                    return result;
                default:
                    logger.warn("checkForResult() queryinstancelistStatus:" + (queryinstancelistStatus == null ? "NULL" : queryinstancelistStatus));
            }

            // Is Query Result instance list complete?
            QueryResultInstanceType queryResultInstance = crcCell
                    .getQueryResultInstanceListFromQueryInstanceId(client, queryId).get(0);

            String queryResultInstanceStatusType = queryResultInstance.getQueryStatusType().getName();
            logger.debug("checkForResult() queryResultInstanceStatusType:" + (queryResultInstanceStatusType == null ? "NULL" : queryResultInstanceStatusType));
            switch (queryResultInstanceStatusType) {
                case "RUNNING":
                    result.setResultStatus(ResultStatus.RUNNING);
                    return result;
                case "ERROR":
                    result.setResultStatus(ResultStatus.ERROR);
                    return result;
                default:
                    result.setMessage("`queryResultInstanceStatusType` is:" + (queryResultInstanceStatusType == null ? "NULL" : queryResultInstanceStatusType));
            }

            // check if only_count exist,
            // if yes, will only retrieve counts from i2b2 and put it into result
            // will not download all the data from i2b2 later (which is thousands millions of rows...)
            if (result.getMetaData() != null
                    && !result.getMetaData().isEmpty()
                    && result.getMetaData().containsKey("only_count")) {
                long counts = queryResultInstance.getSetSize();
                FileResultSet frs = (FileResultSet) result.getData();
                try {
                    frs.appendColumn(new Column("patient_set_counts", PrimitiveDataType.STRING));
                    frs.appendRow();
                    frs.updateString("patient_set_counts", Long.toString(counts));

                    result.setResultStatus(ResultStatus.COMPLETE);
                    result.setMessage(getType() + " `only_count` query has finished.");
                } catch (ResultSetException e) {
                    logger.error("checkForResult() generating patient set counts file error: " + e.getMessage());
                    result.setResultStatus(ResultStatus.ERROR);
                    result.setMessage("generating patient set counts file error: " + e.getMessage());
                } catch (PersistableException e) {
                    logger.error("checkForResult() cannot persist FileResultSet: " + e.getMessage());
                    result.setResultStatus(ResultStatus.ERROR);
                    result.setMessage("cannot persist result file: " + e.getMessage());
                }

            }

            // Stop checking and change the status to COMPLETE
            if (queryResultInstanceStatusType.equalsIgnoreCase("FINISHED")) {
                logger.debug("checkForResult() queryResultInstanceStatusType is "+queryResultInstanceStatusType+" so change result status to COMPLETE");
                        result.setResultStatus(ResultStatus.COMPLETE);
            }


		} catch (JAXBException | I2B2InterfaceException | IOException e) {
			logger.error("checkForResult() OtherException:"+e.getMessage());
			e.printStackTrace();

			result.setMessage("checkForResult() OtherException: "+e.getMessage());
			result.setResultStatus(ResultStatus.ERROR);
		}
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

	// -------------------------------------------------------------------------
	// Utility Methods
	// -------------------------------------------------------------------------

	private ItemType createItemTypeFromWhereClause(WhereClause whereClause) throws DatatypeConfigurationException {
		ItemType item = new ItemType();
		String myPath = getPathFromField(whereClause.getField());

		item.setItemKey(myPath);
		item.setItemName(myPath);

		item.setItemIsSynonym(false);
		if (whereClause.getPredicateType() != null) {
			if (whereClause.getPredicateType().getName().equals("CONSTRAIN_MODIFIER")) {
				item.setConstrainByModifier(createConstrainByModifier(whereClause));
			} else if (whereClause.getPredicateType().getName().equals("CONSTRAIN_VALUE")) {
				item.getConstrainByValue()
						.add(createConstrainByValue(whereClause, whereClause.getField().getDataType()));
			} else if (whereClause.getPredicateType().getName().equals("CONSTRAIN_DATE")) {
				item.getConstrainByDate().add(createConstrainByDate(whereClause));
			}
		}
		return item;
	}

	private ItemType.ConstrainByValue createConstrainByValue(WhereClause whereClause, DataType dataType) {
		ItemType.ConstrainByValue cbv = new ItemType.ConstrainByValue();
		// value_operator
		cbv.setValueOperator(ConstrainOperatorType.fromValue(whereClause.getStringValues().get("OPERATOR")));
		// value_constraint
		cbv.setValueConstraint(whereClause.getStringValues().get("CONSTRAINT"));
		// value_unit_of_measure
		cbv.setValueUnitOfMeasure(whereClause.getStringValues().get("UNIT_OF_MEASURE"));
		// value_type
		if ((dataType.toString().equals("INTEGER")) || (dataType.toString().equals("LONG"))) {
			cbv.setValueType(ConstrainValueType.NUMBER);
		} else if (dataType.toString().equals("STRING")) {
			cbv.setValueType(ConstrainValueType.TEXT);
		}

		return cbv;
	}

	private ItemType.ConstrainByModifier createConstrainByModifier(WhereClause whereClause) {
		String modifierPath = getPathFromString(whereClause.getStringValues().get("MODIFIER_KEY"));
		ItemType.ConstrainByModifier cbm = new ItemType.ConstrainByModifier();
		cbm.setModifierKey(modifierPath);
		return cbm;
	}

	private ItemType.ConstrainByDate createConstrainByDate(WhereClause whereClause)
			throws DatatypeConfigurationException {
		ItemType.ConstrainByDate cbd = new ItemType.ConstrainByDate();
		ConstrainDateType from = new ConstrainDateType();
		from.setInclusive(InclusiveType.fromValue(whereClause.getStringValues().get("FROM_INCLUSIVE")));
		from.setTime(ConstrainDateTimeType.fromValue(whereClause.getStringValues().get("FROM_TIME")));

		from.setValue(
				DatatypeFactory.newInstance().newXMLGregorianCalendar(whereClause.getStringValues().get("FROM_DATE")));

		cbd.setDateFrom(from);

		ConstrainDateType to = new ConstrainDateType();
		to.setInclusive(InclusiveType.fromValue(whereClause.getStringValues().get("TO_INCLUSIVE")));
		to.setTime(ConstrainDateTimeType.fromValue(whereClause.getStringValues().get("TO_TIME")));
		to.setValue(
				DatatypeFactory.newInstance().newXMLGregorianCalendar(whereClause.getStringValues().get("TO_DATE")));
		cbd.setDateTo(to);
		return cbd;
	}

	private String getResourcePathFromPUI(String pui) {
		String[] pathComponents = pui.split("/");

		if (pathComponents.length <= 2) {
			return null;
		}
		String myPath = "";

		for (String pathComponent : Arrays.copyOfRange(pathComponents, 3, pathComponents.length)) {
			myPath += "\\" + pathComponent;
		}

		return myPath;
	}

	/**
	 * to save i2b2 xml query response to FileResultSet
	 * @param patientDataResponse
	 * @param result
	 * @return
	 */
	private void convertPatientDataResponseTypeToResultSet(PatientDataResponseType patientDataResponse, Result result){
		logger.debug("convertPatientDataResponseTypeToResultSet() starting...");


		FileResultSet mrs = (FileResultSet) result.getData();

		if (result.getMetaData().containsKey("aliasMap")){

		}


	}

	/**
	 * FileResultSet will be used to store data
	 * @param patientDataResponse
	 * @param result
	 * @return
	 * @throws ResultSetException
	 * @throws PersistableException
	 */
	private Result convertPatientSetToResultSet(PatientDataResponseType patientDataResponse, Result result)
			throws ResultSetException, PersistableException {
		logger.debug("convertPatientSetToResultSet() Starting...");

		PatientSet patientSet = patientDataResponse.getPatientData().getPatientSet();
		logger.debug("convertPatientSetToResultSet() getting data from ```result```.");
		FileResultSet mrs = (FileResultSet) result.getData();

		if (patientSet.getPatient().size() == 0) {
			logger.warn("convertPatientSetToResultSet() patient set size is 0.");
			return result;
		} else {
			logger.debug("convertPatientSetToResultSet() patient set size is "+patientSet.getPatient().size());
		}

		PatientType columnPT = patientSet.getPatient().get(0);
		Column idColumn = new Column();
		idColumn.setName("Patient Id");
		idColumn.setDataType(PrimitiveDataType.STRING);
		mrs.appendColumn(idColumn);
		for (ParamType paramType : columnPT.getParam()) {
			Column column = new Column();
			column.setName(paramType.getColumn());
			column.setDataType(PrimitiveDataType.STRING);
			mrs.appendColumn(column);
		}

		for (PatientType patientType : patientSet.getPatient()) {
			mrs.appendRow();
			mrs.updateString("Patient Id", patientType.getPatientId().getValue());
			for (ParamType paramType : patientType.getParam()) {
				mrs.updateString(paramType.getColumn(), paramType.getValue());
			}
		}
		result.setData(mrs);

		return result;
	}

	private String getPathFromField(Entity field) {
		return getPathFromString(field.getPui());
	}

	/**
	 * convert string into pui
	 * @param pathString
	 * @return
	 */
	private String getPathFromString(String pathString) {
		String[] pathComponents = pathString.split("/");
		String myPath = "\\";
		for (String pathComponent : Arrays.copyOfRange(pathComponents, 3, pathComponents.length)) {
			myPath += "\\" + pathComponent;
		}
		if (!pathString.endsWith("/")) {
			myPath += "\\";
		}

		return myPath;
	}

	private PanelType createPanel(int panelItem) {
		PanelType panel = new PanelType();
		panel.setPanelNumber(panelItem);
		panel.setInvert(0);
		panel.setPanelTiming("ANY");

		PanelType.TotalItemOccurrences tio = new PanelType.TotalItemOccurrences();
		tio.setValue(1);
		panel.setTotalItemOccurrences(tio);

		return panel;
	}

	private List<Entity> convertConceptsTypeToEntities(String basePath, ConceptsType conceptsType)
			throws UnsupportedEncodingException {
		List<Entity> returns = new ArrayList<Entity>();
		for (ConceptType concept : conceptsType.getConcept()) {
			Entity returnEntity = new Entity();
			returnEntity.setName(concept.getName());
			String appendPath = converti2b2Path(concept.getKey());
			returnEntity.setPui(basePath + appendPath);

			if (concept.getVisualattributes().startsWith("L")) {
				returnEntity.setDataType(PrimitiveDataType.STRING);
			}

			returnEntity.setDisplayName(concept.getName());

			if (concept.getBasecode() != null) {
				String[] baseCode = concept.getBasecode().split(":");
				if (baseCode.length == 2) {
					returnEntity.setOntology(baseCode[0]);
					returnEntity.setOntologyId(baseCode[1]);
				}
			}

			Map<String, String> attributes = new HashMap<String, String>();
			attributes.put("level", Integer.toString(concept.getLevel()));
			attributes.put("key", concept.getKey());
			attributes.put("name", concept.getName());

			attributes.put("synonymCd", concept.getSynonymCd());
			attributes.put("visualattributes", concept.getVisualattributes());
			if (concept.getTotalnum() != null) {
				attributes.put("totalnum", concept.getTotalnum().toString());
			}

			attributes.put("facttablecolumn", concept.getFacttablecolumn());
			attributes.put("tablename", concept.getTablename());
			attributes.put("columnname", concept.getColumnname());
			attributes.put("columndatatype", concept.getColumndatatype());
			attributes.put("operator", concept.getOperator());
			attributes.put("dimcode", concept.getDimcode());
			attributes.put("comment", concept.getComment());
			attributes.put("tooltip", concept.getTooltip());
			attributes.put("sourcesystemCd", concept.getSourcesystemCd());
			attributes.put("valuetypeCd", concept.getValuetypeCd());
			ModifierType modifier = concept.getModifier();
			if (modifier != null) {
				attributes.put("modifier.level", Integer.toString(modifier.getLevel()));
				attributes.put("modifier.appliedPath", modifier.getAppliedPath());
				attributes.put("modifier.key", modifier.getKey());
				attributes.put("modifier.fullname", modifier.getFullname());
				attributes.put("modifier.name", modifier.getName());
				attributes.put("modifier.visualattributes", modifier.getVisualattributes());
				attributes.put("modifier.synonymCd", modifier.getSynonymCd());
				attributes.put("modifier.totalnum", modifier.getTotalnum().toString());
				attributes.put("modifier.basecode", modifier.getBasecode());
				attributes.put("modifier.facttablecolumn", modifier.getFacttablecolumn());
				attributes.put("modifier.tablename", modifier.getTablename());
				attributes.put("modifier.columnname", modifier.getColumnname());
				attributes.put("modifier.columndatatype", modifier.getColumndatatype());
				attributes.put("modifier.operator", modifier.getOperator());
				attributes.put("modifier.dimcode", modifier.getDimcode());
				attributes.put("modifier.comment", modifier.getComment());
				attributes.put("modifier.tooltip", modifier.getTooltip());
				attributes.put("modifier.sourcesystemCd", modifier.getSourcesystemCd());
			}

			returnEntity.setAttributes(attributes);
			returns.add(returnEntity);
		}

		return returns;
	}

	private List<Entity> convertModifiersTypeToEntities(String basePath, ModifiersType modifiersType)
			throws UnsupportedEncodingException {
		List<Entity> returns = new ArrayList<Entity>();
		if (modifiersType == null) {
			return returns;
		}
		for (ModifierType modifier : modifiersType.getModifier()) {
			Entity returnEntity = new Entity();
			returnEntity.setName(modifier.getName());
			String appendPath = converti2b2Path(modifier.getKey());
			returnEntity.setPui(basePath + appendPath);

			if (modifier.getColumndatatype().equals("T")) {
				returnEntity.setDataType(PrimitiveDataType.STRING);
			}

			Map<String, String> attributes = new HashMap<String, String>();

			attributes.put("appliedPath", modifier.getAppliedPath());
			attributes.put("baseCode", modifier.getBasecode());
			attributes.put("columnName", modifier.getColumnname());
			attributes.put("comment", modifier.getComment());
			attributes.put("dimCode", modifier.getDimcode());
			// attributes.put("downloadDate", modifier.getDownloadDate());
			attributes.put("factTableColumn", modifier.getFacttablecolumn());
			attributes.put("fullName", modifier.getFullname());
			// attributes.put("importDate", modifier.getImportDate());
			attributes.put("level", Integer.toString(modifier.getLevel()));
			// attributes.put("metadataXML", modifier.getMetadataxml());
			attributes.put("operator", modifier.getOperator());
			attributes.put("sourceSystemCd", modifier.getSourcesystemCd());
			attributes.put("synonymCd", modifier.getSynonymCd());
			attributes.put("tableName", modifier.getTablename());
			attributes.put("toolTip", modifier.getTooltip());
			if (modifier.getTotalnum() != null) {
				attributes.put("totalNum", Integer.toString(modifier.getTotalnum()));
			}
			// attributes.put("updateDate", modifier.getUpdateDate());
			attributes.put("visualAttributes", modifier.getVisualattributes());
			returnEntity.setAttributes(attributes);
			returns.add(returnEntity);
		}

		return returns;
	}

	protected String converti2b2Path(String i2b2Path) throws UnsupportedEncodingException {
		String[] components = i2b2Path.split("\\\\");
		String escapedPath = "/";
		for (String component : components) {
			if (!component.isEmpty()) {
				escapedPath += component.replaceAll("/", "%2F") + "/";
			}
		}

		return escapedPath;
	}

	private ConceptsType runNameSearch(HttpClient client, String projectId, String category, String strategy,
			String searchTerm)
			throws UnsupportedOperationException, JAXBException, I2B2InterfaceException, IOException {
		ONTCell ontCell = createOntCell(projectId);
		return ontCell.getNameInfo(client, true, category, false, strategy, searchTerm, -1, null, true, "core");
	}

	private ConceptsType getCategories(HttpClient client, String projectId)
			throws JAXBException, ClientProtocolException, IOException, I2B2InterfaceException {
		ONTCell ontCell = createOntCell(projectId);

		return ontCell.getCategories(client, false, false, true, "core");
	}

	private ConceptsType runCategorySearch(HttpClient client, String projectId, String category, String ontologyType,
			String ontologyTerm)
			throws UnsupportedOperationException, JAXBException, I2B2InterfaceException, IOException {
		ONTCell ontCell = createOntCell(projectId);
		return ontCell.getCodeInfo(client, true, category, false, "exact", ontologyType + ":" + ontologyTerm, -1, null,
				true, "core");
	}

	private CRCCell createCRCCell(String projectId, String userName) throws JAXBException {
		if (this.useProxy) {
			crcCell.setupConnection(this.resourceURL, this.domain, userName, "", projectId, this.useProxy,
					this.proxyURL + "/QueryToolService");
		} else {
			crcCell.setupConnection(this.resourceURL + "QueryToolService/", this.domain, this.userName, this.password,
					projectId, false, null);
		}
		return crcCell;
	}

	private ONTCell createOntCell(String projectId) throws JAXBException {
		if (this.useProxy) {
			ontCell.setupConnection(this.resourceURL, this.domain, "", "", projectId, this.useProxy,
					this.proxyURL + "/OntologyService");
		} else {
			ontCell.setupConnection(this.resourceURL + "OntologyService/", this.domain, this.userName, this.password,
					projectId, false, null);
		}
		return ontCell;
	}

	private PMCell createPMCell() throws JAXBException {
		if (this.useProxy) {
			pmCell.setupConnection(this.resourceURL, this.domain, "", "", "", this.useProxy,
					this.proxyURL + "/PMService");
		} else {
			pmCell.setupConnection(this.resourceURL + "PMService/", this.domain, this.userName, this.password, "",
					false, null);
		}
		return pmCell;
	}

	/**
	 * CREATES A CLIENT
	 *
	 * @param user
	 * @return
	 */
	protected HttpClient createClient(User user) {
		// SSL WRAPAROUND
		logger.debug("createClient() user:" + user.getName());
		HttpClientBuilder returns = null;

		if (ignoreCertificate) {
			logger.debug("createClient() ignoring certificates");
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

		this.addAuthenticationHeader(user, defaultHeaders);

		logger.debug("createClient() Header `Content-Type: application/x-www-form-urlencoded` will be added to the builder.");
		defaultHeaders.add(new BasicHeader("Content-Type", "application/x-www-form-urlencoded"));
		returns.setDefaultHeaders(defaultHeaders);
		logger.debug("createClient() Finished");
		return returns.build();
	}

	protected void addAuthenticationHeader(User user, List<Header> defaultHeaders) {
		// Do nothing.
	}

	private HttpClientBuilder ignoreCertificate() throws NoSuchAlgorithmException, KeyManagementException {
		System.setProperty("jsse.enableSNIExtension", "false");

		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}
		} };

		SSLContext sslContext;

		sslContext = SSLContext.getInstance("SSL");
		sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

		HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

		Registry<ConnectionSocketFactory> r = RegistryBuilder.<ConnectionSocketFactory>create().register("https", sslsf)
				.register("http", PlainConnectionSocketFactory.getSocketFactory())
				.build();

		HttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(r);

		return HttpClients.custom().setConnectionManager(cm);
	}


    protected Map<String, String> getAllChildrenAsAliasMap(String basePUI, String subPUI, boolean compact, User user) throws ResourceInterfaceException {
        Map<String, String> returns = new HashMap<String, String>();

        Entity baseEntity = new Entity(basePUI);
        for(Entity entity : getPathRelationship(baseEntity, I2B2OntologyRelationship.CHILD, user)) {

            if(entity.getAttributes().containsKey("visualattributes")) {
                String visualAttributes = entity.getAttributes().get("visualattributes");

                if(visualAttributes.startsWith("C") || visualAttributes.startsWith("F")) {
                    returns.putAll(getAllChildrenAsAliasMap(entity.getPui(), subPUI, compact, user));
                } else if (visualAttributes.startsWith("L")) {
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
                    returns.put(pui, alias);
                }

            }
        }

        return returns;
    }

    protected String convertPUItoI2B2Path(String pui) {
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
