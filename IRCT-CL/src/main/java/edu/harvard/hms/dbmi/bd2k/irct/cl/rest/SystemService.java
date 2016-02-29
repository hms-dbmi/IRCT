package edu.harvard.hms.dbmi.bd2k.irct.cl.rest;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonStructure;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import edu.harvard.hms.dbmi.bd2k.irct.IRCTApplication;
import edu.harvard.hms.dbmi.bd2k.irct.controller.JoinController;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.JoinType;

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
		
		//TODO: BUILD OUT
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
		for (JoinType jt : irctApp.getSupportedJoinTypes().values()) {
			build.add(jt.toJson());
		}
		return build.build();
	}

	/**
	 * Runs a join after the join has been validated.
	 * @param info Request Information
	 * @return A JSON Object representing the status of that request 
	 */
	@GET
	@Path("/runJoin")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure runJoin(@Context UriInfo info) {
		JoinType jt = jc.createJoin("");
		jc.runJoin(jt);
		//TODO: BUILD OUT
		return null;
	}
}
