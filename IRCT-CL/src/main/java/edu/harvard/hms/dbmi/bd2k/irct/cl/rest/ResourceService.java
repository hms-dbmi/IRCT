/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.cl.rest;

import java.util.ArrayList;
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

import com.google.common.collect.ImmutableMap;

import edu.harvard.hms.dbmi.bd2k.irct.cl.feature.JacksonSerialized;
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
	ResourceController rc;

	@Inject
	PathController pc;
	
	//@Context
	//private HttpServletRequest request;

	@Inject
	private HttpSession session;

	/**
	 * Returns a list of all resources. If a type is chosen then only resources
	 * of that type will be returned. Type of resources to return can also be
	 * specified using the type field.
	 *
	 * @param type
	 *            Type
	 * @return Response
	 */
	@GET
	@JacksonSerialized
	@Path("/resources")
	@Produces(MediaType.APPLICATION_JSON)
	public Response resources(@QueryParam(value = "type") String type) {
		return Response.ok(rc.getResourcesOfType(type), MediaType.APPLICATION_JSON)
				.build();
	}

	/**
	 * Returns a list of categories that can be searched for
	 *
	 * @return Category list
	 */
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

	/**
	 * Searches the resources for ones that match a category
	 *
	 * @param info
	 *            URI information
	 * @return List of resources that match that category
	 */
	@GET
	@Path("/search")
	@JacksonSerialized
	@Produces(MediaType.APPLICATION_JSON)
	public Response search(@Context UriInfo info) {
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

		return Response.ok(rc.search(searchParams), MediaType.APPLICATION_JSON)
				.build();
	}

	@GET
	@Path("/find{path : .*}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response find(@PathParam("path") String path, @Context UriInfo info) {
		JsonArrayBuilder response = Json.createArrayBuilder();
		List<Entity> entities = null;
		FindInformationInterface findInformation;

		if(info.getQueryParameters().containsKey("term")) {
			findInformation = new FindByPath();
		} else if(info.getQueryParameters().containsKey("ontologyTerm") && info.getQueryParameters().containsKey("ontologyType")) {
			findInformation = new FindByOntology();
		} else {
			return invalidRequest("Find is missing parameters");
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
		for(String key : info.getQueryParameters().keySet()) {
			findInformation.setValue(key, info.getQueryParameters().getFirst(key));
		}

		if(findInformation instanceof FindByPath) {
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

		// Run find
		try {
			entities = pc.searchForTerm(resource, resourcePath, findInformation, (User) session.getAttribute("user"));
		} catch (ResourceInterfaceException e) {
			return invalidRequest(e.getMessage());
		}

		if (entities != null) {
			for (Entity entity : entities) {
				response.add(entity.toJson());
			}
		}

		return Response.ok(response.build(), MediaType.APPLICATION_JSON).build();
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
		if (resource != null) {
			if (relationshipString == null) {
				relationshipString = "child";
			}
			try {
				entities = pc.traversePath(resource, resourcePath,
						resource.getRelationshipByName(relationshipString),
						(User) session.getAttribute("user"));
			} catch (ResourceInterfaceException e) {
				return invalidRequest(e.toString()+"/"+e.getMessage()+" path:"+path);
			}
		} else if (path == null || path.isEmpty()) {
			entities = pc.getAllResourcePaths();
		} else {
			return invalidRequest("Resource is null and Path is missing.");
		}

		if (entities != null) {
			for (Entity entity : entities) {
				response.add(entity.toJson());
			}
		}
		return Response.ok(response.build(), MediaType.APPLICATION_JSON)
				.build();

	}

	private Response invalidRequest(String message) {
		JsonObjectBuilder build = Json.createObjectBuilder();
		build.add("status", "Invalid Request");
		if (message == null) {
			build.add("message", "The request submitted returned an error");
		} else {
			build.add("message", "The request submitted returned an error: "
					+ message);
		}
		return Response.status(400).entity(build.build()).build();
	}
}
