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

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.DataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyType;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Path;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.ClauseAbstract;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.WhereClause;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.PathResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.QueryResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ResourceState;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Column;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.MemoryResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;
import edu.harvard.hms.dbmi.bd2k.irct.ri.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.i2b2.api.exception.I2B2InterfaceException;
import edu.harvard.hms.dbmi.i2b2.api.crc.CRCCell;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.pdo.OutputOptionSelectType;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.pdo.ParamType;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.pdo.PatientDataResponseType;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.pdo.PatientSet;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.pdo.PatientType;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.psm.ItemType;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.psm.MasterInstanceResultResponseType;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.psm.PanelType;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.psm.QueryResultInstanceType;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.psm.ResultOutputOptionListType;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.psm.ResultOutputOptionType;
import edu.harvard.hms.dbmi.i2b2.api.ont.ONTCell;
import edu.harvard.hms.dbmi.i2b2.api.ont.xml.ConceptType;
import edu.harvard.hms.dbmi.i2b2.api.ont.xml.ConceptsType;
import edu.harvard.hms.dbmi.i2b2.api.ont.xml.ModifierType;
import edu.harvard.hms.dbmi.i2b2.api.pm.PMCell;

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
	private CRCCell crcCell;
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

			// Initiate and setup the CRC Cell
			crcCell = new CRCCell();
			crcCell.setup(parameters);

			// Create the HTTPClient
			client = HttpClients.createDefault();

			serverName = parameters.get("serverName");

		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getType() {
		return "i2b2XML";
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
			} catch (JAXBException | IOException | I2B2InterfaceException e) {
				throw new ResourceInterfaceException("Error traversing relationship", e);

			}
		} else {
			throw new ResourceInterfaceException(relationship.toString()
					+ " not supported by this resource");
		}
	}

	public Path getPathFromString(String path) {
		Path pathObj = new Path();
		pathObj.setPui(path);
		return pathObj;
	}

	public Long run(Query qep) throws ResourceInterfaceException {
		PanelType panel = new PanelType();
		panel.setPanelNumber(1);
		panel.setPanelAccuracyScale(100);
		panel.setInvert(0);
		panel.setPanelTiming("ANY");
		PanelType.TotalItemOccurrences tio = new PanelType.TotalItemOccurrences();
		tio.setValue(1);
		panel.setTotalItemOccurrences(tio);

		for (ClauseAbstract clause : qep.getClauses().values()) {
			if (clause instanceof WhereClause) {
				panel.getItem().add(
						createItemTypeFromWhereClause((WhereClause) clause));
			}
		}
		ResultOutputOptionListType roolt = new ResultOutputOptionListType();
		ResultOutputOptionType root = new ResultOutputOptionType();
		root.setPriorityIndex(9);
		root.setName("patientset");
		roolt.getResultOutput().add(root);
		HttpClient client = HttpClients.createDefault();

		Long queryId = 0L;
		try {
			MasterInstanceResultResponseType mirrt = crcCell
					.runQueryInstanceFromQueryDefinition(client, null, null,
							"Hi Michael", null, "ANY", 0, roolt, panel);

			queryId = Long.parseLong(mirrt.getQueryInstance()
					.getQueryMasterId());
		} catch (JAXBException | IOException | I2B2InterfaceException e) {
			throw new ResourceInterfaceException("Error traversing relationship", e);
		}
		return queryId;
	}

	public ResultSet getResults(Long queryId) throws ResourceInterfaceException {
		HttpClient client = HttpClients.createDefault();
		try {
			List<QueryResultInstanceType> response = crcCell
					.getQueryResultInstanceListFromQueryInstanceId(client,
							queryId.toString());
			String resultInstanceId = response.get(0).getResultInstanceId();
			PatientDataResponseType pdrt = crcCell.getPDOfromInputList(client,
					resultInstanceId, 0, 100000, false, false, false,
					OutputOptionSelectType.USING_INPUT_LIST);

			return convertPatientSetToResultSet(pdrt.getPatientData()
					.getPatientSet());

		} catch (JAXBException | IOException | I2B2InterfaceException e) {
			throw new ResourceInterfaceException("Error getting results", e);
		}
	}

	public ResourceState getState() {
		// TODO Auto-generated method stub
		return null;
	}

	public JsonObject toJson() {
		// TODO Auto-generated method stub
		return null;
	}

	public JsonObject toJson(int depth) {
		// TODO Auto-generated method stub
		return null;
	}

	public Path getReturnEntity() {
		// TODO Auto-generated method stub
		return null;
	}

	// -------------------------------------------------------------------------
	// Utility Methods
	// -------------------------------------------------------------------------

	private ResultSet convertPatientSetToResultSet(PatientSet patientSet) {
		MemoryResultSet mrs = new MemoryResultSet();
		try {
			if(patientSet.getPatient().size() == 0) {
				return mrs;
			}
			PatientType columnPT = patientSet.getPatient().get(0);
			Column idColumn = new Column();
			idColumn.setName("Patient Id");
			mrs.appendColumn(idColumn);
			for (ParamType paramType : columnPT.getParam()) {
				Column column = new Column();
				column.setName(paramType.getColumn());
				column.setDataType(DataType.STRING);
				mrs.appendColumn(column);
			}

			for (PatientType patientType : patientSet.getPatient()) {
				mrs.appendRow();
				mrs.updateString("Patient Id", patientType.getPatientId()
						.getValue());
				for (ParamType paramType : patientType.getParam()) {
					mrs.updateString(paramType.getColumn(),
							paramType.getValue());
				}
			}
		} catch (ResultSetException e) {
			e.printStackTrace();
		}
		return mrs;
	}

	private ItemType createItemTypeFromWhereClause(WhereClause whereClause) {
		ItemType item = new ItemType();
		// item.setHlevel(4);
		// item.setItemName("mild dementia");
		item.setItemKey(whereClause.getField().getPui());
		// item.setTooltip("oasis \\ Clinical Measures \\ Clinical Dementia Rating (CDR) \\ mild dementia");
		// item.setClazz("ENC");
		// item.setItemIcon("LA");
		// item.setItemIsSynonym(false);

		return item;
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
}
