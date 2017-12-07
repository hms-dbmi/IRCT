package edu.harvard.hms.dbmi.bd2k.irct.cl.rest;

import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonStructure;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.auth0.jwt.interfaces.Claim;

import edu.harvard.hms.dbmi.bd2k.irct.IRCTApplication;
import edu.harvard.hms.dbmi.bd2k.irct.cl.util.Utilities;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;

/**
 * Creates a REST interface.
 */
@Path("/")
@RequestScoped
public class Endpoints {
	
	@Inject
	private IRCTApplication picsure;

	@Inject
	private HttpSession session;

	/**
	 * Returns a JSON Array of application settings
	 *
	 * @return JSON Array of settings (key/value pair)
	 */
	@GET
	@Path("/about")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure about() {
		try {
			IRCTApplication app = new IRCTApplication();
			User user = (User) session.getAttribute("user");
			// Get claims out of the token
			Map<String, Claim> token_claims = Utilities.getClaims(user.getToken(), picsure.getClientSecret());
			JsonObjectBuilder claimsObject = Json.createObjectBuilder();
			for (String key : token_claims.keySet()) {
				Claim claim = token_claims.get(key);
				claimsObject.add(key, (String) (claim.asString()==null?claim.asDate().toString():claim.asString()));
			}
			
			return Json.createObjectBuilder()
				.add("appVersion", app.getVersion())
				.add("userId", user.getUserId())
				.add("userName", user.getName())
				.add("userClaims", claimsObject.build())
				.build();

		} catch (Exception e) {
			return Json.createObjectBuilder()
				.add("status", "error")
				.add("message", e.getMessage())
				.build();
		}
	}
}
