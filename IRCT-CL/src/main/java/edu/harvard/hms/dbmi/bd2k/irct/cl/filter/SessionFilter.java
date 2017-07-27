/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.cl.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
//import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import edu.harvard.hms.dbmi.bd2k.irct.cl.rest.SecurityService;
import edu.harvard.hms.dbmi.bd2k.irct.controller.SecurityController;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.Token;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;

/**
 * Creates a session filter for ensuring secure access
 *
 * @author Jeremy R. Easton-Marks
 *
 */
@WebFilter(filterName = "session-filter", urlPatterns = { "/rest/*" })
public class SessionFilter implements Filter {

	Logger logger = Logger.getLogger(this.getClass().getName());

	@javax.annotation.Resource(mappedName = "java:global/client_id")
	private String clientId;
	@javax.annotation.Resource(mappedName = "java:global/client_secret")
	private String clientSecret;
	@javax.annotation.Resource(mappedName = "java:global/userField")
	private String userField;
	
	@PersistenceContext(unitName = "primary")
	EntityManager entityManager;

	@Inject
	private SecurityController sc;

	@Inject
	private SecurityService ss;
	
	@Override
	public void init(FilterConfig fliterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain fc) throws IOException, ServletException {
		logger.log(Level.FINE, "doFilter() Starting");
		HttpServletRequest request = (HttpServletRequest) req;
		
		// If processing URL /securityService/*, we are creating a session/secureSession
		if (request.getRequestURI().substring(request.getContextPath().length()).startsWith("/securityService/")) {
			// Do Nothing 
		} else {
			HttpSession session = ((HttpServletRequest) req).getSession();
			
			try {
				User user = session.getAttribute("user") == null ? 
						sc.ensureUserExists(validateAuthorizationHeader((HttpServletRequest) req)) 
						: (User)session.getAttribute("user");
				Token token = session.getAttribute("token") == null ? 
						ss.createTokenObject(req)
						: (Token)session.getAttribute("token");
				SecureSession secureSession = session.getAttribute("secureSession") == null ?
						sc.validateKey(sc.createKey(user, token))
						: (SecureSession)session.getAttribute("secureSession");
				setSessionAttributes(session, user, token, secureSession);
			} catch (Exception e) {
				String errorMessage = "{\"status\":\"error\",\"message\":\"Could not establish the user identity from request headers. "+e.getMessage()+"\"}"; 
				
				((HttpServletResponse) res).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				res.setContentType("application/json");
				res.getOutputStream()
						.write(errorMessage.getBytes());
				res.getOutputStream().close();
				return;
			}
		}
		
		logger.log(Level.FINE, "doFilter() Finished.");
		fc.doFilter(req, res);
	}

	private void setSessionAttributes(HttpSession session, User user, Token token, SecureSession secureSession) {
		session.setAttribute("user", user);
		session.setAttribute("token", token);
		session.setAttribute("secureSession", secureSession);
	}
	
	private String validateAuthorizationHeader(HttpServletRequest req) throws IllegalArgumentException, UnsupportedEncodingException, com.auth0.jwt.exceptions.SignatureVerificationException {
		logger.log(Level.FINE, "validateAuthorizationHeader() with secret:"+this.clientSecret);
		
		String tokenString = extractToken(req);
		String userEmail = null;
		
		boolean isValidated = false;
		try {
			logger.log(Level.FINE, "validateAuthorizationHeader() validating with un-decoded secret.");
			Algorithm algo = Algorithm.HMAC256(this.clientSecret.getBytes("UTF-8"));
			JWTVerifier verifier = com.auth0.jwt.JWT.require(algo).build();
			DecodedJWT jwt = verifier.verify(tokenString);
			isValidated = true;
			userEmail = jwt.getClaim("email").asString();

		} catch (Exception e) {
			logger.log(Level.WARNING, "validateAuthorizationHeader() First validation with undecoded secret has failed. "+e.getMessage());
		}
		
		// If the first try, with decoding the clientSecret fails, due to whatever reason,
		// try to use a different algorithm, where the clientSecret does not get decoded
		if (!isValidated) {
			try {
				logger.log(Level.FINE, "validateAuthorizationHeader() validating secret while de-coding it first.");
				Algorithm algo = Algorithm.HMAC256(Base64.decodeBase64(this.clientSecret.getBytes("UTF-8")));
				JWTVerifier verifier = com.auth0.jwt.JWT.require(algo).build();
				DecodedJWT jwt = verifier.verify(tokenString);
				isValidated = true;
				
				userEmail = jwt.getClaim("email").asString();
			} catch (Exception e) {
				logger.log(Level.FINE, "validateAuthorizationHeader() Second validation has failed as well."+e.getMessage());
				
				throw new NotAuthorizedException(Response.status(401)
						.entity("Could not validate with a plain, not-encoded client secret. "+e.getMessage()));
			}
		}
		
		if (!isValidated) {
			// If we get here, it means we could not validated the JWT token. Total failure.
			throw new NotAuthorizedException(Response.status(401)
					.entity("Could not validate the JWT token passed in."));
		}
		logger.log(Level.FINE, "validateAuthorizationHeader() Finished. Returning userEmail:"+userEmail);
		return userEmail;
	}

	private String extractToken(HttpServletRequest req) {
		logger.log(Level.FINE, "extractToken() Starting");
		String token = null;

		String authorizationHeader = ((HttpServletRequest) req).getHeader("Authorization");

		if (authorizationHeader != null) {
			logger.log(Level.FINE, "extractToken() header:" + authorizationHeader);

			String[] parts = authorizationHeader.split(" ");

			if (parts.length != 2) {
				throw new NotAuthorizedException(Response.status(401)
						.entity("Invalid formatting of ```Authorization``` header. Only Bearer type header accepted."));
			}
			logger.log(Level.FINE, "extractToken() " + parts[0] + "/" + parts[1]);

			String scheme = parts[0];
			String credentials = parts[1];

			Pattern pattern = Pattern.compile("^Bearer$", Pattern.CASE_INSENSITIVE);
			if (pattern.matcher(scheme).matches()) {
				token = credentials;
			}
			logger.log(Level.FINE, "extractToken() token:" + token);
		} else {
			throw new NotAuthorizedException(Response.status(401)
					.entity("No Authorization header found and no current SecureSession exists for the user."));
		}
		logger.log(Level.FINE, "extractToken() Finished. Token:" + token);
		return token;
	}

	@Override
	public void destroy() {

	}

}
