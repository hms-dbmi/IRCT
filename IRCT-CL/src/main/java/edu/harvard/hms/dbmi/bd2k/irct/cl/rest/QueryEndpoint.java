package edu.harvard.hms.dbmi.bd2k.irct.cl.rest;

import java.io.Serializable;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import edu.harvard.hms.dbmi.bd2k.irct.controller.QueryController;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;

/**
 * Creates a REST interface for the query service
 */
@Path("/query")
@ConversationScoped
@Named
public class QueryEndpoint implements Serializable {
	private static final long serialVersionUID = -3951500710489406681L;

	@Inject
	private QueryController queryService;

	@Inject
	private HttpSession session;

	private Logger logger = Logger.getLogger(this.getClass());

	/**
	 * Creates a new query, initiates it, and returns the query id and some
	 * preliminary information about the query
	 * 
	 * @param payload
	 *            Some preliminary query information about name, user, status
	 * @return QueryResponse
	 */
	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response execute(String payload) {
		logger.info("POST /query Starting new query for user:" + ((User) session.getAttribute("user")).getName());
		String rspStatus = "unknown", rspMessage = "N/A";

		edu.harvard.hms.dbmi.bd2k.irct.model.query.Query query = null;
		try {
			// Convert the payload into an internal representation of a query
			// After the query is parsed and persisted, get the queryObject
			query = queryService.createQuery(payload);
			logger.info("/query "+query.getName());
			logger.info("/query "+query.getId());
			
		} catch (Exception e) {
			rspStatus = "error";
			rspMessage = e.getMessage();
		}

		// Build response object
		JsonObjectBuilder resp = Json.createObjectBuilder();
		if (query != null) {
			try {
				rspStatus = "ok";
				rspMessage = "`query` object has been created. queryId is "+query.getId();
			} catch (Exception e) {
				rspStatus = "error";
				rspMessage = "Could not get queryId, "+e.getMessage();
			}
		} else {
			rspStatus = "error";
			rspMessage = "`query` object cannot be created.";
		}
		resp.add("status", rspStatus);
		resp.add("message", rspMessage);

		logger.info("POST /query Finished.");
		return Response.ok(resp.build(), MediaType.APPLICATION_JSON).build();
	}

	/**
	 * Returns the details of a specific query created previously
	 * 
	 * @param queryId
	 * @return queryDetails
	 */
	@GET
	@Path("/{queryId : .*}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getQueryDetails(@PathParam("queryId") Long queryId) {
		String rspStatus = "unknown", rspMessage = "N/A";
		
		logger.info("GET /query Starting queryId:" + queryId + " user:" + ((User) session.getAttribute("user")).getName());
		edu.harvard.hms.dbmi.bd2k.irct.model.query.Query query = null;
		try {
			queryService.loadQuery(queryId);			
			query = queryService.getQuery();
			
			rspStatus = "ok";
			rspMessage = "Query id:"+query.getId()+" name:"+query.getName()+" class:"+query.getClass();
			
		} catch (Exception e) {
			rspStatus = "error";
			rspMessage = e.getMessage();
		}
		
		return Response.ok(Json.createObjectBuilder().add("status", rspStatus).add("message", rspMessage).build(),
				MediaType.APPLICATION_JSON).build();
	}
}
