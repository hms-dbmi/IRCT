/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.join;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.hash.Funnel;
import com.google.common.hash.PrimitiveSink;

import edu.harvard.hms.dbmi.bd2k.irct.model.join.Join;
import edu.harvard.hms.dbmi.bd2k.irct.model.join.JoinImplementation;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultStatus;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.ResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.ResultSetImpl;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

/**
 * Performs a left outer join between two result sets
 * 
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class LeftOuterBloomArrayJoin implements JoinImplementation {

	@Override
	public void setup(Map<String, Object> parameters) {
	}

	@Override
	public Result run(SecureSession session, Join join, Result result)
			throws ResultSetException, PersistableException {

		ResultSet leftResultSet = (ResultSet) join.getObjectValues().get("LeftResultSet");
		ResultSet rightResultSet = (ResultSet) join.getObjectValues().get("RightResultSet");

		
		// Get Left Matching Column Ids
		List<Integer> leftMatchingColumns = new ArrayList<Integer>();
		for(String columnName : join.getStringValues().get("LeftColumn").split(",")) {
			leftMatchingColumns.add(leftResultSet.findColumn(columnName));
		}
		//Get Right Matching Column Ids
		List<Integer> rightMatchingColumns = new ArrayList<Integer>();
		for(String columnName : join.getStringValues().get("RightColumn").split(",")) {
			rightMatchingColumns.add(leftResultSet.findColumn(columnName));
		}
		
		Funnel<String> rowFunnel = new Funnel<String>() {
			@Override
			public void funnel(String rowIdentifier, PrimitiveSink into) {
				into.putString(rowIdentifier, Charsets.UTF_8);
			}
		};
		
		
		// OLD

		
		ResultSetImpl computedResults = (ResultSetImpl) result.getData();

		for (int leftColumnIterator = 0; leftColumnIterator < leftResultSet.getColumnSize(); leftColumnIterator++) {
			computedResults.appendColumn(leftResultSet.getColumn(leftColumnIterator));
		}
		
		List<Integer> rightColumns = new ArrayList<Integer>();
		for (int rightColumnIterator = 0; rightColumnIterator < rightResultSet.getColumnSize(); rightColumnIterator++) {
			if (rightColumnIterator != rightColumnIndex) {
				computedResults.appendColumn(rightResultSet.getColumn(rightColumnIterator));
				rightColumns.add(rightColumnIterator);
			}
		}
		int baseColumn = leftResultSet.getColumnSize();

		leftResultSet.beforeFirst();
		while (leftResultSet.next()) {
			Object leftRowMatchObj = ((ResultSetImpl) leftResultSet).getObject(leftColumnIndex);

			// Add a new row
			computedResults.appendRow();

			// Copy Left values over
			for (int leftColumnIterator = 0; leftColumnIterator < leftResultSet.getColumnSize(); leftColumnIterator++) {
				computedResults.updateObject(leftColumnIterator, ((ResultSetImpl) leftResultSet).getObject(leftColumnIterator));
			}

			rightResultSet.beforeFirst();
			while (rightResultSet.next()) {
				if (((ResultSetImpl) rightResultSet).getObject(rightColumnIndex).equals(leftRowMatchObj)) {
					// Copy Right values over
					for(int rightColumnIterator = 0; rightColumnIterator < rightColumns.size(); rightColumnIterator++) {
						computedResults.updateObject(baseColumn + rightColumnIterator, ((ResultSetImpl) rightResultSet).getObject(rightColumns.get(rightColumnIterator)));
					}

				}
			}
		}
		computedResults.beforeFirst();
		result.setResultStatus(ResultStatus.COMPLETE);
		result.setData(computedResults);
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
