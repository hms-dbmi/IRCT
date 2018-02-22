/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.cl.rest;

import edu.harvard.hms.dbmi.bd2k.irct.IRCTApplication;
import edu.harvard.hms.dbmi.bd2k.irct.cl.feature.JacksonSerialized;
import edu.harvard.hms.dbmi.bd2k.irct.util.IRCTResponse;
import edu.harvard.hms.dbmi.bd2k.irct.controller.PathController;
import edu.harvard.hms.dbmi.bd2k.irct.controller.ResourceController;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.find.FindByOntology;
import edu.harvard.hms.dbmi.bd2k.irct.model.find.FindByPath;
import edu.harvard.hms.dbmi.bd2k.irct.model.find.FindInformationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Entity;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.PathResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.*;

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
    IRCTApplication picsureAPI;
	
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
	public Response resources(@QueryParam(value = "type") String type, @QueryParam(value = "name") String resourceName) {
		if (resourceName != null) {
			return IRCTResponse.success(rc.getResource(resourceName));
		}
		
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
		logger.debug("GET /find type:"+findInformation.getType());

		try {
			logger.debug("GET /find params  :"+findInformation.getRequiredParameters());
			logger.debug("GET /find resource:"+resource.getName());
			logger.debug("GET /find path    :"+resourcePath.getName());
			
			List<Entity> entities = pc.searchForTerm(resource, resourcePath, findInformation, (User) session.getAttribute("user"));
			if (entities == null || entities.size() == 0) {
				return IRCTResponse.applicationError("No entities were found in `"+resource.getName()+"`");
			} else {
				logger.debug("GET /find There were `"+entities.size()+"` entities found.");
				return IRCTResponse.success(entities);
			}
			
		} catch (ResourceInterfaceException e) {
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

		List<String> paths = new ArrayList<>(Arrays.asList(path));

		return getEntitiesFromPaths(paths, relationshipString);
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
	@Path("/objects")
	@Produces(MediaType.APPLICATION_JSON)
	public Response path2(
            @DefaultValue("child") @QueryParam("relationship") String relationshipString,
	        @DefaultValue("*") @QueryParam("path") String path,
            @QueryParam("source") String datasource
    ) {
		logger.debug("GET /objects Starting");
        logger.debug("GET /objects source      :"+datasource);
        logger.debug("GET /objects path        :"+path);
        logger.debug("GET /objects relationship:"+relationshipString);

		List<Entity> entities = null;
		try {

		    if (path.equals("*")) {
		    	entities = pc.getAllResourcePaths();
            } else {
                Resource resource = picsureAPI.getResources().get(datasource);
                Entity resourcePath = new Entity(path);

                if (resource.getImplementingInterface() instanceof PathResourceImplementationInterface) {
                    entities = ((PathResourceImplementationInterface) resource
                            .getImplementingInterface()).getPathRelationship(
                            resourcePath, resource.getRelationshipByName(relationshipString), (User) session.getAttribute("user"));
                } else
                    return IRCTResponse.applicationError(String.format("traversePath() resource `%s` does not implement PathResource", resource.getName()));

            }

        } catch (ResourceInterfaceException e) {
            return IRCTResponse.error(String.format("Exception `%s` with message `%s`.", e.toString(), e.getMessage()));
        }
        logger.debug("GET /objects Finished.");

        if (entities != null) {
            if (entities.size() < 1) {
                return IRCTResponse.success(String.format("No objects were found."));
            } else
                return IRCTResponse.success(entities);
        } else
            return IRCTResponse.error("Could not find any entities.");
	}

	/**
	 * Returns a list of entities based on paths sent in json form:
     * This could be from traversing the paths, or
	 * through searching for a term or an ontology.
	 *
	 * @param relationshipString
	 *            Relationship
     * @param entityPathsJson
     *      List of Json objects with path for resource under the key "pui":
     *       [ {"pui" : "pui1"}, {"pui": "pui2"}
	 * @return List of entities
	 */
	@POST
    @JacksonSerialized
	@Produces(MediaType.APPLICATION_JSON)
    @Path("/jsonPath")
    public Response path_json(@QueryParam("relationship") String relationshipString, List<Entity> entityPathsJson) {
        logger.debug("POST /jsonPath Starting");

        //Return all resources if no specific paths have been requested
        if (entityPathsJson == null || entityPathsJson.isEmpty()){
            return IRCTResponse.success(pc.getAllResourcePaths());
        }

        //Pull all paths from entities
        List<String> paths = new ArrayList<>();
        for(Entity entity : entityPathsJson){
            paths.add(entity.getPui());
        }

        return getEntitiesFromPaths(paths, relationshipString);
    }

    private Response getEntitiesFromPaths(List<String> paths, String relationshipString) {
        User currentUser = (User) session.getAttribute("user");

        //This will contain all resources from all paths
        List<Entity> allResources = new ArrayList<>();


        for (String path : paths) {
            List<Entity> fetchedResources = null;
            Resource resource = null;
            Entity entity = null;

            if (StringUtils.isNotBlank(path)) {
                if (!path.startsWith("/"))
                    path = "/" + path;

                resource = rc.getResource(path.split("/")[1]);
                entity = new Entity(path);
            }

            if (resource != null) {
                if (relationshipString == null) {
                    relationshipString = "child";
                }

                try {
                    fetchedResources = pc.traversePath(resource, entity, resource.getRelationshipByName(relationshipString), currentUser);
                } catch (ResourceInterfaceException e) {
                    logger.error("Unable to fetch resources: ", e);
                    return IRCTResponse.riError(e.getMessage());
                }
            } else if (StringUtils.isBlank(path)) {
            	fetchedResources = pc.getAllResourcePaths();
            } else {
                return IRCTResponse.protocolError(Response.Status.BAD_REQUEST, "Resource is null and Path is incorrect, nonexistent or malformed");
            }

            //If any resources were successfully fetched, add to the list
            //Note: if a list with multiple blank puis is sent, this will result in duplicate results
            if (fetchedResources != null) {
                allResources.addAll(fetchedResources);
            }

        }

        //If it reaches here empty, presumably the paths were leaf nodes, so we do want to send back the empty list
		return IRCTResponse.success(allResources);
    }

}
