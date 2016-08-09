/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.cl.rest;

import java.util.List;

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

import edu.harvard.hms.dbmi.bd2k.irct.controller.ResultController;
import edu.harvard.hms.dbmi.bd2k.irct.dataconverter.ResultDataStream;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultStatus;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;

/**
 * Creates a REST interface for the result service
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Path("/resultService")
@RequestScoped
public class ResultService {
	
	@Inject
	private ResultController rc;

	@Inject
	private HttpSession session;

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
		User user = (User) session.getAttribute("user");
		List<Result> availableResults = rc.getAvailableResults(user);

		for (Result result : availableResults) {
			JsonObjectBuilder resultJSON = Json.createObjectBuilder();
			resultJSON.add("resultId", result.getId());
			resultJSON.add("status", result.getResultStatus().toString());
			response.add(resultJSON.build());
		}

		return Response.ok(response.build(), MediaType.APPLICATION_JSON)
				.build();
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
		User user = (User) session.getAttribute("user");

		ResultStatus resultStatus = rc.getResultStatus(user, resultId);
		if (resultStatus == null) {
			response.add("message", "Unable to get result for that id");
		} else {
			response.add("resultId", resultId);
			response.add("status", resultStatus.toString());
		}

		return Response.ok(response.build(), MediaType.APPLICATION_JSON)
				.build();
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
		User user = (User) session.getAttribute("user");

		List<String> availableFormats = rc.getAvailableFormats(user, resultId);

		if (availableFormats == null) {
			JsonObjectBuilder notFoundResponse = Json.createObjectBuilder();
			notFoundResponse.add("message",
					"Unable to get available formats for that id");
			return Response.ok(notFoundResponse.build(),
					MediaType.APPLICATION_JSON).build();
		}

		for (String availableFormat : availableFormats) {
			response.add(availableFormat);
		}

		return Response.ok(response.build(), MediaType.APPLICATION_JSON)
				.build();
	}

	/**
	 * Returns a result in the desired format if available. If download is set to
	 * Yes then the file is returned with Content-Disposition set as attachment
	 * and a file name.
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
	public Response download(@PathParam("resultId") Long resultId,
			@PathParam("format") String format,
			@QueryParam("download") String download) {
		User user = (User) session.getAttribute("user");

		ResultDataStream rds = rc.getResultDataStream(user, resultId, format);
		if ((rds == null) || (rds.getMediaType() == null)) {
			JsonObjectBuilder jsonResponse = Json.createObjectBuilder();
			jsonResponse.add("message", "Unable to retrieve result");
			return Response
					.ok(jsonResponse.build(), MediaType.APPLICATION_JSON)
					.build();
		}

		if ((download != null) && (download.equalsIgnoreCase("Yes"))) {
			return Response
					.ok(rds.getResult(), rds.getMediaType())
					.header("Content-Disposition",
							"attachment; filename=IRCT-" + resultId
									+ rds.getFileExtension()).build();
		}

		return Response.ok(rds.getResult(), rds.getMediaType()).build();
	}
}
