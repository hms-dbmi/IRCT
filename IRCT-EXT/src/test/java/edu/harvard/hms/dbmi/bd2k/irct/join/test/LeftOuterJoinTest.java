package edu.harvard.hms.dbmi.bd2k.irct.join.test;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.harvard.hms.dbmi.bd2k.irct.join.LeftOuterJoin;
import edu.harvard.hms.dbmi.bd2k.irct.model.join.Join;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

public class LeftOuterJoinTest {

	@Test
	public void testSetup() {
		LeftOuterJoin loj = new LeftOuterJoin();
		assertNotNull(loj);
	}

	@Test
	public void testRun() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetResults() {
		LeftOuterJoin loj = new LeftOuterJoin();
		SecureSession session = new SecureSession();
		
		Result result = new Result();
		
		
		Join join = new Join();
		join.getStringValues().put("LeftColumn", "id");
		join.getStringValues().put("RightColumn", "user_id");
		
		try {
			result = loj.run(session, join, result);
		} catch (ResultSetException | PersistableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertSame(result, loj.getResults(result));
	}

	@Test
	public void testGetJoinDataType() {
		LeftOuterJoin loj = new LeftOuterJoin();
		assertEquals("Should be result type of tabular", loj.getJoinDataType(), ResultDataType.TABULAR);
	}

}
