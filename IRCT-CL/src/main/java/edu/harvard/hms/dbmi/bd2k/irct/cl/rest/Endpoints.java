package edu.harvard.hms.dbmi.bd2k.irct.cl.rest;

import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonStructure;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.auth0.jwt.interfaces.Claim;

import edu.harvard.hms.dbmi.bd2k.irct.IRCTApplication;
import edu.harvard.hms.dbmi.bd2k.irct.cl.util.Utilities;
import edu.harvard.hms.dbmi.bd2k.irct.controller.ExecutionController;
import edu.harvard.hms.dbmi.bd2k.irct.controller.QueryController;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;

/**
 * Creates a REST interface.
 */
@Path("/")
@RequestScoped
public class Endpoints {

	@Inject
	private IRCTApplication picsure;

	@Inject
	private HttpSession session;

	@Inject
	private QueryController queryService;

	@Inject
	private ExecutionController executionService;

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
	@Path("/query")
	@Produces(MediaType.APPLICATION_JSON)
	public Response runQuery(String payload) {
		User currentUser = (User) session.getAttribute("user");

		logger.info("POST /query Starting new query for user:" + currentUser.getName());
		String rspStatus = "unknown", rspMessage = "N/A";

		JsonObjectBuilder resp = Json.createObjectBuilder();
		Query query = null;
		try {
			// Convert the payload into an internal representation of a query
			// After the query is parsed and persisted, get the queryObject
			query = queryService.createQuery(payload);
			query.setName(currentUser.getName() + "-QUERY-" + query.getId());

			logger.debug("POST /query Query created. now onto executing it");
			Result result = executionService.runQuery(query, currentUser);

			/*
			 * Query query = new Query(payload); Result result =
			 * query.runAsUser(user).getResult();
			 */

			rspStatus = "ok";
			rspMessage = "Query id:" + query.getId() + " named `" + query.getName() + "` is now "
					+ result.getResultStatus();

			resp.add("datasource", ((Resource) query.getResources().toArray()[0]).getName());

			resp.add("query", Json.createObjectBuilder().add("id", query.getId()).add("name", query.getName()));

			resp.add("result",
					Json.createObjectBuilder().add("id", result.getId()).add("JobType", result.getJobType())
							.add("ResultSetLocation",
									(result.getResultSetLocation() == null ? "NULL" : result.getResultSetLocation()))
							.add("ResourceActionId",
									(result.getResourceActionId() == null ? "NULL" : result.getResourceActionId()))
							.add("StartTime", result.getStartTime().toString())
							.add("EndTime", (result.getEndTime() == null ? "NULL"
									: result.getEndTime().toString()))
							.add("ResultStatus", result.getResultStatus().toString()));

		} catch (Exception e) {
			logger.error("/query Exception:" + e.getMessage());

			rspStatus = "error";
			rspMessage = e.getMessage();
		}

		// Build response object

		resp.add("status", rspStatus);
		resp.add("message", (rspMessage == null ? "NULL" : rspMessage));

		logger.info("POST /query Finished.");
		return Response.ok(resp.build(), MediaType.APPLICATION_JSON).build();
	}

	@GET
	@Path("/resources{resourceName: .*}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResources(@PathParam("resourceName") String resourceName) {

		JsonObjectBuilder resp = Json.createObjectBuilder();
		JsonArrayBuilder resourceList = Json.createArrayBuilder();
		String respStatus = "unknown";
		String respMessage = null;
		
		try {
			if (resourceName == null) {
				Map<String, Resource> resources = picsure.getResources();
				for (String resourceKey : resources.keySet()) {
					Resource resource = resources.get(resourceKey);
					resourceList.add(resourceObjectToJsonObject(resource));
				}
			} else {
				Map<String, Resource> resources = picsure.getResources();
				Resource resource = resources.get(resourceName);
				resp.add(resourceName,
						Json.createObjectBuilder().add("id", resource.getId()).add("name", resource.getName()).build());
				resourceList.add(resourceObjectToJsonObject(resource));
			}
			respStatus = "ok";
			
		} catch (Exception e) {
			respStatus = "error";
			respMessage = e.getMessage();
		} finally {
			resp.add("status", respStatus);
			if (respMessage != null) { resp.add("message", respMessage); }
			resp.add("resources", resourceList);
		}

		return Response.ok(resp.build(), MediaType.APPLICATION_JSON).build();
	}

	/**
	 * Returns the details of a specific query created previously
	 * 
	 * @param queryId
	 * @return queryDetails
	 */
	@GET
	@Path("/query/{queryId : .*}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getQuery(@PathParam("queryId") Long queryId) {
		JsonObjectBuilder resp = Json.createObjectBuilder();

		String rspStatus = "unknown", rspMessage = "N/A";

		logger.info(
				"GET /query Starting queryId:" + queryId + " user:" + ((User) session.getAttribute("user")).getName());
		edu.harvard.hms.dbmi.bd2k.irct.model.query.Query query = null;
		try {
			queryService.loadQuery(queryId);
			query = queryService.getQuery();

			rspStatus = "ok";
			rspMessage = "";

			resp.add("query",
					Json.createObjectBuilder().add("id", query.getId())
							.add("name", (query.getName() == null ? "NULL" : query.getName()))
							.add("resources", resourceObjectToJsonObject((Resource) query.getResources().toArray()[0])));

		} catch (Exception e) {
			rspStatus = "error";
			rspMessage = e.getMessage();
		}

		resp.add("status", rspStatus);
		resp.add("message", rspMessage);

		return Response.ok(resp.build(), MediaType.APPLICATION_JSON).build();
	}

	private JsonObject resourceObjectToJsonObject(Resource resource) {
		if (resource == null) {
			return Json.createObjectBuilder().add("id", "N/A")
					.add("name", "N/A").build();
		} else {
			return Json.createObjectBuilder().add("id", resource.getId())
			.add("name", resource.getName()).build();
		}
	}

	/**
	 * Returns a JSON Array of application settings
	 *
	 * @return JSON Array of settings (key/value pair)
	 */
	@GET
	@Path("/about")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure about() {
		try {
			IRCTApplication app = new IRCTApplication();
			User user = (User) session.getAttribute("user");
			// Get claims out of the token
			Map<String, Claim> token_claims = Utilities.getClaims(user.getToken(), picsure.getClientSecret());
			JsonObjectBuilder claimsObject = Json.createObjectBuilder();
			for (String key : token_claims.keySet()) {
				Claim claim = token_claims.get(key);
				claimsObject.add(key,
						(String) (claim.asString() == null ? claim.asDate().toString() : claim.asString()));
			}

			return Json.createObjectBuilder().add("appVersion", app.getVersion()).add("userId", user.getUserId())
					.add("userName", user.getName()).add("userClaims", claimsObject.build()).build();

		} catch (Exception e) {
			return Json.createObjectBuilder().add("status", "error").add("message", e.getMessage()).build();
		}
	}
}
