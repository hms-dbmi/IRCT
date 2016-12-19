package edu.harvard.hms.dbmi.bd2k.irct.join.test;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Test;

import edu.harvard.hms.dbmi.bd2k.irct.exception.JoinActionSetupException;
import edu.harvard.hms.dbmi.bd2k.irct.join.InnerJoin;
import edu.harvard.hms.dbmi.bd2k.irct.model.join.Join;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultStatus;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.Column;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.ResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.ResultSetImpl;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

public class InnerJoinTest {

	/**
	 * Tests the creation of a Left Outer Join
	 * 
	 */
	@Test
	public void testSetup() {
		InnerJoin ij = new InnerJoin();
		try {
			ij.setup(new HashMap<String, Object>());
			assertNotNull(ij);
		} catch (JoinActionSetupException e) {
			e.printStackTrace();
			fail("Exception thrown");
		}
	}

	/**
	 * Runs a join between two result sets and tests to see if the results are
	 * equal
	 */
	@Test
	public void testRunPositive() {
		InnerJoin ij = new InnerJoin();
		SecureSession session = new SecureSession();
		Result result = new Result();
		MemoryResultSet rsi = new MemoryResultSet();
		Join join = new Join();

		try {
			result.setData(rsi);

			join.getObjectValues().put("LeftResultSet", createLeftResult());
			join.getStringValues().put("LeftColumn", "id");
			join.getObjectValues().put("RightResultSet", createRightResult());
			join.getStringValues().put("RightColumn", "user_id");

			ResultSetImpl returnedData = (ResultSetImpl) ij.run(session, join,
					result).getData();
			
			
			assertTrue("Results are not equal",
					JoinTestUtil.isEqual(returnedData, createComparator()));
		} catch (ResultSetException | PersistableException e) {
			e.printStackTrace();
			fail("Exception thrown");
		}

	}

	/**
	 * Runs a join between two results sets that should fail on joining
	 */
	@Test
	public void testRunNegative() {
		InnerJoin ij = new InnerJoin();
		SecureSession session = new SecureSession();
		Result result = new Result();
		MemoryResultSet rsi = new MemoryResultSet();
		Join join = new Join();

		try {
			result.setData(rsi);

			join.getObjectValues().put("LeftResultSet", createRightResult());
			join.getStringValues().put("LeftColumn", "id");
			join.getObjectValues().put("RightResultSet", createLeftResult());
			join.getStringValues().put("RightColumn", "user_id");

			result = ij.run(session, join, result);
			assertEquals("ResultStatus is not ERROR", ResultStatus.ERROR,
					result.getResultStatus());
		} catch (ResultSetException | PersistableException e) {
			e.printStackTrace();
			fail("Exception thrown");
		}

	}

	/**
	 * Runs a join between two results sets where the result sets are null
	 */
	@Test
	public void testRunNull() {
		InnerJoin ij = new InnerJoin();
		SecureSession session = new SecureSession();
		Result result = new Result();
		MemoryResultSet rsi = new MemoryResultSet();
		Join join = new Join();

		try {
			result.setData(rsi);

			join.getObjectValues().put("LeftResultSet", null);
			join.getStringValues().put("LeftColumn", "id");
			join.getObjectValues().put("RightResultSet", null);
			join.getStringValues().put("RightColumn", "user_id");

			result = ij.run(session, join, result);
			assertEquals("ResultStatus is not ERROR", ResultStatus.ERROR,
					result.getResultStatus());
		} catch (ResultSetException | PersistableException e) {
			e.printStackTrace();
			fail("Exception thrown");
		}

	}

	/**
	 * Tests to ensure that the results from the getResults is equal to the
	 * results from the run
	 */
	@Test
	public void testGetResults() {
		InnerJoin ij = new InnerJoin();
		SecureSession session = new SecureSession();
		Result result = new Result();
		MemoryResultSet rsi = new MemoryResultSet();
		Join join = new Join();

		try {
			result.setData(rsi);

			join.getObjectValues().put("LeftResultSet", createLeftResult());
			join.getStringValues().put("LeftColumn", "id");
			join.getObjectValues().put("RightResultSet", createRightResult());
			join.getStringValues().put("RightColumn", "user_id");

			ij.run(session, join, result);

			ResultSetImpl returnedData = (ResultSetImpl) ij.getResults(result)
					.getData();
			assertTrue("Results are not equal",
					JoinTestUtil.isEqual(returnedData, createComparator()));

			assertSame(result, ij.getResults(result));
		} catch (ResultSetException | PersistableException e) {
			e.printStackTrace();
			fail("Exception thrown");

		}

	}

	/**
	 * Tests to make sure that the result type that is returned is tabular;
	 */
	@Test
	public void testGetJoinDataType() {
		InnerJoin ij = new InnerJoin();
		assertEquals("Should be result type of tabular", ij.getJoinDataType(),
				ResultDataType.TABULAR);
	}

	/**
	 * Creates a left result set for testing
	 * 
	 * @return ResultSet
	 * @throws ResultSetException
	 *             An exception occurred
	 * @throws PersistableException
	 *             An exception occurred
	 */
	private ResultSet createLeftResult() throws ResultSetException,
			PersistableException {
		Column leftIdColumn = new Column();
		leftIdColumn.setName("id");
		leftIdColumn.setDataType(PrimitiveDataType.INTEGER);

		Column leftNameColumn = new Column();
		leftNameColumn.setName("Name");
		leftNameColumn.setDataType(PrimitiveDataType.STRING);

		return JoinTestUtil.createResultSet(new Column[] { leftIdColumn,
				leftNameColumn }, new Object[] { 1, "Jeremy", 2, "James", 3,
				"Bob" });
	}

	/**
	 * Creates a right result set for testing
	 * 
	 * @return ResultSet
	 * @throws ResultSetException
	 *             An exception occurred
	 * @throws PersistableException
	 *             An exception occurred
	 */
	private ResultSet createRightResult() throws ResultSetException,
			PersistableException {
		Column rightIdColumn = new Column();
		rightIdColumn.setName("user_id");
		rightIdColumn.setDataType(PrimitiveDataType.INTEGER);

		Column rightAgeColumn = new Column();
		rightAgeColumn.setName("Age");
		rightAgeColumn.setDataType(PrimitiveDataType.INTEGER);

		return JoinTestUtil.createResultSet(new Column[] { rightIdColumn,
				rightAgeColumn }, new Object[] { 1, 20, 2, 30, 5, 10 });
	}

	/**
	 * Creates a comparator result set for testing
	 * 
	 * @return ResultSet
	 * @throws ResultSetException
	 *             An exception occurred
	 * @throws PersistableException
	 *             An exception occurred
	 */
	private ResultSet createComparator() throws ResultSetException,
			PersistableException {
		Column leftIdColumn = new Column();
		leftIdColumn.setName("id");
		leftIdColumn.setDataType(PrimitiveDataType.INTEGER);

		Column leftNameColumn = new Column();
		leftNameColumn.setName("Name");
		leftNameColumn.setDataType(PrimitiveDataType.STRING);

		Column rightAgeColumn = new Column();
		rightAgeColumn.setName("Age");
		rightAgeColumn.setDataType(PrimitiveDataType.INTEGER);

		return JoinTestUtil.createResultSet(new Column[] { leftIdColumn,
				leftNameColumn, rightAgeColumn }, new Object[] { 1, "Jeremy",
				20, 2, "James", 30, 3 });
	}

}
