/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.controller;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateful;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue.ValueType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;

import edu.harvard.hms.dbmi.bd2k.irct.IRCTApplication;
import edu.harvard.hms.dbmi.bd2k.irct.exception.QueryException;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.DataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Entity;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.JoinClause;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.JoinType;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.PredicateType;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.SelectClause;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.SelectOperationType;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.SortClause;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.SortOperationType;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.SubQuery;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.WhereClause;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Field;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.LogicalOperator;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;

/**
 * A stateful controller for creating a query
 */
@Stateful
public class QueryController {

	@PersistenceContext(unitName = "primary")
	EntityManager entityManager;

	@Inject
	IRCTApplication picsure;

	private Logger logger = Logger.getLogger(this.getClass());

	private Query query;
	private Long lastId;

	/**
	 * Deletes the current query and creates a new one
	 * 
	 */
	public void createQuery() {
		this.query = new Query();
		this.lastId = 0L;
	}

	public Query createQuery(String queryString) throws QueryException {
		logger.info("createQuery(String) Starting");

		JsonReader jsonReader = Json.createReader(new StringReader(queryString));
		JsonObject jsonQuery = jsonReader.readObject();
		jsonReader.close();

		this.query = new Query();
		this.lastId = 0L;

		if (jsonQuery.getString("resource") != null) {
			logger.info("createQuery(String) resource:" + jsonQuery.getString("resource"));
			String resourceName = jsonQuery.getString("resource");
			logger.info("createQuery(String) resourceName:" + resourceName);

			// Add the named resource to the query object.
			Resource resource = picsure.getResources().get(resourceName);
			if (resource == null) {
				throw new RuntimeException("Resource `" + resourceName + "` is not available via PIC-SURE API.");
			}

			logger.info("createQuery(String) resource:" + resource.getId() + " name:" + resource.getName());
			this.query.getResources().add(resource);
			logger.info("createQuery(String) added resource '" + resourceName + "' to query");

		} else {
			logger.error("createQuery(String) Missing `resource` field.");
			throw new RuntimeException("Missing `resource` field. Cannot determine with resource to use!");
		}
		this.query.setPayload(queryString);
		logger.info("createQuery(String) calling `saveQuery()`");
		saveQuery();

		logger.info("createQuery(String) Finished");
		return query;
	}

	public SubQuery createSubQuery() {
		return new SubQuery();
	}

	/**
	 * Adds or updates a where clause to the query
	 * 
	 * @param clauseId
	 *            Clause Id
	 * @param resource
	 *            Resource
	 * @param field
	 *            Field
	 * @param predicate
	 *            Predicate
	 * @param logicalOperator
	 *            Logical Operator
	 * @param fields
	 *            Map of Field values
	 * @param objectFields
	 *            Map of Object values
	 * @return Id of the clause
	 * @throws QueryException
	 *             An exception occurred adding the where clause
	 */
	public Long addWhereClause(Long clauseId, Resource resource, Entity field, PredicateType predicate,
			LogicalOperator logicalOperator, Map<String, String> fields, Map<String, Object> objectFields)
			throws QueryException {
		logger.debug("addWhereClause() Starting");

		return addWhereClause(null, clauseId, resource, field, predicate, logicalOperator, fields, objectFields);
	}

	public Long addWhereClause(SubQuery subQuery, Long clauseId, Resource resource, Entity field,
			PredicateType predicate, LogicalOperator logicalOperator, Map<String, String> fields,
			Map<String, Object> objectFields) throws QueryException {

		// Is valid where clause
		validateWhereClause(resource, field, predicate, logicalOperator, fields);

		// Create the where Clause
		WhereClause wc = new WhereClause();
		wc.setField(field);
		wc.setLogicalOperator(logicalOperator);
		wc.setPredicateType(predicate);
		wc.setStringValues(fields);
		wc.setObjectValues(objectFields);

		// Assign the where clause an id if it doesn't have one
		if (clauseId == null) {
			clauseId = this.lastId;
			this.lastId++;
		}

		// Add the where clause to the query
		if (subQuery != null) {
			subQuery.addClause(clauseId, wc);
		} else {
			query.addClause(clauseId, wc);
		}

		return clauseId;
	}

	/**
	 * Deletes a where clause
	 * 
	 * @param clauseId
	 *            Clause Id to delete
	 */
	public void deleteWhereClause(Long clauseId) {
		this.query.getClauses().remove(clauseId);
	}

	public void deleteWhereClause(SubQuery subQuery, Long clauseId) {
		subQuery.getClauses().remove(clauseId);
	}

	/**
	 * Adds or updates a select clause
	 * 
	 * @param clauseId
	 *            Clause Id
	 * @param resource
	 *            Resource
	 * @param field
	 *            Field
	 * @param alias
	 *            Alias for the column
	 * @param operation
	 *            Operation
	 * @param fields
	 *            Map of Field values
	 * @return Clause Id
	 * @throws QueryException
	 *             An exception occurred adding the select clause
	 */
	public Long addSelectClause(Long clauseId, Resource resource, Entity field, String alias,
			SelectOperationType operation, Map<String, String> fields) throws QueryException {
		return addSelectClause(null, clauseId, resource, field, alias, operation, fields, null);
	}

	public Long addSelectClause(SubQuery subQuery, Long clauseId, Resource resource, Entity field, String alias,
			SelectOperationType operation, Map<String, String> fields, Map<String, Object> objectFields)
			throws QueryException {
		// Is this a valid select clause
		validateSelectClause(resource, operation, fields);

		// Crete the select clause
		SelectClause sc = new SelectClause();
		sc.setParameters(field);
		sc.setAlias(alias);
		sc.setOperationType(operation);
		sc.setStringValues(fields);
		sc.setObjectValues(objectFields);

		// Assign the where clause an id if it doesn't have one
		if (clauseId == null) {
			clauseId = this.lastId;
			this.lastId++;
		}

		// Add the where clause to the query
		if (subQuery != null) {
			subQuery.addClause(clauseId, sc);
		} else {
			query.addClause(clauseId, sc);
		}
		return clauseId;
	}

	/**
	 * Adds or updates a sort clause
	 * 
	 * @param clauseId
	 *            Clause Id
	 * @param resource
	 *            Resource
	 * @param field
	 *            Field
	 * @param operation
	 *            Operation
	 * @param fields
	 *            Map of Field values
	 * @param objectFields
	 *            Map of Object values
	 * @return Clause Id
	 * @throws QueryException
	 *             An exception occurred adding the sort clause
	 */
	public Long addSortClause(Long clauseId, Resource resource, Entity field, SortOperationType operation,
			Map<String, String> fields, Map<String, Object> objectFields) throws QueryException {
		return addSortClause(null, clauseId, resource, field, operation, fields, objectFields);
	}

	public Long addSortClause(SubQuery subQuery, Long clauseId, Resource resource, Entity field,
			SortOperationType operation, Map<String, String> fields, Map<String, Object> objectFields)
			throws QueryException {
		validateSortClause(resource, operation, fields);

		// Create the sort clause
		SortClause sc = new SortClause();
		sc.setParameters(field);
		sc.setStringValues(fields);
		sc.setOperationType(operation);
		sc.setObjectValues(objectFields);

		// Assign the where clause an id if it doesn't have one
		if (clauseId == null) {
			clauseId = this.lastId;
			this.lastId++;
		}

		if (subQuery != null) {
			subQuery.addClause(clauseId, sc);
		} else {
			query.addClause(clauseId, sc);
		}

		return clauseId;
	}

	public Long addJoinClause(Long clauseId, Resource resource, Entity field, JoinType joinType,
			Map<String, String> joinFields, Map<String, Object> objectFields) throws QueryException {
		return addJoinClause(null, clauseId, resource, field, joinType, joinFields, objectFields);
	}

	public Long addJoinClause(SubQuery subQuery, Long clauseId, Resource resource, Entity field, JoinType joinType,
			Map<String, String> joinFields, Map<String, Object> objectFields) throws QueryException {
		validateJoinClause(resource, joinType, joinFields, objectFields);

		JoinClause jc = new JoinClause();
		jc.setJoinType(joinType);
		jc.setStringValues(joinFields);
		jc.setField(field);
		jc.setObjectValues(objectFields);

		// Assign the where clause an id if it doesn't have one
		if (clauseId == null) {
			clauseId = this.lastId;
			this.lastId++;
		}

		// Add the where clause to the query
		if (subQuery != null) {
			subQuery.addClause(clauseId, jc);
		} else {
			query.addClause(clauseId, jc);
		}
		return clauseId;
	}

	public void addSubQuery(String subQueryId, SubQuery subQuery) throws QueryException {
		if (subQueryId == null) {
			throw new QueryException("Invalid SubQuery ID");
		}

		// Add the where clause to the query
		query.addSubQuery(subQueryId, subQuery);
	}

	/**
	 * Deletes the current query
	 * 
	 */
	public void deleteQuery() {
		this.query = null;
	}

	public void deleteSubQuery(Long subQueryId) {
		this.query.removeSubQuery(subQueryId);
	}

	private void validateWhereClause(Resource resource, Entity field, PredicateType predicate,
			LogicalOperator logicalOperator, Map<String, String> queryFields) throws QueryException {
		// Is resource part of query?
		if (this.query.getResources().isEmpty()) {
			this.query.getResources().add(resource);
		} else if (!this.query.getResources().contains(resource)) {
			throw new QueryException("Queries only support one resource");
		}
		// Does the resource support the logical operator
		if ((logicalOperator != null) && (!resource.getLogicalOperators().contains(logicalOperator))) {
			throw new QueryException("Logical operator is not supported by the resource");
		}
		// Does the resource support the predicate?
		if (!resource.getSupportedPredicates().contains(predicate)) {
			throw new QueryException("Predicate is not supported by the resource");
		}
		// Does the predicate support the entity?
		if ((!predicate.getDataTypes().isEmpty()) && (!predicate.getDataTypes().contains(field.getDataType()))) {
			throw new QueryException("Predicate does not support this type of field");
		}
		// Are all the fields valid?
		validateFields(predicate.getFields(), queryFields, null);
	}

	private void validateSelectClause(Resource resource, SelectOperationType operation,
			Map<String, String> selectFields) throws QueryException {
		// Is resource part of query?
		if (this.query.getResources().isEmpty()) {
			this.query.getResources().add(resource);
		} else if (!this.query.getResources().contains(resource)) {
			throw new QueryException("Queries only support one resource");
		}

		// Is the select operation supported by the resource
		if (operation != null) {
			if (!resource.getSupportedSelectOperations().contains(operation)) {
				throw new QueryException("Select operation is not supported by the resource");
			}
			// Are all the fields valid?
			validateFields(operation.getFields(), selectFields, null);

		}

	}

	private void validateJoinClause(Resource resource, JoinType joinType, Map<String, String> joinFields,
			Map<String, Object> objectFields) throws QueryException {
		// Is the resource part of query?
		if (this.query.getResources().isEmpty()) {
			this.query.getResources().add(resource);
		} else if (!this.query.getResources().contains(resource)) {
			throw new QueryException("Queries only support one resource");
		}

		// Does the resource support the join type
		if (!resource.getSupportedJoins().contains(joinType)) {
			throw new QueryException("Join Type is not supported by the resource");
		}

		// Are all the fields valid?
		validateFields(joinType.getFields(), joinFields, objectFields);
	}

	private void validateSortClause(Resource resource, SortOperationType operation, Map<String, String> sortFields)
			throws QueryException {
		// Is resource part of query?
		if (this.query.getResources().isEmpty()) {
			this.query.getResources().add(resource);
		} else if (!this.query.getResources().contains(resource)) {
			throw new QueryException("Queries only support one resource");
		}

		// Is the sort operation supported by the resource
		if ((operation != null) && (!resource.getSupportedSortOperations().contains(operation))) {
			throw new QueryException("Sort operation is not supported by the resource");
		}

		// Are all the fields valid?
		validateFields(operation.getFields(), sortFields, null);
	}

	private void validateFields(List<Field> fields, Map<String, String> valueFields, Map<String, Object> objectFields)
			throws QueryException {
		logger.debug("validateFields() Starting"); 

		for (Field predicateField : fields) {
			if (predicateField.isRequired() && ((valueFields != null) && (valueFields.containsKey(predicateField.getPath())))) {
				String queryFieldValue = valueFields.get(predicateField.getPath());

				if (queryFieldValue != null) {
					// Is the predicate field data type allowed for this query
					// field
					if (!predicateField.getDataTypes().isEmpty()) {
						boolean validateFieldValue = false;

						for (DataType dt : predicateField.getDataTypes()) {
							if (dt.validate(queryFieldValue)) {
								validateFieldValue = true;
								break;
							}
						}

						if (!validateFieldValue) {
							throw new QueryException("The field value set is not a supported type for this field");
						}
					}
					// Is the predicate field of allowedTypes
					if (!predicateField.getPermittedValues().isEmpty()
							&& (!predicateField.getPermittedValues().contains(queryFieldValue))) {
						throw new QueryException(
								"The field value ```" + queryFieldValue + "``` is not of an allowed type. Only allowed:"
										+ predicateField.getPermittedValues().toString());
					}
				}

			} else if (predicateField.isRequired()
					&& ((objectFields != null) && (objectFields.containsKey(predicateField.getPath())))) {

			} else if (predicateField.isRequired()) {
				throw new QueryException("Required field " + predicateField.getName() + " is not set in "+valueFields.keySet().toString());
			}
		}
		logger.debug("validateFields() Finished"); 
	}

	/**
	 * Save the query
	 * 
	 * @throws QueryException
	 *             An exception occurred saving the query
	 */
	public void saveQuery() throws QueryException {
		logger.debug("saveQuery() Starting.");

		if (this.query == null) {
			logger.error("saveQuery() No query to save");

			throw new QueryException("No query to save.");
		}
		if (this.query.getId() == null) {
			logger.error("saveQuery() Writing new query with id null");

			entityManager.persist(this.query);

			logger.error("saveQuery() Wrote new query, now the id is " + this.query.getId());
		} else {
			logger.error("saveQuery() Updating existing query.");
			entityManager.merge(this.query);
		}
		logger.debug("saveQuery() Finished.");
	}

	/**
	 * Load the query
	 * 
	 * @param queryId
	 *            Query to load
	 * @throws QueryException
	 *             An exception occurred loading the query
	 */
	public void loadQuery(Long queryId) throws QueryException {
		if (queryId == null) {
			throw new QueryException("No query id.");
		}
		this.query = entityManager.find(Query.class, queryId);
		if (this.query == null) {
			throw new QueryException("Could not find query with id `" + queryId + "`");
		}
	}

	/**
	 * Returns the given query
	 * 
	 * @return Query
	 */
	public Query getQuery() {
		return query;
	}

	/**
	 * Sets the given query
	 * 
	 * @param query
	 *            Query
	 */
	public void setQuery(Query query) {
		this.query = query;
	}

	private Long addJsonSelectClauseToQuery(JsonObject selectClause) throws QueryException {
		logger.debug("addJsonSelectClauseToQuery() Starting");

		String path = null;
		String dataType = null;
		if (selectClause.containsKey("field")) {
			path = selectClause.getJsonObject("field").getString("pui");
			if (selectClause.getJsonObject("field").containsKey("dataType")) {
				dataType = selectClause.getJsonObject("field").getString("dataType");
			}
		}

		Long clauseId = null;
		if (selectClause.containsKey("clauseId")) {
			clauseId = selectClause.getJsonNumber("clauseId").longValue();
		}

		Entity entity = null;
		Resource resource = null;
		if (path != null && !path.isEmpty()) {
			path = "/" + path;
			path = path.substring(1);
			resource = picsure.getResources().get(path.split("/")[1]);
			if (resource == null) {
				throw new QueryException("Invalid Resource");
			}
			entity = new Entity(path);
			if (dataType != null) {
				entity.setDataType(resource.getDataTypeByName(dataType));
			}
		}

		String alias = null;
		if (selectClause.containsKey("alias")) {
			alias = selectClause.getString("alias");
		}

		String operationName = null;

		Map<String, Object> objectFields = new HashMap<String, Object>();
		Map<String, String> fields = new HashMap<String, String>();
		if (selectClause.containsKey("operation")) {
			operationName = selectClause.getString("operation");

			SelectOperationType st = resource.getSupportedSelectOperationByName(operationName);

			if (st == null) {
				throw new QueryException("Unsupported Select Operation Type");
			}

			Map<String, Field> clauseFields = new HashMap<String, Field>();
			for (Field field : st.getFields()) {
				clauseFields.put(field.getPath(), field);
			}

			if (selectClause.containsKey("fields")) {
				JsonObject fieldObject = selectClause.getJsonObject("fields");
				objectFields = getObjectFields(clauseFields, fieldObject);
				fields = getStringFields(clauseFields, fieldObject);
			}
		}

		if ((resource.getSupportedSelectFields() != null) && (!resource.getSupportedSelectFields().isEmpty())) {

			Map<String, Field> clauseFields = new HashMap<String, Field>();
			for (Field field : resource.getSupportedSelectFields()) {
				clauseFields.put(field.getPath(), field);
			}

			if (selectClause.containsKey("fields")) {
				JsonObject fieldObject = selectClause.getJsonObject("fields");
				objectFields = getObjectFields(clauseFields, fieldObject);
				fields = getStringFields(clauseFields, fieldObject);
			}
		}

		return validateSelectClause(clauseId, resource, entity, alias, operationName, fields, objectFields);
	}

	private Map<String, Object> getObjectFields(Map<String, Field> clauseFields, JsonObject fieldObject)
			throws QueryException {
		Map<String, Object> objectFields = new HashMap<String, Object>();
		for (String key : fieldObject.keySet()) {
			ValueType vt = fieldObject.get(key).getValueType();

			if ((vt == ValueType.ARRAY)) {
				if (clauseFields.containsKey(key)
						&& (clauseFields.get(key).getDataTypes().contains(PrimitiveDataType.ARRAY))) {

					JsonArray array = fieldObject.getJsonArray(key);
					String[] stringArray = new String[array.size()];
					for (int sa_i = 0; sa_i < array.size(); sa_i++) {
						stringArray[sa_i] = array.getString(sa_i);
					}
					objectFields.put(key, stringArray);
				} else {
					throw new QueryException(key + " field does not support arrays.");
				}

			} else if (vt == ValueType.OBJECT) {
				// TODO To be implemented later. Check QueryService endpoint for
				// example
			}
		}

		return objectFields;
	}

	private Map<String, String> getStringFields(Map<String, Field> clauseFields, JsonObject fieldObject) {
		Map<String, String> fields = new HashMap<String, String>();
		for (String key : fieldObject.keySet()) {
			ValueType vt = fieldObject.get(key).getValueType();
			if ((vt != ValueType.ARRAY) && (vt != ValueType.OBJECT)) {
				fields.put(key, fieldObject.getString(key));
			}
		}

		return fields;
	}

	private Long validateSelectClause(Long clauseId, Resource resource, Entity field, String alias,
			String operationName, Map<String, String> fields, Map<String, Object> objectFields) throws QueryException {

		SelectOperationType operation = null;
		if (operationName != null) {
			operation = resource.getSupportedSelectOperationByName(operationName);
			if (operation == null) {
				throw new QueryException("Unknown select operation");
			}
		}

		// TODO this drops the objectFiels, but so far there is no need for it
		return addSelectClause(clauseId, resource, field, alias, operation, fields);
	}

}
