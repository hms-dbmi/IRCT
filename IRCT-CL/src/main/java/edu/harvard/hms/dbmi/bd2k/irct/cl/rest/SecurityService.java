/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.cl.rest;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

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

import org.apache.log4j.Logger;

import edu.harvard.hms.dbmi.bd2k.irct.cl.util.Utilities;
import edu.harvard.hms.dbmi.bd2k.irct.controller.SecurityController;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;

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

		JsonObjectBuilder build = Json.createObjectBuilder();
		try {
			User userObject = sc.ensureUserExists(Utilities.extractEmailFromJWT(req , this.clientSecret));
			logger.debug("/createKey user exists");
			userObject.setToken(Utilities.extractToken(req));
			
			/*
			Enumeration<String> keys = req.getAttributeNames();
			while(keys.hasMoreElements()) {
				String element = keys.nextElement();
				logger.debug("Element:"+element);
			}
			*/
			
			String key = sc.createKey(userObject);
			// IF USER IS LOGGED IN
			if (key != null) {
				userObject.setAccessKey(key);
				sc.updateUserRecord(userObject);
				build.add("status", "ok");
				build.add("key", key);
			} else {
				build.add("status", "error");
				build.add("message", "Unable to generate key for user:"+userObject.getName()+" and token:"+session.getAttribute("token"));
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
						.entity(build.build()).build();
			}	
		} catch (IllegalArgumentException e) {
			logger.error("/createKey IllegalArgumentException");
			
			build.add("status", "error");
			build.add("message", "JWT token is not a token."+e.getMessage());
			return Response.status(Response.Status.FORBIDDEN)
					.entity(build.build()).build();
			
		} catch (UnsupportedEncodingException e) {
			logger.error("/createKey UnsupportedEncodingException");
			
			build.add("status", "error");
			build.add("message", "Invalid encoding for JWT token handling");
			return Response.status(Response.Status.FORBIDDEN)
					.entity(build.build()).build();
		} catch (Exception e) {
			logger.error("/createKey Exception:"+e.getMessage());
			
			build.add("status", "error");
			build.add("message", "Unknown exception, while creating key:"+e.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(build.build()).build();
		}
		logger.debug("/createKey Success. Finished");
		return Response.ok(build.build(), MediaType.APPLICATION_JSON).build();
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
		try {
			User u = sc.validateKey(key);
			session.setAttribute("user", u);
			build.add("status", "success");
			build.add("message", warning);
			build.add("token", u.getToken());
		} catch (Exception e) {
			logger.error("/startSession "+e.getMessage());
			build.add("status", "error");
			build.add("message", (e.getMessage()!=null?e.getMessage():"Unkonw error starting session."+e.toString()));
		}
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
