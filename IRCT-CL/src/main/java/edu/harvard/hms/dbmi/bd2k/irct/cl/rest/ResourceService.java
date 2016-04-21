/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.cl.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import edu.harvard.hms.dbmi.bd2k.irct.controller.PathController;
import edu.harvard.hms.dbmi.bd2k.irct.controller.ResourceController;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Entity;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

@Path("/resourceService")
@RequestScoped
public class ResourceService {

	@Inject
	private ResourceController rc;

	@Inject
	private PathController pc;

	@Inject
	Logger log;

	@Inject
	private HttpSession session;

	/**
	 * Returns a list of all resources. If a type is chosen then only resources
	 * of that type will be returned.
	 * 
	 * 
	 * @return Response
	 */
	@GET
	@Path("/resources")
	@Produces(MediaType.APPLICATION_JSON)
	public Response resources(@QueryParam(value = "type") String type) {
		JsonArrayBuilder response = Json.createArrayBuilder();

		List<Resource> returnResources = null;
		if (type == null || type.isEmpty()) {
			returnResources = rc.getResources();
		} else {
			switch (type.toLowerCase()) {
			case "process":
				returnResources = rc.getProcessResources();
				break;
			case "visualization":
				returnResources = rc.getVisualizationResources();
				break;
			case "query":
				returnResources = rc.getQueryResources();
				break;
			default:
				break;
			}
		}

		if (returnResources == null) {
			JsonObjectBuilder build = Json.createObjectBuilder();
			build.add("status", "Invalid type");
			build.add("message",
					"The type submitted is not a supported resource type");
			return Response.status(400).entity(build.build()).build();
		}

		for (Resource resource : returnResources) {
			response.add(resource.toJson());
		}

		return Response.ok(response.build(), MediaType.APPLICATION_JSON)
				.build();
	}

	@GET
	@Path("/searchCategories")
	@Produces(MediaType.APPLICATION_JSON)
	public Response categories() {
		JsonArrayBuilder response = Json.createArrayBuilder();

		List<String> categories = rc.getCategories();
		if (categories != null) {
			for (String category : categories) {
				response.add(category);
			}
		}

		return Response.ok(response.build(), MediaType.APPLICATION_JSON)
				.build();
	}

	@GET
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	public Response search(@Context UriInfo info) {
		JsonArrayBuilder response = Json.createArrayBuilder();
		Map<String, List<String>> searchParams = new HashMap<String, List<String>>();
		for (String categoryName : info.getQueryParameters().keySet()) {
			List<String> values = info.getQueryParameters().get(categoryName);

			if (!rc.isValidCategory(categoryName)) {
				JsonObjectBuilder build = Json.createObjectBuilder();
				build.add("status", "Invalid resource category");
				build.add("message", categoryName
						+ " is not a supported resource category");
				return Response.status(400).entity(build.build()).build();
			}
			searchParams.put(categoryName, values);
		}

		List<Resource> returnResources = rc.search(searchParams);
		if (returnResources != null) {
			for (Resource resource : returnResources) {
				response.add(resource.toJson());
			}
		}
		return Response.ok(response.build(), MediaType.APPLICATION_JSON)
				.build();
	}

	@GET
	@Path("/path{path : .*}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response path(@PathParam("path") String path,
			@QueryParam("relationship") String relationshipString,
			@QueryParam("searchTerm") String searchTerm,
			@QueryParam("searchOntologyType") String ontologyType,
			@QueryParam("searchOntologyTerm") String ontologyTerm) {
		
		
		JsonArrayBuilder response = Json.createArrayBuilder();
		List<Entity> entities = null;

		Resource resource = null;
		Entity resourcePath = null;

		if (path != null && !path.isEmpty()) {
			path = "/" + path;
			path = path.substring(1);
			resource = rc.getResource(path.split("/")[1]);

			resourcePath = new Entity(path);
		}

		if (resource != null && searchTerm == null && ontologyType == null && ontologyTerm == null) {
			if (relationshipString == null) {
				relationshipString = "child";
			}
			try {
				entities = pc
						.traversePath(resource, resourcePath, resource
								.getRelationshipByName(relationshipString),
								(SecureSession) session
										.getAttribute("secureSession"));
			} catch (ResourceInterfaceException e) {
				log.log(Level.INFO,
						"Error in /resourceService/path/" + path
								+ "?relationship=" + relationshipString + " : "
								+ e.getMessage());
				return invalidRequest();
			}
		} else if (searchTerm != null) {
			try {
				entities = pc
						.searchForTerm(resource, resourcePath, searchTerm,
								(SecureSession) session
										.getAttribute("secureSession"));
			} catch (ResourceInterfaceException e) {
				log.log(Level.INFO, "Error in /resourceService/path/" + path
						+ "?searchTerm=" + searchTerm + " : " + e.getMessage());
				return invalidRequest();
			}
		} else if (ontologyType != null && ontologyTerm != null) {
			try {
				entities = pc
						.searchForOntology(resource, resourcePath,
								ontologyType, ontologyTerm,
								(SecureSession) session
										.getAttribute("secureSession"));
			} catch (ResourceInterfaceException e) {
				log.log(Level.INFO,
						"Error in /resourceService/path/" + path
								+ "?searchOntologyType=" + ontologyType
								+ "&searchOntologyTerm" + ontologyTerm + " : "
								+ e.getMessage());
				return invalidRequest();
			}
		} else if (path == null || path.isEmpty()) {
			entities = pc.getAllResourcePaths();
		} else {
			return invalidRequest();
		}

		if (entities != null) {
			for (Entity entity : entities) {
				response.add(entity.toJson());
			}
		}
		return Response.ok(response.build(), MediaType.APPLICATION_JSON)
				.build();

	}

	private Response invalidRequest() {
		JsonObjectBuilder build = Json.createObjectBuilder();
		build.add("status", "Invalid Request");
		build.add("message",
				"The request submitted is not correctly formatted.");
		return Response.status(400).entity(build.build()).build();
	}
}
