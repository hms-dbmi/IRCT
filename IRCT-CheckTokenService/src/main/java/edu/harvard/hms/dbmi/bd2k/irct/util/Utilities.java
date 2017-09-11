package edu.harvard.hms.dbmi.bd2k.irct.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

public class Utilities {
	private static Logger logger = Logger.getLogger(Utilities.class);
	
	private static String clientSecret;
	private static String propPath = "/var/lib/tomcat";
	static Properties prop;
	
	static {
		prop = new Properties();
		
		try {
			FileInputStream fileInput = new FileInputStream(new File(propPath + "/irct.properties"));
			prop.load(fileInput);
			fileInput.close();
			
			if (prop.getProperty("client_secret") != null && !prop.getProperty("client_secret").isEmpty()) {
				clientSecret = prop.getProperty("client_secret");
			}
			else {
				logger.error("Could not get client_secret from properties file. Check configuration.");
				throw new RuntimeException("Internal server Error. Contact administrator.");
			}
			 
		} catch (Exception e) {
			logger.error("Failed to load properties.", e);
			throw new RuntimeException("Internal server Error. Contact administrator.");
		}
		
	}
	
	public static String extractEmailFromJWT(String token, String clientSecret)
		throws IllegalArgumentException, UnsupportedEncodingException {
		logger.info("extractEmailFromJWT() with secret:"+clientSecret);

		String userEmail = null;
		if (clientSecret == null) {
			clientSecret = Utilities.clientSecret;
		}
		boolean isValidated = false;
		try {
			logger.info("validateAuthorizationHeader() validating with un-decoded secret.");
			Algorithm algo = Algorithm.HMAC256(clientSecret.getBytes("UTF-8"));
			JWTVerifier verifier = com.auth0.jwt.JWT.require(algo).build();
			DecodedJWT jwt = verifier.verify(token);
			isValidated = true;
			userEmail = jwt.getClaim("email").asString();

		} catch (Exception e) {
			logger.error("extractEmailFromJWT() First validation with undecoded secret has failed. "+e.getMessage());
		}

		// If the first try, with decoding the clientSecret fails, due to whatever reason,
		// try to use a different algorithm, where the clientSecret does not get decoded
		if (!isValidated) {
			try {
				logger.info("extractEmailFromJWT() validating secret while de-coding it first.");
				Algorithm algo = Algorithm.HMAC256(Base64.decodeBase64(clientSecret.getBytes("UTF-8")));
				JWTVerifier verifier = com.auth0.jwt.JWT.require(algo).build();
				DecodedJWT jwt = verifier.verify(token);
				isValidated = true;
				userEmail = jwt.getClaim("email").asString();
			} catch (Exception e) {
				logger.error("extractEmailFromJWT() Second validation has failed as well."+e.getMessage(), e);
				throw new NotAuthorizedException(Response.status(401)
						.entity("Could not validate with a plain, not-encoded client secret. "+e.getMessage()));
			}
		}

		if (!isValidated) {
			// If we get here, it means we could not validated the JWT token. Total failure.
			throw new NotAuthorizedException(Response.status(401)
					.entity("Could not validate the JWT token passed in."));
		}
		logger.info("extractEmailFromJWT() Finished. Returning userEmail:"+userEmail);
		return userEmail;

	}
	
}
