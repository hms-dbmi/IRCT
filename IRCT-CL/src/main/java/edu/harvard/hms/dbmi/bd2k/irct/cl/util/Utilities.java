/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.cl.util;

import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.MultivaluedMap;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

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
            return null;
        }
		
		String tokenString = extractToken(req);
        if (StringUtils.isBlank(tokenString)){
			throw new NotAuthorizedException("token string is null or empty");
		}


		String userEmail = null;
		
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
		} catch (JWTVerificationException e) {
			try{
				jwt = com.auth0.jwt.JWT.require(Algorithm
						.HMAC256(Base64.decodeBase64(clientSecret
								.getBytes("UTF-8"))))
						.build()
						.verify(tokenString);
			} catch (UnsupportedEncodingException ex){
				logger.error("extractEmailFromJWT() getting bytes for initialize jwt token algorithm error: " + e.getMessage());
			} catch (JWTVerificationException ex) {
				logger.error("extractEmailFromJWT() token is invalid after tried with another algorithm: " + e.getMessage());
				throw new NotAuthorizedException("Token is invalid, please request a new one");
			}
		}

		logger.debug("extractEmailFromJWT() validation is successful.");
		
		if (jwt != null) {
			// Just in case someone cares, this will list all the claims that are 
			// attached to the incoming JWT.
			Map<String, Claim> claims = jwt.getClaims();
			for (String s: claims.keySet()) {
				Claim myClaim = claims.get(s);
				logger.debug("extractEmailFromJWT() claim: "+s+"="+myClaim.asString());
			}

            userEmail = jwt.getClaim(userField).asString();

			if (userEmail == null) {
                logger.error("extractEmailFromJWT() No " + userField + " claim found");
			}
		}
		
		logger.debug("extractEmailFromJWT() Finished. Returning userEmail: "+userEmail);
		return userEmail;

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
	public static String extractToken(HttpServletRequest req)
			throws NotAuthorizedException{
		logger.debug("extractToken() Starting");
		String token = null;
		
		String authorizationHeader = req.getHeader("Authorization");
		if (authorizationHeader != null) {
			logger.debug("extractToken() header:" + authorizationHeader);

			String[] parts = authorizationHeader.split(" ");

			if (parts.length != 2) {
				throw new NotAuthorizedException("token structure is incorrect, expecting: \"scheme_string token_string\"");
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
}
