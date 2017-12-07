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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import edu.harvard.hms.dbmi.bd2k.irct.controller.ExecutionController;
import edu.harvard.hms.dbmi.bd2k.irct.controller.ProcessController;
import edu.harvard.hms.dbmi.bd2k.irct.controller.ResourceController;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ProcessException;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ProcessType;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;

/**
 * Creates a REST interface for the process service
 */
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
	private ExecutionController ec;

	@Inject
	private HttpSession session;
	
	private Logger logger = Logger.getLogger(this.getClass());

	/**
	 * Starts the creation of a process
	 * 
	 * @return Conversation Id
	 */
	@GET
	@Path("/startProcess")
	@Produces(MediaType.APPLICATION_JSON)
	public Response startProcess() {
		JsonObjectBuilder response = Json.createObjectBuilder();
		pc.createProcess();
		return Response.ok(response.build(), MediaType.APPLICATION_JSON)
				.build();
	}

	/**
	 * Saves a process
	 * 
	 * @return Process Id
	 */
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

		response.add("processId", pc.getProcess().getId());
		return Response.ok(response.build(), MediaType.APPLICATION_JSON)
				.build();
	}

	/**
	 * Loads a process
	 * 
	 * @param processId
	 *            Process Id
	 * @return Conversation Id
	 */
	@GET
	@Path("/loadProcess")
	@Produces(MediaType.APPLICATION_JSON)
	public Response loadQuery(@QueryParam(value = "processId") Long processId) {
		JsonObjectBuilder response = Json.createObjectBuilder();
		
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
		return Response.ok(response.build(), MediaType.APPLICATION_JSON)
				.build();
	}

	/**
	 * Updates a process from a JSON representation
	 * 
	 * @param payload
	 *            JSON
	 * @return Status
	 */
	@POST
	@Path("/process")
	@Produces(MediaType.APPLICATION_JSON)
	public Response process(String payload) {
		logger.info("POST /process Starting");
		JsonObjectBuilder response = Json.createObjectBuilder();

		JsonReader jsonReader = Json.createReader(new StringReader(payload));
		JsonObject object = jsonReader.readObject();
		jsonReader.close();

		try {
			pc.createProcess();
		} catch (Exception e) {
			response.add("status", "error");
			response.add("message", "Could not initialize `process`."+e.getMessage());
			return Response.status(400).entity(response.build()).build();
		}
		
		if (!object.containsKey("resource") || !object.containsKey("name")) {
			response.add("status", "Invalid Request");
			response.add("message", "No resource or process name is set");
			return Response.status(400).entity(response.build()).build();
		}
		logger.info("resource:"+object.getString("resource"));
		logger.info("name:"+object.getString("name"));
		for (String key: object.keySet()) {
			logger.info("\t"+key);
		}
		Resource resource = rc.getResource(object.getString("resource"));
		if (resource == null) {
			response.add("status", "Invalid Request");
			response.add("message", "Unknown resource name");
			return Response.status(400).entity(response.build()).build();
		}
		logger.info("POST /process Resource:"+resource.getName());
		
		logger.info("POST /process supportedProcessName:"+object.getString("name"));
		ProcessType pt = resource.getSupportedProcessesByName(object.getString("name"));
		
		if (pt == null) {
			response.add("status", "Invalid Request");
			response.add("message", "Unknown process for this resource");
			return Response.status(400).entity(response.build()).build();
		}

		Map<String, String> fields = new HashMap<String, String>();
		if (object.containsKey("fields")) {
			JsonArray fieldList = object.getJsonArray("fields");
			
			Iterator<JsonValue> fldListIterator = fieldList.iterator();
			while (fldListIterator.hasNext()) {
				JsonObject o = (JsonObject) fldListIterator.next();
				logger.info(" type:"+o.getValueType());
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

	/**
	 * Updates the parameters of a process
	 * 
	 * @param info
	 *            URI Info
	 * @return Status
	 */
	@GET
	@Path("/process")
	@Produces(MediaType.APPLICATION_JSON)
	public Response process(@Context UriInfo info) {
		JsonObjectBuilder response = Json.createObjectBuilder();

		response.add("status", "Invalid Request");
		response.add("message", "The clause type is unknown");
		return Response.status(400).entity(response.build()).build();
	}

	/**
	 * Runs the created process
	 * 
	 * @return Result Id
	 */
	@GET
	@Path("/runProcess")
	@Produces(MediaType.APPLICATION_JSON)
	public Response runProcess() {
		JsonObjectBuilder response = Json.createObjectBuilder();
		try {
			response.add("resultId", ec.runProcess(pc.getProcess(), (User) session.getAttribute("user")));
		} catch (PersistableException e) {
			response.add("status", "Error running request");
			response.add("message", "An error occurred running this request");
			return Response.status(400).entity(response.build()).build();
		}
		return Response.ok(response.build(), MediaType.APPLICATION_JSON)
				.build();
	}

	/**
	 * Runs a process from a JSON representation
	 * 
	 * @param payload
	 *            JSON
	 * @return Result Id
	 */
	@POST
	@Path("/runProcess")
	@Produces(MediaType.APPLICATION_JSON)
	public Response runProcess(String payload) {
		JsonObjectBuilder response = Json.createObjectBuilder();

		JsonReader jsonReader = Json.createReader(new StringReader(payload));
		JsonObject object = jsonReader.readObject();
		jsonReader.close();

		pc.createProcess();

		if (!object.containsKey("resource") || !object.containsKey("name")) {
			response.add("status", "Invalid Request");
			response.add("message", "No resource or process name is set");
			return Response.status(400).entity(response.build()).build();
		}

		Resource resource = rc.getResource(object.getString("resource"));
		if (resource == null) {
			response.add("status", "Invalid Request");
			response.add("message", "Unknown resource name");
			return Response.status(400).entity(response.build()).build();
		}
		ProcessType pt = resource.getSupportedProcessesByName(object
				.getString("name"));
		if (pt == null) {
			response.add("status", "Invalid Request");
			response.add("message", "Unknown process for this resource");
			return Response.status(400).entity(response.build()).build();
		}

		Map<String, String> fields = new HashMap<String, String>();
		if (object.containsKey("fields")) {
			JsonObject fieldObject = object.getJsonObject("fields");
			for (String key : fieldObject.keySet()) {
				fields.put(key, fieldObject.getString(key));
			}
		}

		try {
			pc.updateProcess(resource, pt, fields);
			response.add("resultId", ec.runProcess(pc.getProcess(), (User) session.getAttribute("user")));

		} catch (PersistableException | ProcessException e) {
			response.add("status", "Error running request");
			response.add("message", "An error occurred running this request");
			return Response.status(400).entity(response.build()).build();
		}

		return Response.ok(response.build(), MediaType.APPLICATION_JSON)
				.build();
	}

}
