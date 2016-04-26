/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.cl.rest;

import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
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
import edu.harvard.hms.dbmi.bd2k.irct.model.query.PredicateType;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.LogicalOperator;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

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
	
	
	@GET
	@Path("/loadQuery")
	@Produces(MediaType.APPLICATION_JSON)
	public Response loadQuery(@QueryParam(value = "queryId") Long queryId) {
		JsonObjectBuilder response = Json.createObjectBuilder();
		String conversationId = admin.startConversation();
		
		if(queryId == null) {
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
	

	@POST
	@Path("/clause")
	@Produces(MediaType.APPLICATION_JSON)
	public Response clause(String payload) {
		JsonObjectBuilder response = Json.createObjectBuilder();

		JsonReader jsonReader = Json.createReader(new StringReader(payload));
		JsonObject object = jsonReader.readObject();
		jsonReader.close();

		String path = null;
		String dataType = null;
		if(object.containsKey("field")) {
			path = object.getJsonObject("field").getString("pui");
			if(object.getJsonObject("field").containsKey("dataType")) {
				dataType = object.getJsonObject("field").getString("dataType");
			}
		}
		
		
		
		
		Long clauseId = null;
		if(object.containsKey("clauseId")) {
			clauseId = object.getJsonNumber("clauseId").longValue();
		}
		
		Entity field = null;
		Resource resource = null;
		if (path != null && !path.isEmpty()) {
			path = "/" + path;
			path = path.substring(1);
			resource = rc.getResource(path.split("/")[1]);
			field = new Entity(path);
			if(dataType != null) {
				field.setDataType(resource.getDataTypeByName(dataType));
			}
		}
		if ((resource == null) || (field == null)) {
			response.add("status", "Invalid Request");
			response.add("message", "Invalid Path");
			return Response.status(400).entity(response.build()).build();
		}
		
		if (object.getString("type").equals("where")) {
			String predicateName = object.getString("predicate");
			String logicalOperatorName = null;
			if(object.containsKey("logicalOperator")) {
				logicalOperatorName = object.getString("logicalOperator");
			}
			Map<String, String> fields = new HashMap<String, String>();
			if(object.containsKey("fields")) {
				JsonObject fieldObject = object.getJsonObject("fields");
				for(String key : fieldObject.keySet()) {
					fields.put(key, fieldObject.getString(key));
				}
			}
			
			return validateWhereClause(clauseId, resource, field,
					predicateName, logicalOperatorName, fields);
		}

		response.add("status", "Invalid Request");
		response.add("message", "The clause type is unknown");
		return Response.status(400).entity(response.build()).build();
	}

	@GET
	@Path("/clause")
	@Produces(MediaType.APPLICATION_JSON)
	public Response clause(@QueryParam(value = "type") String type,
			@Context UriInfo info) {
		JsonObjectBuilder response = Json.createObjectBuilder();
		
		MultivaluedMap<String, String> queryParameters = info
				.getQueryParameters();

		String path = queryParameters.getFirst("path");
		Entity field = null;
		Resource resource = null;
		if (path != null && !path.isEmpty()) {
			path = "/" + path;
			path = path.substring(1);
			resource = rc.getResource(path.split("/")[1]);
			field = new Entity(path);
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

		if (type.equals("where")) {
			String predicateName = queryParameters.getFirst("predicate");

			String logicalOperatorName = queryParameters
					.getFirst("logicalOperator");

			Map<String, String> fields = new HashMap<String, String>();
			for (String key : queryParameters.keySet()) {
				if (key.startsWith("data-")) {
					fields.put(key.substring(5), queryParameters.getFirst(key));
				}
			}

			return validateWhereClause(clauseId, resource, field,
					predicateName, logicalOperatorName, fields);

		} else if (type.equals("select")) {

		} else if (type.equals("join")) {

		}
		response.add("status", "Invalid Request");
		response.add("message", "The clause type is unknown");
		return Response.status(400).entity(response.build()).build();
	}

	private Response validateWhereClause(Long clauseId, Resource resource,
			Entity field, String predicateName, String logicalOperatorName,
			Map<String, String> fields) {
		JsonObjectBuilder response = Json.createObjectBuilder();
		PredicateType predicateType = resource
				.getSupportedPredicateByName(predicateName);
		if (predicateType == null) {
			response.add("status", "Invalid Request");
			response.add("message", "Unknown predicate type");
			return Response.status(400).entity(response.build()).build();
		}

		LogicalOperator logicalOperator = null;
		if (logicalOperatorName != null) {
			logicalOperator = resource
					.getLogicalOperatorByName(logicalOperatorName);
			if (logicalOperator == null) {
				response.add("status", "Invalid Request");
				response.add("message", "Unknown logical operator");
				return Response.status(400).entity(response.build()).build();
			}
		}

		try {
			response.add("clauseId", qc.addWhereClause(clauseId, resource,
					field, predicateType, logicalOperator, fields));
		} catch (QueryException queryException) {
			response.add("status", "Invalid Request");
			response.add("message", queryException.getMessage());
			return Response.status(400).entity(response.build()).build();
		}

		return Response.ok(response.build(), MediaType.APPLICATION_JSON)
				.build();
	}

	@GET
	@Path("/runQuery")
	@Produces(MediaType.APPLICATION_JSON)
	public Response runQuery() {
		JsonObjectBuilder response = Json.createObjectBuilder();
		try {
			response.add("resultId", ec.runQuery(qc.getQuery(), (SecureSession) session.getAttribute("secureSession")));
		} catch (PersistableException e) {
			response.add("status", "Error running request");
			response.add("message", "An error occurred running this request");
			return Response.status(400).entity(response.build()).build();
		}
		admin.endConversation();
		return Response.ok(response.build(), MediaType.APPLICATION_JSON)
				.build();
	}
}
