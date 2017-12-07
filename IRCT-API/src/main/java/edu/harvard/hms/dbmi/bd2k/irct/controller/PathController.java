/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.controller;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import edu.harvard.hms.dbmi.bd2k.irct.event.IRCTEventListener;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.find.FindInformationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Entity;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.PathResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;

/**
 * A stateless controller that manages the relationships, and paths for a
 * resource
 */
@Stateless
public class PathController {

	@Inject
	private ResourceController rc;

	private Logger logger = Logger.getLogger(this.getClass());
	
	@Inject
	private IRCTEventListener irctEventListener;

	/**
	 * Traverses the path in the resource with the given relationship
	 * 
	 * @param resource
	 *            Resource
	 * @param resourcePath
	 *            Path in the resource
	 * @param relationship
	 *            Relationship type
	 * @param session
	 *            Session to run it in
	 * @return Paths
	 * @throws ResourceInterfaceException
	 *             A resource interface exception occurred
	 */
	public List<Entity> traversePath(Resource resource, Entity resourcePath,
			OntologyRelationship relationship, User user)
			throws ResourceInterfaceException {
		
		logger.debug("traversePath() resource:"+resource.getName()+" resourcePath pui:"+resourcePath.getPui());
		
		if (resource.getImplementingInterface() == null) {
			logger.debug("traversePath() is an interface implementing NULL");
		}
		
		if (resource.getImplementingInterface() instanceof PathResourceImplementationInterface) {
			return ((PathResourceImplementationInterface) resource
					.getImplementingInterface()).getPathRelationship(resourcePath, relationship, user);
		} else {
			logger.error("traversePath() resource `"+resource.getName()+"` does not implement PathResource");
		}
		logger.debug("traversePath() returning NULL");
		return null;
	}

	/**
	 * Searches a resource for a given searchTerm. If the resource is null it
	 * searches all resources for that term regardless of path.
	 * 
	 * @param resource
	 *            Resource
	 * @param resourcePath
	 *            Resource Path
	 * @param findInformation
	 *            Information for the find operation
	 * @param session
	 *            Session to run it in
	 * @return Paths
	 * @throws ResourceInterfaceException
	 *             A resource interface exception occurred
	 */
	public List<Entity> searchForTerm(Resource resource, Entity resourcePath,
			FindInformationInterface findInformation, User user)
			throws ResourceInterfaceException {
		List<Entity> matches = new ArrayList<Entity>();
		List<FindInformationInterface> findInformationList = new ArrayList<FindInformationInterface>();
		findInformationList.add(findInformation);
		
		irctEventListener.beforeFind(resource, resourcePath, findInformationList, user);
		
		for (FindInformationInterface findInformationEntry : findInformationList) {
			if (resource == null) {
				for (Resource searchResource : rc.getPathResources()) {
					matches.addAll(find(
							(PathResourceImplementationInterface) searchResource
									.getImplementingInterface(), null,
							findInformationEntry, user));
				}
				
			} else {
				if (resource.getImplementingInterface() instanceof PathResourceImplementationInterface) {
					matches.addAll(find(
							(PathResourceImplementationInterface) resource
									.getImplementingInterface(),
							resourcePath, findInformationEntry, user));
				}
			}
		}		
		irctEventListener.afterFind(matches, findInformation, user);
		
		return matches;
	}

	private List<Entity> find(PathResourceImplementationInterface resource,
			Entity resourcePath, FindInformationInterface findInformation,
			User user) {
		List<Entity> entities = new ArrayList<Entity>();
		try {
			entities = resource.find(resourcePath, findInformation, user);
		} catch (Exception e) {
			logger.error("find() Unable to search for term on resource " + resource
					+ " message: " + e.getMessage());
		}
		return entities;
	}

	/**
	 * Returns a list of entities that represent all the resources that
	 * implement the Path Resource Interface
	 * 
	 * @return Available Path Resources
	 */
	public List<Entity> getAllResourcePaths() {
		List<Entity> returns = new ArrayList<Entity>();

		for (Resource resource : rc.getPathResources()) {
			Entity entity = new Entity("/" + resource.getName());
			entity.setName(resource.getName());
			entity.setDisplayName(resource.getName());
			returns.add(entity);
		}

		return returns;
	}
}
