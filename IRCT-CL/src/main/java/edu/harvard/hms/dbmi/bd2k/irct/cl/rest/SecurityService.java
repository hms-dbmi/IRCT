/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.cl.rest;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.enterprise.context.SessionScoped;
import javax.faces.bean.ManagedBean;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonStructure;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.Resty;
import static us.monoid.web.Resty.content;

import com.auth0.NonceGenerator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import edu.harvard.hms.dbmi.bd2k.irct.controller.SecurityController;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.JWT;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.Token;

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

	private final NonceGenerator nonceGenerator = new NonceGenerator();
	private String state;

	@javax.annotation.Resource(mappedName ="java:global/redirect_on_success")
	private String redirectOnSuccess;

	@javax.annotation.Resource(mappedName ="java:global/domain")
	private String domain;

	@javax.annotation.Resource(mappedName ="java:global/client_id")
	private String clientId;

	@javax.annotation.Resource(mappedName ="java:global/client_secret")
	private String clientSecret;
	private User user;
	private Token token;

	/**
	 * Creates the initial security service
	 */
	public SecurityService() {
		this.state = null;

	}

	/**
	 * Creates a state and keeps it associated with the users http session
	 *
	 * @return A JSON Object containing the state information
	 */
	@GET
	@Path("/createState")
	@Deprecated
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure createState() {
		JsonObjectBuilder build = Json.createObjectBuilder();

		String nonce = nonceGenerator.generateNonce();

		build.add("state", nonce);

		this.state = nonce;

		return build.build();
	}

	/**
	 * Receives the callback from the authentication provider and validates it
	 *
	 * @param code
	 *            Identity code
	 * @param state
	 *            State code
	 * @param error
	 *            Error message
	 * @return A redirect, or error message
	 */
	@GET
	@Path("/callback")
	@Deprecated
	@Produces(MediaType.APPLICATION_JSON)
	public Response callback(@QueryParam(value = "code") String code,
			@QueryParam(value = "state") String state,
			@QueryParam(value = "error") String error) {

		JsonObjectBuilder build = Json.createObjectBuilder();
		URI redirect = null;

		// An Error occurred or is not valid
		if (error != null || state == null || !state.equals(this.state)
				|| code == null) {
			build.add("status", "Invalid callback");
			build.add(
					"message",
					"An error occurred during the callback to the IRCT server from the identity provider.");
			return Response.status(400).entity(build.build()).build();
		}

		try {
			// Fetch Tokens
			this.token = fetchToken(code);

			// Fetch User
			this.user = fetchUser(((JWT) token).getAccess());

			// Associate the session with that user
			session.setAttribute("user", this.user);
			session.setAttribute("token", this.token);

			// If everything is successful
			redirect = this.uriInfo.getAbsolutePath().resolve(
					this.redirectOnSuccess);

		} catch (JSONException e) {
			build.add("status", "Internal Error");
			build.add(
					"message",
					"An error occurred during the callback to the IRCT server from the identity provider.");
			return Response.status(500).entity(build.build()).build();
		} catch (IOException e) {
			build.add("status", "Internal Error");
			build.add(
					"message",
					"An error occurred during the callback to the IRCT server from the identity provider.");
			return Response.status(500).entity(build.build()).build();
		}

		return Response.seeOther(redirect).build();
	}

	private Token fetchToken(String authorizationCode) throws JSONException,
			IOException {
		Resty resty = new Resty();

		JSONObject json = new JSONObject();

		json.put("client_id", this.clientId);
		json.put("client_secret", this.clientSecret);

		json.put("redirect_uri", uriInfo.getAbsolutePath().toString());
		json.put("grant_type", "authorization_code");
		json.put("code", authorizationCode);

		JSONObject tokenInfo = resty.json(String.format("https://%s%s",
				domain, "/userinfo"), content(json)).toObject();

		return new JWT((String) tokenInfo.get("id_token"),
				(String) tokenInfo.get("access_token"),
				(String) tokenInfo.get("token_type"), this.clientId);
	}

	private User fetchUser(String accessToken) throws IOException,
			JSONException {
		Resty resty = new Resty();

		JSONObject userInfo = resty.json(String.format("https://%s%s",
				domain, "/userinfo?access_token=") + accessToken).toObject();

		String userEmail = userInfo.getString("email");

		return sc.ensureUserExists(userEmail);
	}

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

//		if((this.user == null) && (session.getAttribute("user") != null)) {
//			this.user = (User) session.getAttribute("user");
//			this.token = (Token) session.getAttribute("token");
//		}

		
		User userObject;
		try {
			userObject = sc.ensureUserExists(extractEmailFromJWT(req));
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			build.add("status", "fail");
			build.add("message", "JWT token is not a token.");
			return Response.status(Response.Status.FORBIDDEN)
					.entity(build.build()).build();
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			build.add("status", "fail");
			build.add("message", "Invalid encoding for JWT token handling");
			return Response.status(Response.Status.FORBIDDEN)
					.entity(build.build()).build();
			
		}
		
		// TODO : This is evil, we should not even know how to create a valid tranSMART token.
		Token tokenObject = createTokenObject(req);
		
		String key = sc.createKey(userObject, tokenObject);
		// IF USER IS LOGGED IN
		if (key != null) {
			build.add("key", key);
		} else {
			build.add("status", "fail");
			build.add("message", "Unable to generate key for user:"+this.user+" and token:"+this.token);
			return Response.status(Response.Status.FORBIDDEN)
					.entity(build.build()).build();
		}

		return Response.ok(build.build(), MediaType.APPLICATION_JSON).build();
	}

	public Token createTokenObject(ServletRequest req) {
		Token tokenObject = new JWT(((HttpServletRequest)req).getHeader("Authorization"), "", "Bearer",
				this.clientId);
		return tokenObject;
	}

	private String extractEmailFromJWT(HttpServletRequest req) 
			throws IllegalArgumentException, UnsupportedEncodingException {
		Algorithm algo = Algorithm.HMAC256(this.clientSecret);
		JWTVerifier verifier = com.auth0.jwt.JWT.require(algo).build();
		DecodedJWT jwt = verifier.verify(extractToken(req));
		logger.log(Level.FINE, jwt.toString());

		// OK, we can trust this JWT
		logger.log(Level.INFO, "validateAuthorizationHeader() Token trusted)");
		return jwt.getClaim("email").asString();
	}

	private String extractToken(HttpServletRequest req) {
		logger.log(Level.FINE, "extractToken() Starting");
		String token = null;
		
		String authorizationHeader = ((HttpServletRequest) req).getHeader("Authorization");

		if (authorizationHeader != null) {
			logger.log(Level.FINE, "extractToken() header:" + authorizationHeader);
			try {

				String[] parts = authorizationHeader.split(" ");

				if (parts.length != 2) {
					return null;
				}
				logger.log(Level.INFO, "extractToken() "+parts[0] + "/" + parts[1]);

				String scheme = parts[0];
				String credentials = parts[1];

				Pattern pattern = Pattern.compile("^Bearer$", Pattern.CASE_INSENSITIVE);
				if (pattern.matcher(scheme).matches()) {
					token = credentials;
				}
				logger.log(Level.FINE, "extractToken() token:" + token);

			} catch (Exception e) {
				// e.printStackTrace();
				logger.log(Level.SEVERE,
						"extractToken() token validation failed: " + e + "/" + e.getMessage());
			}
		} else {
			throw new NotAuthorizedException(Response.status(401).entity("No Authorization header found and no current SecureSession exists for the user."));
		}
		logger.log(Level.SEVERE, "extractToken() Finished (null returned)");
		
		return token;
	}
	
	/**
	 * Starts a session if presented with a valid key
	 *
	 * @param key
	 *            A session key
	 * @return A status message stating if a new session has started
	 */
	@GET
	@Path("/startSession")
	@Produces(MediaType.APPLICATION_JSON)
	public Response startSession(@QueryParam(value = "key") String key) {
		JsonObjectBuilder build = Json.createObjectBuilder();

		// Validate the key
		SecureSession ss = sc.validateKey(key);
		if (ss == null) {
			build.add("status", "failed");
			build.add("message", "Unable to start session");

			return Response.status(Response.Status.FORBIDDEN)
					.entity(build.build()).build();
		}

		this.user = ss.getUser();
		this.token = ss.getToken();

		// Associate the session with that user
		session.setAttribute("user", this.user);
		session.setAttribute("token", this.token);
		session.setAttribute("secureSession", ss);
		build.add("status", "success");
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
		this.user = null;
		this.token = null;
		session.removeAttribute("user");
		session.removeAttribute("token");
		session.removeAttribute("secureSession");
		build.add("status", "success");
		session.invalidate();
		return Response.ok(build.build(), MediaType.APPLICATION_JSON).build();
	}
}
