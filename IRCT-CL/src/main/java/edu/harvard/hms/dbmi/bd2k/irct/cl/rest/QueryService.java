/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.cl.rest;

import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import edu.harvard.hms.dbmi.bd2k.irct.cl.util.AdminBean;
import edu.harvard.hms.dbmi.bd2k.irct.controller.ExecutionController;
import edu.harvard.hms.dbmi.bd2k.irct.controller.QueryController;
import edu.harvard.hms.dbmi.bd2k.irct.controller.ResourceController;
import edu.harvard.hms.dbmi.bd2k.irct.exception.QueryException;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Entity;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.JoinType;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.PredicateType;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.LogicalOperator;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

/**
 * Creates a REST interface for the query service
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Path("/queryService")
@ConversationScoped
@Named
public class QueryService implements Serializable {
	private static final long serialVersionUID = -3951500710489406681L;

	@Inject
	private QueryController qc;

	@Inject
	private ResourceController rc;

	@Inject
	private AdminBean admin;

	@Inject
	private ExecutionController ec;

	@Inject
	private HttpSession session;

	/**
	 * Starts the creation of a query
	 * 
	 * @return Conversation id
	 */
	@GET
	@Path("/startQuery")
	@Produces(MediaType.APPLICATION_JSON)
	public Response startQuery() {
		JsonObjectBuilder response = Json.createObjectBuilder();
		String conversationId = admin.startConversation();

		qc.createQuery();

		response.add("cid", conversationId);
		return Response.ok(response.build(), MediaType.APPLICATION_JSON)
				.build();
	}

	/**
	 * Saves a query
	 * 
	 * @return Query Id
	 */
	@GET
	@Path("/saveQuery")
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveQuery() {
		JsonObjectBuilder response = Json.createObjectBuilder();
		try {
			qc.saveQuery();
		} catch (QueryException e) {
			response.add("status", "Invalid Request");
			response.add("message", e.getMessage());
			return Response.status(400).entity(response.build()).build();
		}

		response.add("queryId", qc.getQuery().getId());
		return Response.ok(response.build(), MediaType.APPLICATION_JSON)
				.build();
	}

	/**
	 * Loads a saved Query
	 * 
	 * @param queryId Query Id
	 * @return Conversation Id
	 */
	@GET
	@Path("/loadQuery")
	@Produces(MediaType.APPLICATION_JSON)
	public Response loadQuery(@QueryParam(value = "queryId") Long queryId) {
		JsonObjectBuilder response = Json.createObjectBuilder();
		String conversationId = admin.startConversation();

		if (queryId == null) {
			response.add("status", "Invalid Request");
			response.add("message", "queryId is not set");
			return Response.status(400).entity(response.build()).build();
		}

		try {
			qc.loadQuery(queryId);
		} catch (QueryException e) {
			response.add("status", "Invalid Request");
			response.add("message", e.getMessage());
			return Response.status(400).entity(response.build()).build();
		}

		response.add("cid", conversationId);
		return Response.ok(response.build(), MediaType.APPLICATION_JSON)
				.build();
	}

	/**
	 * Adds a clause through a JSON representation
	 * 
	 * @param payload JSON
	 * @return Clause Id
	 */
	@POST
	@Path("/clause")
	@Produces(MediaType.APPLICATION_JSON)
	public Response clause(String payload) {
		JsonObjectBuilder response = Json.createObjectBuilder();

		JsonReader jsonReader = Json.createReader(new StringReader(payload));
		JsonObject clauseObject = jsonReader.readObject();
		jsonReader.close();

		Long clauseId = null;
		if (!clauseObject.containsKey("type")) {
			response.add("status", "Invalid Request");
			response.add("message", "The clause type is unknown");
			return Response.status(400).entity(response.build()).build();
		}

		try {
			if (clauseObject.getString("type").equals("where")) {
				clauseId = addJsonWhereClauseToQuery(clauseObject);
			} else if (clauseObject.getString("type").equals("select")) {
				clauseId = addJsonSelectClauseToQuery(clauseObject);
			} else if (clauseObject.getString("type").equals("join")) {
				clauseId = addJsonJoinClauseToQuery(clauseObject);
			} else {
				response.add("status", "Invalid Request");
				response.add("message", "The clause type is unknown");
				return Response.status(400).entity(response.build()).build();
			}
		} catch (QueryException e) {
			response.add("status", "Invalid Request");
			response.add("message", e.getMessage());
			return Response.status(400).entity(response.build()).build();
		}

		response.add("clauseId", clauseId);
		return Response.ok(response.build(), MediaType.APPLICATION_JSON)
				.build();
	}

	/**
	 * Adds a clause through URI information
	 * 
	 * @param type Type of clause
	 * @param info URI information
	 * @return Clause Id
	 */
	@GET
	@Path("/clause")
	@Produces(MediaType.APPLICATION_JSON)
	public Response clause(@QueryParam(value = "type") String type,
			@Context UriInfo info) {
		JsonObjectBuilder response = Json.createObjectBuilder();

		MultivaluedMap<String, String> queryParameters = info
				.getQueryParameters();

		String path = queryParameters.getFirst("field");
		Entity field = null;
		Resource resource = null;
		if (path != null && !path.isEmpty()) {
			path = "/" + path;
			path = path.substring(1);
			resource = rc.getResource(path.split("/")[1]);
			field = new Entity(path);
		}
		String dataType = queryParameters.getFirst("dataType"); 
		if((dataType != null) && (field != null)) {
			field.setDataType(resource.getDataTypeByName(dataType));
		}
		
		if ((resource == null) || (field == null)) {
			response.add("status", "Invalid Request");
			response.add("message", "Invalid Path");
			return Response.status(400).entity(response.build()).build();
		}

		Long clauseId = null;
		if (queryParameters.containsKey("clauseId")) {
			clauseId = Long.parseLong(queryParameters.getFirst("clauseId")
					.trim());
		}
		try {
			if (type.equals("where")) {
				String predicateName = queryParameters.getFirst("predicate");

				String logicalOperatorName = queryParameters
						.getFirst("logicalOperator");

				Map<String, String> fields = new HashMap<String, String>();
				for (String key : queryParameters.keySet()) {
					if (key.startsWith("data-")) {
						fields.put(key.substring(5),
								queryParameters.getFirst(key));
					}
				}

				clauseId = validateWhereClause(clauseId, resource, field,
						predicateName, logicalOperatorName, fields);

			} else if (type.equals("select")) {
				String alias = queryParameters.getFirst("alias");
				clauseId = validateSelectClause(clauseId, resource, field, alias);
				
			} else if (type.equals("join")) {
				String joinType = queryParameters.getFirst("joinType");
				
				Map<String, String> fields = new HashMap<String, String>();
				for (String key : queryParameters.keySet()) {
					if (key.startsWith("data-")) {
						fields.put(key.substring(5),
								queryParameters.getFirst(key));
					}
				}
				
				clauseId = validateJoinClause(clauseId, resource, joinType, fields);
			} else {
				throw new QueryException("No type set");
			}
		} catch (QueryException e) {
			response.add("status", "Invalid Request");
			response.add("message", e.getMessage());
			return Response.status(400).entity(response.build()).build();
		}
		response.add("clauseId", clauseId);
		return Response.ok(response.build(), MediaType.APPLICATION_JSON)
				.build();
	}

	/**
	 * Runs a query using a JSON representation of the Query
	 * 
	 * @param payload JSON
	 * @return Result Id
	 */
	@POST
	@Path("/runQuery")
	@Produces(MediaType.APPLICATION_JSON)
	public Response runQuery(String payload) {
		JsonObjectBuilder response = Json.createObjectBuilder();

		JsonReader jsonReader = Json.createReader(new StringReader(payload));
		JsonObject query = jsonReader.readObject();
		jsonReader.close();

		// Create the query
		qc.createQuery();

		try {
			// Convert JSON Selects to Query
			if (query.containsKey("select")) {
				JsonArray selectClauses = query.getJsonArray("select");
				Iterator<JsonValue> selectIterator = selectClauses.iterator();
				while (selectIterator.hasNext()) {
					addJsonSelectClauseToQuery((JsonObject) selectIterator
							.next());
				}

			}
			// Convert JSON Where to Query
			if (query.containsKey("where")) {
				JsonArray whereClauses = query.getJsonArray("where");
				Iterator<JsonValue> whereIterator = whereClauses.iterator();
				while (whereIterator.hasNext()) {
					addJsonWhereClauseToQuery((JsonObject) whereIterator.next());
				}
			}
			// Convert JSON Join to Query
			if (query.containsKey("join")) {
				JsonArray joinClauses = query.getJsonArray("join");
				Iterator<JsonValue> joinIterator = joinClauses.iterator();
				while (joinIterator.hasNext()) {
					addJsonJoinClauseToQuery((JsonObject) joinIterator.next());
				}
			}
		} catch (QueryException e) {
			response.add("status", "Invalid Request");
			response.add("message", e.getMessage());
			return Response.status(400).entity(response.build()).build();
		}

		try {
			response.add("resultId", ec.runQuery(qc.getQuery(),
					(SecureSession) session.getAttribute("secureSession")));
		} catch (PersistableException e) {
			response.add("status", "Error running request");
			response.add("message", "An error occurred running this request");
			return Response.status(400).entity(response.build()).build();
		}

		return Response.ok(response.build(), MediaType.APPLICATION_JSON)
				.build();
	}

	/**
	 * Runs a query
	 * 
	 * @return Result Id
	 */
	@GET
	@Path("/runQuery")
	@Produces(MediaType.APPLICATION_JSON)
	public Response runQuery() {
		JsonObjectBuilder response = Json.createObjectBuilder();
		try {
			response.add("resultId", ec.runQuery(qc.getQuery(),
					(SecureSession) session.getAttribute("secureSession")));
		} catch (PersistableException e) {
			response.add("status", "Error running request");
			response.add("message", "An error occurred running this request");
			return Response.status(400).entity(response.build()).build();
		}
		admin.endConversation();
		return Response.ok(response.build(), MediaType.APPLICATION_JSON)
				.build();
	}

	private Long validateWhereClause(Long clauseId, Resource resource,
			Entity field, String predicateName, String logicalOperatorName,
			Map<String, String> fields) throws QueryException {

		PredicateType predicateType = resource
				.getSupportedPredicateByName(predicateName);
		if (predicateType == null) {
			throw new QueryException("Unknown predicate type");
		}

		LogicalOperator logicalOperator = null;
		if (logicalOperatorName != null) {
			logicalOperator = resource
					.getLogicalOperatorByName(logicalOperatorName);
			if (logicalOperator == null) {
				throw new QueryException("Unknown logical operator");
			}
		}

		return qc.addWhereClause(clauseId, resource, field, predicateType,
				logicalOperator, fields);
	}
	
	private Long validateJoinClause(Long clauseId, Resource resource, String joinName,
			Map<String, String> fields) throws QueryException {
		JoinType joinType = resource.getSupportedJoinByName(joinName);
		if(joinType == null) {
			throw new QueryException("Unknown join type");
		}
		return qc.addJoinClause(clauseId, resource, joinType, fields);
	}

	private Long validateSelectClause(Long clauseId, Resource resource,
			Entity field, String alias) throws QueryException {
		
		return qc.addSelectClause(clauseId, resource, field, alias);
	}

	private Long addJsonWhereClauseToQuery(JsonObject whereClause)
			throws QueryException {
		String path = null;
		String dataType = null;
		if (whereClause.containsKey("field")) {
			path = whereClause.getJsonObject("field").getString("pui");
			if (whereClause.getJsonObject("field").containsKey("dataType")) {
				dataType = whereClause.getJsonObject("field").getString(
						"dataType");
			}
		}

		Long clauseId = null;
		if (whereClause.containsKey("clauseId")) {
			clauseId = whereClause.getJsonNumber("clauseId").longValue();
		}

		Entity field = null;
		Resource resource = null;
		if (path != null && !path.isEmpty()) {
			path = "/" + path;
			path = path.substring(1);
			resource = rc.getResource(path.split("/")[1]);
			field = new Entity(path);
			if (dataType != null) {
				field.setDataType(resource.getDataTypeByName(dataType));
			}
		}
		if ((resource == null) || (field == null)) {
			throw new QueryException("Invalid Path");
		}
		String predicateName = whereClause.getString("predicate");
		String logicalOperatorName = null;
		if (whereClause.containsKey("logicalOperator")) {
			logicalOperatorName = whereClause.getString("logicalOperator");
		}
		Map<String, String> fields = new HashMap<String, String>();
		if (whereClause.containsKey("fields")) {
			JsonObject fieldObject = whereClause.getJsonObject("fields");
			for (String key : fieldObject.keySet()) {
				fields.put(key, fieldObject.getString(key));
			}
		}

		return validateWhereClause(clauseId, resource, field, predicateName,
				logicalOperatorName, fields);
	}

	private Long addJsonSelectClauseToQuery(JsonObject selectClause)
			throws QueryException {
		String path = null;
		String dataType = null;
		if (selectClause.containsKey("field")) {
			path = selectClause.getJsonObject("field").getString("pui");
			if (selectClause.getJsonObject("field").containsKey("dataType")) {
				dataType = selectClause.getJsonObject("field").getString(
						"dataType");
			}
		}

		Long clauseId = null;
		if (selectClause.containsKey("clauseId")) {
			clauseId = selectClause.getJsonNumber("clauseId").longValue();
		}

		Entity field = null;
		Resource resource = null;
		if (path != null && !path.isEmpty()) {
			path = "/" + path;
			path = path.substring(1);
			resource = rc.getResource(path.split("/")[1]);
			field = new Entity(path);
			if (dataType != null) {
				field.setDataType(resource.getDataTypeByName(dataType));
			}
		}
		if ((resource == null) || (field == null)) {
			throw new QueryException("Invalid Path");
		}
		String alias = null;
		if (selectClause.containsKey("alias")) {
			alias = selectClause.getString("alias");
		}
		return qc.addSelectClause(clauseId, resource, field, alias);
	}

	private Long addJsonJoinClauseToQuery(JsonObject joinObject)
			throws QueryException {
		return null;
	}
}
