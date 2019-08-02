package edu.harvard.hms.dbmi.bd2k.irct.cl.filter;

import org.apache.log4j.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

@Provider
/**
 * Filter adding support for the Cross-Origin Resource Sharing specification (https://www.w3.org/TR/cors/).
 * Configuration with:
 * - global/cors_enabled (boolean true / false)
 * - global/cors_allow_origin (string for "Allow Origin" value)
 */
public class CORSFilter implements ContainerResponseFilter {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    private String corsAllowOrigin;
    private Boolean corsEnabled;

    /**
     * Loads the CORS (Cross-Origin Resource Sharing) configuration.
     */
    private void loadConfig() {
        if (corsEnabled != null) {
            return;
        }

        try {
            Context ctx = new InitialContext();
            corsEnabled = (Boolean) ctx.lookup("global/cors_enabled");
            corsAllowOrigin = (String) ctx.lookup("global/cors_allow_origin");
            ctx.close();
        } catch (NamingException e) {
            logger.debug("CORS configuration NamingException, feature disabled", e);
            corsEnabled = false;
        }

        if (!corsEnabled) {
            logger.info("CORS is not enabled");
        } else {
            logger.info("CORS enabled, allow origin: " + this.corsAllowOrigin);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) {

        this.loadConfig();

        if (this.corsEnabled) {
            responseContext.getHeaders().add("Access-Control-Allow-Origin", this.corsAllowOrigin);
            responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
            responseContext.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
            responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        }
    }
}
