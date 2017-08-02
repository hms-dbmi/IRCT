/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.join.test;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Test;

import edu.harvard.hms.dbmi.bd2k.irct.exception.JoinActionSetupException;
import edu.harvard.hms.dbmi.bd2k.irct.join.UnionJoin;
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

public class UnionJoinTest {

	/**
	 * Tests the creation of a Left Outer Join
	 * 
	 */
	@Test
	public void testSetup() {
		UnionJoin uj = new UnionJoin();
		try {
			uj.setup(new HashMap<String, Object>());
			assertNotNull(uj);
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
		UnionJoin uj = new UnionJoin();
		SecureSession session = new SecureSession();
		Result result = new Result();
		MemoryResultSet rsi = new MemoryResultSet();
		Join join = new Join();

		try {
			result.setData(rsi);

			join.getObjectValues().put("LeftResultSet", createLeftResult());
			join.getObjectValues().put("RightResultSet", createRightResult());

			ResultSetImpl returnedData = (ResultSetImpl) uj.run(session, join,
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
		UnionJoin uj = new UnionJoin();
		SecureSession session = new SecureSession();
		Result result = new Result();
		MemoryResultSet rsi = new MemoryResultSet();
		Join join = new Join();

		try {
			result.setData(rsi);

			join.getObjectValues().put("LeftResultSet", createRightResult());
			join.getObjectValues().put("RightResultSet", null);

			result = uj.run(session, join, result);
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
		UnionJoin uj = new UnionJoin();
		SecureSession session = new SecureSession();
		Result result = new Result();
		MemoryResultSet rsi = new MemoryResultSet();
		Join join = new Join();

		try {
			result.setData(rsi);

			join.getObjectValues().put("LeftResultSet", null);
			join.getObjectValues().put("RightResultSet", null);

			result = uj.run(session, join, result);
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
		UnionJoin uj = new UnionJoin();
		SecureSession session = new SecureSession();
		Result result = new Result();
		MemoryResultSet rsi = new MemoryResultSet();
		Join join = new Join();

		try {
			result.setData(rsi);

			join.getObjectValues().put("LeftResultSet", createLeftResult());
			join.getObjectValues().put("RightResultSet", createRightResult());

			uj.run(session, join, result);

			ResultSetImpl returnedData = (ResultSetImpl) uj.getResults(result)
					.getData();
			assertTrue("Results are not equal",
					JoinTestUtil.isEqual(returnedData, createComparator()));

			assertSame(result, uj.getResults(result));
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
		UnionJoin uj = new UnionJoin();
		assertEquals("Should be result type of tabular", uj.getJoinDataType(),
				ResultDataType.TABULAR);
	}

	/**
	 * Creates a left result set for testing
	 * 
	 * @return ResultSet
	 * @throws ResultSetException An exception occurred
	 * @throws PersistableException An exception occurred
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
	 * @throws ResultSetException An exception occurred
	 * @throws PersistableException An exception occurred
	 */
	private ResultSet createRightResult() throws ResultSetException,
			PersistableException {
		Column leftIdColumn = new Column();
		leftIdColumn.setName("id");
		leftIdColumn.setDataType(PrimitiveDataType.INTEGER);

		Column leftNameColumn = new Column();
		leftNameColumn.setName("Name");
		leftNameColumn.setDataType(PrimitiveDataType.STRING);

		return JoinTestUtil.createResultSet(new Column[] { leftIdColumn,
				leftNameColumn }, new Object[] { 4, "Sue", 5, "Timmy", 6,
				"Sam" });
	}

	/**
	 * Creates a comparator result set for testing
	 * 
	 * @return ResultSet
	 * @throws ResultSetException An exception occurred
	 * @throws PersistableException An exception occurred
	 */
	private ResultSet createComparator() throws ResultSetException,
			PersistableException {
		Column leftIdColumn = new Column();
		leftIdColumn.setName("id");
		leftIdColumn.setDataType(PrimitiveDataType.INTEGER);

		Column leftNameColumn = new Column();
		leftNameColumn.setName("Name");
		leftNameColumn.setDataType(PrimitiveDataType.STRING);

		return JoinTestUtil.createResultSet(new Column[] { leftIdColumn,
				leftNameColumn }, new Object[] { 1, "Jeremy", 2, "James", 3, "Bob", 4, "Sue", 5, "Timmy", 6, "Sam"});
	}

}
