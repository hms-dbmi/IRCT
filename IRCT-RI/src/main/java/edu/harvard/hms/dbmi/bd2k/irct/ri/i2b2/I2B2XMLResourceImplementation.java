/*
 *  This file is part of Inter-Resource Communication Tool (IRCT).
 *
 *  IRCT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  IRCT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with IRCT.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.JsonObject;
import javax.xml.bind.JAXBException;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyType;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Path;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.PathResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.QueryResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ResourceState;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.ri.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.message.ont.ONTCell;
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.message.ont.xml.ConceptType;
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.message.ont.xml.ConceptsType;
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.message.ont.xml.ModifierType;
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.message.pm.PMCell;

/**
 * An implementation of a resource that communicates with the i2b2 servers via
 * XML
 * 
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class I2B2XMLResourceImplementation implements
		QueryResourceImplementationInterface,
		PathResourceImplementationInterface {
	private ONTCell ontCell;
	private PMCell pmCell;
	private HttpClient client;
	private String serverName;

	public void setup(Map<String, String> parameters) {

		try {
			// Initiate and setup Ontology Cell
			ontCell = new ONTCell();
			ontCell.setup(parameters);

			// Initiate and setup PM Cell
			pmCell = new PMCell();
			pmCell.setup(parameters);

			// Create the HTTPClient
			client = HttpClients.createDefault();

			serverName = parameters.get("serverName");

		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getType() {
		return "i2b2";
	}

	public OntologyType getOntologyType() {
		return OntologyType.TREE;
	}

	public Path getPathRoot() {
		Path root = new Path();
		root.setName(serverName);
		root.setPui(serverName);

		return root;
	}

	public List<Path> getPathRelationship(Path path,
			OntologyRelationship relationship)
			throws ResourceInterfaceException {

		if (relationship == OntologyRelationship.CHILD) {
			try {
				ConceptsType conceptsType = null;
				if (path.getPui().equals(serverName)) {

					conceptsType = ontCell.getCategories(client, false, false,
							true, "core");

				} else {
					conceptsType = ontCell.getChildren(client, path.getPui()
							.replaceAll(serverName + "/", "")
							.replace('/', '\\'), false, false, false, -1,
							"core");
				}
				return convertConceptsTypeToPath(conceptsType);
			} catch (JAXBException | IOException e) {
				e.printStackTrace();

			}
		} else {
			throw new ResourceInterfaceException(relationship.toString()
					+ " not supported by this resource");
		}
		return null;
	}

	private List<Path> convertConceptsTypeToPath(ConceptsType conceptsType) {
		List<Path> returns = new ArrayList<Path>();
		for (ConceptType concept : conceptsType.getConcept()) {
			Path childPath = new Path();
			childPath.setName(concept.getName());
			childPath.setPui(serverName + "/"
					+ concept.getKey().replace('\\', '/'));
			Map<String, String> attributes = new HashMap<String, String>();
			attributes.put("level", Integer.toString(concept.getLevel()));
			attributes.put("key", concept.getKey());
			attributes.put("name", concept.getName());
			attributes.put("synonymCd", concept.getSynonymCd());
			attributes.put("visualattributes", concept.getVisualattributes());
			if (concept.getTotalnum() != null) {
				attributes.put("totalnum", concept.getTotalnum().toString());
			}
			attributes.put("basecode", concept.getBasecode());
			// attributes.put("metadataxml", concept.getMetadataxml().
			attributes.put("facttablecolumn", concept.getFacttablecolumn());
			attributes.put("tablename", concept.getTablename());
			attributes.put("columnname", concept.getColumnname());
			attributes.put("columndatatype", concept.getColumndatatype());
			attributes.put("operator", concept.getOperator());
			attributes.put("dimcode", concept.getDimcode());
			attributes.put("comment", concept.getComment());
			attributes.put("tooltip", concept.getTooltip());
			// attributes.put("updateDate", concept.getU
			// attributes.put("downloadDate", concept.getDownloadDate());
			// attributes.put("importDate", concept.getDownloadDate());
			attributes.put("sourcesystemCd", concept.getSourcesystemCd());
			attributes.put("valuetypeCd", concept.getValuetypeCd());
			// attributes.put("modifier", concept.getModifier().
			ModifierType modifier = concept.getModifier();
			if (modifier != null) {
				attributes.put("modifier.level",
						Integer.toString(modifier.getLevel()));
				attributes.put("modifier.appliedPath",
						modifier.getAppliedPath());
				attributes.put("modifier.key", modifier.getKey());
				attributes.put("modifier.fullname", modifier.getFullname());
				attributes.put("modifier.name", modifier.getName());
				attributes.put("modifier.visualattributes",
						modifier.getVisualattributes());
				attributes.put("modifier.synonymCd", modifier.getSynonymCd());
				attributes.put("modifier.totalnum", modifier.getTotalnum()
						.toString());
				attributes.put("modifier.basecode", modifier.getBasecode());
				// attributes.put("modifier.metadataxml",
				// modifier.getMetadataxml());
				attributes.put("modifier.facttablecolumn",
						modifier.getFacttablecolumn());
				attributes.put("modifier.tablename", modifier.getTablename());
				attributes.put("modifier.columnname", modifier.getColumnname());
				attributes.put("modifier.columndatatype",
						modifier.getColumndatatype());
				attributes.put("modifier.operator", modifier.getOperator());
				attributes.put("modifier.dimcode", modifier.getDimcode());
				attributes.put("modifier.comment", modifier.getComment());
				attributes.put("modifier.tooltip", modifier.getTooltip());
				// attributes.put("modifier.updateDate",
				// modifier.getUpdateDate());
				// attributes.put("modifier.downloadDate",
				// modifier.getDownloadDate());
				// attributes.put("modifier.importDate",
				// modifier.getImportDate());
				attributes.put("modifier.sourcesystemCd",
						modifier.getSourcesystemCd());
			}
			childPath.setAttributes(attributes);
			returns.add(childPath);
		}

		return returns;
	}

	public Path getPathFromString(String path) {
		Path pathObj = new Path();
		pathObj.setPui(path);
		return pathObj;
	}

	public JsonObject toJson() {
		// TODO Auto-generated method stub
		return null;
	}

	public JsonObject toJson(int depth) {
		// TODO Auto-generated method stub
		return null;
	}

	public Long run(Query qep) {
		// TODO Auto-generated method stub
		return null;
	}

	public ResultSet getResults(Long queryId) {
		// TODO Auto-generated method stub
		return null;
	}

	public ResourceState getState() {
		// TODO Auto-generated method stub
		return null;
	}

	public Path getReturnEntity() {
		// TODO Auto-generated method stub
		return null;
	}

}
