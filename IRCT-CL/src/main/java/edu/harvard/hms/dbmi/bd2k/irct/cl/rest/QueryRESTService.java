package edu.harvard.hms.dbmi.bd2k.irct.cl.rest;

import java.io.Serializable;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonStructure;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import edu.harvard.hms.dbmi.bd2k.irct.cl.util.AdminBean;
import edu.harvard.hms.dbmi.bd2k.irct.cl.util.Constants;
import edu.harvard.hms.dbmi.bd2k.irct.controller.LogicalOperatorNotFound;
import edu.harvard.hms.dbmi.bd2k.irct.controller.PredicateTypeNotSupported;
import edu.harvard.hms.dbmi.bd2k.irct.controller.QueryController;
import edu.harvard.hms.dbmi.bd2k.irct.controller.QueryExecutor;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ClauseIsNotTheCorrectType;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ClauseNotFoundException;
import edu.harvard.hms.dbmi.bd2k.irct.exception.JoinTypeNotSupported;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceNotFoundException;
import edu.harvard.hms.dbmi.bd2k.irct.exception.SubQueryNotFoundException;

@Path("/queryService")
@ConversationScoped
@Named
public class QueryRESTService implements Serializable {

	private static final long serialVersionUID = 5458918309919812803L;

	@Inject
	private QueryController qc;

	@Inject
	private QueryExecutor qe;

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
	 * @return A JSON Object representing the status of that request
	 * @throws ResourceNotFoundException
	 */
	@GET
	@Path("/startSubQuery")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure startSubQuery(
			@DefaultValue("") @QueryParam(value = "resource") String resource) {
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
		Long sqId;
		try {
			if (resource.equals("")) {
				sqId = qc.createSubQuery(new String[] {});
			} else {
				sqId = qc.createSubQuery(resource.split(","));
			}

			responseBuilder.add("status", Constants.STATUS_OK);
			responseBuilder.add("subQueryId", sqId);
		} catch (ResourceNotFoundException e) {
			responseBuilder.add("status", Constants.STATUS_ERROR_FAIL);
			responseBuilder.add("message", e.getMessage());
		}

		return responseBuilder.build();
	}

	@GET
	@Path("/selectClause")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure selectClause(
			@DefaultValue("") @QueryParam(value = "sq") String sq,
			@DefaultValue("") @QueryParam(value = "parameters") String parameters) {
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
		Long selectId;
		String[] paramArray = parameters.split(",");

//		try {
//			if (sq.equals("")) {
//				selectId = qc.addSelectClause(null, paramArray);
//
//			} else {
//				selectId = qc.addSelectClause(Long.parseLong(sq), paramArray);
//			}
//			responseBuilder.add("status", Constants.STATUS_OK);
//			responseBuilder.add("selectId", selectId);
//
//		} catch (SubQueryNotFoundException e) {
//			responseBuilder.add("status", Constants.STATUS_ERROR_FAIL);
//			responseBuilder.add("message", e.getMessage());
//		}

		return responseBuilder.build();
	}

	@GET
	@Path("/joinClause")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure joinClause(@Context UriInfo info) {
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
		MultivaluedMap<String, String> parameters = info.getPathParameters();
		Long sqId1 = Long.parseLong(getFirstMultiValuedMap(parameters, "q1"));
		Long sqId2 = Long.parseLong(getFirstMultiValuedMap(parameters, "q2"));
		String joinType = getFirstMultiValuedMap(parameters, "joinType");
		Long field1 = Long.parseLong(getFirstMultiValuedMap(parameters, "f1"));
		Long field2 = Long.parseLong(getFirstMultiValuedMap(parameters, "f2"));
		String relationship = getFirstMultiValuedMap(parameters, "relationship");
		Long joinId = Long.parseLong(getFirstMultiValuedMap(parameters,
				"joinID"));

		if (joinType == null) {
			responseBuilder.add("status", Constants.STATUS_ERROR_FAIL);
			responseBuilder.add("message",
					"Required parameter: joinType not passed");
			return responseBuilder.build();
		}

//		try {
//			Long newJoinId = qc.addJoinClause(sqId1, sqId2, joinType, field1,
//					field2, relationship, joinId);
			responseBuilder.add("status", Constants.STATUS_OK);
//			responseBuilder.add("joinID", newJoinId);

//		} catch (ClauseNotFoundException | ClauseIsNotTheCorrectType
//				| SubQueryNotFoundException | JoinTypeNotSupported e) {
//			responseBuilder.add("status", Constants.STATUS_ERROR_FAIL);
//			responseBuilder.add("message", e.getMessage());
//		}

		return responseBuilder.build();
	}

	@GET
	@Path("/whereClause")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure whereClause(@Context UriInfo info) {
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
		MultivaluedMap<String, String> parameters = info.getPathParameters();

		Long sqId = Long.parseLong(getFirstMultiValuedMap(parameters, "sq"));
		String logicalOperator = getFirstMultiValuedMap(parameters,
				"logicalOperator");
		Long fieldId = Long.parseLong(getFirstMultiValuedMap(parameters,
				"field"));
		String predicate = getFirstMultiValuedMap(parameters, "predicate");
		String value = getFirstMultiValuedMap(parameters, "value");
		String additionalValue = getFirstMultiValuedMap(parameters,
				"additionalValue");
		Long whereId = Long.parseLong(getFirstMultiValuedMap(parameters,
				"whereID"));

		if (logicalOperator == null) {
			logicalOperator = "AND";
		}

		if (fieldId == null) {
			responseBuilder.add("status", Constants.STATUS_ERROR_FAIL);
			responseBuilder.add("message",
					"Required parameter: fieldId not passed");
			return responseBuilder.build();
		}
		if (predicate == null) {
			responseBuilder.add("status", Constants.STATUS_ERROR_FAIL);
			responseBuilder.add("message",
					"Required parameter: predicate not passed");
			return responseBuilder.build();
		}

		Long newJoinId;
//		try {
//			newJoinId = qc.addWhereClause(sqId, logicalOperator, fieldId,
//					predicate, value, additionalValue, whereId);
//			responseBuilder.add("status", Constants.STATUS_OK);
//			responseBuilder.add("whereID", newJoinId);
//		} catch (ClauseNotFoundException | ClauseIsNotTheCorrectType
//				| SubQueryNotFoundException | LogicalOperatorNotFound
//				| PredicateTypeNotSupported e) {
//			responseBuilder.add("status", Constants.STATUS_ERROR_FAIL);
//			responseBuilder.add("message", e.getMessage());
//		}

		return responseBuilder.build();
	}

	@GET
	@Path("/deleteClause")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure deleteClause(@QueryParam(value = "clauseId") Long clauseId) {
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();

//		try {
//			qc.deleteClause(clauseId);
//			responseBuilder.add("status", Constants.STATUS_OK);
//		} catch (ClauseNotFoundException e) {
//			responseBuilder.add("status", Constants.STATUS_ERROR_FAIL);
//			responseBuilder.add("message", e.getMessage());
//		}

		return responseBuilder.build();
	}

	@GET
	@Path("/runQuery")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure runQuery() {
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
		qe.runQuery(qc.getQuery());
		admin.endConversation();
		return responseBuilder.build();
	}

	@GET
	@Path("/cancelQuery")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure cancelQuery() {
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
//		qc.cancelQuery();
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
}
