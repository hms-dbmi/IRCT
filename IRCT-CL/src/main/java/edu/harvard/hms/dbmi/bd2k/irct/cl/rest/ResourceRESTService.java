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
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;

@Path("/resourceService")
@RequestScoped
public class ResourceRESTService {

	@Inject
	private PathController pc;

	@Inject
	private ResourceController rc;

	@GET
	@Path("/hello")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure test() {
		// TODO: REMOVE ME
		JsonObjectBuilder build = Json.createObjectBuilder();
		build.add("hello", "world");
		return build.build();

	}

	@GET
	@Path("/resources")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure listResource() {
		JsonArrayBuilder paths = Json.createArrayBuilder();

		for (Resource resource : rc.getResources()) {
			paths.add(resource.toJson());
		}

		return paths.build();
	}

	@GET
	@Path("/queryResources")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure listQueryResource() {
		JsonArrayBuilder paths = Json.createArrayBuilder();

		for (Resource resource : rc.getQueryResources()) {
			paths.add(resource.toJson());
		}

		return paths.build();
	}

	@GET
	@Path("/processResources")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonStructure listProcessResource() {
		JsonArrayBuilder paths = Json.createArrayBuilder();

		for (Resource resource : rc.getProcessResources()) {
			paths.add(resource.toJson());
		}

		return paths.build();
	}

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
			OntologyRelationship ontologyRelationship = OntologyRelationship
					.valueOf(relationship);

			if (path.equals("")) {
				paths.add(pc.getPathRoot(resource).toJson());
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

		} catch (IllegalArgumentException iae) {

		}

		JsonObjectBuilder build = Json.createObjectBuilder();
		build.add("ERROR", "Unknown Relationship Type");
		return build.build();

	}

}
