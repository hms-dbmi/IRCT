package edu.harvard.hms.dbmi.bd2k.irct.cl.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.harvard.hms.dbmi.bd2k.irct.IRCTApplication;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.DataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.JoinType;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.PredicateType;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.SelectOperationType;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.SortOperationType;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.*;
import edu.harvard.hms.dbmi.bd2k.irct.model.visualization.VisualizationReturnType;
import edu.harvard.hms.dbmi.bd2k.irct.model.visualization.VisualizationType;
import edu.harvard.hms.dbmi.bd2k.irct.ri.exac.EXACOntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.I2B2OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2transmart.I2B2TranSMARTResourceImplementation;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

import static java.util.Collections.binarySearch;
import static org.junit.Assert.*;

public class ResourceServiceSerializationTest {

	private static String serializedResource;
	
	/*
	 *  Why not a Resource instance here? 
	 *  
	 *  Because the Resource serialization is not symmetrical. This is mostly restricted
	 *  due to overuse of object orientation around DataType enums.
	 */
	private static Map<String, Object> deserializedResource;
	
	/*
	 * Disclaimer : Like lots of other things in the IRCT codebase, there are a ton of
	 * non-implemented, or half implemented features in the Resource class.
	 * 
	 * This test does not test things that are not actually in use currently. Included
	 * in this list are JoinTypes, VisualizationTypes, etc. These are populated in
	 * the test Resource instance to ensure that they don't contain anything that cannot
	 * be serialized by Jackson, but no other expectations are enforced as the behavior
	 * of these is unspecified(so they should probably just be removed as noise).
	 * 
	 * The purpose of this test is to ensure that the information that needs to be
	 * private is not serialized, and that information helpful to users is serialized
	 * in a useful way.
	 */
	@BeforeClass
	public static void setup() throws Exception {
		Resource r = new Resource();
		r.setId(4);
		r.setName("Foo");
		
		List<DataType> dataTypes = new ArrayList<>();
		dataTypes.addAll((Collection<? extends DataType>) ImmutableList.of(
				PrimitiveDataType.ARRAY, 
				PrimitiveDataType.BOOLEAN, 
				PrimitiveDataType.BYTE, 
				PrimitiveDataType.COLUMN, 
				PrimitiveDataType.DATE, 
				PrimitiveDataType.DATETIME, 
				PrimitiveDataType.DOUBLE, 
				PrimitiveDataType.FLOAT, 
				PrimitiveDataType.INTEGER, 
				PrimitiveDataType.LONG, 
				PrimitiveDataType.RESULTSET, 
				PrimitiveDataType.STRING, 
				PrimitiveDataType.SUBQUERY, 
				PrimitiveDataType.TIME));
		r.setDataTypes(dataTypes);
		
		
		I2B2TranSMARTResourceImplementation resourceImplementationInterface = new I2B2TranSMARTResourceImplementation();
		resourceImplementationInterface.setup(ImmutableMap.of(
				"resourceName", "FORTEST", 
				"resourceURL", "https://foo.bar", 
				"transmartURL", "https://foo.bar.tm",
				"domain", "domainForTest"));
		r.setImplementingInterface(resourceImplementationInterface);
		
		r.setLogicalOperators(ImmutableList.of(LogicalOperator.AND, LogicalOperator.NOT, LogicalOperator.OR));
		r.setParameters(ImmutableMap.of("foo", "bar"));
		
		List<OntologyRelationship> relationships = new ArrayList<>();
		relationships.addAll(ImmutableList.of(
				I2B2OntologyRelationship.MODIFIER, 
				I2B2OntologyRelationship.CHILD, 
				I2B2OntologyRelationship.SIBLING, 
				I2B2OntologyRelationship.PARENT, 
				I2B2OntologyRelationship.TERM, 
				EXACOntologyRelationship.CHILD, 
				EXACOntologyRelationship.PARENT));
		r.setRelationships(relationships);

		Field field = new Field();
		field.setDataTypes(dataTypes);
		field.setDescription("foofield");
		field.setId(3);
		field.setName("name");
		field.setPath("/foo/bar/foo");
		field.setPermittedValues(ImmutableList.of("FOO", "BAR"));
		field.setRelationship(I2B2OntologyRelationship.CHILD);
		field.setRequired(false);
		JoinType joinType = new JoinType();
		joinType.setDataTypes(dataTypes);
		joinType.setDescription("foobar");
		joinType.setDisplayName("foojoin");
		joinType.setName("joinTypeName");
		joinType.setId(333);
		ImmutableList<Field> fields = ImmutableList.of(field);
		joinType.setFields(fields);
		r.setSupportedJoins(ImmutableList.of(joinType));
		PredicateType predicateType = new PredicateType();
		predicateType.setName("fooPredicate");
		predicateType.setDescription("fooPredicateDescription");
		predicateType.setDefaultPredicate(false);
		predicateType.setDisplayName("FOOPREDICATE");
		predicateType.setFields(fields);
		predicateType.setId(9);
		predicateType.setDataTypes(dataTypes);
		predicateType.setPaths(ImmutableList.of("FOO", "BAR"));
		r.setSupportedPredicates(ImmutableList.of(predicateType));
		ProcessType processType = new ProcessType();
		processType.setDescription("FooProcessDescription");
		processType.setDisplayName("FOOProcessDisplayName");
		processType.setFields(fields);
		processType.setId(5);
		processType.setName("FooProcessDescription");
		processType.setReturns(fields);
		r.setSupportedProcesses(ImmutableList.of(processType));
		r.setSupportedSelectFields(fields);
		SelectOperationType selectOperationType = new SelectOperationType();
		selectOperationType.setDataTypes(dataTypes);
		selectOperationType.setDescription("selectOperationDescription");
		selectOperationType.setDisplayName("selectOperationDisplayName");
		selectOperationType.setFields(fields);
		selectOperationType.setId(8);
		selectOperationType.setName("name");
		selectOperationType.setPaths(ImmutableList.of("//foo///path"));
		r.setSupportedSelectOperations(ImmutableList.of(selectOperationType));
		SortOperationType sortOperationType = new SortOperationType();
		sortOperationType.setDataTypes(dataTypes);
		sortOperationType.setDescription("SortOperationTypeDescription");
		sortOperationType.setDisplayName("sort operation type");
		sortOperationType.setFields(fields);
		sortOperationType.setId(34);
		sortOperationType.setName("sortname");
		sortOperationType.setPaths(ImmutableList.of("//foo///path"));
		r.setSupportedSortOperations(ImmutableList.of(sortOperationType));
		VisualizationType visualizationType = new VisualizationType();
		visualizationType.setDescription("visualization description");
		visualizationType.setDisplayName("viz displayName");
		visualizationType.setFields(fields);
		visualizationType.setId(3333);
		visualizationType.setName("viz name");
		visualizationType.setReturns(VisualizationReturnType.HTML);
		r.setSupportedVisualizations(ImmutableList.of(visualizationType));
		ObjectMapper mapper = IRCTApplication.objectMapper;
		serializedResource = mapper.writeValueAsString(r);
		deserializedResource = mapper.readValue(serializedResource, Map.class);	
	}
	
	@Test
	public void resourceParametersAreNotSerializedBecauseTheyContainSecrets() throws Exception {
		assertNull(deserializedResource.get("parameters"));
	}
	
	@Test
	public void allPrimitiveDataTypesAreInSerializedResourceAndHaveUsefulMetadataAttached() throws Exception {
		List<Map<String, String>> dataTypes = (List<Map<String, String>>)deserializedResource.get("dataTypes");
		for(DataType t : PrimitiveDataType.values()){
			dataTypeAssertions("PrimitiveDataType", t, findDataTypeByName(dataTypes, t));
		}
	}

	private Map<String, String> findDataTypeByName(List<Map<String, String>> dataTypes, DataType t) {
		DataTypeNameComparator comparator = new DataTypeNameComparator();
		Collections.sort(dataTypes, comparator);
		int index = binarySearch(dataTypes, ImmutableMap.of("name", t.getName()), comparator);
		assertTrue("The " + t + " should be in the deserialized resource.", index > -1);
		Map<String, String> deserializedDataType = dataTypes.get(index);
		return deserializedDataType;
	}
	
	private void dataTypeAssertions(String dataTypeEnum, DataType type, Map<String, String> deserializedDataType){
		assertNotNull("The " + dataTypeEnum + "::" + type + " should be in the deserialized resource.", type);
		assertEquals("The validation pattern should be included in the serialized representation of " + type.getName() + ". ", type.getPattern().toString(), deserializedDataType.get("pattern"));
		assertEquals("The description should be included in the serialized representation of " + ". ", type.getDescription(), deserializedDataType.get("description"));
		assertEquals("The typeof hint should be included in the serialized representation of " + ". ", type.typeOf() == null ? null : type.typeOf().getName(), deserializedDataType.get("typeof"));		
	}
	
	private final class DataTypeNameComparator implements Comparator<Map<String, String>> {
		public int compare(Map<String, String> o1, Map<String, String> o2) {
			return o1.get("name").toLowerCase().compareTo(o2.get("name").toLowerCase());
		}
	}
}
