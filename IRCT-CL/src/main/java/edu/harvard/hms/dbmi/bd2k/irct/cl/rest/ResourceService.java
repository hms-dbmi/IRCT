/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.cl.rest;

import edu.harvard.hms.dbmi.bd2k.irct.cl.feature.JacksonSerialized;
import edu.harvard.hms.dbmi.bd2k.irct.cl.util.IRCTResponse;
import edu.harvard.hms.dbmi.bd2k.irct.controller.PathController;
import edu.harvard.hms.dbmi.bd2k.irct.controller.ResourceController;
import edu.harvard.hms.dbmi.bd2k.irct.model.find.FindByOntology;
import edu.harvard.hms.dbmi.bd2k.irct.model.find.FindByPath;
import edu.harvard.hms.dbmi.bd2k.irct.model.find.FindInformationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Entity;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates a REST interface for the resource service
 */
@Path("/resourceService")
public class ResourceService {

	@Inject
	ResourceController rc;

	@Inject
	PathController pc;
	
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
	@JacksonSerialized
	@Path("/resources")
	@Produces(MediaType.APPLICATION_JSON)
	public Response resources(@QueryParam(value = "type") String type) {

		return Response.ok(rc.getResourcesOfType(type), MediaType.APPLICATION_JSON)
				.build();
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
			return IRCTResponse.success(rc.getResource(name));
		} else {
			List<Resource> returnResources = null;
			if (type == null || type.isEmpty()) {
				return IRCTResponse.success(rc.getResources());
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
					return IRCTResponse.error("The type `" + type + "` is unsupported.");
				}
			}
			if (returnResources == null)
				return IRCTResponse.error("The type `" + type + "` is not a supported resource type");
			if (returnResources.size() < 1)
				return IRCTResponse.success("There are no resources available.");
			return IRCTResponse.success(returnResources);
		}
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
			searchParams.put(categoryName, values);
		}

		return IRCTResponse.applicationError("This feature is not yet implemented.");
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
			return IRCTResponse.error("Find is missing parameters. Use `term` or `ontologyTerm` and `ontologyType`");
		}

		// Set up path information
		Resource resource = null;
		Entity resourcePath = null;

		if (path != null && !path.isEmpty()) {
			if (!path.startsWith("/"))
				path = "/" + path;
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
			return IRCTResponse.success(entities);
		} catch (Exception e) {
			return IRCTResponse.error(e);
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
			if (!path.startsWith("/"))
				path = "/" + path;

			resource = rc.getResource(path.split("/")[1]);
			resourcePath = new Entity(path);
		}
		
		if (resource != null) {
			if (relationshipString == null) {
				relationshipString = "child";
			}
			
			try {
				entities = pc.traversePath(resource, resourcePath, resource.getRelationshipByName(relationshipString),currentUser);
			} catch (Exception e) {
				return IRCTResponse.error(e.getMessage());
			}
			
		} else if (path == null || path.isEmpty()) {
			try {
				entities = pc.getAllResourcePaths();
			} catch (Exception e) {
				logger.error("GET /path Exception:",e);
				return IRCTResponse.error(e.getMessage());
			}
		} else {
			return IRCTResponse.error("Resource is null and Path is missing.");
		}

		if (entities != null) {
			return IRCTResponse.success(entities);
		} else {
			return IRCTResponse.error("Could not find any entities.");
		}
	}
}
