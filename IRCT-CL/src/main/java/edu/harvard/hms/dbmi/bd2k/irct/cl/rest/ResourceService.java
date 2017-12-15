/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.cl.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import org.apache.log4j.Logger;

import edu.harvard.hms.dbmi.bd2k.irct.controller.PathController;
import edu.harvard.hms.dbmi.bd2k.irct.controller.ResourceController;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.find.FindByOntology;
import edu.harvard.hms.dbmi.bd2k.irct.model.find.FindByPath;
import edu.harvard.hms.dbmi.bd2k.irct.model.find.FindInformationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Entity;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;

/**
 * Creates a REST interface for the resource service
 */
@Path("/resourceService")
public class ResourceService {

	@Inject
	private ResourceController rc;

	@Inject
	private PathController pc;

	// @Context
	// private HttpServletRequest request;

	@Inject
	private HttpSession session;

	private Logger logger = Logger.getLogger(this.getClass());

	/**
	 * Returns a list of all resources. If a type is chosen then only resources of
	 * that type will be returned. Type of resources to return can also be specified
	 * using the type field.
	 *
	 * @param type
	 *            Type
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
			build.add("message", "The type submitted is not a supported resource type");
			return Response.status(400).entity(build.build()).build();
		}

		for (Resource resource : returnResources) {
			response.add(resource.toJson());
		}
		return Response.ok(response.build(), MediaType.APPLICATION_JSON).build();
	}

	/**
	 * Returns a list of all resources. If a type is chosen then only resources of
	 * that type will be returned. If a name is chosen, then only the named
	 * resource's information will be returned.
	 *
	 * @param name
	 *            The name of the resource to retrieve information about
	 * @param type
	 *            The type of resources, currently supported values are
	 *            process|visualization|query
	 * @return Response
	 */
	@GET
	@Path("/resource")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResourceByArgs(@QueryParam(value = "name") String name,
			@QueryParam(value = "type") String type) {
		logger.debug("GET /resource Starting");

		if (name != null && !name.isEmpty()) {
			// Only get the named resource.
			return success(rc.getResource(name));
		} else {
			List<Resource> returnResources = null;
			if (type == null || type.isEmpty()) {
				return success(rc.getResources());
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
					return error("The type `" + type + "` is unsupported.");
				}
			}
			if (returnResources == null)
				return error("The type `" + type + "` is not a supported resource type");
			if (returnResources.size() < 1)
				return success("There are no resources available.");
			return success(returnResources);
		}
	}

	private Response success(String msg) {
		return Response.status(200).type(MediaType.APPLICATION_JSON)
				.entity(Json.createObjectBuilder().add("message", msg).build()).build();
	}

	private Response success(Object obj) {
		return Response.status(200).type(MediaType.APPLICATION_JSON).entity(obj).build();
	}

	private Response error(Object obj) {
		return Response.status(400).type(MediaType.APPLICATION_JSON).entity(obj).build();
	}

	@GET
	@Path("/find{path : .*}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response find(@PathParam("path") String path, @Context UriInfo info) {
		
		
		FindInformationInterface findInformation;
		if (info.getQueryParameters().containsKey("term")) {
			findInformation = new FindByPath();
		} else if (info.getQueryParameters().containsKey("ontologyTerm")
				&& info.getQueryParameters().containsKey("ontologyType")) {
			findInformation = new FindByOntology();
		} else {
			return error("Find is missing parameters. Use `term` or `ontologyTerm` and `ontologyType`");
		}

		// Set up path information
		Resource resource = null;
		Entity resourcePath = null;

		if (path != null && !path.isEmpty()) {
			path = "/" + path;
			path = path.substring(1);
			resource = rc.getResource(path.split("/")[1]);

			resourcePath = new Entity(path);
		}

		// Load all values into find information object
		for (String key : info.getQueryParameters().keySet()) {
			findInformation.setValue(key, info.getQueryParameters().getFirst(key));
		}

		if (findInformation instanceof FindByPath) {
			String searchTerm = info.getQueryParameters().getFirst("term");
			String strategy = "exact";
			if (searchTerm.charAt(0) == '%') {
				if (searchTerm.charAt(searchTerm.length() - 1) == '%') {
					searchTerm = searchTerm.substring(1, searchTerm.length() - 1);
					strategy = "contains";
				} else {
					searchTerm = searchTerm.substring(1);
					strategy = "right";
				}
			} else if (searchTerm.charAt(searchTerm.length() - 1) == '%') {
				searchTerm = searchTerm.substring(0, searchTerm.length() - 1);
				strategy = "left";
			}
			findInformation.setValue("term", searchTerm);
			findInformation.setValue("strategy", strategy);
		}
		logger.debug("GET /find "+findInformation.toString());

		try {
			List<Entity> entities = pc.searchForTerm(resource, resourcePath, findInformation, (User) session.getAttribute("user"));
			return success(entities);
		} catch (Exception e) {
			return error(e);
		}
	}

	/**
	 * Returns a list of entities. This could be from traversing the paths, or
	 * through searching for a term or an ontology.
	 *
	 * @param path
	 *            Path
	 * @param relationshipString
	 *            Relationship
	 * @return List of entities
	 */
	@GET
	@Path("/path{path : .*}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response path(@PathParam("path") String path, @QueryParam("relationship") String relationshipString) {
		logger.debug("GET /path Starting");
		User currentUser = (User) session.getAttribute("user");
		
		List<Entity> entities = null;

		Resource resource = null;
		Entity resourcePath = null;

		if (path != null && !path.isEmpty()) {
			path = "/" + path;
			path = path.substring(1);
			resource = rc.getResource(path.split("/")[1]);
			resourcePath = new Entity(path);
		}
		if (resource != null) {
			if (relationshipString == null) {
				relationshipString = "child";
			}
			try {
				entities = pc.traversePath(resource, resourcePath, resource.getRelationshipByName(relationshipString),currentUser);
			} catch (ResourceInterfaceException e) {
				return error(e.toString() + "/" + e.getMessage() + " path:" + path);
			}
		} else if (path == null || path.isEmpty()) {
			entities = pc.getAllResourcePaths();
		} else {
			return error("Resource is null and Path is missing.");
		}

		if (entities != null) {
			return success(entities);
		} else {
			return error("Could not find any entities.");
		}
	}
}
