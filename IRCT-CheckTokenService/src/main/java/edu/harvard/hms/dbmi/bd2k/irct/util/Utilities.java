package edu.harvard.hms.dbmi.bd2k.irct.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

public class Utilities {
	private static java.util.logging.Logger logger = java.util.logging.Logger.getGlobal();
	private static String clientSecret = "bogus_client_secret";//test_secret = 	"qwertyuiopasdfghjklzxcvbnm123456";
	private String  test_token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJiY2giLCJpYXQiOjE1MDM1ODM4NzAsImV4cCI6MTUzNTExOTg3MCwiYXVkIjoid3d3LmNoaWxkcmVucy5oYXJ2YXJkLmVkdSIsInN1YiI6ImFsZXhAY2hpbGRyZW5zLmhhcnZhcmQuZWR1IiwiZmlyc3ROYW1lIjoiQWxleCIsImxhc3ROYW1lIjoiTmlraXRpbiIsIkVtYWlsIjoiYWxleEBjaGlsZHJlbnMuaGFydmFyZC5lZHUiLCJSb2xlIjoiZGV2IiwidXNlcklkIjoiYWxleG5rdG4ifQ.IH4iUf-_oMdQ0xi2OR89adn4XQBgAI7zrUBjaRbZylU";
	private static String propPath = "/var/lib/tomcat";
	static Properties prop;
	
	static {
		prop = new Properties();
		//ClassLoader loader = Thread.currentThread().getContextClassLoader(); 
		
		
		//InputStream stream = loader.getResourceAsStream(propPath + "/irct.properties");
		try {
			FileInputStream fileInput = new FileInputStream(new File(propPath + "/irct.properties"));
			prop.load(fileInput);
			fileInput.close();
			if (prop.getProperty("client_secret") != null) {
				clientSecret = prop.getProperty("client_secret");
				System.out.println("Got clientSecret from properties: " + clientSecret);
			}
		} catch (IOException e) {
			logger.log(Level.WARNING, "Failed to load properties. Using defaults if provided.", e);
		}
		
	}
	
	public static String extractEmailFromJWT(String token, String clientSecret)
		throws IllegalArgumentException, UnsupportedEncodingException {
		logger.log(Level.FINE, "extractEmailFromJWT() with secret:"+clientSecret);
		//logger.info("getUserIdFromToken(String, String) - start");

		String userEmail = null;

		boolean isValidated = false;
		try {
			logger.log(Level.FINE, "validateAuthorizationHeader() validating with un-decoded secret.");
			Algorithm algo = Algorithm.HMAC256(clientSecret.getBytes("UTF-8"));
			JWTVerifier verifier = com.auth0.jwt.JWT.require(algo).build();
			DecodedJWT jwt = verifier.verify(token);
			isValidated = true;
			userEmail = jwt.getClaim("email").asString();

		} catch (Exception e) {
			logger.log(Level.WARNING, "extractEmailFromJWT() First validation with undecoded secret has failed. "+e.getMessage());
		}

		// If the first try, with decoding the clientSecret fails, due to whatever reason,
		// try to use a different algorithm, where the clientSecret does not get decoded
		if (!isValidated) {
			try {
				logger.log(Level.FINE, "extractEmailFromJWT() validating secret while de-coding it first.");
				Algorithm algo = Algorithm.HMAC256(Base64.decodeBase64(clientSecret.getBytes("UTF-8")));
				JWTVerifier verifier = com.auth0.jwt.JWT.require(algo).build();
				DecodedJWT jwt = verifier.verify(token);
				isValidated = true;

				userEmail = jwt.getClaim("email").asString();
			} catch (Exception e) {
				logger.log(Level.FINE, "extractEmailFromJWT() Second validation has failed as well."+e.getMessage());

				throw new NotAuthorizedException(Response.status(401)
						.entity("Could not validate with a plain, not-encoded client secret. "+e.getMessage()));
			}
		}

		if (!isValidated) {
			// If we get here, it means we could not validated the JWT token. Total failure.
			throw new NotAuthorizedException(Response.status(401)
					.entity("Could not validate the JWT token passed in."));
		}
		logger.log(Level.FINE, "extractEmailFromJWT() Finished. Returning userEmail:"+userEmail);
		return userEmail;
		//return "TheUserID";


	}
		
	/**
	 * Returns userId if token is verified
	 * @param token
	 * @param clientSecret
	 * @return
	 * @throws IllegalArgumentException
	 * @throws UnsupportedEncodingException
	 */
	public static String extractUserIdFromJWT(String token, String clientSecret)
			throws IllegalArgumentException, UnsupportedEncodingException {
		logger.log(Level.FINE, "extractUserIdFromJWT(String, String) with secret:" + clientSecret);
		//logger.info("getUserIdFromToken(String, String) - start");

		String userId = null;
		if (clientSecret == null) {
			clientSecret = Utilities.clientSecret;
		}
		boolean isValidated = false;
		try {
			logger.log(Level.FINE, "validateAuthorizationHeader() validating with un-decoded secret.");
			Algorithm algo = Algorithm.HMAC256(clientSecret.getBytes("UTF-8"));
			JWTVerifier verifier = com.auth0.jwt.JWT.require(algo).build();
			DecodedJWT jwt = verifier.verify(token);
			isValidated = true;
			userId = jwt.getClaim("userId").asString();

		} catch (Exception e) {
			logger.log(Level.WARNING, "extractEmailFromJWT() First validation with undecoded secret has failed. "+e.getMessage());
		}

		// If the first try, with decoding the clientSecret fails, due to whatever reason,
		// try to use a different algorithm, where the clientSecret does not get decoded
		if (!isValidated) {
			try {
				logger.log(Level.FINE, "extractEmailFromJWT() validating secret while de-coding it first.");
				Algorithm algo = Algorithm.HMAC256(Base64.decodeBase64(clientSecret.getBytes("UTF-8")));
				JWTVerifier verifier = com.auth0.jwt.JWT.require(algo).build();
				DecodedJWT jwt = verifier.verify(token);
				isValidated = true;

				userId = jwt.getClaim("userId").asString();
			} catch (Exception e) {
				logger.log(Level.FINE, "extractEmailFromJWT() Second validation has failed as well."+e.getMessage());

				throw new NotAuthorizedException(Response.status(401)
						.entity("Could not validate with a plain, not-encoded client secret. "+e.getMessage()));
			}
		}

		if (!isValidated) {
			// If we get here, it means we could not validated the JWT token. Total failure.
			throw new NotAuthorizedException(Response.status(401)
					.entity("Could not validate the JWT token passed in."));
		}
		logger.log(Level.FINE, "extractEmailFromJWT() Finished. Returning userEmail:" + userId);
		return userId;
	}
	
}
