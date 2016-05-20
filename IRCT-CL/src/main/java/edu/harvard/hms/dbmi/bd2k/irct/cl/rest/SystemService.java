/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.cl.rest;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonStructure;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import edu.harvard.hms.dbmi.bd2k.irct.IRCTApplication;
import edu.harvard.hms.dbmi.bd2k.irct.cl.util.Utilities;
import edu.harvard.hms.dbmi.bd2k.irct.controller.ExecutionController;
import edu.harvard.hms.dbmi.bd2k.irct.controller.JoinController;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ActionNotSetException;
import edu.harvard.hms.dbmi.bd2k.irct.exception.FieldException;
import edu.harvard.hms.dbmi.bd2k.irct.exception.JoinActionSetupException;
import edu.harvard.hms.dbmi.bd2k.irct.model.join.IRCTJoin;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

/**
 * Creates a REST interface for the systems service.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Path("/systemService")
@RequestScoped
public class SystemService {

	@Inject
	private IRCTApplication irctApp;

	@Inject
	private JoinController jc;

	@Inject
	private ExecutionController ec;

	@Inject
	Logger log;
	
	@Inject
	private HttpSession session;

	/**
	 * Returns a JSON Array of supported Data Types by the IRCT core.
	 * 
	 * @return JSON Array of data types
	 */
	@GET
	@Path("/dataTypes")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure dataTypes() {
		JsonArrayBuilder build = Json.createArrayBuilder();
		for (PrimitiveDataType pt : PrimitiveDataType.values()) {
			build.add(pt.toJson());
		}
		return build.build();
	}

	/**
	 * Returns a list of joins that are supported by the IRCT.
	 * 
	 * @return JSON Array of joins supported by the IRCT
	 */
	@GET
	@Path("/joins")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure joins() {
		JsonArrayBuilder build = Json.createArrayBuilder();
		for (IRCTJoin jt : irctApp.getSupportedJoinTypes().values()) {
			build.add(jt.toJson());
		}
		return build.build();
	}

	/**
	 * Runs a join after the join has been validated.
	 * 
	 * @param info
	 *            Request Information
	 * @return A JSON Object representing the status of that request
	 */
	@GET
	@Path("/runJoin")
	@Produces(MediaType.APPLICATION_JSON)
	public Response runJoin(@Context UriInfo info) {
		String joinName = info.getQueryParameters().getFirst("joinName");

		jc.createJoin(irctApp.getSupportedJoinTypes().get(joinName));

		Map<String, String> parameters = Utilities.getFirstFromMultiMap(
				info.getQueryParameters(), "data-");

		try {
			jc.setup(parameters);
			ec.runJoin(jc.getJoinType(), (SecureSession) session.getAttribute("secureSession"));

		} catch (ActionNotSetException | FieldException
				| JoinActionSetupException e) {
			log.log(Level.INFO,
					"Error in /systemService/runJoin/" + e.getMessage());
			JsonObjectBuilder build = Json.createObjectBuilder();
			build.add("status", "Invalid Run Join Request");
			build.add("message", e.getMessage());
			return Response.status(400).entity(build.build()).build();
		} catch (PersistableException e) {
			log.log(Level.INFO,
					"Error in /systemService/runJoin/" + e.getMessage());
			JsonObjectBuilder build = Json.createObjectBuilder();
			build.add("status", "Error running Join ");
			build.add("message",
					"An error occurred trying to run the join. Please try again later.");
			return Response.status(400).entity(build.build()).build();
		}

		// TODO: BUILD OUT
		JsonObjectBuilder build = Json.createObjectBuilder();
		build.add("status", "Not Implemented");
		build.add("message", "This feature has not been implemented");
		return Response.status(400).entity(build.build()).build();
	}
}
