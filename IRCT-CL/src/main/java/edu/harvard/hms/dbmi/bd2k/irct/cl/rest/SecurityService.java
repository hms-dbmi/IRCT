/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.cl.rest;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import javax.enterprise.context.SessionScoped;
import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import edu.harvard.hms.dbmi.bd2k.irct.cl.util.Utilities;
import edu.harvard.hms.dbmi.bd2k.irct.controller.SecurityController;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;

/**
 * Creates a REST interface for the security service
 *
 * @author Jeremy R. Easton-Marks
 *
 */
@Path("/securityService")
@SessionScoped
@ManagedBean
public class SecurityService implements Serializable {

	Logger logger = Logger.getLogger(this.getClass().getName());

	private static final long serialVersionUID = 8769258362839602228L;

	@Context
	UriInfo uriInfo;
	
	@Inject
	private SecurityController sc;
	
	@Inject
	private HttpSession session;

	@javax.annotation.Resource(mappedName ="java:global/redirect_on_success")
	private String redirectOnSuccess;

	@javax.annotation.Resource(mappedName ="java:global/domain")
	private String domain;

	@javax.annotation.Resource(mappedName ="java:global/client_id")
	private String clientId;

	@javax.annotation.Resource(mappedName ="java:global/client_secret")
	private String clientSecret;

	/**
	 * Creates a secure key if the user is inside a valid session
	 *
	 * @return A secure generated random key
	 */
	@GET
	@Path("/createKey")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createKey(@Context HttpServletRequest req) {

		JsonObjectBuilder build = Json.createObjectBuilder();
		try {
			User userObject = sc.ensureUserExists(Utilities.extractEmailFromJWT(req , this.clientSecret));
			
			String key = sc.createKey(userObject, (String) session.getAttribute("token"));
			// IF USER IS LOGGED IN
			if (key != null) {
				build.add("key", key);
			} else {
				build.add("status", "fail");
				build.add("message", "Unable to generate key for user:"+userObject.getName()+" and token:"+session.getAttribute("token"));
				return Response.status(Response.Status.FORBIDDEN)
						.entity(build.build()).build();
			}	
		} catch (IllegalArgumentException e) {
			build.add("status", "fail");
			build.add("message", "JWT token is not a token."+e.getMessage());
			return Response.status(Response.Status.FORBIDDEN)
					.entity(build.build()).build();
			
		} catch (UnsupportedEncodingException e) {
			build.add("status", "fail");
			build.add("message", "Invalid encoding for JWT token handling");
			return Response.status(Response.Status.FORBIDDEN)
					.entity(build.build()).build();
		}return Response.ok(build.build(), MediaType.APPLICATION_JSON).build();
	}
	
	/**
	 * Starts a session if presented with a valid key
	 *
	 * @param key
	 *            A session key
	 * @return A status message stating if a new session has started
	 */
	@Deprecated
	@GET
	@Path("/startSession")
	@Produces(MediaType.APPLICATION_JSON)
	public Response startSession(@QueryParam(value = "key") String key) {
		JsonObjectBuilder build = Json.createObjectBuilder();

		// Validate the key
		//SecureSession ss = sc.validateKey(key);
		//if (ss == null) {
		//	build.add("status", "failed");
		//	build.add("message", "Unable to start session");

		//	return Response.status(Response.Status.FORBIDDEN)
		//			.entity(build.build()).build();
		//}

		//this.user = ss.getUser();
		//this.token = ss.getToken();

		// Associate the session with that user
		//session.setAttribute("user", this.user);
		//session.setAttribute("token", this.token);
		//session.setAttribute("secureSession", ss);
		build.add("status", "deprecated");
		return Response.ok(build.build(), MediaType.APPLICATION_JSON).build();
	}

	/**
	 * Ends a session if currently within one
	 *
	 * @return A status message stating if the session has ended.
	 */
	@GET
	@Path("/endSession")
	@Produces(MediaType.APPLICATION_JSON)
	public Response endSession() {
		JsonObjectBuilder build = Json.createObjectBuilder();		
		session.removeAttribute("user");
		//session.removeAttribute("token");
		//session.removeAttribute("secureSession");
		build.add("status", "success");
		session.invalidate();
		return Response.ok(build.build(), MediaType.APPLICATION_JSON).build();
	}
}
