/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.cl.util;

import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A collection of static methods that provide shared functionality throughout
 * the IRCT-CL
 */
public class Utilities {

	private static Logger logger = Logger.getLogger(Utilities.class);

	/**
	 * Returns all the first values from a MultiValue Map
	 * 
	 * @param multiValueMap
	 *            MultiValue Map
	 * @return First Values Map
	 */
	public static Map<String, String> getFirstFromMultiMap(
			MultivaluedMap<String, String> multiValueMap) {
		return getFirstFromMultiMap(multiValueMap, null);
	}

	/**
	 * Returns all the first values from a MultiValue Map if the key starts with
	 * a given string. If it is a match then the string is removed from the
	 * beginning of the key.
	 * 
	 * @param multiValueMap
	 *            MultiValue Map
	 * @param startsWith
	 *            Starts with
	 * @return First Values Map
	 */
	public static Map<String, String> getFirstFromMultiMap(
			MultivaluedMap<String, String> multiValueMap, String startsWith) {

		Map<String, String> firstMap = new HashMap<String, String>();

		for (String key : multiValueMap.keySet()) {
			if (startsWith == null) {
				firstMap.put(key, multiValueMap.getFirst(key));
			} else if (key.startsWith(startsWith)) {
				firstMap.put(key.substring(startsWith.length()),
						multiValueMap.getFirst(key));
			}
		}

		return firstMap;
	}

	/**
	 * extract specific user field from JWT token
	 * @param req
	 * @param clientSecret
	 * @param userField specifies which user field is going to be extracted from JWT token
	 * @return
	 * @throws NotAuthorizedException
	 */
	public static String extractEmailFromJWT(HttpServletRequest req, String clientSecret, String userField) {
		logger.debug("extractEmailFromJWT() with secret:"+clientSecret);

		//No point in doing anything if there's no userField
        if (userField == null){
            logger.error("extractEmailFromJWT() No userField set for determining JWT claim");
            throw new NotAuthorizedException("extractEmailFromJWT() application error: userField is null");
        }
		
		String tokenString = extractToken(req);
        if (StringUtils.isBlank(tokenString)){
			throw new NotAuthorizedException("token string is null or empty");
		}


		String userSubject = null;
		
		DecodedJWT jwt = null;
		try {
			logger.debug("validateAuthorizationHeader() validating with un-decoded secret.");
			jwt = com.auth0.jwt.JWT.require(Algorithm
					.HMAC256(clientSecret
							.getBytes("UTF-8")))
					.build()
					.verify(tokenString);
		} catch (UnsupportedEncodingException e){
			logger.error("extractEmailFromJWT() getting bytes for initialize jwt token algorithm error: " + e.getMessage());
			throw new NotAuthorizedException("Token is invalid, please request a new one");
		} catch (JWTVerificationException e) {
			try{
				jwt = com.auth0.jwt.JWT.require(Algorithm
						.HMAC256(Base64.decodeBase64(clientSecret
								.getBytes("UTF-8"))))
						.build()
						.verify(tokenString);
			} catch (UnsupportedEncodingException ex){
				logger.error("extractEmailFromJWT() getting bytes for initialize jwt token algorithm error: " + e.getMessage());
				throw new NotAuthorizedException("Token is invalid, please request a new one");
			} catch (JWTVerificationException ex) {
				logger.error("extractEmailFromJWT() token is invalid after tried with another algorithm: " + e.getMessage());
				throw new NotAuthorizedException("Token is invalid, please request a new one");
			}
		}

		logger.debug("extractEmailFromJWT() validation is successful.");
		
		if (jwt != null) {
			// Just in case someone cares, this will list all the claims that are 
			// attached to the incoming JWT.
			if (logger.isDebugEnabled()){
				Map<String, Claim> claims = jwt.getClaims();
				for (Map.Entry entry : claims.entrySet()){
					logger.debug("extractEmailFromJWT() claim: "+entry.getKey()+"="+entry.getValue());
				}
			}

            userSubject = jwt.getClaim(userField).asString();

			if (userSubject == null) {
                logger.error("extractEmailFromJWT() No " + userField + " claim found");
                throw new NotAuthorizedException("Token is invalid, please request a new one with " +
						"userField " + userField + " included" );
			}
		}
		
		logger.debug("extractEmailFromJWT() Finished. Returning " + userField +
				": "+userSubject);
		return userSubject;
	}
	
	// TODO This is silly, but for backward compatibility
	public static String extractHeaderValue(HttpServletRequest req, String headerType) {
		return Utilities.extractToken(req);		
	}

	/**
	 * extract token from http request
	 * @param req
	 * @return
	 */
	public static String extractToken(HttpServletRequest req) {
		logger.debug("extractToken() Starting");
		String token = null;
		
		String authorizationHeader = req.getHeader("Authorization");
		if (authorizationHeader != null) {
			logger.debug("extractToken() header:" + authorizationHeader);

			String[] parts = authorizationHeader.split(" ");

			if (parts.length != 2) {
				throw new NotAuthorizedException("token structure is incorrect, expecting: \'scheme_string token_string\'");
			}

			String scheme = parts[0];
			String credentials = parts[1];


			// if in the future, we need to handle multiple token types,
			// several ways to choose:
			// use regular expression: if (schema.matches("typeA|typeB|typeC")){...}
			// or use a HashSet<String> to pre-store all the possible values
			if (StringUtils.isBlank(scheme) || !scheme.equalsIgnoreCase("Bearer")){
				throw new NotAuthorizedException("token scheme is not specified or not supported.");
			} else {
				token = credentials;
			}

			logger.debug("extractToken() token:" + token);

		} else {
			throw new NotAuthorizedException("No Authorization header found in request.");
		}
		logger.debug("extractToken() Finished.");
		return token;
	}


	/**
	 *
	 * @param req ServletRequest that contains the token
	 * @param userField Name of key to extract from token endpoint result
	 * @param token_introspection_url Url to call for token introspection
	 * @param token_introspection_token Token to validate into endpoint
	 * @return user id
	 * @throws IOException
	 */
	public static String extractUserFromTokenIntrospection(HttpServletRequest req, String userField, String token_introspection_url, String token_introspection_token)
			throws IOException {
		ObjectMapper json = new ObjectMapper();
		HttpClient client = HttpClients.createDefault();
		HttpPost post = new HttpPost(token_introspection_url);
		String token = extractToken(req);
		Map<String, String> tokenMap = new HashMap<String, String>();
		tokenMap.put("token", token);
		post.setEntity(new StringEntity(json.writeValueAsString(tokenMap)));
		post.setHeader("Content-Type", "application/json");
		//Authorize into the token introspection endpoint
		post.setHeader("Authorization", "Bearer " + token_introspection_token);
		HttpResponse response = client.execute(post);
		if (response.getStatusLine().getStatusCode() != 200){
			throw new RuntimeException("Server Error");
		}
		JsonNode responseMessage = json.readTree(response.getEntity().getContent());
		if (!responseMessage.get("active").asBoolean()){
			throw new NotAuthorizedException("Token invalid or expired");
		}

		return responseMessage.get(userField) != null ? responseMessage.get(userField).asText() : null;
	}
}
