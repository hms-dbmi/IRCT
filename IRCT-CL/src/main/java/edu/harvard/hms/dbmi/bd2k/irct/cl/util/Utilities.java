/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.cl.util;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

/**
 * A collection of static methods that provide shared functionality throughout
 * the IRCT-UI
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
	
	public static String extractEmailFromJWT(HttpServletRequest req, String clientSecret, String userField)
			throws IllegalArgumentException, UnsupportedEncodingException {
		logger.debug("extractEmailFromJWT() with secret:"+clientSecret);

		//No point in doing anything if there's no userField
        if (userField == null){
            logger.error("extractEmailFromJWT() No userField set for determining JWT claim");
            return null;
        }
		
		String tokenString = extractToken(req);
		String userEmail = null;
		
		DecodedJWT jwt = null;
		boolean isValidated = false;
		try {
			logger.debug("validateAuthorizationHeader() validating with un-decoded secret.");
			Algorithm algo = Algorithm.HMAC256(clientSecret.getBytes("UTF-8"));
			JWTVerifier verifier = com.auth0.jwt.JWT.require(algo).build();
			jwt = verifier.verify(tokenString);
			isValidated = true;
		} catch (Exception e) {
			logger.warn("extractEmailFromJWT() First validation with undecoded secret has failed. "+e.getMessage());
		}
		
		// If the first try, with decoding the clientSecret fails, due to whatever reason,
		// try to use a different algorithm, where the clientSecret does not get decoded
		if (!isValidated) {
			try {
				logger.debug("extractEmailFromJWT() validating with de-coded secret.");
				Algorithm algo = Algorithm.HMAC256(Base64.decodeBase64(clientSecret.getBytes("UTF-8")));
				JWTVerifier verifier = com.auth0.jwt.JWT.require(algo).build();
				jwt = verifier.verify(tokenString);
				isValidated = true;
				logger.debug("extractEmailFromJWT() validation is successful.");
			} catch (Exception e) {
				logger.debug("extractEmailFromJWT() Second validation has failed as well."+e.getMessage());
				throw new RuntimeException("Could not validate with a plain, not-encoded client secret. "+e.getMessage());
			}
		}
		
		if (!isValidated) {
			// If we get here, it means we could not validated the JWT token. Total failure.
			throw new NotAuthorizedException(Response.status(401)
					.entity("Could not validate the JWT token passed in."));
		}
		
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
		
		logger.debug("extractEmailFromJWT() Finished. Returning userEmail:"+userEmail);
		return userEmail;

	}
	
	// TODO This is silly, but for backward compatibility
	public static String extractHeaderValue(HttpServletRequest req, String headerType) {
		return Utilities.extractToken(req);		
	}

	public static String extractToken(HttpServletRequest req) {
		logger.debug("extractToken() Starting");
		String token = null;
		
		String authorizationHeader = ((HttpServletRequest) req).getHeader("Authorization");
		if (authorizationHeader != null) {
			logger.debug("extractToken() header:" + authorizationHeader);
			try {

				String[] parts = authorizationHeader.split(" ");

				if (parts.length != 2) {
					return null;
				}

				String scheme = parts[0];
				String credentials = parts[1];

				Pattern pattern = Pattern.compile("^Bearer$", Pattern.CASE_INSENSITIVE);
				if (pattern.matcher(scheme).matches()) {
					token = credentials;
				}
				logger.debug("extractToken() token:" + token);

			} catch (Exception e) {
				logger.error("extractToken() token validation failed: " + e + "/" + e.getMessage());
			}
		} else {
			throw new NotAuthorizedException(Response.status(401).entity("No Authorization header found in request."));
		}
		logger.debug("extractToken() Finished.");
		return token;
	}
}
