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
import edu.harvard.hms.dbmi.bd2k.irct.cl.util.Utilities;
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
						sc.ensureUserExists(Utilities.extractEmailFromJWT((HttpServletRequest) req, this.clientSecret))
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

	@Override
	public void destroy() {

	}

}
