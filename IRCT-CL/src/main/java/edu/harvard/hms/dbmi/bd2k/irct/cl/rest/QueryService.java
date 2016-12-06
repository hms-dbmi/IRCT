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
import javax.json.JsonValue.ValueType;
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
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.SelectOperationType;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.SortOperationType;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.SubQuery;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Field;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.LogicalOperator;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType;
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
	 * @param queryId
	 *            Query Id
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
	 * @param payload
	 *            JSON
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
				clauseId = addJsonWhereClauseToQuery(null, clauseObject);
			} else if (clauseObject.getString("type").equals("select")) {
				clauseId = addJsonSelectClauseToQuery(null, clauseObject);
			} else if (clauseObject.getString("type").equals("join")) {
				clauseId = addJsonJoinClauseToQuery(null, clauseObject);
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
	 * @param type
	 *            Type of clause
	 * @param info
	 *            URI information
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
		if ((dataType != null) && (field != null)) {
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

				clauseId = validateWhereClause(null, clauseId, resource, field,
						predicateName, logicalOperatorName, fields, null);

			} else if (type.equals("select")) {
				String alias = queryParameters.getFirst("alias");
				String operationName = queryParameters.getFirst("operation");

				Map<String, String> fields = new HashMap<String, String>();
				for (String key : queryParameters.keySet()) {
					if (key.startsWith("data-")) {
						fields.put(key.substring(5),
								queryParameters.getFirst(key));
					}
				}

				clauseId = validateSelectClause(null, clauseId, resource,
						field, alias, operationName, fields, null);

			} else if (type.equals("join")) {
				String joinType = queryParameters.getFirst("joinType");

				Map<String, String> fields = new HashMap<String, String>();
				for (String key : queryParameters.keySet()) {
					if (key.startsWith("data-")) {
						fields.put(key.substring(5),
								queryParameters.getFirst(key));
					}
				}

				clauseId = validateJoinClause(null, clauseId, resource, field,
						joinType, fields, null);
			} else if (type.equals("sort")) {
				String sortType = queryParameters.getFirst("sortType");
				Map<String, String> fields = new HashMap<String, String>();
				for (String key : queryParameters.keySet()) {
					if (key.startsWith("data-")) {
						fields.put(key.substring(5),
								queryParameters.getFirst(key));
					}
				}

				clauseId = validateSortClause(null, clauseId, resource, field,
						sortType, fields, null);
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
	 * Saves a query
	 * 
	 * @param payload
	 *            JSON
	 * @return Query Id
	 */
	@POST
	@Path("/saveQuery")
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveQuery(String payload) {
		JsonObjectBuilder response = Json.createObjectBuilder();

		JsonReader jsonReader = Json.createReader(new StringReader(payload));
		JsonObject jsonQuery = jsonReader.readObject();
		jsonReader.close();

		// Create the query
		try {
			convertJsonToQuery(jsonQuery);
		} catch (QueryException e) {
			response.add("status", "Invalid Request");
			response.add("message", e.getMessage());
			return Response.status(400).entity(response.build()).build();
		}

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
	 * Runs a query using a JSON representation of the Query
	 * 
	 * @param payload
	 *            JSON
	 * @return Result Id
	 */
	@POST
	@Path("/runQuery")
	@Produces(MediaType.APPLICATION_JSON)
	public Response runQuery(String payload) {
		JsonObjectBuilder response = Json.createObjectBuilder();

		JsonReader jsonReader = Json.createReader(new StringReader(payload));
		JsonObject jsonQuery = jsonReader.readObject();
		jsonReader.close();
		Query query = null;
		try {
			query = convertJsonToQuery(jsonQuery);
		} catch (QueryException e) {
			response.add("status", "Invalid Request");
			response.add("message", e.getMessage());
			return Response.status(400).entity(response.build()).build();
		}

		try {
			response.add("resultId", ec.runQuery(query,
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

	private Query convertJsonToQuery(JsonObject jsonQuery)
			throws QueryException {
		// Create the query
		qc.createQuery();
		return convertJsonToQuery(null, jsonQuery);
	}

	private Query convertJsonToQuery(SubQuery subQuery, JsonObject jsonQuery)
			throws QueryException {

		// Convert JSON Selects to Query
		if (jsonQuery.containsKey("select")) {
			JsonArray selectClauses = jsonQuery.getJsonArray("select");
			Iterator<JsonValue> selectIterator = selectClauses.iterator();
			while (selectIterator.hasNext()) {
				addJsonSelectClauseToQuery(subQuery,
						(JsonObject) selectIterator.next());
			}

		}
		// Convert JSON Where to Query
		if (jsonQuery.containsKey("where")) {
			JsonArray whereClauses = jsonQuery.getJsonArray("where");
			Iterator<JsonValue> whereIterator = whereClauses.iterator();
			while (whereIterator.hasNext()) {
				addJsonWhereClauseToQuery(subQuery,
						(JsonObject) whereIterator.next());
			}
		}
		// Convert JSON Join to Query
		if (jsonQuery.containsKey("join")) {
			JsonArray joinClauses = jsonQuery.getJsonArray("join");
			Iterator<JsonValue> joinIterator = joinClauses.iterator();
			while (joinIterator.hasNext()) {
				addJsonJoinClauseToQuery(subQuery,
						(JsonObject) joinIterator.next());
			}
		}
		// Convert JSON Sort to Query
		if (jsonQuery.containsKey("sort")) {
			JsonArray sortClauses = jsonQuery.getJsonArray("sort");
			Iterator<JsonValue> sortIterator = sortClauses.iterator();
			while (sortIterator.hasNext()) {
				addJsonSortClauseToQuery(subQuery,
						(JsonObject) sortIterator.next());
			}
		}
		// Convert JSON SubQueries to Query
		if (jsonQuery.containsKey("subquery")) {
			JsonObject subQueryObject = jsonQuery.getJsonObject("subquery");
			for (String key : subQueryObject.keySet()) {
				addJsonSubQueryToQuery(subQuery, key, subQueryObject.getJsonObject(key));
			}
		}

		if (subQuery != null) {
			return subQuery;
		} else {
			return qc.getQuery();
		}
	}

	private Long validateJoinClause(SubQuery sq, Long clauseId,
			Resource resource, Entity field, String joinName,
			Map<String, String> fields, Map<String, Object> objectFields)
			throws QueryException {
		JoinType joinType = resource.getSupportedJoinByName(joinName);
		if (joinType == null) {
			throw new QueryException("Unknown join type");
		}
		return qc.addJoinClause(sq, clauseId, resource, field, joinType,
				fields, objectFields);
	}

	private Long validateSelectClause(SubQuery sq, Long clauseId,
			Resource resource, Entity field, String alias,
			String operationName, Map<String, String> fields,
			Map<String, Object> objectFields) throws QueryException {

		SelectOperationType operation = null;
		if (operationName != null) {
			operation = resource
					.getSupportedSelectOperationByName(operationName);
			if (operation == null) {
				throw new QueryException("Unknown select operation");
			}
		}

		return qc.addSelectClause(sq, clauseId, resource, field, alias,
				operation, fields, objectFields);
	}

	private Long addJsonWhereClauseToQuery(SubQuery sq, JsonObject whereClause)
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

		Entity entity = null;
		Resource resource = null;
		if (path != null && !path.isEmpty()) {
			path = "/" + path;
			path = path.substring(1);
			resource = rc.getResource(path.split("/")[1]);
			entity = new Entity(path);
			if (dataType != null) {
				entity.setDataType(resource.getDataTypeByName(dataType));
			}
		}
		if ((resource == null) || (entity == null)) {
			throw new QueryException("Invalid Path");
		}
		String predicateName = whereClause.getString("predicate");
		String logicalOperatorName = null;
		if (whereClause.containsKey("logicalOperator")) {
			logicalOperatorName = whereClause.getString("logicalOperator");
		}

		PredicateType predicateType = resource
				.getSupportedPredicateByName(predicateName);
		if (predicateType == null) {
			throw new QueryException("Unknown predicate type");
		}

		Map<String, Field> clauseFields = new HashMap<String, Field>();
		for (Field field : predicateType.getFields()) {
			clauseFields.put(field.getPath(), field);
		}

		Map<String, Object> objectFields = new HashMap<String, Object>();
		Map<String, String> fields = new HashMap<String, String>();

		if (whereClause.containsKey("fields")) {
			JsonObject fieldObject = whereClause.getJsonObject("fields");
			objectFields = getObjectFields(clauseFields, fieldObject);
			fields = getStringFields(clauseFields, fieldObject);
		}

		return validateWhereClause(sq, clauseId, resource, entity,
				predicateName, logicalOperatorName, fields, objectFields);
	}

	private Long validateWhereClause(SubQuery sq, Long clauseId,
			Resource resource, Entity field, String predicateName,
			String logicalOperatorName, Map<String, String> fields,
			Map<String, Object> objectFields) throws QueryException {

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

		return qc.addWhereClause(sq, clauseId, resource, field, predicateType,
				logicalOperator, fields, objectFields);
	}

	private Long addJsonSelectClauseToQuery(SubQuery sq, JsonObject selectClause)
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

		Entity entity = null;
		Resource resource = null;
		if (path != null && !path.isEmpty()) {
			path = "/" + path;
			path = path.substring(1);
			resource = rc.getResource(path.split("/")[1]);
			if (resource == null) {
				throw new QueryException("Invalid Resource");
			}
			entity = new Entity(path);
			if (dataType != null) {
				entity.setDataType(resource.getDataTypeByName(dataType));
			}
		}

		String alias = null;
		if (selectClause.containsKey("alias")) {
			alias = selectClause.getString("alias");
		}

		String operationName = null;

		Map<String, Object> objectFields = new HashMap<String, Object>();
		Map<String, String> fields = new HashMap<String, String>();
		if (selectClause.containsKey("operation")) {
			operationName = selectClause.getString("operation");

			SelectOperationType st = resource
					.getSupportedSelectOperationByName(operationName);

			if (st == null) {
				throw new QueryException("Unsupported Select Operation Type");
			}

			Map<String, Field> clauseFields = new HashMap<String, Field>();
			for (Field field : st.getFields()) {
				clauseFields.put(field.getPath(), field);
			}

			if (selectClause.containsKey("fields")) {
				JsonObject fieldObject = selectClause.getJsonObject("fields");
				objectFields = getObjectFields(clauseFields, fieldObject);
				fields = getStringFields(clauseFields, fieldObject);
			}
		}
		return validateSelectClause(sq, clauseId, resource, entity, alias,
				operationName, fields, objectFields);
	}

	private Long addJsonJoinClauseToQuery(SubQuery sq, JsonObject joinClause)
			throws QueryException {
		String path = null;

		if (joinClause.containsKey("field")) {
			path = joinClause.getJsonObject("field").getString("pui");
		}

		Long clauseId = null;
		if (joinClause.containsKey("clauseId")) {
			clauseId = joinClause.getJsonNumber("clauseId").longValue();
		}

		Entity entity = null;
		Resource resource = null;
		if (path != null && !path.isEmpty()) {
			path = "/" + path;
			path = path.substring(1);
			resource = rc.getResource(path.split("/")[1]);
			if (resource == null) {
				throw new QueryException("Invalid Resource");
			}
			entity = new Entity(path);
		}
		if ((resource == null) || (entity == null)) {
			throw new QueryException("Invalid Path");
		}

		String joinName = joinClause.getString("joinType");

		JoinType jt = resource.getSupportedJoinByName(joinName);

		if (jt == null) {
			throw new QueryException("Unsupported Join Type");
		}

		Map<String, Field> clauseFields = new HashMap<String, Field>();
		for (Field field : jt.getFields()) {
			clauseFields.put(field.getPath(), field);
		}

		Map<String, Object> objectFields = new HashMap<String, Object>();
		Map<String, String> fields = new HashMap<String, String>();

		if (joinClause.containsKey("fields")) {
			JsonObject fieldObject = joinClause.getJsonObject("fields");
			objectFields = getObjectFields(clauseFields, fieldObject);
			fields = getStringFields(clauseFields, fieldObject);
		}

		return validateJoinClause(sq, clauseId, resource, entity, joinName,
				fields, objectFields);
	}

	private Map<String, Object> getObjectFields(
			Map<String, Field> clauseFields, JsonObject fieldObject)
			throws QueryException {
		Map<String, Object> objectFields = new HashMap<String, Object>();
		for (String key : fieldObject.keySet()) {
			ValueType vt = fieldObject.get(key).getValueType();

			if ((vt == ValueType.ARRAY)) {
				if (clauseFields.containsKey(key)
						&& (clauseFields.get(key).getDataTypes()
								.contains(PrimitiveDataType.ARRAY))) {

					JsonArray array = fieldObject.getJsonArray(key);
					String[] stringArray = new String[array.size()];
					for (int sa_i = 0; sa_i < array.size(); sa_i++) {
						stringArray[sa_i] = array.getString(sa_i);
					}
					objectFields.put(key, stringArray);
				} else {
					throw new QueryException(key
							+ " field does not support arrays.");
				}

			} else if (vt == ValueType.OBJECT) {
				if (clauseFields.containsKey(key)
						&& (clauseFields.get(key).getDataTypes()
								.contains(PrimitiveDataType.SUBQUERY))) {

					objectFields.put(
							key,
							convertJsonToQuery(qc.createSubQuery(),
									fieldObject.getJsonObject(key)));

				} else {
					throw new QueryException(key
							+ " field does not support subqueries.");
				}
			}
		}

		return objectFields;
	}

	private Map<String, String> getStringFields(
			Map<String, Field> clauseFields, JsonObject fieldObject) {
		Map<String, String> fields = new HashMap<String, String>();
		for (String key : fieldObject.keySet()) {
			ValueType vt = fieldObject.get(key).getValueType();
			if ((vt != ValueType.ARRAY) && (vt != ValueType.OBJECT)) {
				fields.put(key, fieldObject.getString(key));
			}
		}

		return fields;
	}

	private Long addJsonSortClauseToQuery(SubQuery sq, JsonObject sortClause)
			throws QueryException {
		String path = null;
		if (sortClause.containsKey("field")) {
			path = sortClause.getJsonObject("field").getString("pui");
		}

		Long clauseId = null;
		if (sortClause.containsKey("clauseId")) {
			clauseId = sortClause.getJsonNumber("clauseId").longValue();
		}

		Entity entity = null;
		Resource resource = null;
		if (path != null && !path.isEmpty()) {
			path = "/" + path;
			path = path.substring(1);
			resource = rc.getResource(path.split("/")[1]);
			if (resource == null) {
				throw new QueryException("Invalid Resource");
			}
			entity = new Entity(path);
		}
		if ((resource == null) || (entity == null)) {
			throw new QueryException("Invalid Path");
		}

		if(!sortClause.containsKey("sortType")) {
			throw new QueryException("No sort type defined");
		}
		String sortName = sortClause.getString("sortType");

		SortOperationType sortType = resource
				.getSupportedSortOperationByName(sortName);
		if (sortType == null) {
			throw new QueryException("Unknown sort type");
		}
		
		Map<String, Field> clauseFields = new HashMap<String, Field>();
		for (Field field : sortType.getFields()) {
			clauseFields.put(field.getPath(), field);
		}

		Map<String, Object> objectFields = new HashMap<String, Object>();
		Map<String, String> fields = new HashMap<String, String>();

		if (sortClause.containsKey("fields")) {
			JsonObject fieldObject = sortClause.getJsonObject("fields");
			objectFields = getObjectFields(clauseFields, fieldObject);
			fields = getStringFields(clauseFields, fieldObject);
		}

		return validateSortClause(sq, clauseId, resource, entity, sortName,
				fields, objectFields);
	}
	
	private Long validateSortClause(SubQuery sq, Long clauseId,
			Resource resource, Entity field, String sortName,
			Map<String, String> fields, Map<String, Object> objectFields) throws QueryException {
		SortOperationType sortType = resource
				.getSupportedSortOperationByName(sortName);
		if (sortType == null) {
			throw new QueryException("Unknown sort type");
		}
		return qc
				.addSortClause(sq, clauseId, resource, field, sortType, fields, objectFields);
	}

	private void addJsonSubQueryToQuery(Query query, String subQueryId, JsonObject subQuery)
			throws QueryException {
		SubQuery sq = (SubQuery) convertJsonToQuery(qc.createSubQuery(),
				subQuery);
		if(query == null) {
			qc.addSubQuery(subQueryId, sq);	
		} else {
			query.addSubQuery(subQueryId, sq);
		}
	}
}
