package edu.harvard.hms.dbmi.bd2k.irct.cl.rest;

import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonStructure;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.auth0.jwt.interfaces.Claim;

import edu.harvard.hms.dbmi.bd2k.irct.IRCTApplication;
import edu.harvard.hms.dbmi.bd2k.irct.cl.util.Utilities;
import edu.harvard.hms.dbmi.bd2k.irct.controller.ExecutionController;
import edu.harvard.hms.dbmi.bd2k.irct.controller.QueryController;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;

/**
 * Creates a REST interface.
 */
@Path("/")
@RequestScoped
public class Endpoints {

	@Inject
	private IRCTApplication picsure;

	@Inject
	private HttpSession session;

	@Inject
	private QueryController queryService;

	@Inject
	private ExecutionController executionService;

	private Logger logger = Logger.getLogger(this.getClass());

	/**
	 * Creates a new query, initiates it, and returns the query id and some
	 * preliminary information about the query
	 *
	 * @param payload Some preliminary query information about name, user, status
	 * @return QueryResponse
	 */
	@POST
	@Path("/query")
	@Produces(MediaType.APPLICATION_JSON)
	public Response runQuery(String payload) {
		User currentUser = (User) session.getAttribute("user");

		logger.info("POST /query Starting new query for user:" + currentUser.getName());
		String rspStatus = "unknown", rspMessage = "N/A";

		JsonObjectBuilder resp = Json.createObjectBuilder();
		Query query = null;
		try {
			// Convert the payload into an internal representation of a query
			// After the query is parsed and persisted, get the queryObject
			query = queryService.createQuery(payload);
			query.setName(currentUser.getName() + "-QUERY-" + query.getId());

			logger.debug("POST /query Query created. now onto executing it");
			Result result = executionService.runQuery(query, currentUser);

			/*
			 * Query query = new Query(payload); Result result =
			 * query.runAsUser(user).getResult();
			 */

			rspStatus = "ok";
			rspMessage = "Query id:" + query.getId() + " named `" + query.getName() + "` is now "
					+ result.getResultStatus();

			resp.add("datasource", ((Resource) query.getResources().toArray()[0]).getName());

			resp.add("query", Json.createObjectBuilder().add("id", query.getId()).add("name", query.getName()));

			resp.add("result",
					Json.createObjectBuilder().add("id", result.getId()).add("JobType", result.getJobType())
							.add("ResultSetLocation",
									(result.getResultSetLocation() == null ? "NULL" : result.getResultSetLocation()))
							.add("ResourceActionId",
									(result.getResourceActionId() == null ? "NULL" : result.getResourceActionId()))
							.add("StartTime", result.getStartTime().toString())
							.add("EndTime", (result.getEndTime() == null ? "NULL"
									: result.getEndTime().toString()))
							.add("ResultStatus", result.getResultStatus().toString()));

		} catch (Exception e) {
			logger.error("/query Exception:" + e.getMessage());

			rspStatus = "error";
			rspMessage = e.getMessage();
		}

		// Build response object

		resp.add("status", rspStatus);
		resp.add("message", (rspMessage == null ? "NULL" : rspMessage));

		logger.info("POST /query Finished.");
		return Response.ok(resp.build(), MediaType.APPLICATION_JSON).build();
	}


	/**
	 * Runs a query using a JSON representation of the QueryEndpoint, this is
	 * a copy of the old /queryService/runQuery endpoint.
	 *
	 * @param payload JSON formatted string, that is validated by the API
	 * @return ResultRecord, a JSON structure, representing the current
	 *         status of the running or completed query.
	 */
	@POST
	@Path("/run")
	@Produces(MediaType.APPLICATION_JSON)
	public Response runQuery(String payload) {
		logger.debug("POST /runQuery Starting");

		JsonObjectBuilder response = Json.createObjectBuilder();
		JsonObject jsonQuery = null;
		try {
			JsonReader jsonReader = Json.createReader(new StringReader(payload));
			jsonQuery = jsonReader.readObject();
			jsonReader.close();
		} catch (Exception e) {
			response.add("status", "error");
			response.add("message", "Error processing JSON."+(e.getMessage()==null?"":e.getMessage()));
			return Response.status(400).entity(response.build()).build();
		}

		try {
			Query query = convertJsonToQuery(jsonQuery);

			Result r = ec.runQuery(query, (User) session.getAttribute("user"));
			if (r==null){
				logger.error("POST /runQuery `Result` object could not be generated.");
				response.add("status", "error");
				response.add("message", "`Result` could not be generated");
			} else {
				r.setQuery(query);
				response.add("resultId", r.getId());
				// Add a separate section for query
				response.add("query",
						Json.createObjectBuilder()
						.add("id", query.getId())
						.add("name", query.getName())
						.build()
					);

				// Add a separate section for Result
				response.add("result",
						Json.createObjectBuilder()
						.add("id", r.getId())
						.add("type", r.getJobType())
						.add("status", r.getResultStatus().toString())
						.add("startTime", r.getStartTime().toString())
						.add("endTime", r.getEndTime().toString())
						.build()
					);
			}
		} catch (QueryException e) {
			logger.error("POST /runQuery QueryException:"+e.getMessage());

			response.add("status", "error");
			response.add("message", "Invalid query."+e.getMessage());
			return Response.status(400).entity(response.build()).build();
		} catch (Exception e) {
			logger.error("POST /runQuery Exception:"+e.getMessage());

			response.add("status", "error");
			response.add("message", (e.getMessage()==null?"Error running query":e.getMessage()));
			return Response.status(400).entity(response.build()).build();
		}

		logger.debug("POST /runQuery Finished");
		return Response.ok(response.build(), MediaType.APPLICATION_JSON)
				.build();
	}

	@GET
	@Path("/resources{resourceName: .*}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResources(@PathParam("resourceName") String resourceName) {

		JsonObjectBuilder resp = Json.createObjectBuilder();
		JsonArrayBuilder resourceList = Json.createArrayBuilder();
		String respStatus = "unknown";
		String respMessage = null;

		try {
			if (resourceName == null) {
				Map<String, Resource> resources = picsure.getResources();
				for (String resourceKey : resources.keySet()) {
					Resource resource = resources.get(resourceKey);
					resourceList.add(resourceObjectToJsonObject(resource));
				}
			} else {
				Map<String, Resource> resources = picsure.getResources();
				Resource resource = resources.get(resourceName);
				resp.add(resourceName,
						Json.createObjectBuilder().add("id", resource.getId()).add("name", resource.getName()).build());
				resourceList.add(resourceObjectToJsonObject(resource));
			}
			respStatus = "ok";

		} catch (Exception e) {
			respStatus = "error";
			respMessage = e.getMessage();
		} finally {
			resp.add("status", respStatus);
			if (respMessage != null) { resp.add("message", respMessage); }
			resp.add("resources", resourceList);
		}

		return Response.ok(resp.build(), MediaType.APPLICATION_JSON).build();
	}

	/**
	 * Returns the details of a specific query created previously
	 *
	 * @param queryId
	 * @return queryDetails
	 */
	@GET
	@Path("/query/{queryId : .*}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getQuery(@PathParam("queryId") Long queryId) {
		JsonObjectBuilder resp = Json.createObjectBuilder();

		String rspStatus = "unknown", rspMessage = "N/A";

		logger.info(
				"GET /query Starting queryId:" + queryId + " user:" + ((User) session.getAttribute("user")).getName());
		edu.harvard.hms.dbmi.bd2k.irct.model.query.Query query = null;
		try {
			queryService.loadQuery(queryId);
			query = queryService.getQuery();

			rspStatus = "ok";
			rspMessage = "";

			resp.add("query",
					Json.createObjectBuilder().add("id", query.getId())
							.add("name", (query.getName() == null ? "NULL" : query.getName()))
							.add("resources", resourceObjectToJsonObject((Resource) query.getResources().toArray()[0])));

		} catch (Exception e) {
			rspStatus = "error";
			rspMessage = e.getMessage();
		}

		resp.add("status", rspStatus);
		resp.add("message", rspMessage);

		return Response.ok(resp.build(), MediaType.APPLICATION_JSON).build();
	}

	private JsonObject resourceObjectToJsonObject(Resource resource) {
		if (resource == null) {
			return Json.createObjectBuilder().add("id", "N/A")
					.add("name", "N/A").build();
		} else {
			return Json.createObjectBuilder().add("id", resource.getId())
			.add("name", resource.getName()).build();
		}
	}

	/**
	 * This endpoint only returns the status/details of the currentUser
	 * query. No queryId is required, since it uses the current query from
	 * the qc.getQuery() call. This is a previously used "GET /queryService/runQuery" call.
	 *
	 * @return A list of QueryRecord objects in JSON format
	 */
	@HEAD
	@Path("/queries")
	@Produces(MediaType.APPLICATION_JSON)
	public Response runQuery() {
		JsonObjectBuilder resp = Json.createObjectBuilder();
		List<String> messages = new List<String>;

		try {
			response.add("status", "ok");

			List<Query> queryList = qc.getAllQueries();
			if (queryList.size() == 0) {
				response.add("message", "There are no queries to list.");
			} else {
				foreach(Query q: qc.getAllQueries())
					if (r != null) {
						//Result r = ec.runQuery(qc.getQuery(), (User) session.getAttribute("user"));
						resp.add("result", Json.createObjectBuilder()
							.add("id", r.getId())
							.add("jobType", r.getJobType())
							.add("message", r.getMessage())
							.add("startTime", r.getStartTime().toString())
							.add("endTime", r.getEndTime().toString())
							.build()
							);
					} else {
						resp.add("message", "No result is associated with this query.");
					}
				}
				resp.add("message", "Listed "+queryList.size()+" queries.");
			}

		} catch (Exception e) {
			resp.add("status", "error");
			resp.add("message", e.getMessage());
		}
		return Response.ok(response.build(), MediaType.APPLICATION_JSON)
				.build();
	}


	/**
	 * Returns a JSON Array of application settings
	 *
	 * @return JSON Array of settings (key/value pair)
	 */
	@GET
	@Path("/about")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure about() {
		try {
			IRCTApplication app = new IRCTApplication();
			User user = (User) session.getAttribute("user");
			// Get claims out of the token
			Map<String, Claim> token_claims = Utilities.getClaims(user.getToken(), picsure.getClientSecret());
			JsonObjectBuilder claimsObject = Json.createObjectBuilder();
			for (String key : token_claims.keySet()) {
				Claim claim = token_claims.get(key);
				claimsObject.add(key,
						(String) (claim.asString() == null ? claim.asDate().toString() : claim.asString()));
			}

			return Json.createObjectBuilder().add("appVersion", app.getVersion()).add("userId", user.getUserId())
					.add("userName", user.getName()).add("userClaims", claimsObject.build()).build();

		} catch (Exception e) {
			return Json.createObjectBuilder().add("status", "error").add("message", e.getMessage()).build();
		}
	}

	// TODO: The below methods should be cleared out, or moved to a utility class
	//       since validating the query should NOT be happening in the QueryController
	//       class.



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
		logger.debug("validateSelectClause(SubQuery, ...) Starting");

		SelectOperationType operation = null;
		if (operationName != null) {
			operation = resource
					.getSupportedSelectOperationByName(operationName);
			if (operation == null) {
				throw new QueryException("Unknown select operation");
			}
		}
		logger.debug("validateSelectClause(SubQuery, ...) returning `QueryController` addSelectClause() call");
		return qc.addSelectClause(sq, clauseId, resource, field, alias,
				operation, fields, objectFields);
	}

	private Long addJsonWhereClauseToQuery(SubQuery sq, JsonObject whereClause)
			throws QueryException {
		logger.debug("addJsonWhereClauseToQuery(SubQuery, JsonObject) Starting");

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

		logger.debug("addJsonWhereClauseToQuery(SubQuery, JsonObject) processing entities from clause");
		Entity entity = null;
		Resource resource = null;
		if (path != null && !path.isEmpty()) {

			// TODO This is stupid. We should remove this and test it and forgetaboutit!!!
			path = "/" + path;
			path = path.substring(1);

			String newResourceName = path.split("/")[1];
			logger.debug("addJsonWhereClauseToQuery(SubQuery, JsonObject) check `Resource` named '"+newResourceName+"'");
			try {
				resource = rc.getResource(newResourceName);
			} catch (Exception e) {
				throw new QueryException("Could not initialize `Resource` from '"+newResourceName+"'");
			}
			entity = new Entity(path);
			if (dataType != null) {
				entity.setDataType(resource.getDataTypeByName(dataType));
			}
		}
		if ((resource == null) || (entity == null)) {
			throw new QueryException("Invalid Path '"+(path!=null?path:"NULL")+"'");
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
		logger.debug("validateWhereClause(SubQuery, ...) Starting");

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
		logger.debug("validateWhereClause(SubQuery, ...) Adding WHERE clause in `QueryController`");
		return qc.addWhereClause(sq, clauseId, resource, field, predicateType,
				logicalOperator, fields, objectFields);
	}

	private Long addJsonSelectClauseToQuery(SubQuery sq, JsonObject selectClause)
			throws QueryException {
		logger.debug("addJsonSelectClauseToQuery(SubQuery, ...) Starting");

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
			String newResourceName = path.split("/")[1];
			resource = rc.getResource(newResourceName);
			if (resource == null) {
				throw new QueryException("Invalid Resource `"+newResourceName+"`");
			}
			logger.debug("addJsonSelectClauseToQuery(SubQuery, ...) resource '"+newResourceName+"' is found.");
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

		if ((resource.getSupportedSelectFields() != null) && (!resource.getSupportedSelectFields().isEmpty())) {

			Map<String, Field> clauseFields = new HashMap<String, Field>();
			for(Field field : resource.getSupportedSelectFields()) {
				clauseFields.put(field.getPath(), field);
			}

			if (selectClause.containsKey("fields")) {
				JsonObject fieldObject = selectClause.getJsonObject("fields");
				objectFields = getObjectFields(clauseFields, fieldObject);
				fields = getStringFields(clauseFields, fieldObject);
			}
		}


		logger.debug("addJsonSelectClauseToQuery(SubQuery, ...) returning `validateSelectClause`");
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
		logger.debug("addJsonSubQueryToQuery(Query, String, JsonObject) Starting");
		SubQuery sq = (SubQuery) convertJsonToQuery(qc.createSubQuery(),
				subQuery);
		if(query == null) {
			qc.addSubQuery(subQueryId, sq);
		} else {
			query.addSubQuery(subQueryId, sq);
		}
		logger.debug("addJsonSubQueryToQuery(Query, String, JsonObject) Finished");
	}

	private Query convertJsonToQuery(JsonObject jsonQuery)
			throws QueryException {
		logger.debug("convertJsonToQuery(JsonObject) Starting");
		// Create the query
		qc.createQuery();
		logger.debug("convertJsonToQuery(JsonObject) `Query` object has been created.");

		return convertJsonToQuery(null, jsonQuery);
	}

	private Query convertJsonToQuery(SubQuery subQuery, JsonObject jsonQuery)
			throws QueryException {
		logger.debug("convertJsonToQuery(SubQuery, JsonObject) Starting ");

		// Convert JSON Selects to QueryEndpoint
		if (jsonQuery.containsKey("select")) {
			logger.debug("convertJsonToQuery(SubQuery, JsonObject) processing SELECT ");

			JsonArray selectClauses = jsonQuery.getJsonArray("select");
			Iterator<JsonValue> selectIterator = selectClauses.iterator();
			while (selectIterator.hasNext()) {
				addJsonSelectClauseToQuery(subQuery,
						(JsonObject) selectIterator.next());
			}

		}
		// Convert JSON Where to QueryEndpoint
		if (jsonQuery.containsKey("where")) {
			logger.debug("convertJsonToQuery(SubQuery, JsonObject) processing WHERE ");

			JsonArray whereClauses = jsonQuery.getJsonArray("where");
			Iterator<JsonValue> whereIterator = whereClauses.iterator();
			while (whereIterator.hasNext()) {
				addJsonWhereClauseToQuery(subQuery,
						(JsonObject) whereIterator.next());
			}
		}
		// Convert JSON Join to QueryEndpoint
		if (jsonQuery.containsKey("join")) {
			logger.debug("convertJsonToQuery(SubQuery, JsonObject) processing JOIN ");

			JsonArray joinClauses = jsonQuery.getJsonArray("join");
			Iterator<JsonValue> joinIterator = joinClauses.iterator();
			while (joinIterator.hasNext()) {
				addJsonJoinClauseToQuery(subQuery,
						(JsonObject) joinIterator.next());
			}
		}
		// Convert JSON Sort to QueryEndpoint
		if (jsonQuery.containsKey("sort")) {
			logger.debug("convertJsonToQuery(SubQuery, JsonObject) processing SORT ");

			JsonArray sortClauses = jsonQuery.getJsonArray("sort");
			Iterator<JsonValue> sortIterator = sortClauses.iterator();
			while (sortIterator.hasNext()) {
				addJsonSortClauseToQuery(subQuery,
						(JsonObject) sortIterator.next());
			}
		}
		// Convert JSON SubQueries to QueryEndpoint
		if (jsonQuery.containsKey("subquery")) {
			logger.debug("convertJsonToQuery(SubQuery, JsonObject) processing SUBQUERY ");

			JsonObject subQueryObject = jsonQuery.getJsonObject("subquery");
			for (String key : subQueryObject.keySet()) {
				addJsonSubQueryToQuery(subQuery, key, subQueryObject.getJsonObject(key));
			}
		}

		if (subQuery != null) {
			logger.debug("convertJsonToQuery(SubQuery, JsonObject) returning update `subQuery` ");
			return subQuery;
		} else {
			logger.debug("convertJsonToQuery(SubQuery, JsonObject) returning new query ");
			return qc.getQuery();
		}
	}
}
