/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.cl.filter;

import java.io.IOException;
import java.util.Enumeration;

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

import org.apache.log4j.Logger;

import edu.harvard.hms.dbmi.bd2k.irct.cl.util.Utilities;
import edu.harvard.hms.dbmi.bd2k.irct.controller.SecurityController;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;

/**
 * Creates a session filter for ensuring secure access
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

	@Override
	public void init(FilterConfig fliterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain fc) throws IOException, ServletException {
		logger.debug("doFilter() Starting");
		HttpServletRequest request = (HttpServletRequest) req;

		// If processing URL /securityService/*, we are creating a session/secureSession
		if (request.getRequestURI().endsWith("/securityService/startSession")) {
			// Do Nothing
			logger.debug("doFilter() securityService URL is NOT filtered.");
		} else {
			HttpSession session = ((HttpServletRequest) req).getSession();
			logger.debug("doFilter() got session from request.");
			
			Enumeration<String> keys = session.getAttributeNames();
			while(keys.hasMoreElements()) {
				String element = keys.nextElement();
				logger.debug("doFilter() Element:"+element);
			}
			
			try {
				User user = session.getAttribute("user") == null ?
						sc.ensureUserExists(Utilities.extractEmailFromJWT((HttpServletRequest) req, this.clientSecret))
						: (User) session.getAttribute("user");
				logger.debug("doFilter() got user object.");
				
				// TODO DI-896 change. Since the user above gets created without an actual token, we need 
				// to re-extract the token, from the header and parse it and place it inside the user object, 
				// for future playtime.
				if (user.getToken() == null) {
					logger.debug("doFilter() No token in user object, so let's add one.");
					String headerValue = ((HttpServletRequest)req).getHeader("Authorization");
					if (headerValue == null || headerValue.isEmpty()) {
						logger.debug("doFilter() No token in user object, so let's add one.");
						throw new RuntimeException("No `Authorization` header was provided");
					} else {
						logger.debug("doFilter() Found a token in the HTTP header.");
						// TODO Check if this split produces two element list, actually.
						String tokenString = headerValue.split(" ")[1];
						user.setToken(tokenString);
					}
				}
				logger.debug("doFilter() Token in `user` object is "+user.getToken());
				
				session.setAttribute("user", user);
				req.setAttribute("user", user);
				logger.debug("doFilter() set session attributes.");

			} catch (Exception e) {
				logger.error("doFilter() "+e.getMessage());

				String errorMessage = "{\"status\":\"error\",\"message\":\"Could not establish the user identity from request headers. "+e.getMessage()+"\"}";

				((HttpServletResponse) res).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				res.setContentType("application/json");
				res.getOutputStream()
						.write(errorMessage.getBytes());
				res.getOutputStream().close();
				return;
			}
		}
		logger.debug("doFilter() Finished.");
		fc.doFilter(req, res);
	}
	
	@Override
	public void destroy() {

	}

}
