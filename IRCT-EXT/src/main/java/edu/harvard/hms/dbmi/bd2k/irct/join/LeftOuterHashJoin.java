/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.join;

import java.util.Map;

import edu.harvard.hms.dbmi.bd2k.irct.exception.JoinActionSetupException;
import edu.harvard.hms.dbmi.bd2k.irct.join.HashJoinImpl.HashJoinImplType;
import edu.harvard.hms.dbmi.bd2k.irct.model.join.Join;
import edu.harvard.hms.dbmi.bd2k.irct.model.join.JoinImplementation;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultStatus;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.ResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

public class LeftOuterHashJoin implements JoinImplementation {
	private long blockSize;

	@Override
	public void setup(Map<String, Object> parameters)
			throws JoinActionSetupException {
		this.blockSize = 100000L;
	}

	@Override
	public Result run(SecureSession session, Join join, Result result)
			throws ResultSetException, PersistableException {

		ResultSet leftResultSet = (ResultSet) join.getObjectValues().get(
				"LeftResultSet");

		if (leftResultSet == null) {
			result.setResultStatus(ResultStatus.ERROR);
			result.setMessage("LeftResultSet is null");
			return result;
		}
		ResultSet rightResultSet = (ResultSet) join.getObjectValues().get(
				"RightResultSet");
		if (rightResultSet == null) {
			result.setResultStatus(ResultStatus.ERROR);
			result.setMessage("RightResultSet is null");
			return result;
		}

		// Get Left Matching Column Ids
		String[] leftStringColumnNames = join.getStringValues().get("LeftColumn").split(",");
		int[] leftColumns = new int[leftStringColumnNames.length];
		
		int counter = 0;
		try {
			for (String columnName : leftStringColumnNames) {
				leftColumns[counter] = leftResultSet.findColumn(columnName);
				counter++;
			}
		} catch (ResultSetException rse) {
			result.setResultStatus(ResultStatus.ERROR);
			result.setMessage("LeftColumn : " + rse.getMessage());
			return result;
		}
		
		// Get Right Matching Column Ids
		String[] rightStringColumnNames = join.getStringValues().get("RightColumn").split(",");
		int[] rightColumns = new int[rightStringColumnNames.length];
		
		counter = 0;
		try {
			for (String columnName : rightStringColumnNames) {
				rightColumns[counter] = rightResultSet.findColumn(columnName);
				counter++;
			}
		} catch (ResultSetException rse) {
			result.setResultStatus(ResultStatus.ERROR);
			result.setMessage("RightColumn : " + rse.getMessage());
			return result;
		}

		HashJoinImpl hashJoin = new HashJoinImpl(leftResultSet, rightResultSet, leftColumns, rightColumns, HashJoinImplType.LEFTOUTER, this.blockSize);

		ResultSet outputResult = (ResultSet) result.getData();
		
		outputResult = hashJoin.join(outputResult);

		outputResult.beforeFirst();
		result.setResultStatus(ResultStatus.COMPLETE);
		result.setData(outputResult);
		
		return result;
	}

	@Override
	public Result getResults(Result result) {
		return result;
	}

	@Override
	public ResultDataType getJoinDataType() {
		return ResultDataType.TABULAR;
	}
}
