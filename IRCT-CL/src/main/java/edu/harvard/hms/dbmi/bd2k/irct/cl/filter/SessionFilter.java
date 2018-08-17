/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.cl.filter;

import edu.harvard.hms.dbmi.bd2k.irct.IRCTApplication;
import edu.harvard.hms.dbmi.bd2k.irct.cl.util.Utilities;
import edu.harvard.hms.dbmi.bd2k.irct.controller.SecurityController;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.NotAuthorizedException;
import java.io.IOException;

/**
 * Creates a session filter for ensuring secure access
 */
@WebFilter(filterName = "session-filter", urlPatterns = { "/rest/*" })
public class SessionFilter implements Filter {

	Logger logger = Logger.getLogger(this.getClass().getName());
	
	@Inject
	private IRCTApplication irctApp;

	@javax.annotation.Resource(mappedName = "java:global/jwks_uri")
	private String jwksUri;

	@javax.annotation.Resource(mappedName = "java:global/userField")
	private String userField;

	@Inject
	private SecurityController sc;

	@Override
	public void init(FilterConfig fliterConfig){
    }

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain fc) throws IOException, ServletException {
		logger.debug("doFilter() Starting");
		HttpServletRequest request = (HttpServletRequest) req;

		// If processing URL /securityService/*, we are creating a session/secureSession; ignore CORS preflight calls
		if (	request.getRequestURI().endsWith("/securityService/startSession") ||
				request.getRequestURI().endsWith("/securityService/createKey") ||
				request.getMethod().equals("OPTIONS")) {
			// Do Nothing
			logger.debug("doFilter() securityService URL is NOT filtered.");
		} else {
			HttpSession session = ((HttpServletRequest) req).getSession();
			logger.debug("doFilter() got session from request.");
			
			try {
				User user = null;
				String tokenString = null;

				String headerValue = ((HttpServletRequest)req).getHeader("Authorization");
				if (headerValue == null) {
					/*
					 * When Authorization header is missing, we can still check if the session contains a valid
					 * user. This is required, for now, because startSession can establish one, and notebooks should
					 * still be able to send requests without a token.
					 *
					 * In the future (as of now 2018-April-10) the session should be eliminated and
					 * the the "Authorization" header can be made mandatory on every request.
					 */
					 user = (User) session.getAttribute("user");
				} else {
					if (headerValue.isEmpty()){
						logger.debug("doFilter() No token in user object, so let's add one.");
						throw new RuntimeException("No `Authorization` header was provided");
					}
					tokenString = headerValue.split(" ")[1];

					/*
					 * Check if the user has the same token that the session was established with.
					 */
					if (user == null
							|| user.getToken() == null
							|| !user.getToken().equals(tokenString))
					    if (!IRCTApplication.VERIFY_METHOD_SESSION_FILETER.equals(irctApp.getVerify_user_method())){
                            //Get information from token introspection endpoint in 2.0
                            user = sc.ensureUserExists(Utilities.extractUserFromTokenIntrospection((HttpServletRequest) req, this.userField, irctApp.getToken_introspection_url(), irctApp.getToken_introspection_token()));
                        } else{
    						user = sc.ensureUserExists(Utilities.extractEmailFromJWT((HttpServletRequest) req, irctApp.getClientSecret(), this.jwksUri, this.userField));
                        }
				}

				if (user == null)
					throw new NotAuthorizedException("Cannot create user for the token: " + tokenString);

				logger.debug("doFilter() User(userId:"+user.getUserId()+")");
				
				//DI-994: email whitelist for authorization without a token
				//currently just authorized for all if the user is in the white list
				//could be added for different resources in the future
				if (user.getUserId() != null && !user.getUserId().isEmpty() && irctApp.isWhitelistEnabled()) {
					if (irctApp.getWhitelist().containsKey(user.getUserId())) {
						logger.debug("doFilter() User(userId:" + user.getUserId() + ") is in whitelist.");
					} else {
						throw new NotAuthorizedException("User `"+user.getUserId()+"` not in white list", res);
					}
				}
				
				// TODO DI-896 change. Since the user above gets created without an actual token, we need 
				// to re-extract the token, from the header and parse it and place it inside the user object, 
				// for future playtime.
				if (user.getToken() == null || !user.getToken().equals(tokenString)) {
					logger.debug("doFilter() No token in user object, so let's add one.");
					user.setToken(tokenString);
					sc.updateUserRecord(user);
				}
				logger.debug("doFilter() User(token:"+user.getToken()+")");

				session.setAttribute("user", user);
				req.setAttribute("user", user);

				logger.debug("doFilter() set session attributes.");

			} catch (NotAuthorizedException e) {
				logger.error("doFilter() "+e.getMessage());

				String errorMessage = "{\"status\":\"error\",\"message\":\"Could not establish the user identity from request headers. "+e.getChallenges()+"\"}";

				((HttpServletResponse) res).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				res.setContentType("application/json");
				res.getOutputStream()
						.write(errorMessage.getBytes());
				res.getOutputStream().close();
				return;
			} catch (Exception e) {
				logger.error("doFilter() "+e.getMessage());

				e.printStackTrace();

				String errorMessage = "{\"status\":\"error\",\"message\":\"Could not establish the user identity from request headers. "+ e.getClass().getName() + " " +e.getMessage()+"\"}";

				((HttpServletResponse) res).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
