/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.cl.filter;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.inject.Inject;
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

import edu.harvard.hms.dbmi.bd2k.irct.controller.SecurityController;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.JWT;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.Token;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;

/**
 * Creates a session filter for ensuring secure access
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@WebFilter(filterName = "session-filter", urlPatterns = { "/*" })
public class SessionFilter implements Filter {

	Logger logger = Logger.getLogger(this.getClass().getName());

	@javax.annotation.Resource(mappedName = "java:global/client_id")
	private String clientId;
	@javax.annotation.Resource(mappedName = "java:global/client_secret")
	private String clientSecret;
	@javax.annotation.Resource(mappedName = "java:global/userField")
	private String userField;

	@Inject
	private SecurityController sc;

	@Override
	public void init(FilterConfig fliterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain fc) throws IOException, ServletException {
		logger.log(Level.FINE, "doFilter() Starting");
		HttpServletRequest request = (HttpServletRequest) req;

		logger.log(Level.INFO, "doFilter() requestURI:"+request.getRequestURI());
		// Calls to the Security Service can go straight through
		if (!request.getRequestURI().substring(request.getContextPath().length()).startsWith("/securityService/")) {
			// Get the session and user information
			HttpSession session = ((HttpServletRequest) req).getSession();
			User user = (User) session.getAttribute("user");

			// Is a user already associated with a session?
			if (user == null) {

				// If no user is associated then validate the authorization
				// header
				String email = validateAuthorizationHeader((HttpServletRequest) req);

				if (email == null) {
					((HttpServletResponse) res).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					res.getOutputStream().write("{\"message\":\"Session is not authorized\"}".getBytes());
					res.getOutputStream().close();
					return;
				}

				user = sc.getUser(email);
				Token token = new JWT(((HttpServletRequest) req).getHeader("Authorization"), "", "Bearer",
						this.clientId);
				SecureSession secureSession = new SecureSession();
				secureSession.setToken(token);
				secureSession.setUser(user);

				session.setAttribute("user", user);
				session.setAttribute("token", token);
				session.setAttribute("secureSession", secureSession);

			}
		} else {
			String name = validateAuthorizationHeader((HttpServletRequest) req);

			if (name != null) {
				HttpSession session = ((HttpServletRequest) req).getSession();

				User user = sc.getUser(name);
				Token token = new JWT(((HttpServletRequest) req).getHeader("Authorization"), "", "Bearer",
						this.clientId);
				SecureSession secureSession = new SecureSession();
				secureSession.setToken(token);
				secureSession.setUser(user);

				session.setAttribute("user", user);
				session.setAttribute("token", token);
				session.setAttribute("secureSession", secureSession);
			} else {
				throw new RuntimeException("Error establising identity from request headers.");
			}

		}
		logger.log(Level.FINE, "doFilter() Finished");
		fc.doFilter(req, res);
	}

	private String validateAuthorizationHeader(HttpServletRequest req) {
		logger.log(Level.FINE, "validateAuthorizationHeader() Starting");
		String authorizationHeader = ((HttpServletRequest) req).getHeader("Authorization");
		if (authorizationHeader != null) {
			logger.log(Level.FINE, "validateAuthorizationHeader() header:" + authorizationHeader);
			try {

				String[] parts = authorizationHeader.split(" ");
				Logger.getGlobal().log(Level.INFO, parts[0] + "/" + parts[1]);

				if (parts.length != 2) {
					return null;
				}

				String scheme = parts[0];
				String credentials = parts[1];
				String token = "";

				Pattern pattern = Pattern.compile("^Bearer$", Pattern.CASE_INSENSITIVE);

				if (pattern.matcher(scheme).matches()) {
					token = credentials;
				}
				System.err.println("validateAuthorizationHeader() token:" + token);

				try {

					Jwt<io.jsonwebtoken.Header, Claims> claims = Jwts.parser().setSigningKey(this.clientSecret)
							.parseClaimsJwt(token);

					System.err.println(claims.toString());

					// OK, we can trust this JWT

				} catch (SignatureException e) {
					logger.log(Level.SEVERE, "Token exception:" + e.getMessage());
					e.printStackTrace();
				}

				// byte[] secret = Base64.decodeBase64(this.clientSecret);

				// Map<String, Object> decodedPayload = new JWTVerifier(secret,
				// this.clientId).verify(token);

				// return (String) decodedPayload.get(this.userField);
				/*
				 * try { logger.log(Level.INFO,
				 * "validateAuthorizationHeader() clientSecret:"+this.
				 * clientSecret); logger.log(Level.INFO,
				 * "validateAuthorizationHeader() token       :"+token);
				 * 
				 * //Algorithm algorithm =
				 * com.auth0.jwt.algorithms.Algorithm.HMAC256(Base64.
				 * decodeBase64(this.clientSecret)); Algorithm algorithm =
				 * com.auth0.jwt.algorithms.Algorithm.HMAC256(this.clientSecret)
				 * ;
				 * 
				 * JWTVerifier verifier =
				 * com.auth0.jwt.JWT.require(algorithm).build(); //Reusable
				 * verifier instance DecodedJWT jwt = verifier.verify(token);
				 * 
				 * logger.log(Level.INFO, jwt.getHeader());
				 * logger.log(Level.INFO, jwt.getIssuer());
				 * logger.log(Level.INFO, jwt.getKeyId());
				 * logger.log(Level.INFO, jwt.getPayload());
				 * logger.log(Level.INFO, jwt.getSubject());
				 * logger.log(Level.INFO, jwt.toString());
				 * 
				 * 
				 * JWTVerifier jwtVerifier = new
				 * JWTVerifier(this.clientSecret.getBytes())); Map<String,
				 * Object> verify = jwtVerifier.verify(token);
				 * 
				 * return (String) decodedPayload.get(this.userField);
				 */
				/*
				 * } catch (UnsupportedEncodingException exception){ //UTF-8
				 * encoding not supported logger.log(Level.SEVERE,
				 * exception.getClass().getName()+"/"+exception.getMessage());
				 * exception.printStackTrace();
				 * 
				 * } catch (JWTVerificationException exception){ //Invalid
				 * signature/claims logger.log(Level.SEVERE,
				 * "validateAuthorizationHeader() " +
				 * exception.getClass().getName() + "/" + exception.getMessage()
				 * ); //exception.printStackTrace(); }
				 */

			} catch (Exception e) {
				// e.printStackTrace();
				logger.log(Level.SEVERE,
						"validateAuthorizationHeader() token validation failed: " + e + "/" + e.getMessage());
			}
		}
		return null;
	}

	@Override
	public void destroy() {

	}

}
