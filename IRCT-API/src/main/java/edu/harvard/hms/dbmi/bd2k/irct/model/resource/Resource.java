/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.resource;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.DataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.DataTypeJsonConverter;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.JoinType;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.PredicateType;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.SelectOperationType;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.SortOperationType;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.ResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.visualization.VisualizationType;
import edu.harvard.hms.dbmi.bd2k.irct.util.converter.DataTypeConverter;
import edu.harvard.hms.dbmi.bd2k.irct.util.converter.OntologyRelationshipConverter;
import edu.harvard.hms.dbmi.bd2k.irct.util.converter.ResourceImplementationConverter;

/**
 * The resource class provides a way for the IRCT application to keep track of
 * which resources are available.
 */
@Entity
public class Resource implements Serializable {
	private static final long serialVersionUID = 8099637983212553759L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(unique = true)
	private String name;

	// TODO : This field is unused, see if we can figure out if it should be used
	private String ontologyType;

	@Convert(converter = ResourceImplementationConverter.class)
	@JsonProperty("implementation")
	@JsonSerialize(converter = ResourceImplementationInterfaceConverter.class)
	private ResourceImplementationInterface implementingInterface;

	@ElementCollection(fetch = FetchType.LAZY)
	@Convert(converter = DataTypeConverter.class)
	@JsonSerialize(contentConverter = DataTypeJsonConverter.class)
	private List<DataType> dataTypes;

	@ElementCollection(fetch = FetchType.LAZY)
	@Convert(converter = OntologyRelationshipConverter.class)
	private List<OntologyRelationship> relationships;

	@ElementCollection(targetClass = LogicalOperator.class)
	@CollectionTable(name = "Resource_LogicalOperator", joinColumns = @JoinColumn(name = "id"))
	@Column(name = "logicalOperator", nullable = false)
	@Enumerated(EnumType.STRING)
	private List<LogicalOperator> logicalOperators;

	@OneToMany
	@JsonProperty("predicates")
	private List<PredicateType> supportedPredicates;

	@OneToMany
	@JsonProperty("selectOperations")
	private List<SelectOperationType> supportedSelectOperations;

	@OneToMany
	@JsonProperty("selectFields")
	private List<Field> supportedSelectFields;

	@OneToMany
	@JsonProperty("sorts")
	private List<SortOperationType> supportedSortOperations;

	@OneToMany
	@JsonProperty("joins")
	private List<JoinType> supportedJoins;

	@OneToMany
	@JsonProperty("processes")
	private List<ProcessType> supportedProcesses;

	@OneToMany
	@JsonProperty("visualizations")
	private List<VisualizationType> supportedVisualizations;

	@ElementCollection(fetch = FetchType.EAGER)
	@MapKeyColumn(name = "name")
	@Column(name = "value")
	@CollectionTable(name = "resource_parameters", joinColumns = @JoinColumn(name = "id"))
	@JsonIgnore
	private Map<String, String> parameters;

	@Transient
	@JsonIgnore
	private boolean setup = false;

	/**
	 * Sets up the Resource and the implementing interface
	 * @throws ResourceInterfaceException Throws a resource interface
	 */
	public void setup() throws ResourceInterfaceException {
		boolean isDoneSettingUp = false;
		try {
			if (implementingInterface != null)
				implementingInterface.setup(this.parameters);
			else
				org.apache.log4j.Logger.getLogger(this.getClass()).warn("Resource.setup() resource implementation is null, resource name:  " +
						this.name);
			isDoneSettingUp = true;
		} catch (Exception e) {
			org.apache.log4j.Logger.getLogger(this.getClass()).error("Resource.setup() Exception: "+e.getMessage());
		}
		this.setSetup(isDoneSettingUp);
	}


	/**
	 * Returns a relationship from its name. It will return null if it does not exist.
	 *
	 * @param relationshipString Relationship name
	 * @return Ontology Relationship
	 */
	public OntologyRelationship getRelationshipByName(String relationshipString) {
		for(OntologyRelationship relationship : this.relationships) {
			if(relationship.toString().equalsIgnoreCase(relationshipString)) {
				return relationship;
			}
		}
		return null;
	}

	/**
	 * Returns a predicate type from its name. It will return null if it does not exist.
	 *
	 * @param predicateName Predicate name
	 * @return Predicate Type
	 */
	public PredicateType getSupportedPredicateByName(String predicateName) {
		for(PredicateType predicateType : this.supportedPredicates) {
			if(predicateType.getName().equals(predicateName)) {
				return predicateType;
			}
		}
		return null;
	}

	/**
	 * Returns a select operation from its name. It will return null if it does not exist
	 *
	 * @param operationName Operation name
	 * @return Select Operation Type
	 */
	public SelectOperationType getSupportedSelectOperationByName(
			String operationName) {
		for(SelectOperationType operationType : this.supportedSelectOperations) {
			if(operationType.getName().equals(operationName)) {
				return operationType;
			}
		}
		return null;
	}

	/**
	 * Returns a sort operation from its name. It will return null if it does not exist.
	 *
	 * @param operationName Sort name
	 * @return Sort Operation Type
	 */
	public SortOperationType getSupportedSortOperationByName(
			String operationName) {
		for(SortOperationType operationType : this.supportedSortOperations) {
			if(operationType.getName().equals(operationName)) {
				return operationType;
			}
		}
		return null;
	}

	/**
	 * Returns a process type from its name. It will return null if it does not exist.
	 *
	 * @param processName Process Name
	 * @return Process Type
	 */
	public ProcessType getSupportedProcessesByName(String processName) {
		for(ProcessType processType : this.supportedProcesses) {
			if(processType.getName().equalsIgnoreCase(processName)) {
				return processType;
			}
		}
		return null;
	}

	/**
	 * Returns a Logical Operator from its name. It will return null if it does not exist.
	 *
	 * @param logicalOperatorName Logical Operator Name
	 * @return Logical Operator
	 */
	public LogicalOperator getLogicalOperatorByName(String logicalOperatorName) {
		for(LogicalOperator logicalOperator : this.logicalOperators) {
			if(logicalOperator.toString().equals(logicalOperatorName)) {
				return logicalOperator;
			}
		}
		return null;
	}

	/**
	 * Returns a data type from its name. It will return null if it does not exist.
	 *
	 * @param dataTypeName Data Type Name
	 * @return Data Type
	 */
	public DataType getDataTypeByName(String dataTypeName) {
		for(DataType dataType : this.dataTypes) {
			if(dataType.toString().equals(dataTypeName)) {
				return dataType;
			}
		}
		return null;
	}


	public JoinType getSupportedJoinByName(String joinTypeName) {
		for(JoinType joinType : this.supportedJoins) {
			if(joinType.getName().equals(joinTypeName)) {
				return joinType;
			}
		}
		return null;
	}

	// -------------------------------------------------------------------------
	// SETTERS AND GETTERS
	// -------------------------------------------------------------------------

	/**
	 * Returns the id of the resource
	 *
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * Sets the id of the resource
	 *
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Returns the name of the resource
	 *
	 * @return the name
	 */
	public String getName() {
		return name==null?"null":name;
	}

	/**
	 * Sets the name of the resource
	 *
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the implementing interface of the resource
	 *
	 * @return the implementingInterface
	 */
	public ResourceImplementationInterface getImplementingInterface() {
		return implementingInterface;
	}

	/**
	 * Sets the implementing interface of the resource
	 *
	 * @param implementingInterface
	 *            the implementingInterface to set
	 */
	public void setImplementingInterface(
			ResourceImplementationInterface implementingInterface) {
		this.implementingInterface = implementingInterface;
	}

	/**
	 * Returns a list of data types that are supported by the resource
	 *
	 * @return the dataTypes
	 */
	public List<DataType> getDataTypes() {
		return dataTypes;
	}

	/**
	 * Sets the list of data type that are supported by the resource
	 *
	 * @param dataTypes
	 *            the dataTypes to set
	 */
	public void setDataTypes(List<DataType> dataTypes) {
		this.dataTypes = dataTypes;
	}

	/**
	 * Returns a list of relationships that are supported by the resource
	 *
	 * @return the relationships
	 */
	public List<OntologyRelationship> getRelationships() {
		return relationships;
	}

	/**
	 * Sets the list of relationships that are supported by the resource
	 *
	 * @param relationships
	 *            the relationships to set
	 */
	public void setRelationships(List<OntologyRelationship> relationships) {
		this.relationships = relationships;
	}

	/**
	 * Returns a list of logical operators that are supported by the resource
	 *
	 * @return the logicalOperators
	 */
	public List<LogicalOperator> getLogicalOperators() {
		return logicalOperators;
	}

	/**
	 * Sets a list of logical operators that are supported by the resource
	 *
	 * @param logicalOperators
	 *            the logicalOperators to set
	 */
	public void setLogicalOperators(List<LogicalOperator> logicalOperators) {
		this.logicalOperators = logicalOperators;
	}

	/**
	 * Returns a list of supported predicates that are supported by the resource
	 *
	 * @return the supportedPredicates
	 */
	public List<PredicateType> getSupportedPredicates() {
		return supportedPredicates;
	}

	/**
	 * Sets the list of supported predicates that are supported by the resource
	 *
	 * @param supportedPredicates
	 *            the supportedPredicates to set
	 */
	public void setSupportedPredicates(List<PredicateType> supportedPredicates) {
		this.supportedPredicates = supportedPredicates;
	}

	/**
	 * Returns a list of select operation that are supported by the resource
	 *
	 * @return the supportedSelectOperations
	 */
	public List<SelectOperationType> getSupportedSelectOperations() {
		return supportedSelectOperations;
	}

	/**
	 * Sets the list of select operations that are supported by the resources
	 *
	 * @param supportedSelectOperations the supportedSelectOperations to set
	 */
	public void setSupportedSelectOperations(
			List<SelectOperationType> supportedSelectOperations) {
		this.supportedSelectOperations = supportedSelectOperations;
	}

	/**
	 * Gets the list of supported select fields that are supported by the resource
	 *
	 * @return the supportedSelectFields
	 */
	public List<Field> getSupportedSelectFields() {
		return supportedSelectFields;
	}

	/**
	 * Sets the list of supported select fields that are supported by the resource
	 *
	 * @param supportedSelectFields the supportedSelectFields to set
	 */
	public void setSupportedSelectFields(List<Field> supportedSelectFields) {
		this.supportedSelectFields = supportedSelectFields;
	}

	/**
	 * Returns a list of sort operation that are supported by the resource
	 *
	 * @return the supportedSortOperations
	 */
	public List<SortOperationType> getSupportedSortOperations() {
		return supportedSortOperations;
	}

	/**
	 * Sets the list of sort operations that are supporterd by the resource
	 *
	 * @param supportedSortOperations the supportedSortOperations to set
	 */
	public void setSupportedSortOperations(List<SortOperationType> supportedSortOperations) {
		this.supportedSortOperations = supportedSortOperations;
	}

	/**
	 * Returns a list joins that are supported by the resource
	 *
	 * @return the supportedJoins
	 */
	public List<JoinType> getSupportedJoins() {
		return supportedJoins;
	}

	/**
	 * Sets the list joins that are supported by the resource
	 *
	 * @param supportedJoins
	 *            the supportedJoins to set
	 */
	public void setSupportedJoins(List<JoinType> supportedJoins) {
		this.supportedJoins = supportedJoins;
	}

	/**
	 * Returns a list of process that are supported by the resource
	 *
	 * @return the supportedProcesses
	 */
	public List<ProcessType> getSupportedProcesses() {
		return supportedProcesses;
	}

	/**
	 * Sets the list of processes that are supported by the resource
	 * @param supportedProcesses
	 *            the supportedProcesses to set
	 */
	public void setSupportedProcesses(List<ProcessType> supportedProcesses) {
		this.supportedProcesses = supportedProcesses;
	}

	/**
	 * Returns a list of visualizations that are supported by the resource
	 *
	 * @return the supportedVisualizations
	 */
	public List<VisualizationType> getSupportedVisualizations() {
		return supportedVisualizations;
	}

	/**
	 * Sets a list of visualizations that are supported by the resource
	 *
	 * @param supportedVisualizations
	 *            the supportedVisualizations to set
	 */
	public void setSupportedVisualizations(
			List<VisualizationType> supportedVisualizations) {
		this.supportedVisualizations = supportedVisualizations;
	}

	/**
	 * Returns a map of parameters for the resource
	 *
	 * @return the parameters
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}

	/**
	 * Sets a map of resources for the resource
	 *
	 * @param parameters
	 *            the parameters to set
	 */
	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	/**
	 * @return the setup
	 */
	public boolean isSetup() {
		return setup;
	}

	/**
	 * @param setup the setup to set
	 */
	public void setSetup(boolean setup) {
		this.setup = setup;
	}


}
