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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import edu.harvard.hms.dbmi.bd2k.irct.cl.util.AdminBean;
import edu.harvard.hms.dbmi.bd2k.irct.controller.ExecutionController;
import edu.harvard.hms.dbmi.bd2k.irct.controller.ProcessController;
import edu.harvard.hms.dbmi.bd2k.irct.controller.ResourceController;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ProcessException;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ProcessType;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

@Path("/processService")
@ConversationScoped
@Named
public class ProcessService implements Serializable {

	private static final long serialVersionUID = -7776350350430366492L;

	@Inject
	private ResourceController rc;

	@Inject
	private ProcessController pc;

	@Inject
	private AdminBean admin;

	@Inject
	private ExecutionController ec;

	@Inject
	private HttpSession session;

	@GET
	@Path("/startProcess")
	@Produces(MediaType.APPLICATION_JSON)
	public Response startProcess() {
		JsonObjectBuilder response = Json.createObjectBuilder();
		String conversationId = admin.startConversation();

		pc.createProcess();

		response.add("cid", conversationId);
		return Response.ok(response.build(), MediaType.APPLICATION_JSON)
				.build();
	}

	@GET
	@Path("/saveProcess")
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveQuery() {
		JsonObjectBuilder response = Json.createObjectBuilder();
		try {
			pc.saveProcess();
		} catch (ProcessException e) {
			response.add("status", "Invalid Request");
			response.add("message", e.getMessage());
			return Response.status(400).entity(response.build()).build();
		}

		response.add("queryId", pc.getProcess().getId());
		return Response.ok(response.build(), MediaType.APPLICATION_JSON)
				.build();
	}

	@GET
	@Path("/loadProcess")
	@Produces(MediaType.APPLICATION_JSON)
	public Response loadQuery(@QueryParam(value = "processId") Long processId) {
		JsonObjectBuilder response = Json.createObjectBuilder();
		String conversationId = admin.startConversation();

		if (processId == null) {
			response.add("status", "Invalid Request");
			response.add("message", "processId is not set");
			return Response.status(400).entity(response.build()).build();
		}

		try {
			pc.loadProcess(processId);
		} catch (ProcessException e) {
			response.add("status", "Invalid Request");
			response.add("message", e.getMessage());
			return Response.status(400).entity(response.build()).build();
		}

		response.add("cid", conversationId);
		return Response.ok(response.build(), MediaType.APPLICATION_JSON)
				.build();
	}
	
	@POST
	@Path("/process")
	@Produces(MediaType.APPLICATION_JSON)
	public Response process(String payload) {
		JsonObjectBuilder response = Json.createObjectBuilder();

		JsonReader jsonReader = Json.createReader(new StringReader(payload));
		JsonObject object = jsonReader.readObject();
		jsonReader.close();
		
		if(!object.containsKey("resource") || !object.containsKey("name")) {
			response.add("status", "Invalid Request");
			response.add("message", "No resource or process name is set");
			return Response.status(400).entity(response.build()).build();
		}
		
		Resource resource = rc.getResource(object.getString("resource"));
		if(resource == null) {
			response.add("status", "Invalid Request");
			response.add("message", "Unknown resource name");
			return Response.status(400).entity(response.build()).build();	
		}
		ProcessType pt = resource.getSupportedProcessesByName(object.getString("name"));
		if(pt == null) {
			response.add("status", "Invalid Request");
			response.add("message", "Unknown process for this resource");
			return Response.status(400).entity(response.build()).build();
		}
		
		Map<String, String> fields = new HashMap<String, String>();
		if(object.containsKey("fields")) {
			JsonObject fieldObject = object.getJsonObject("fields");
			for(String key : fieldObject.keySet()) {
				fields.put(key, fieldObject.getString(key));
			}
		}
		
		try {
			pc.updateProcess(resource, pt, fields);
		} catch (ProcessException e) {
			response.add("status", "Invalid Request");
			response.add("message", e.getMessage());
			return Response.status(400).entity(response.build()).build();
		}
		

		response.add("status", "ok");
		return Response.ok(response.build(), MediaType.APPLICATION_JSON)
				.build();
	}
	
	@GET
	@Path("/process")
	@Produces(MediaType.APPLICATION_JSON)
	public Response process(@Context UriInfo info) {
		JsonObjectBuilder response = Json.createObjectBuilder();
		
		response.add("status", "Invalid Request");
		response.add("message", "The clause type is unknown");
		return Response.status(400).entity(response.build()).build();	
	}
	
	@GET
	@Path("/runProcess")
	@Produces(MediaType.APPLICATION_JSON)
	public Response runQuery() {
		JsonObjectBuilder response = Json.createObjectBuilder();
		try {
			response.add("resultId", ec.runProcess(pc.getProcess(), (SecureSession) session.getAttribute("secureSession")));
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
