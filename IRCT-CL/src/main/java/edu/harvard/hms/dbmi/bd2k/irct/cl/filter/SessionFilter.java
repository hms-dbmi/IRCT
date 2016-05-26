/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.cl.filter;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Map;
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

import org.apache.commons.codec.binary.Base64;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;

import edu.harvard.hms.dbmi.bd2k.irct.controller.SecurityController;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.JWT;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.Token;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;

/**
 * Creates a session filter for ensuring secure access
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@WebFilter(filterName = "session-filter", urlPatterns = { "/*" })
public class SessionFilter implements Filter {
	
	@javax.annotation.Resource(mappedName ="java:global/client_secret")
	private String clientId;
	@javax.annotation.Resource(mappedName ="java:global/client_id")
	private String clientSecret;
	@javax.annotation.Resource(mappedName ="java:global/userField")
	private String userField;

//	@Inject
//	private ServletContext context;

	@Inject
	private SecurityController sc;

	@Override
	public void init(FilterConfig fliterConfig) throws ServletException {
//		this.clientSecret = context.getInitParameter("client_secret");
//		this.clientId = context.getInitParameter("client_id");
//		this.userField = context.getInitParameter("userField");
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain fc)
			throws IOException, ServletException {

		// Calls to the Security Service can go straight through
		if (!((HttpServletRequest) req).getPathInfo().startsWith("/securityService/")) {
			//Get the session and user information
			HttpSession session = ((HttpServletRequest) req).getSession();
			User user = (User) session.getAttribute("user");

			// Is a user already associated with a session?
			if (user == null) {

				// If no user is associated then validate the authorization
				// header
				String email = validateAuthorizationHeader((HttpServletRequest) req);

				if (email == null) {
					((HttpServletResponse) res)
							.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					res.getOutputStream().write(
							"{\"message\":\"Session is not authorized\"}"
									.getBytes());
					res.getOutputStream().close();
					return;
				}
				
				user = sc.getUser(email);
				Token token = new JWT(((HttpServletRequest) req).getHeader("Authorization"), "", "Bearer", this.clientId);
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
				Token token = new JWT(((HttpServletRequest) req).getHeader("Authorization"), "", "Bearer", this.clientId);
				SecureSession secureSession = new SecureSession();
				secureSession.setToken(token);
				secureSession.setUser(user);
				
				session.setAttribute("user", user);
				session.setAttribute("token", token);
				session.setAttribute("secureSession", secureSession);
			}

		}
		
//		HttpSession session = ((HttpServletRequest) req).getSession();
//		
//		User user = sc.getUser("Jeremy_Easton-Marks@hms.harvard.edu");
//		Token token = new JWT(((HttpServletRequest) req).getHeader("Authorization"), "", "Bearer", this.clientId);
//		SecureSession secureSession = new SecureSession();
//		secureSession.setToken(token);
//		secureSession.setUser(user);
//		
//		session.setAttribute("user", user);
//		session.setAttribute("token", token);
//		session.setAttribute("secureSession", secureSession);
		
		fc.doFilter(req, res);
	}

	private String validateAuthorizationHeader(HttpServletRequest req) {
		String authorizationHeader = ((HttpServletRequest) req)
				.getHeader("Authorization");
		if (authorizationHeader != null) {
			try {

				String[] parts = authorizationHeader.split(" ");
				if (parts.length != 2) {
					return null;
				}

				String scheme = parts[0];
				String credentials = parts[1];
				String token = "";

				Pattern pattern = Pattern.compile("^Bearer$",
						Pattern.CASE_INSENSITIVE);
				if (pattern.matcher(scheme).matches()) {
					token = credentials;
				}

				byte[] secret = Base64.decodeBase64(this.clientSecret);
				Map<String, Object> decodedPayload = new JWTVerifier(secret,
						this.clientId).verify(token);

				return (String) decodedPayload.get(this.userField);

			} catch (InvalidKeyException | NoSuchAlgorithmException
					| IllegalStateException | SignatureException | IOException
					| JWTVerifyException e) {
				System.out.println("TOKEN VALIDATION FAILED: " + e.getMessage());
			}
		}
		return null;
	}

	@Override
	public void destroy() {

	}

}
