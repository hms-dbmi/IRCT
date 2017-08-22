/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.cl.rest;

import java.io.UnsupportedEncodingException;
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
import javax.ws.rs.core.MediaType;

import org.apache.commons.codec.binary.Base64;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import edu.harvard.hms.dbmi.bd2k.irct.IRCTApplication;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.Token;
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
	 * Returns a JSON Array of supported Data Types by the IRCT core.
	 *
	 * @return JSON Array of data types
	 */
	@GET
	@Path("/error")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure error() {
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
		log.log(Level.FINE, "/about Starting...");

		JsonArrayBuilder build = Json.createArrayBuilder();
		IRCTApplication app = new IRCTApplication();
		// Add user details
		User user = (User) session.getAttribute("user");
		JsonObjectBuilder userDetails = Json.createObjectBuilder()
				.add("userid", user.getUserId())
				.add("name", user.getName());
		
		build.add(Json.createObjectBuilder().add("user", userDetails));
		
		Token token = (Token) session.getAttribute("token");
		SecureSession secureSession = (SecureSession) session.getAttribute("secureSession");
		
		
		log.log(Level.FINE, "/about Verifying token...");
		Algorithm algo;
		try {
			algo = Algorithm.HMAC256(Base64.decodeBase64("qLl_-GaA9LfavbgmjkRSZdVrDYc9U3m8hrZwE8iKtZuiqGVFWAI4fBTKwnDkFmcE".getBytes("UTF-8")));
			String tokenString = secureSession.getToken().toString().split(" ")[1];
			JWTVerifier verifier = com.auth0.jwt.JWT.require(algo).build();
			DecodedJWT jwt = verifier.verify(tokenString);
			
			for(String claimKey: jwt.getClaims().keySet()) {
				System.out.println(")))))))))))))))");
				System.out.println("extractEmailFromJWT() ```claim``` :"+claimKey);
			}
		} catch (IllegalArgumentException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			log.log(Level.SEVERE, "/about Throwing EXCEPTION while verifying token. ```"+e.getMessage()+"```");
		}
		
		JsonObjectBuilder root = Json.createObjectBuilder()
				.add("application", Json.createObjectBuilder()
						.add("version", app.getVersion()))
				.add("session", Json.createObjectBuilder()
						.add("secureSession", Json.createObjectBuilder()
								.add("access_key", secureSession.getAccessKey().toString())
								.add("token", secureSession.getToken().toString())
								.add("user", Json.createObjectBuilder()
										.add("userid", secureSession.getUser().getUserId())
										.add("name", secureSession.getUser().getName())))
						.add("user", Json.createObjectBuilder()
								.add("name", user.getName())
								.add("userId", user.getUserId())
								.add("id", user.getId()))
						.add("token", Json.createObjectBuilder()
								.add("id", (token==null?"NULL":(token.getId()==null?"null":token.getId()).toString())))
								);
		log.log(Level.FINE, "/about Finished");
		return root.build();
	}
}
