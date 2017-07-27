package edu.harvard.hms.dbmi.bd2k.irct.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import edu.harvard.hms.dbmi.bd2k.irct.model.security.JWT;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

/**
 * The Security Utility class provides a set of useful functions for handling
 * security functions.
 *
 * @author Jeremy R. Easton-Marks
 *
 */
public class SecurityUtility {

	/**
	 * Returns a delegated a token for a user for a new Auth0 application. If an
	 * error occurs then NULL is returned.
	 *
	 * @param namespace
	 *            URL of the namespace
	 * @param resourceClientId
	 *            New client id
	 * @param session
	 *            Session of the user
	 * @return New Delegated token
	 */
	@Deprecated
	public static String delegateToken(String namespace,
			String resourceClientId, SecureSession session) {

		if (session.getToken() != null) {
			java.util.logging.Logger.getGlobal().log(Level.FINE, "delegateToken() returning token stored in the session:"+session.getToken().toString());
			return session.getToken().toString();
		} else {
			java.util.logging.Logger.getGlobal().log(Level.SEVERE, "delegateToken() session DOES NOT CONTAINT A TOKEN!");
		}

		java.util.logging.Logger.getGlobal().log(Level.FINE, "delegateToken() namespace:"+namespace+" resource.client.id:"+resourceClientId+" session.id:"+session.getId());

		if (((JWT) session.getToken()).getClientId().equals(resourceClientId)) {
			java.util.logging.Logger.getGlobal().log(Level.FINE, "delegateToken() session token matches the specified resourceClientId");
			return session.getToken().toString();
		} else {
			java.util.logging.Logger.getGlobal().log(Level.FINE, "delegateToken() session token DOES NOT MATCH the specified resourceClientId");
		}

		if (session.getDelegated().containsKey(resourceClientId)) {
			java.util.logging.Logger.getGlobal().log(Level.FINE, "delegateToken() returning a Bearer token");
			return "Bearer "
					+ session.getDelegated().get(resourceClientId).toString();
		} else {
			java.util.logging.Logger.getGlobal().log(Level.FINE, "delegateToken() session does NOT have a delegated token.");
		}

		try {
			HttpClient client = HttpClientBuilder.create().build();
			HttpPost post = new HttpPost("https://" + namespace + "/delegation");
			java.util.logging.Logger.getGlobal().log(Level.FINE, "delegateToken() call to ```https://"+namespace+"/delegation``` URL.");

			List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
			urlParameters.add(new BasicNameValuePair("grant_type",
					"urn:ietf:params:oauth:grant-type:jwt-bearer"));
			urlParameters
					.add(new BasicNameValuePair("target", resourceClientId));

			//
			urlParameters.add(new BasicNameValuePair("client_id",
					((JWT) session.getToken()).getClientId()));
			urlParameters.add(new BasicNameValuePair("scope",
					"openid name email"));
			urlParameters.add(new BasicNameValuePair("api_type", "app"));
			urlParameters.add(new BasicNameValuePair("id_token", session
					.getToken().toString().split(" ")[1]));

			post.setEntity(new UrlEncodedFormEntity(urlParameters));

			HttpResponse response = client.execute(post);
			JsonReader reader = Json.createReader(response.getEntity()
					.getContent());

			JsonObject responseObject = reader.readObject();

			if(responseObject.containsKey("error")) {
				java.util.logging.Logger.getGlobal().log(Level.FINE, "delegateToken() Could not get delegated token. "+responseObject.toString());
				java.util.logging.Logger.getGlobal().log(Level.FINE, "delegateToken() returning NULL, after error response from delegation authority");
				return null;
			}

			JWT jwt = new JWT();
			jwt.setType(responseObject.getString("token_type"));
			jwt.setIdToken(responseObject.getString("id_token"));
			session.getDelegated().put(resourceClientId, jwt);
			java.util.logging.Logger.getGlobal().log(Level.FINE, "delegateToken() returning new delegation token:"+responseObject.getString("id_token"));
			return "Bearer " + responseObject.getString("id_token");

		} catch (IOException e) {
			java.util.logging.Logger.getGlobal().log(Level.SEVERE, "delegateToken() Exception:"+e.getMessage());
			e.printStackTrace();
		}

		java.util.logging.Logger.getGlobal().log(Level.FINE, "delegateToken() returning NULL, after exception.");
		return null;
	}
}
