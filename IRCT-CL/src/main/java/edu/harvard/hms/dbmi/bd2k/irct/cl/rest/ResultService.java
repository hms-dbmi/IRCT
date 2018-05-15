/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.cl.rest;

import edu.harvard.hms.dbmi.bd2k.irct.controller.ResultController;
import edu.harvard.hms.dbmi.bd2k.irct.dataconverter.ResultDataStream;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;
import edu.harvard.hms.dbmi.bd2k.irct.util.IRCTResponse;
import org.apache.log4j.Logger;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

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
		logger.debug("GET /available ");
		
		JsonArrayBuilder response = Json.createArrayBuilder();
		User user = (User) session.getAttribute("user");
		List<Result> availableResults = rc.getAvailableResults(user);

		if (availableResults.size()<1) {
			return IRCTResponse.success("There are no results available.");
		}
		
		logger.debug("GET /available There are "+availableResults.size()+" results available.");
		for (Result result : availableResults) {
			JsonObjectBuilder resultJSON = Json.createObjectBuilder();
			resultJSON.add("resultId", result.getId());
			resultJSON.add("status", result.getResultStatus().toString());
			response.add(resultJSON.build());
		}
		logger.debug("GET /available Finished.");
		return IRCTResponse.success(availableResults);
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
		logger.debug("GET resultStatus/ starting");

		JsonObjectBuilder response = Json.createObjectBuilder();
		User user = (User) session.getAttribute("user");

		Result result = rc.getResult(user, resultId);
		if (result == null) {
			response.add("message", "Unable to get result for that id");

		} else {
			response.add("resultId", result.getId());
			response.add("riActionId", String.valueOf(result.getResourceActionId()));

			response.add("status", result.getResultStatus().toString());

			response.add("starttime", String.valueOf(result.getStartTime()));
			response.add("endtime", String.valueOf(result.getEndTime()));

			response.add("dataType", String.valueOf(result.getDataType()));
			response.add("path", String.valueOf(result.getResultSetLocation()));

			response.add("message", String.valueOf(result.getMessage()));
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
		User user = (User) session.getAttribute("user");

		List<String> availableFormats = rc.getAvailableFormats(user, resultId);

		if (availableFormats == null) {
			return IRCTResponse.success("Unable to get available formats for result #"+resultId);
		}
		
		if (availableFormats.size()<1) {
			return IRCTResponse.success("There are no formats available.");
		}

		return IRCTResponse.success(availableFormats);
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
		
		logger.debug("GET /result format:"+(format==null?"null":format)+" download:"+(download==null?"null":download));
		User user = (User) session.getAttribute("user");

		ResultDataStream rds = rc.getResultDataStream(user, resultId, format);

		if (rds == null) {
			return IRCTResponse.riError("Unable to retrieve result.");
		}

		if ( (rds.getMediaType() == null)) {
			logger.debug("GET /result rds or mediaType is null");
			return IRCTResponse.riError(rds.getMessage());
		}

 		if ((download != null) && (download.equalsIgnoreCase("Yes"))) {
			logger.debug("GET /result initiate download with mediaType:"+rds.getMediaType().toString());
			return Response
					.ok(rds.getResult(), rds.getMediaType())
					.header("Content-Disposition",
							"attachment; filename=IRCT-" + resultId
									+ rds.getFileExtension()).build();
		}
		logger.debug("GET /result returning");
		return IRCTResponse.success(rds.getResult(), rds.getMediaType());
	}
}
