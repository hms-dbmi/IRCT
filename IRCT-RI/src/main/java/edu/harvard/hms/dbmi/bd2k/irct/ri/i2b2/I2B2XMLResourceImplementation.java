/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.xml.bind.JAXBException;

import org.apache.http.Header;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;

import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.action.ActionState;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Entity;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ResourceState;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.PathResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.QueryResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;
import edu.harvard.hms.dbmi.i2b2.api.I2B2Factory;
import edu.harvard.hms.dbmi.i2b2.api.exception.I2B2InterfaceException;
import edu.harvard.hms.dbmi.i2b2.api.ont.ONTCell;
import edu.harvard.hms.dbmi.i2b2.api.ont.xml.ConceptType;
import edu.harvard.hms.dbmi.i2b2.api.ont.xml.ConceptsType;
import edu.harvard.hms.dbmi.i2b2.api.ont.xml.ModifierType;
import edu.harvard.hms.dbmi.i2b2.api.ont.xml.XmlValueType;
import edu.harvard.hms.dbmi.i2b2.api.pm.PMCell;
import edu.harvard.hms.dbmi.i2b2.api.pm.xml.ConfigureType;
import edu.harvard.hms.dbmi.i2b2.api.pm.xml.ProjectType;

/**
 * A resource implementation of a resource that communicates with the i2b2 servers via
 * XML
 * 
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class I2B2XMLResourceImplementation implements
		QueryResourceImplementationInterface,
		PathResourceImplementationInterface {
	private String resourceName;
	private String resourceURL;
	private String domain;
	private I2B2Factory i2b2Factory;

	@Override
	public void setup(Map<String, String> parameters)
			throws ResourceInterfaceException {
		String[] strArray = { "resourceName", "resourceURL", "domain" };
		if (!parameters.keySet().containsAll(Arrays.asList(strArray))) {
			throw new ResourceInterfaceException("Missing parameters");
		}
		this.resourceName = parameters.get("resourceName");
		this.resourceURL = parameters.get("resourceURL");
		this.domain = parameters.get("domain");

		i2b2Factory = new I2B2Factory();
		i2b2Factory.setup();
	}

	@Override
	public String getType() {
		return "i2b2XML";
	}

	@Override
	public List<Entity> getPathRelationship(Entity path,
			OntologyRelationship relationship, SecureSession session)
			throws ResourceInterfaceException {
		List<Entity> entities = new ArrayList<Entity>();

		// Build
		HttpClient client = createClient(session.getToken().toString());
		try {
			if (relationship == I2B2OntologyRelationship.CHILD) {
				String[] pathComponents = path.getPui().split("/");
				String basePath = path.getPui();
				// If first then get projects
				if (pathComponents.length == 2) {
					PMCell pmCell = new PMCell();

					pmCell.setup(this.resourceURL, this.domain, "", "", true);
					ConfigureType configureType = pmCell.getUserConfiguration(
							client, null, new String[] { "undefined" });
					for (ProjectType pt : configureType.getUser().getProject()) {
						Entity entity = new Entity();
						entity.setPui(path.getPui() + pt.getPath());
						entity.setDisplayName(pt.getName());
						entity.setName(pt.getId());
						entity.setDescription(pt.getDescription());
						entities.add(entity);
					}

				} else {
					ONTCell ontCell = new ONTCell();
					ontCell.setup(this.resourceURL, this.domain, "", "",
							pathComponents[2], true);

					ConceptsType conceptsType = null;

					if (pathComponents.length == 3) {
						// If beyond second then get ontology categories
						conceptsType = ontCell.getCategories(client, false,
								false, true, "core");
					} else {
						// If second then get categories
						String myPath = "\\";
						for (String pathComponent : Arrays.copyOfRange(
								pathComponents, 3, pathComponents.length)) {
							myPath += "\\" + pathComponent;
						}
						basePath = pathComponents[0] + "/" + pathComponents[1]
								+ "/" + pathComponents[2];

						conceptsType = ontCell.getChildren(client, myPath,
								false, false, false, -1, "core");

					}
					// Convert ConceptsType to Entities
					entities = convertConceptsTypeToEntities(basePath,
							conceptsType);
				}

			} else if (relationship == I2B2OntologyRelationship.MODIFIER) {

			} else if (relationship == I2B2OntologyRelationship.TERM) {

			} else {
				throw new ResourceInterfaceException(relationship.toString()
						+ " not supported by this resource");
			}
		} catch (JAXBException | UnsupportedOperationException
				| I2B2InterfaceException | IOException e) {
			e.printStackTrace();
		}

		return entities;
	}

	@Override
	public List<Entity> searchPaths(Entity path, String searchTerm, SecureSession session) throws ResourceInterfaceException {
		String strategy = "exact";
		if(searchTerm.charAt(0) == '%') {
			if(searchTerm.charAt(searchTerm.length() - 1) == '%') {
				searchTerm = searchTerm.substring(1, searchTerm.length() - 1);
				strategy = "contains";
			} else {
				searchTerm = searchTerm.substring(1);
				strategy = "right";
			}
		} else if(searchTerm.charAt(searchTerm.length() - 1) == '%') {
			searchTerm = searchTerm.substring(0, searchTerm.length() - 1);
			strategy = "left";
		}
		
		List<Entity> entities = new ArrayList<Entity>();
		HttpClient client = createClient(session.getToken().toString());
		try {
			
			if ((path == null) || (path.getPui().split("/").length <= 2)) {
				PMCell pmCell = new PMCell();

				pmCell.setup(this.resourceURL, this.domain, "", "", true);
				ConfigureType configureType = pmCell.getUserConfiguration(
						client, null, new String[] { "undefined" });
				for (ProjectType pt : configureType.getUser().getProject()) {
					for(ConceptType category : getCategories(client, pt.getId()).getConcept()) {
						String categoryName = converti2b2Path(category.getKey()).split("/")[1];
						entities.addAll(convertConceptsTypeToEntities("/" + this.resourceName, runNameSearch(client, pt.getId(), categoryName, strategy, searchTerm)));
					}
				}
			} else {
				String[] pathComponents = path.getPui().split("/");
				if (pathComponents.length == 3) {
					// Get All Categories
					for(ConceptType category : getCategories(client, pathComponents[2]).getConcept()) {
						String categoryName = converti2b2Path(category.getKey()).split("/")[1];
						entities.addAll(convertConceptsTypeToEntities("/" + this.resourceName, runNameSearch(client, pathComponents[2], categoryName, strategy, searchTerm)));
					}
				} else {
					// Run request
					entities.addAll(convertConceptsTypeToEntities("/" + this.resourceName, runNameSearch(client, pathComponents[2], pathComponents[3], strategy, searchTerm)));
				}
			}
		} catch (JAXBException | UnsupportedOperationException
				| I2B2InterfaceException | IOException e) {
			e.printStackTrace();
		}
		return entities;
	}
	
//  <ns4:get_code_info blob="true" type="core" max='200'  synonyms="true" hiddens="false">
//     <match_str strategy="exact">ICD10:P84</match_str>
//  </ns4:get_code_info>
	@Override
	public List<Entity> searchOntology(Entity path, String ontologyType,
			String ontologyTerm, SecureSession session)
			throws ResourceInterfaceException {
		List<Entity> entities = new ArrayList<Entity>();
		HttpClient client = createClient(session.getToken().toString());
		try {
			
			if ((path == null) || (path.getPui().split("/").length <= 2)) {
				PMCell pmCell = new PMCell();

				pmCell.setup(this.resourceURL, this.domain, "", "", true);
				ConfigureType configureType = pmCell.getUserConfiguration(
						client, null, new String[] { "undefined" });
				for (ProjectType pt : configureType.getUser().getProject()) {
					entities.addAll(convertConceptsTypeToEntities("/" + this.resourceName, runCategorySearch(client, pt.getId(), null, ontologyType, ontologyTerm)));
				}
			} else {
				String[] pathComponents = path.getPui().split("/");
				if (pathComponents.length == 3) {
					// Get All Categories
					entities.addAll(convertConceptsTypeToEntities("/" + this.resourceName, runCategorySearch(client, pathComponents[2], null, ontologyType, ontologyTerm)));
				} else {
					// Run request
					entities.addAll(convertConceptsTypeToEntities("/" + this.resourceName, runCategorySearch(client, pathComponents[2], pathComponents[3], ontologyType, ontologyTerm)));
				}
			}
		} catch (JAXBException | UnsupportedOperationException
				| I2B2InterfaceException | IOException e) {
			e.printStackTrace();
		}
		return entities;
	}
	
	private ConceptsType runCategorySearch(HttpClient client, String projectId, String category, String ontologyType, String ontologyTerm) throws UnsupportedOperationException, JAXBException, I2B2InterfaceException, IOException {
		ONTCell ontCell = new ONTCell();
		ontCell.setup(this.resourceURL, this.domain, "", "", projectId, true);
		return ontCell.getCodeInfo(client, true, category, false, "exact", ontologyType + ":" + ontologyTerm, -1, null, true, "core");
	}
	
	
	@Override
	public ActionState runQuery(Query qep) throws ResourceInterfaceException {
		return null;
	}

	@Override
	public ResultSet getResults(ActionState actionState)
			throws ResourceInterfaceException {
		return null;
	}

	@Override
	public ResourceState getState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonObject toJson() {
		return toJson(1);
	}

	@Override
	public JsonObject toJson(int depth) {
		JsonObjectBuilder returnJSON = Json.createObjectBuilder();

		return returnJSON.build();
	}

	@Override
	public List<Entity> getReturnEntity() {
		List<Entity> returnEntity = new ArrayList<Entity>();
		// Patient Id
		Entity patientId = new Entity();
		patientId.setName("Patient Id");
		patientId.setPui("Patient Id");
		patientId.setDataType(PrimitiveDataType.STRING);
		returnEntity.add(patientId);

		// vital_status_cd
		Entity vitalStatusCd = new Entity();
		vitalStatusCd.setName("Vital Status");
		vitalStatusCd.setPui("vital_status_cd");
		vitalStatusCd.setDataType(PrimitiveDataType.STRING);
		returnEntity.add(vitalStatusCd);

		// language_cd
		Entity languageCd = new Entity();
		languageCd.setName("Language");
		languageCd.setPui("language_cd");
		languageCd.setDataType(PrimitiveDataType.STRING);
		returnEntity.add(languageCd);

		// birth_date
		Entity birthDate = new Entity();
		birthDate.setName("Birth Date");
		birthDate.setPui("birth_date");
		birthDate.setDataType(PrimitiveDataType.STRING);
		returnEntity.add(birthDate);

		// race_cd
		Entity raceCd = new Entity();
		raceCd.setName("Race");
		raceCd.setPui("race_cd");
		raceCd.setDataType(PrimitiveDataType.STRING);
		returnEntity.add(raceCd);

		// religion_cd
		Entity religionCd = new Entity();
		religionCd.setName("Religion");
		religionCd.setPui("Religion");
		religionCd.setDataType(PrimitiveDataType.STRING);
		returnEntity.add(religionCd);

		// income_cd
		Entity incomeCd = new Entity();
		incomeCd.setName("Income");
		incomeCd.setPui("income_cd");
		incomeCd.setDataType(PrimitiveDataType.STRING);
		returnEntity.add(incomeCd);

		// statecityzip_path
		Entity stateCityCd = new Entity();
		stateCityCd.setName("State, City Zip");
		stateCityCd.setPui("statecityzip_path");
		stateCityCd.setDataType(PrimitiveDataType.STRING);
		returnEntity.add(stateCityCd);

		// zip_cd
		Entity zipCd = new Entity();
		zipCd.setName("Zip");
		zipCd.setPui("zip_cd");
		zipCd.setDataType(PrimitiveDataType.STRING);
		returnEntity.add(zipCd);

		// marital_status_cd
		Entity maritalCd = new Entity();
		maritalCd.setName("marital_status_cd");
		maritalCd.setPui("marital_status_cd");
		maritalCd.setDataType(PrimitiveDataType.STRING);
		returnEntity.add(maritalCd);

		// age_in_years_num
		Entity ageCd = new Entity();
		ageCd.setName("Age");
		ageCd.setPui("age_in_years_num");
		ageCd.setDataType(PrimitiveDataType.STRING);
		returnEntity.add(ageCd);

		// sex_cd
		Entity sexCd = new Entity();
		sexCd.setName("Sex");
		sexCd.setPui("sex_cd");
		sexCd.setDataType(PrimitiveDataType.STRING);
		returnEntity.add(sexCd);
		return returnEntity;
	}

	@Override
	public Boolean editableReturnEntity() {
		return false;
	}

	// -------------------------------------------------------------------------
	// Utility Methods
	// -------------------------------------------------------------------------

	private List<Entity> convertConceptsTypeToEntities(String basePath,
			ConceptsType conceptsType) {
		List<Entity> returns = new ArrayList<Entity>();
		for (ConceptType concept : conceptsType.getConcept()) {
			Entity returnEntity = new Entity();
			returnEntity.setName(concept.getName());
			String appendPath = converti2b2Path(concept.getKey());
			returnEntity.setPui(basePath + appendPath);

			if (concept.getVisualattributes().startsWith("L")) {
				returnEntity.setDataType(PrimitiveDataType.STRING);
			}

			returnEntity.setDisplayName(concept.getName());
			;

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
			returnEntity.setAttributes(attributes);
			returns.add(returnEntity);
		}

		return returns;
	}
	
	private String converti2b2Path(String i2b2Path) {
		return i2b2Path.replaceAll("\\\\\\\\", "/").replace('\\', '/');
	}
	
	private ConceptsType runNameSearch(HttpClient client, String projectId, String category, String strategy, String searchTerm) throws UnsupportedOperationException, JAXBException, I2B2InterfaceException, IOException {
		ONTCell ontCell = new ONTCell();
		ontCell.setup(this.resourceURL, this.domain, "", "", projectId, true);
		return ontCell.getNameInfo(client, true, category, false, strategy, searchTerm, -1, null, true, "core");
	}


	private ConceptsType getCategories(HttpClient client, String projectId) throws JAXBException, ClientProtocolException, IOException, I2B2InterfaceException {
		ONTCell ontCell = new ONTCell();
		ontCell.setup(this.resourceURL, this.domain, "", "", projectId, true);
		
		return ontCell.getCategories(client, false, false, true, "core");
	}

	/**
	 * CREATES A CLIENT
	 * 
	 * @param token
	 * @return
	 */
	private HttpClient createClient(String token) {
		HttpClientBuilder returns = HttpClientBuilder.create();
		List<Header> defaultHeaders = new ArrayList<Header>();
		defaultHeaders.add(new BasicHeader("Authorization", token));
		defaultHeaders.add(new BasicHeader("Content-Type",
				"application/x-www-form-urlencoded"));
		returns.setDefaultHeaders(defaultHeaders);

		return returns.build();
	}
}
