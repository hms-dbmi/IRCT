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

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;

import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.DataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyType;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Path;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.ClauseAbstract;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.LogicalOperator;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.WhereClause;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.PathResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.QueryResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ResourceState;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Column;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.FileResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;
import edu.harvard.hms.dbmi.bd2k.irct.ri.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.i2b2.api.exception.I2B2InterfaceException;
import edu.harvard.hms.dbmi.i2b2.api.crc.CRCCell;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.pdo.OutputOptionSelectType;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.pdo.ParamType;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.pdo.PatientDataResponseType;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.pdo.PatientSet;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.pdo.PatientType;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.psm.ConstrainDateTimeType;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.psm.ConstrainDateType;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.psm.ConstrainOperatorType;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.psm.ConstrainValueType;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.psm.InclusiveType;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.psm.ItemType;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.psm.MasterInstanceResultResponseType;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.psm.PanelType;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.psm.ResultOutputOptionListType;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.psm.ResultOutputOptionType;
import edu.harvard.hms.dbmi.i2b2.api.ont.ONTCell;
import edu.harvard.hms.dbmi.i2b2.api.ont.xml.ConceptType;
import edu.harvard.hms.dbmi.i2b2.api.ont.xml.ConceptsType;
import edu.harvard.hms.dbmi.i2b2.api.ont.xml.ModifierType;
import edu.harvard.hms.dbmi.i2b2.api.ont.xml.ModifiersType;
import edu.harvard.hms.dbmi.i2b2.api.ont.xml.XmlValueType;
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
			String userName = parameters.get("i2b2username");
			String domain = parameters.get("i2b2domain");
			String password = parameters.get("i2b2password");
			String projectId = parameters.get("i2b2projectID");
			String ontConnectionURL = parameters.get("ONTConnectionURL");
			String crcConnectionURL = parameters.get("CRCConnectionURL");
			String pmConnectionURL = parameters.get("PMConnectionURL");

			// Initiate and setup Ontology Cell
			ontCell = new ONTCell();
			ontCell.setup(ontConnectionURL, domain, userName, password,
					projectId);

			// Initiate and setup PM Cell
			pmCell = new PMCell();
			pmCell.setup(pmConnectionURL, domain, userName, password, projectId);

			// Initiate and setup the CRC Cell
			crcCell = new CRCCell();
			crcCell.setup(crcConnectionURL, domain, userName, password,
					projectId);

			// Create the HTTPClient
			setClient(HttpClients.createDefault());

			setServerName(parameters.get("serverName"));

			// Setup predicates

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

	public List<Path> getPathRoot() {
		Path root = new Path();
		root.setName(getServerName());
		root.setPui(getServerName());
		List<Path> roots = new ArrayList<Path>();
		roots.add(root);
		return roots;
	}

	public List<Path> getPathRelationship(Path path,
			OntologyRelationship relationship)
			throws ResourceInterfaceException {

		if (relationship == I2B2OntologyRelationship.CHILD) {
			try {
				ConceptsType conceptsType = null;

				if (path.getPui().equals(getServerName())) {

					conceptsType = ontCell.getCategories(getClient(), false,
							false, true, "core");

				} else {
					String self = path.getPui()
							.replaceAll(getServerName() + "/", "")
							.replace('/', '\\');
					conceptsType = ontCell.getChildren(getClient(), self,
							false, false, false, -1, "core");

				}
				List<Path> paths = convertConceptsTypeToPath(conceptsType);

				return paths;
			} catch (JAXBException | IOException | I2B2InterfaceException e) {
				throw new ResourceInterfaceException(
						"Error traversing relationship", e);

			}
		} else if (relationship == I2B2OntologyRelationship.MODIFIER) {
			try {
				ModifiersType modifiersType = null;
				if (!path.getPui().equals(getServerName())) {
					String self = path.getPui()
							.replaceAll(getServerName() + "/", "")
							.replace('/', '\\');
					if (self.lastIndexOf('\\') != self.length() - 1) {
						self += '\\';
					}
					modifiersType = ontCell.getModifiers(client, false, false,
							null, -1, self, false, null);
				}
				List<Path> paths = convertModifiersTypeToPath(modifiersType, "");
				return paths;
			} catch (JAXBException | IOException | I2B2InterfaceException e) {
				throw new ResourceInterfaceException(
						"Error obtaining modifiers", e);

			}
		} else if (relationship == I2B2OntologyRelationship.TERM) {
			try {
				ConceptsType conceptsType = null;
				if (!path.getPui().equals(getServerName())) {
					String self = path.getPui()
							.replaceAll(getServerName() + "/", "")
							.replace('/', '\\');
					if (self.lastIndexOf('\\') != self.length() - 1) {
						self += '\\';
					}
					conceptsType = ontCell.getTermInfo(client, true, self,
							true, -1, true, "core");
				}
				List<Path> paths = convertConceptsTypeToPath(conceptsType);
				return paths;

			} catch (JAXBException | IOException | I2B2InterfaceException e) {
				throw new ResourceInterfaceException("Error obtaining type", e);
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

	public Long runQuery(Query qep) throws ResourceInterfaceException {
		int panelCount = 1;
		ArrayList<PanelType> panels = new ArrayList<PanelType>();
		
		
		PanelType currentPanel = createPanel(panelCount);
		for (ClauseAbstract clause : qep.getClauses().values()) {
			if (clause instanceof WhereClause) {
				WhereClause whereClause = (WhereClause) clause;
				ItemType itemType = createItemTypeFromWhereClause((WhereClause) clause);

				//FIRST
				if(panels.isEmpty() && currentPanel.getItem().isEmpty()) {
					currentPanel.getItem().add(itemType);
				} else if(whereClause.getLogicalOperator() == LogicalOperator.AND) {
					panels.add(currentPanel);
					currentPanel = createPanel(panelCount++);
					currentPanel.getItem().add(itemType);
				} else if (whereClause.getLogicalOperator() == LogicalOperator.OR) {
					currentPanel.getItem().add(itemType);
				} else if (whereClause.getLogicalOperator() == LogicalOperator.NOT) {
					panels.add(currentPanel);
					currentPanel = createPanel(panelCount++);
					currentPanel.getItem().add(itemType);
					currentPanel.setInvert(1);
					panels.add(currentPanel);
					currentPanel = createPanel(panelCount++);
				}				
			}
		}
		if (currentPanel.getItem().size() != 0) {
			panels.add(currentPanel);
		}

		ResultOutputOptionListType roolt = new ResultOutputOptionListType();
		ResultOutputOptionType root = new ResultOutputOptionType();
		root.setPriorityIndex(10);
		root.setName("PATIENTSET");
		roolt.getResultOutput().add(root);

		Long queryId = 0L;
		try {
			MasterInstanceResultResponseType mirrt = crcCell
					.runQueryInstanceFromQueryDefinition(client, null, null,
							"IRCT", null, "SAMEVISIT", 0, roolt,
							panels.toArray(new PanelType[panels.size()]));
			
			queryId = Long.parseLong(mirrt.getQueryResultInstance().get(0).getResultInstanceId());
		} catch (JAXBException | IOException | I2B2InterfaceException e) {
			throw new ResourceInterfaceException(
					"Error traversing relationship", e);
		}
		return queryId;
	}

	private PanelType createPanel(int panelItem) {
		PanelType panel = new PanelType();
		panel.setPanelNumber(panelItem);
		panel.setInvert(0);
		panel.setPanelTiming("ANY");
		
		PanelType.TotalItemOccurrences tio = new PanelType.TotalItemOccurrences();
		tio.setValue(1);
		panel.setTotalItemOccurrences(tio);

		return panel;
	}

	public ResultSet getResults(Long queryId) throws ResourceInterfaceException {
		try {
			
			String resultInstanceId = queryId.toString();
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
		return toJson(1);
	}

	public JsonObject toJson(int depth) {
		depth--;
		JsonObjectBuilder returnJSON = Json.createObjectBuilder();
		returnJSON.add("type", this.getType());
		return returnJSON.build();
	}

	public Path getReturnEntity() {
		// TODO Auto-generated method stub
		return null;
	}

	// -------------------------------------------------------------------------
	// Utility Methods
	// -------------------------------------------------------------------------

	private ResultSet convertPatientSetToResultSet(PatientSet patientSet) {
		FileResultSet mrs = new FileResultSet();

		try {
			if (patientSet.getPatient().size() == 0) {
				return mrs;
			}
			PatientType columnPT = patientSet.getPatient().get(0);
			Column idColumn = new Column();
			idColumn.setName("Patient Id");
			idColumn.setDataType(DataType.STRING);
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
		} catch (ResultSetException | PersistableException e) {
			e.printStackTrace();
		}
		return mrs;
	}

	private ItemType createItemTypeFromWhereClause(WhereClause whereClause) {
		ItemType item = new ItemType();
		item.setHlevel(3);//????
		item.setClazz("ENC");//???
		item.setItemKey(whereClause.getField().getPui()
				.replaceAll(getServerName() + "/", "").replace('/', '\\'));
		item.setItemName(item.getItemKey());
		if (whereClause.getPredicateType() != null) {
			if (whereClause.getPredicateType().getName()
					.equals("CONSTRAIN_MODIFIER")) {

				item.setConstrainByModifier(createConstrainByModifier(whereClause));
			} else if (whereClause.getPredicateType().getName()
					.equals("CONSTRAIN_VALUE")) {
				item.getConstrainByValue().add(
						createConstrainByValue(whereClause));
			} else if (whereClause.getPredicateType().getName()
					.equals("CONSTRAIN_DATE")) {
				item.getConstrainByDate().add(
						createConstrainByDate(whereClause));
			}
		}
		return item;
	}

	private ItemType.ConstrainByValue createConstrainByValue(
			WhereClause whereClause) {
		ItemType.ConstrainByValue cbv = new ItemType.ConstrainByValue();
		// value_operator
		cbv.setValueOperator(ConstrainOperatorType.fromValue(whereClause
				.getValues().get("value_operator")));
		// value_constraint
		cbv.setValueConstraint(whereClause.getValues().get("value_constraint"));
		// value_unit_of_measure
		cbv.setValueUnitOfMeasure(whereClause.getValues().get(
				"value_unit_of_measure"));
		// value_type
		cbv.setValueType(ConstrainValueType.fromValue(whereClause.getValues()
				.get("value_type")));

		return cbv;
	}

	private ItemType.ConstrainByModifier createConstrainByModifier(
			WhereClause whereClause) {
		ItemType.ConstrainByModifier cbm = new ItemType.ConstrainByModifier();
		cbm.setModifierKey(whereClause.getValues().get("modifier_key")
				.replaceAll(getServerName() + "/", "").replace('/', '\\'));
		return cbm;
	}

	private ItemType.ConstrainByDate createConstrainByDate(
			WhereClause whereClause) {
		ItemType.ConstrainByDate cbd = new ItemType.ConstrainByDate();
		try {
			ConstrainDateType from = new ConstrainDateType();
			from.setInclusive(InclusiveType.fromValue(whereClause.getValues()
					.get("value_from_inclusive")));
			from.setTime(ConstrainDateTimeType.fromValue(whereClause
					.getValues().get("value_from_time")));

			from.setValue(DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(
							whereClause.getValues().get("value_to_date")));

			cbd.setDateFrom(from);

			ConstrainDateType to = new ConstrainDateType();
			to.setInclusive(InclusiveType.fromValue(whereClause.getValues()
					.get("value_to_inclusive")));
			to.setTime(ConstrainDateTimeType.fromValue(whereClause.getValues()
					.get("value_to_time")));
			to.setValue(DatatypeFactory.newInstance().newXMLGregorianCalendar(
					whereClause.getValues().get("value_to_date")));
			cbd.setDateTo(to);
		} catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cbd;
	}

	private List<Path> convertModifiersTypeToPath(ModifiersType modifiersType,
			String parent) {
		List<Path> returns = new ArrayList<Path>();
		if (modifiersType == null) {
			return returns;
		}
		for (ModifierType modifier : modifiersType.getModifier()) {
			Path childPath = new Path();
			childPath.setName(modifier.getName());
			childPath.setPui(getServerName() + "/"
					+ modifier.getKey().replace('\\', '/'));
			if (modifier.getColumndatatype().equals("T")) {
				childPath.setDataType(DataType.STRING);
			}

			Map<String, String> attributes = new HashMap<String, String>();

			attributes.put("appliedPath", modifier.getAppliedPath());
			attributes.put("baseCode", modifier.getBasecode());
			attributes.put("columnName", modifier.getColumnname());
			attributes.put("comment", modifier.getComment());
			attributes.put("dimCode", modifier.getDimcode());
			// attributes.put("downloadDate", modifier.getDownloadDate());
			attributes.put("factTableColumn", modifier.getFacttablecolumn());
			attributes.put("fullName", modifier.getFullname());
			// attributes.put("importDate", modifier.getImportDate());
			attributes.put("level", Integer.toString(modifier.getLevel()));
			// attributes.put("metadataXML", modifier.getMetadataxml());
			attributes.put("operator", modifier.getOperator());
			attributes.put("sourceSystemCd", modifier.getSourcesystemCd());
			attributes.put("synonymCd", modifier.getSynonymCd());
			attributes.put("tableName", modifier.getTablename());
			attributes.put("toolTip", modifier.getTooltip());
			if (modifier.getTotalnum() != null) {
				attributes.put("totalNum",
						Integer.toString(modifier.getTotalnum()));
			}
			// attributes.put("updateDate", modifier.getUpdateDate());
			attributes.put("visualAttributes", modifier.getVisualattributes());
			childPath.setAttributes(attributes);
			returns.add(childPath);
		}

		return returns;
	}

	private List<Path> convertConceptsTypeToPath(ConceptsType conceptsType) {
		List<Path> returns = new ArrayList<Path>();
		for (ConceptType concept : conceptsType.getConcept()) {
			Path childPath = new Path();
			childPath.setName(concept.getName());
			childPath.setPui(getServerName() + "/"
					+ concept.getKey().replace('\\', '/'));
			if (concept.getVisualattributes().startsWith("L")) {
				childPath.setDataType(DataType.STRING);
			}
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

			XmlValueType metadata = concept.getMetadataxml();
			if (metadata != null) {
				// TODO IMPLEMENT
			}
			childPath.setAttributes(attributes);
			returns.add(childPath);
		}

		return returns;
	}

	/**
	 * Returns the HTTP Client used for connections
	 * 
	 * @return HTTP Client
	 */
	public HttpClient getClient() {
		return client;
	}

	/**
	 * Sets the HTTP Client for the connection
	 * 
	 * @param client HTTP Client
	 */
	public void setClient(HttpClient client) {
		this.client = client;
	}

	@Override
	public List<OntologyRelationship> relationships() {
		List<OntologyRelationship> relationships = new ArrayList<OntologyRelationship>();
		relationships.add(I2B2OntologyRelationship.CHILD);
		relationships.add(I2B2OntologyRelationship.PARENT);
		relationships.add(I2B2OntologyRelationship.SIBLING);
		relationships.add(I2B2OntologyRelationship.MODIFIER);
		return relationships;
	}

	@Override
	public OntologyRelationship getRelationshipFromString(String relationship) {
		return I2B2OntologyRelationship.valueOf(relationship);
	}

	/**
	 * Returns a server name
	 * 
	 * @return Server name
	 */
	public String getServerName() {
		return serverName;
	}

	/**
	 * Sets a server name
	 * 
	 * @param serverName Server name
	 */
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
}
