/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.cl.rest;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonStructure;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import edu.harvard.hms.dbmi.bd2k.irct.controller.PathController;
import edu.harvard.hms.dbmi.bd2k.irct.controller.ResourceController;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;

/**
 * Creates the resource service for the JAX-RS REST service
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
@Path("/resourceService")
@RequestScoped
public class ResourceRESTService {

	@Inject
	private PathController pc;

	@Inject
	private ResourceController rc;

	/**
	 * Returns a list of resources as a JSON Array
	 * 
	 * @return Resources JSON Array
	 */
	@GET
	@Path("/resources")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure listResource() {
		JsonArrayBuilder paths = Json.createArrayBuilder();

		for (Resource resource : rc.getResources()) {
			paths.add(resource.toJson(3));
		}

		return paths.build();
	}

	/**
	 * Returns a list of the query resources as a JSON Array
	 * 
	 * @return Query Resources JSON Array
	 */
	@GET
	@Path("/queryResources")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure listQueryResource() {
		JsonArrayBuilder paths = Json.createArrayBuilder();

		for (Resource resource : rc.getQueryResources()) {
			paths.add(resource.toJson(2));
		}

		return paths.build();
	}

	/**
	 * Returns a list of the process resources as a JSON Array
	 * 
	 * @return Process Resources JSON Array
	 */
	@GET
	@Path("/processResources")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure listProcessResource() {
		JsonArrayBuilder paths = Json.createArrayBuilder();

		for (Resource resource : rc.getProcessResources()) {
			paths.add(resource.toJson(3));
		}

		return paths.build();
	}

	/**
	 * Returns a JSON Array of Paths from a given resource, path, and
	 * relationship. The default relationship type is CHILD. If the path is
	 * empty then it returns the path root of the resource.
	 * 
	 * 
	 * @param resourceName
	 *            Name of Resource
	 * @param path
	 *            Initial Path
	 * @param relationship
	 *            Relationship Type
	 * @return JSON Array of Paths
	 */
	@GET
	@Path("/path/{resource}{path:.*}")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure getPath(
			@PathParam("resource") String resourceName,
			@PathParam("path") String path,
			@DefaultValue("CHILD") @QueryParam(value = "relationship") String relationship) {

		try {
			JsonArrayBuilder paths = Json.createArrayBuilder();
			Resource resource = rc.getResource(resourceName);
			OntologyRelationship ontologyRelationship = pc
					.getRelationshipFromString(resource, relationship);

			if (path.equals("")) {
				for (edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Path childPath : pc
						.getPathRoot(resource)) {
					paths.add(childPath.toJson());
				}

			} else {
				if (path.startsWith("/")) {
					path = path.substring(1);
				}
				edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Path parentPath = pc
						.getPathFromString(resource, path);
				for (edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Path childPath : pc
						.getPathRelationship(resource, parentPath,
								ontologyRelationship)) {
					paths.add(childPath.toJson());
				}
			}

			return paths.build();

		} catch (IllegalArgumentException | ResourceInterfaceException iae) {

		}

		JsonObjectBuilder build = Json.createObjectBuilder();
		build.add("ERROR", "Unknown Relationship Type");
		return build.build();

	}

}
