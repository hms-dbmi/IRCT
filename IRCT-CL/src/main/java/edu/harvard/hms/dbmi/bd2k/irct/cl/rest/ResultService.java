/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.cl.rest;

import java.util.List;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import edu.harvard.hms.dbmi.bd2k.irct.controller.ResultController;
import edu.harvard.hms.dbmi.bd2k.irct.dataconverter.ResultDataStream;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;

/**
 * Creates a REST interface for the result service
 */
@Path("/resultService")
@RequestScoped
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ResultService {

	@Inject
	private ResultController rc;

	@Inject
	private HttpSession session;

	private Logger logger = Logger.getLogger(this.getClass());

	/**
	 * Returns a list of available results for that user
	 * 
	 * @return Result List
	 */
	@GET
	@Path("/available")
	@Produces(MediaType.APPLICATION_JSON)
	public Response availableResults() {
		JsonArrayBuilder response = Json.createArrayBuilder();
		try {
			User user = (User) session.getAttribute("user");

			for (Result result : rc.getAvailableResults(user)) {
				response.add(Json.createObjectBuilder().add("status", result.getResultStatus().toString())
						.add("resultId", result.getId()));
			}
		} catch (Exception e) {
			response.add(Json.createObjectBuilder().add("status", "error").add("message", e.getMessage()));
		}
		return Response.ok(response.build(), MediaType.APPLICATION_JSON).build();
	}

	/**
	 * Returns the status of a result if it is available
	 * 
	 * @param resultId
	 *            Id of the result
	 * @return Status of the result
	 */
	@GET
	@Path("/resultStatus/{resultId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response resultStatus(@PathParam("resultId") Long resultId) {
		JsonObjectBuilder response = Json.createObjectBuilder();
		try {
			User user = (User) session.getAttribute("user");
			Result result = rc.getResult(user, resultId);
			if (result == null) {
				response.add("message", "Unable to get result for that id");
			} else {
				response.add("resultId", resultId);
				response.add("status", result.getResultStatus().toString());
				response.add("message", result.getMessage());
			}
		} catch (Exception e) {
			response.add("status", "error");
			response.add("message", e.getMessage());
		}

		return Response.ok(response.build(), MediaType.APPLICATION_JSON).build();
	}

	/**
	 * Returns the full status of a result
	 * 
	 * @param resultId
	 *            Id of the result
	 * @return Status details of the result
	 */
	@GET
	@Path("/results/{resultId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response results(@PathParam("resultId") Long resultId) {
		JsonObjectBuilder response = Json.createObjectBuilder();
		try {
			User user = (User) session.getAttribute("user");
			response.add("user",
					Json.createObjectBuilder().add("name", user.getName()).add("userId", user.getUserId()));

			Result result = rc.getResult(user, resultId);
			if (result == null) {
				response.add("message", "Unable to get result for that id");
			} else {
				response.add("resultId", resultId);
				response.add("resultStatus", result.getResultStatus().toString());
				response.add("jobType", result.getJobType());
				response.add("resourceActionId", result.getResourceActionId());
				response.add("resultId", result.getResultSetLocation());
				response.add("startTime", result.getStartTime().toString());
				response.add("endTime", result.getEndTime().toString());
				response.add("message", result.getMessage());
			}
		} catch (Exception e) {
			response.add("status", "error");
			response.add("message", e.getMessage());
		}

		return Response.ok(response.build(), MediaType.APPLICATION_JSON).build();
	}

	/**
	 * Returns the list of available formats for a result if available
	 * 
	 * @param resultId
	 *            Result Id
	 * @return Array of formats
	 */
	@GET
	@Path("/availableFormats/{resultId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response availableFormats(@PathParam("resultId") Long resultId) {
		JsonArrayBuilder response = Json.createArrayBuilder();
		try {
			User user = (User) session.getAttribute("user");
			List<String> availableFormats = rc.getAvailableFormats(user, resultId);
			if (availableFormats == null) {
				JsonObjectBuilder notFoundResponse = Json.createObjectBuilder();
				notFoundResponse.add("message", "Unable to get available formats for that id");
				return Response.ok(notFoundResponse.build(), MediaType.APPLICATION_JSON).build();
			}

			for (String availableFormat : availableFormats) {
				response.add(availableFormat);
			}
		} catch (Exception e) {
			response.add(Json.createObjectBuilder().add("status", "error").add("message", e.getMessage()));
		}

		return Response.ok(response.build(), MediaType.APPLICATION_JSON).build();
	}

	/**
	 * Returns a result in the desired format if available. If download is set
	 * to Yes then the file is returned with Content-Disposition set as
	 * attachment and a file name.
	 * 
	 * @param resultId
	 *            Result Id
	 * @param format
	 *            Format
	 * @param download
	 *            Download
	 * @return Results in desired format
	 */
	@GET
	@Path("/result/{resultId}/{format}")
	public Response download(@PathParam("resultId") Long resultId, @PathParam("format") String format,
			@QueryParam("download") String download) {

		logger.debug("/result resultId:" + resultId + " format:" + format + " download:" + download);
		ResultDataStream rds = null;
		try {
			User user = (User) session.getAttribute("user");
			rds = rc.getResultDataStream(user, resultId, format);

			logger.debug("/result `ResultDataStream` mediatype is " + rds.getMediaType());

			if ((rds == null) || (rds.getMediaType() == null)) {
				logger.debug("/result rds is null");

				JsonObjectBuilder jsonResponse = Json.createObjectBuilder();
				jsonResponse.add("message", "Unable to retrieve result.");
				return Response.ok(jsonResponse.build(), MediaType.APPLICATION_JSON).build();
			}

			if ((download != null) && (download.equalsIgnoreCase("Yes"))) {
				logger.debug("/result initiate download with mediaType:" + rds.getMediaType().toString());
				return Response.ok(rds.getResult(), rds.getMediaType())
						.header("Content-Disposition", "attachment; filename=IRCT-" + resultId + rds.getFileExtension())
						.build();
			}

		} catch (Exception e) {
			return Response.ok(Json.createObjectBuilder().add("status", "error").add("message", e.getMessage()),
					MediaType.APPLICATION_JSON).build();
		}
		logger.debug("/result returning data");
		return Response.ok(rds.getResult(), rds.getMediaType()).build();
	}
}
