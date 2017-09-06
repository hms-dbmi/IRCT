/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.cl.filter;

import java.io.IOException;

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
		logger.debug("doFilter() Starting");
		HttpServletRequest request = (HttpServletRequest) req;

		// If processing URL /securityService/*, we are creating a session/secureSession
		if (request.getRequestURI().endsWith("/securityService/startSession")) {
			// Do Nothing
			logger.debug("doFilter() securityService URL is NOT filtered.");
		} else {
			HttpSession session = ((HttpServletRequest) req).getSession();
			logger.debug("doFilter() got session from request.");
			try {
				User user = session.getAttribute("user") == null ?
						sc.ensureUserExists(Utilities.extractEmailFromJWT((HttpServletRequest) req, this.clientSecret))
						: (User)session.getAttribute("user");
				logger.debug("doFilter() got user object.");

				Token token = session.getAttribute("token") == null ?
						ss.createTokenObject(req)
						: (Token)session.getAttribute("token");
				logger.debug("doFilter() got token object.");

				SecureSession secureSession = session.getAttribute("secureSession") == null ?
						sc.validateKey(sc.createKey(user, token))
						: (SecureSession)session.getAttribute("secureSession");
				logger.debug("doFilter() got securesession object.");
				setSessionAndRequestAttributes(req, session, user, token, secureSession);
				logger.debug("doFilter() set session attributes.");

			} catch (Exception e) {
				logger.error("EXCEPTION: "+e.getMessage());

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

	/*
	 *  TODO : This is a temporary hackaround, we should move to only storing attributes on the request
	 *  if they may change across a session.
	 */
	private void setSessionAndRequestAttributes(ServletRequest req, HttpSession session, User user, Token token, SecureSession secureSession) {
		session.setAttribute("user", user);
		session.setAttribute("token", token);
		session.setAttribute("secureSession", secureSession);
		req.setAttribute("user", user);
		req.setAttribute("token", token);
		req.setAttribute("secureSession", secureSession);
	}

	@Override
	public void destroy() {

	}

}
