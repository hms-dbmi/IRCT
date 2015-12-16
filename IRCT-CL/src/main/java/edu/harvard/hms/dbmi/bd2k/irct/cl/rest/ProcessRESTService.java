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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import edu.harvard.hms.dbmi.bd2k.irct.cl.util.AdminBean;
import edu.harvard.hms.dbmi.bd2k.irct.cl.util.Constants;
import edu.harvard.hms.dbmi.bd2k.irct.controller.ExecutionController;
import edu.harvard.hms.dbmi.bd2k.irct.controller.ProcessController;
import edu.harvard.hms.dbmi.bd2k.irct.controller.ResourceController;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;

/**
 * Creates the process service for the JAX-RS REST service. This service is
 * conversation scoped all requests for working with a process must include the
 * conversation id that represents the query. An end user may have multiple
 * queries going along the same time.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Path("/processRESTService")
@ConversationScoped
@Named
public class ProcessRESTService implements Serializable {

	private static final long serialVersionUID = 4035110852879835263L;

	@Inject
	private AdminBean admin;

	@Inject
	private ProcessController pc;

	@Inject
	private ExecutionController ec;

	@Inject
	private ResourceController rc;

	/**
	 * Initiates the creation of a process
	 * 
	 * @return A JSON Object representing the status of that request
	 */
	@GET
	@Path("/startProcess")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure startProcess() {
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();

		String conversationId = admin.startConversation();
		pc.createProcess(conversationId);

		responseBuilder.add("status", Constants.STATUS_OK);
		responseBuilder.add("cid", conversationId);
		responseBuilder.add("version", Constants.QUERYPROTOCOL);
		return responseBuilder.build();
	}

	/**
	 * Updates a process
	 * 
	 * @param info
	 *            Request Information
	 * @return A JSON Object representing the status of that request
	 */
	@GET
	@Path("/updateProcess")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure addProcess(@Context UriInfo info) {
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();
		MultivaluedMap<String, String> parameters = info.getQueryParameters();

		Resource resource = rc.getResource(getFirstMultiValuedMap(parameters,
				"resource"));
		String processName = getFirstMultiValuedMap(parameters, "processName");

		Map<String, String> values = new HashMap<String, String>();
		for (String key : parameters.keySet()) {
			if (key.startsWith("data-")) {
				values.put(key.replace("data-", ""),
						getFirstMultiValuedMap(parameters, key));
			}
		}

		pc.updateProcess(resource, processName, values);

		responseBuilder.add("status", Constants.STATUS_OK);
		return responseBuilder.build();
	}

	/**
	 * Runs a process
	 * 
	 * @return A JSON Object representing the status of that request
	 */
	@GET
	@Path("/runProcess")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure runProcess() {
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();

		try {
			responseBuilder.add("resultId", ec.runProcess(pc.getProcess()));
		} catch (PersistableException e) {
			e.printStackTrace();
		}

		admin.endConversation();
		return responseBuilder.build();
	}

	/**
	 * Attempts to cancel a process
	 * 
	 * @return A JSON Object representing the status of that request
	 */
	@GET
	@Path("/cancelProcess")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure cancelProcess() {
		JsonObjectBuilder responseBuilder = Json.createObjectBuilder();

		pc.cancelProcess();
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
