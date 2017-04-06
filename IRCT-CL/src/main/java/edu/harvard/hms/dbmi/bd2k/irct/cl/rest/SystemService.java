/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.cl.rest;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonStructure;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.harvard.hms.dbmi.bd2k.irct.IRCTApplication;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;

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
	 * Returns a JSON Array of application settings
	 *
	 * @return JSON Array of settings (key/value pair)
	 */
	@GET
	@Path("/about")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure about() {
		log.log(Level.FINE, "/about URL");

		IRCTApplication app = new IRCTApplication();
		JsonArrayBuilder build = Json.createArrayBuilder();
		build.add(Json.createObjectBuilder().add("version", app.getVersion()));
		User user = (User) session.getAttribute("user");
		build.add(Json.createObjectBuilder().add("username", user.getName()));
		build.add(Json.createObjectBuilder().add("userid", user.getUserId()));
		return build.build();
	}
}
