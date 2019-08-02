/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.cl.rest;


import edu.harvard.hms.dbmi.bd2k.irct.util.IRCTResponse;
import edu.harvard.hms.dbmi.bd2k.irct.cl.util.Utilities;
import edu.harvard.hms.dbmi.bd2k.irct.controller.SecurityController;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;
import org.apache.log4j.Logger;

import javax.enterprise.context.SessionScoped;
import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.io.Serializable;

/**
 * Creates a REST interface for the security service
 */
@Path("/securityService")
@SessionScoped
@ManagedBean
public class SecurityService implements Serializable {

	Logger logger = Logger.getLogger(this.getClass());

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

	@javax.annotation.Resource(mappedName ="java:global/jwks_uri")
	private String jwksUri;

	@javax.annotation.Resource(mappedName ="java:global/userField")
	private String userField;

	/**
	 * Creates a secure key if the user is inside a valid session
	 *
	 * @return A secure generated random key
	 */
	@GET
	@Path("/createKey")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createKey(@Context HttpServletRequest req) {
		logger.debug("/createKey Starting");

		try {
			User userObject = sc.ensureUserExists(Utilities
					.extractEmailFromJWT(req, this.clientSecret, this.jwksUri, this.userField));
			logger.debug("/createKey user exists");
			userObject.setToken(Utilities.extractToken(req));

			String key = sc.createKey(userObject);
			// IF USER IS LOGGED IN
			if (key != null) {
				userObject.setAccessKey(key);
				sc.updateUserRecord(userObject);
				logger.debug("/createKey user updated. key:" + key);
				return IRCTResponse.success(userObject);
			} else {
				return IRCTResponse.protocolError(Status.UNAUTHORIZED, "Unable to generate key for user:" + userObject.getName() + " and token:" + session.getAttribute("token"));
			}
		} catch (NotAuthorizedException e){
			logger.error("/createKey cannot validate the token: "+ e.getChallenges());
			return IRCTResponse.protocolError(Status.UNAUTHORIZED, "unable to validate the token");
		}

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
		String warning = "This authentication method should only be used to validate prior research. Any new research being developed should instead use the Bearer token functionality. Contact an administrator to acquire a bearer token and instructions on how to use it.";
		
		JsonObjectBuilder build = Json.createObjectBuilder();

		User u = sc.validateKey(key);
		if (u == null){
			logger.error("/startSession Cannot validate key: " + key);
			throw new NotAuthorizedException("Cannot validate key");
		}

		session.setAttribute("user", u);
		build.add("status", "success");
		build.add("message", warning);
		build.add("token", u.getToken());

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
