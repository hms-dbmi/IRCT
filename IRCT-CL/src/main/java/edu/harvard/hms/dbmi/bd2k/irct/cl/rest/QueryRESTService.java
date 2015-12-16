/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.cl.rest;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonStructure;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import edu.harvard.hms.dbmi.bd2k.irct.cl.util.AdminBean;
import edu.harvard.hms.dbmi.bd2k.irct.cl.util.Constants;
import edu.harvard.hms.dbmi.bd2k.irct.controller.PathController;
import edu.harvard.hms.dbmi.bd2k.irct.controller.QueryController;
import edu.harvard.hms.dbmi.bd2k.irct.controller.ExecutionController;
import edu.harvard.hms.dbmi.bd2k.irct.controller.ResourceController;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ClauseIsNotTheCorrectType;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ClauseNotFoundException;
import edu.harvard.hms.dbmi.bd2k.irct.exception.JoinTypeNotSupported;
import edu.harvard.hms.dbmi.bd2k.irct.exception.LogicalOperatorNotFound;
import edu.harvard.hms.dbmi.bd2k.irct.exception.PredicateTypeNotSupported;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceNotFoundException;
import edu.harvard.hms.dbmi.bd2k.irct.exception.SubQueryNotFoundException;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;

/**
 * Creates the query service for the JAX-RS REST service. This service is
 * conversation scoped all requests for working with a query must include the
 * conversation id that represents the query. An end user may have multiple
 * queries going along the same time.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Path("/queryRESTService")
@ConversationScoped
@Named
public class QueryRESTService implements Serializable {

	private static final long serialVersionUID = 5458918309919812803L;

	@Inject
	private QueryController qc;

	@Inject
	private ResourceController rc;

	@Inject
	private PathController pc;

	@Inject
	private ExecutionController ec;

	@Inject
	private AdminBean admin;

	/**
	 * Initiates the creation of a query
	 * 
	 * @return A JSON Object representing the status of that request
	 */
	@GET
	@Path("/startQuery")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure startQuery() {
		String conversationId = admin.startConversation();
		qc.createQuery(conversationId);

		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
		responseBuilder.add("status", Constants.STATUS_OK);
		responseBuilder.add("cid", conversationId);
		responseBuilder.add("version", Constants.QUERYPROTOCOL);

		return responseBuilder.build();
	}


	/**
	 * Initiates the creation of a subQuery. Each query can have one or more
	 * subQueries. These can be used to combine datasets from multiple
	 * resources. Each subQuery can be associated with one or more resources.
	 * 
	 * @param resource The Resource
	 * @return A JSON Object representing the status of that request
	 */
	@GET
	@Path("/startSubQuery")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure startSubQuery(
			@DefaultValue("") @QueryParam(value = "resource") String resource) {
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
		Long sqId;
		try {
			String[] resourceNames = resource.split(",");

			Resource[] resources = new Resource[resourceNames.length];

			for (int resourceI = 0; resourceI < resourceNames.length; resourceI++) {
				resources[resourceI] = rc.getResource(resourceNames[resourceI]);
			}

			sqId = qc.createSubQuery(resources);

			responseBuilder.add("status", Constants.STATUS_OK);
			responseBuilder.add("subQueryId", sqId);
		} catch (ResourceNotFoundException e) {
			responseBuilder.add("status", Constants.STATUS_ERROR_FAIL);
			responseBuilder.add("message", e.getMessage());
		}

		return responseBuilder.build();
	}

	/**
	 * Adds a select clause to the query
	 * 
	 * @param sq
	 *            Query ID
	 * @param field
	 *            Parameter name
	 * @param alias
	 *            Parameter alias         
	 * @return A JSON Object representing the status of that request
	 */
	@GET
	@Path("/selectClause")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure selectClause(
			@DefaultValue("") @QueryParam(value = "sq") String sq,
			@DefaultValue("") @QueryParam(value = "field") String field,
			@DefaultValue("") @QueryParam(value = "alias") String alias) {
		
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
		Long selectId;

		try {
			Resource resource = rc.getResource(field.split("/")[0]);
			edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Path path = getPath(field);
			path.setName(alias);
			
			if (sq.equals("")) {
				selectId = qc.addSelectClause(null, path, resource);

			} else {
				selectId = qc.addSelectClause(Long.parseLong(sq),
						path, resource);
			}
			responseBuilder.add("status", Constants.STATUS_OK);
			responseBuilder.add("clauseId", selectId);

		} catch (SubQueryNotFoundException e) {
			responseBuilder.add("status", Constants.STATUS_ERROR_FAIL);
			responseBuilder.add("message", e.getMessage());
		}

		return responseBuilder.build();
	}

	/**
	 * Adds a join clause to the query
	 * 
	 * @param info
	 *            Request Information
	 * 
	 * @return A JSON Object representing the status of that request
	 */
	@GET
	@Path("/joinClause")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure joinClause(@Context UriInfo info) {
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
		MultivaluedMap<String, String> parameters = info.getPathParameters();
		Long sqId1 = Long.parseLong(getFirstMultiValuedMap(parameters, "q1"));
		Long sqId2 = Long.parseLong(getFirstMultiValuedMap(parameters, "q2"));
		String joinType = getFirstMultiValuedMap(parameters, "joinType");
		String f1 = getFirstMultiValuedMap(parameters, "f1");
		String f2 = getFirstMultiValuedMap(parameters, "f2");
		String relationship = getFirstMultiValuedMap(parameters, "relationship");
		Long joinId = Long.parseLong(getFirstMultiValuedMap(parameters,
				"joinID"));

		if (joinType == null) {
			responseBuilder.add("status", Constants.STATUS_ERROR_FAIL);
			responseBuilder.add("message",
					"Required parameter: joinType not passed");
			return responseBuilder.build();
		}

		try {

			Long newJoinId = qc.addJoinClause(sqId1, sqId2, joinType,
					getPath(f1), getPath(f2), relationship, joinId);
			responseBuilder.add("status", Constants.STATUS_OK);
			responseBuilder.add("clauseId", newJoinId);

		} catch (ClauseNotFoundException | ClauseIsNotTheCorrectType
				| SubQueryNotFoundException | JoinTypeNotSupported e) {
			responseBuilder.add("status", Constants.STATUS_ERROR_FAIL);
			responseBuilder.add("message", e.getMessage());
		}

		return responseBuilder.build();
	}

	/**
	 *  Adds or edits a where clause
	 *  
	 * @param info Information about that request
	 * @return A JSON Object representing the status of that request
	 */
	@GET
	@POST
	@Path("/whereClause")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure whereClause(@Context UriInfo info) {
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
		MultivaluedMap<String, String> parameters = info.getQueryParameters();

		String sq = getFirstMultiValuedMap(parameters, "sq");
		Long sqId = null;
		if (sq != null) {
			sqId = Long.parseLong(sq);
		}
		String logicalOperator = getFirstMultiValuedMap(parameters,
				"logicalOperator");
		if (logicalOperator == null) {
			logicalOperator = "AND";
		}

		String field = getFirstMultiValuedMap(parameters, "field");
		String predicate = getFirstMultiValuedMap(parameters, "predicate");

		String whereParam = getFirstMultiValuedMap(parameters, "clauseId");
		Long whereId = null;

		if (whereParam != null) {
			whereId = Long.parseLong(whereParam);
		}

		if (field == null) {
			responseBuilder.add("status", Constants.STATUS_ERROR_FAIL);
			responseBuilder.add("message",
					"Required parameter: field not passed");
			return responseBuilder.build();
		}
		Map<String, String> values = new HashMap<String, String>();

		for (String key : parameters.keySet()) {
			if (key.startsWith("data-")) {
				values.put(key.replace("data-", ""),
						getFirstMultiValuedMap(parameters, key));
			}
		}

		// if (predicate == null) {
		// responseBuilder.add("status", Constants.STATUS_ERROR_FAIL);
		// responseBuilder.add("message",
		// "Required parameter: predicate not passed");
		// return responseBuilder.build();
		// }

		Long newWhereId;
		try {
			Resource resource = rc.getResource(field.split("/")[0]);
			newWhereId = qc.addWhereClause(sqId, logicalOperator,
					getPath(field), predicate, values, whereId, resource);
			responseBuilder.add("status", Constants.STATUS_OK);
			responseBuilder.add("clauseId", newWhereId);
		} catch (ClauseNotFoundException | ClauseIsNotTheCorrectType
				| SubQueryNotFoundException | LogicalOperatorNotFound
				| PredicateTypeNotSupported e) {
			responseBuilder.add("status", Constants.STATUS_ERROR_FAIL);
			responseBuilder.add("message", e.getMessage());
		}

		return responseBuilder.build();
	}

	/**
	 * Deletes a given clause
	 * 
	 * @param clauseId Clause ID
	 * @return A JSON Object representing the status of that request
	 */
	@GET
	@Path("/deleteClause")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure deleteClause(
			@QueryParam(value = "clauseId") Long clauseId) {
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();

		try {
			qc.deleteClause(clauseId);
			responseBuilder.add("status", Constants.STATUS_OK);
		} catch (ClauseNotFoundException e) {
			responseBuilder.add("status", Constants.STATUS_ERROR_FAIL);
			responseBuilder.add("message", e.getMessage());
		}

		return responseBuilder.build();
	}

	/**
	 * Runs the query created by the rest service
	 * 
	 * @return A JSON Object representing the status of that request
	 */
	@GET
	@Path("/runQuery")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure runQuery() {
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
		try {
			responseBuilder.add("resultId", ec.runQuery(qc.getQuery()));
		} catch (PersistableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		admin.endConversation();
		return responseBuilder.build();
	}

	/**
	 * Cancels the given query
	 * 
	 * @return A JSON Object representing the status of that request
	 */
	@GET
	@Path("/cancelQuery")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure cancelQuery() {
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
		qc.cancelQuery();
		admin.endConversation();

		responseBuilder.add("status", Constants.STATUS_OK);
		return responseBuilder.build();
	}

	private String getFirstMultiValuedMap(
			MultivaluedMap<String, String> parameters, String parameter) {
		if (parameters.containsKey(parameter)) {
			return parameters.getFirst(parameter);
		}
		return null;
	}

	private edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Path getPath(
			String fullPath) {
		Resource resource = rc.getResource(fullPath.split("/")[0]);

		String newPath = fullPath.replaceAll(resource.getName() + "/", "");
		return pc.getPathFromString(resource, newPath);
	}
}
