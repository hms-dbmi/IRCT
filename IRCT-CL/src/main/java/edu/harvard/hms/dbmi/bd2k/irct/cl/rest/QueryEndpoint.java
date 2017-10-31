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

import edu.harvard.hms.dbmi.bd2k.irct.controller.ExecutionController;
import edu.harvard.hms.dbmi.bd2k.irct.controller.QueryController;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
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
	private ExecutionController executionService;

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
		User currentUser = (User) session.getAttribute("user");
		
		logger.info("POST /query Starting new query for user:" + currentUser.getName());
		String rspStatus = "unknown", rspMessage = "N/A";
		
		JsonObjectBuilder resp = Json.createObjectBuilder();
		Query query = null;
		try {
			// Convert the payload into an internal representation of a query
			// After the query is parsed and persisted, get the queryObject
			query = queryService.createQuery(payload);
			query.setName(currentUser.getName()+"-QUERY-"+query.getId());
			
			logger.debug("POST /query Query created. now onto executing it");
			Result result = executionService.runQuery(query, currentUser);
			
			rspStatus = "ok";
			rspMessage = "Query id:"+query.getId()+" named `"+query.getName()+"` is now "+result.getResultStatus();
			resp.add("queryId", query.getId());
			resp.add("queryName", query.getName());
			resp.add("queryResource", ((Resource) query.getResources().toArray()[0]).getName());
			
			resp.add("result", Json.createObjectBuilder()
					.add("resultid", result.getId())
					.add("JobType", result.getJobType())
					.add("ResultSetLocation", (result.getResultSetLocation()==null?"NULL":result.getResultSetLocation()))
					.add("ResourceActionId", (result.getResourceActionId()==null?"NULL":result.getResourceActionId()))
					.add("StartTime", result.getStartTime().toString())
					.add("EndTime", (result.getEndTime()==null?"NULL":result.getEndTime().toString()))
					.add("ResultStatus", result.getResultStatus().toString())
				);
			
		} catch (Exception e) {
			logger.error("/query Exception:"+e.getMessage());
			
			rspStatus = "error";
			rspMessage = e.getMessage();
		}
		
		// Build response object
		
		resp.add("status", rspStatus);
		resp.add("message", (rspMessage==null?"NULL":rspMessage));

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
		JsonObjectBuilder resp = Json.createObjectBuilder();
		
		String rspStatus = "unknown", rspMessage = "N/A";
		
		logger.info("GET /query Starting queryId:" + queryId + " user:" + ((User) session.getAttribute("user")).getName());
		edu.harvard.hms.dbmi.bd2k.irct.model.query.Query query = null;
		try {
			queryService.loadQuery(queryId);			
			query = queryService.getQuery();
			
			rspStatus = "ok";
			rspMessage = "";
			
			resp.add("query", Json.createObjectBuilder()
					.add("id", query.getId())
					.add("name", (query.getName()==null?"NULL":query.getName()))
					.add("resources", resourceObjectToJson((Resource) query.getResources().toArray()[0]))
				);
			
		} catch (Exception e) {
			rspStatus = "error";
			rspMessage = e.getMessage();
		}
		
		resp.add("status", rspStatus);
		resp.add("message", rspMessage);
		
		return Response.ok(resp.build(),
				MediaType.APPLICATION_JSON).build();
	}
	
	private String resourceObjectToJson(Resource resource) {
		if (resource == null) {
			return "NULL";
		}
		return resource.getName();
	}
}
