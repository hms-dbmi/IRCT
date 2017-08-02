/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.join.test;

import static org.junit.Assert.*;

import java.util.concurrent.ThreadLocalRandom;

import org.junit.Test;

import edu.harvard.hms.dbmi.bd2k.irct.exception.JoinActionSetupException;
import edu.harvard.hms.dbmi.bd2k.irct.join.FullOuterHashJoin;
import edu.harvard.hms.dbmi.bd2k.irct.join.InnerHashJoin;
import edu.harvard.hms.dbmi.bd2k.irct.join.LeftOuterHashJoin;
import edu.harvard.hms.dbmi.bd2k.irct.join.RightOuterHashJoin;
import edu.harvard.hms.dbmi.bd2k.irct.model.join.Join;
import edu.harvard.hms.dbmi.bd2k.irct.model.join.JoinImplementation;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.Column;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.ResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.ResultSetImpl;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

public class HashJoinScaleTest {

	// Left Join

	@Test
	public void testTenLeftJoin() {
		try {

			ResultSetImpl returnedData = runLeftJoin(10, 0, 4);

		} catch (ResultSetException | PersistableException
				| JoinActionSetupException e) {
			e.printStackTrace();
			fail("Exception thrown");
		}
	}

	@Test
	public void testOneHundredLeftJoin() {
		try {

			ResultSetImpl returnedData = runLeftJoin(100, 0, 40);

		} catch (ResultSetException | PersistableException
				| JoinActionSetupException e) {
			e.printStackTrace();
			fail("Exception thrown");
		}
	}

	@Test
	public void testOneThousandLeftJoin() {
		try {

			ResultSetImpl returnedData = runLeftJoin(1000, 100, 400);

		} catch (ResultSetException | PersistableException
				| JoinActionSetupException e) {
			e.printStackTrace();
			fail("Exception thrown");
		}
	}

	@Test
	public void testOneMillionLeftJoin() {
		try {

			ResultSetImpl returnedData = runLeftJoin(1000000, 100000, 400000);

		} catch (ResultSetException | PersistableException
				| JoinActionSetupException e) {
			e.printStackTrace();
			fail("Exception thrown");
		}
	}
	

	// Right Join
	@Test
	public void testTenRightJoin() {
		try {

			ResultSetImpl returnedData = runRightJoin(10, 0, 4);

		} catch (ResultSetException | PersistableException
				| JoinActionSetupException e) {
			e.printStackTrace();
			fail("Exception thrown");
		}
	}

	@Test
	public void testOneHundredRightJoin() {
		try {

			ResultSetImpl returnedData = runRightJoin(100, 10, 40);

		} catch (ResultSetException | PersistableException
				| JoinActionSetupException e) {
			e.printStackTrace();
			fail("Exception thrown");
		}
	}

	@Test
	public void testOneThousandRightJoin() {
		try {

			ResultSetImpl returnedData = runRightJoin(1000, 100, 400);

		} catch (ResultSetException | PersistableException
				| JoinActionSetupException e) {
			e.printStackTrace();
			fail("Exception thrown");
		}
	}

	@Test
	public void testOneMillionRightJoin() {
		try {

			ResultSetImpl returnedData = runRightJoin(1000000, 100000, 400000);

		} catch (ResultSetException | PersistableException
				| JoinActionSetupException e) {
			e.printStackTrace();
			fail("Exception thrown");
		}
	}
	

	// Inner Join
	@Test
	public void testTenInnerJoin() {
		try {
			ResultSetImpl returnedData = runInnerJoin(10, 0, 4);

		} catch (ResultSetException | PersistableException
				| JoinActionSetupException e) {
			e.printStackTrace();
			fail("Exception thrown");
		}
	}

	@Test
	public void testOneHundredInnerJoin() {
		try {
			ResultSetImpl returnedData = runInnerJoin(100, 10, 40);

		} catch (ResultSetException | PersistableException
				| JoinActionSetupException e) {
			e.printStackTrace();
			fail("Exception thrown");
		}
	}

	@Test
	public void testOneThousandInnerJoin() {
		try {
			ResultSetImpl returnedData = runInnerJoin(1000, 100, 400);

		} catch (ResultSetException | PersistableException
				| JoinActionSetupException e) {
			e.printStackTrace();
			fail("Exception thrown");
		}
	}

	@Test
	public void testOneMillionInnerJoin() {
		try {
			ResultSetImpl returnedData = runInnerJoin(1000000, 100000, 400000);

		} catch (ResultSetException | PersistableException
				| JoinActionSetupException e) {
			e.printStackTrace();
			fail("Exception thrown");
		}
	}
	

	// Outer Join
	@Test
	public void testTenFullOuterJoin() {
		try {
			ResultSetImpl returnedData = runFullOuterJoin(10, 0, 4);

		} catch (ResultSetException | PersistableException
				| JoinActionSetupException e) {
			e.printStackTrace();
			fail("Exception thrown");
		}
	}

	@Test
	public void testOneHundredFullOuterJoin() {
		try {
			ResultSetImpl returnedData = runFullOuterJoin(100, 10, 40);

		} catch (ResultSetException | PersistableException
				| JoinActionSetupException e) {
			e.printStackTrace();
			fail("Exception thrown");
		}
	}

	@Test
	public void testOneThousandFullOuterJoin() {
		try {
			ResultSetImpl returnedData = runFullOuterJoin(1000, 100, 400);

		} catch (ResultSetException | PersistableException
				| JoinActionSetupException e) {
			e.printStackTrace();
			fail("Exception thrown");
		}
	}

	@Test
	public void testOneMillionFullOuterJoin() {
		try {
			ResultSetImpl returnedData = runFullOuterJoin(1000000, 100000, 400000);

		} catch (ResultSetException | PersistableException
				| JoinActionSetupException e) {
			e.printStackTrace();
			fail("Exception thrown");
		}
	}
	
	//Utilities
	
	
	private ResultSetImpl runLeftJoin(int size, int min, int max)
			throws JoinActionSetupException, ResultSetException,
			PersistableException {

		return runJoin(new LeftOuterHashJoin(), size, min, max);
	}
	
	private ResultSetImpl runRightJoin(int size, int min, int max)
			throws JoinActionSetupException, ResultSetException,
			PersistableException {

		return runJoin(new RightOuterHashJoin(), size, min, max);
	}
	
	private ResultSetImpl runInnerJoin(int size, int min, int max)
			throws JoinActionSetupException, ResultSetException,
			PersistableException {

		return runJoin(new InnerHashJoin(), size, min, max);
	}
	
	private ResultSetImpl runFullOuterJoin(int size, int min, int max)
			throws JoinActionSetupException, ResultSetException,
			PersistableException {

		return runJoin(new FullOuterHashJoin(), size, min, max);
	}
	
	private ResultSetImpl runJoin(JoinImplementation hashJoin, int size, int min, int max)
			throws JoinActionSetupException, ResultSetException,
			PersistableException {
		Column leftIdColumn = new Column();
		leftIdColumn.setName("id");
		leftIdColumn.setDataType(PrimitiveDataType.INTEGER);

		Column leftNameColumn = new Column();
		leftNameColumn.setName("Name");
		leftNameColumn.setDataType(PrimitiveDataType.STRING);

		Column rightIdColumn = new Column();
		rightIdColumn.setName("user_id");
		rightIdColumn.setDataType(PrimitiveDataType.INTEGER);

		Column rightAgeColumn = new Column();
		rightAgeColumn.setName("Row");
		rightAgeColumn.setDataType(PrimitiveDataType.STRING);

		SecureSession session = new SecureSession();
		Result result = new Result();
		MemoryResultSet rsi = new MemoryResultSet();
		Join join = new Join();

		hashJoin.setup(null);
		result.setData(rsi);

		join.getObjectValues().put(
				"LeftResultSet",
				createResultSet(size, new Column[] { leftIdColumn,
						leftNameColumn }, min, max));
		join.getStringValues().put("LeftColumn", "id");
		join.getObjectValues().put(
				"RightResultSet",
				createResultSet(size, new Column[] { rightIdColumn,
						rightAgeColumn }, min, max));
		join.getStringValues().put("RightColumn", "user_id");

		return (ResultSetImpl) hashJoin.run(session, join, result).getData();
	}

	private ResultSet createResultSet(int size, Column[] columns, int min,
			int max) throws ResultSetException, PersistableException {
		MemoryResultSet mrs = new MemoryResultSet();

		for (Column column : columns) {
			mrs.appendColumn(column);
		}

		mrs.beforeFirst();
		for (int row = 0; row < size; row++) {
			mrs.appendRow();
			int id = ThreadLocalRandom.current().nextInt(min, max + 1);

			mrs.updateInt(0, id);
			mrs.updateString(1, Integer.toString(row));
		}

		return mrs;
	}
}
