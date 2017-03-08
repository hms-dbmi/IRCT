/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.join;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Charsets;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.Column;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.ResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.Row;

/**
 * This is an implementation of a equi hybrid hash join. It performs joins
 * between two result sets of either full, inner, left, or right type.
 * 
 * You can read more about a hybrid hash join at
 * https://en.wikipedia.org/wiki/Hash_join#Hybrid_hash_join
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class HashJoinImpl {
	enum HashJoinImplType {
		FULLOUTER, INNERJOIN, LEFTOUTER, RIGHTOUTER
	}

	private HashFunction hashFunction;
	private boolean isLeftOuterResultSet;
	private ResultSet innerResultSet;
	private ResultSet outerResultSet;

	private List<Integer> ignoreOuterColumns;
	private List<Integer> ignoreInnerColumns;

	private int[] innerColumns;
	private PrimitiveDataType[] innerDataTypes;
	private int[] outerColumns;
	private PrimitiveDataType[] outerDataTypes;
	private HashJoinImplType joinType;
	private long blockSize;
	private LinkedHashMultimap<HashCode, Row> hashMultiMap;

	public HashJoinImpl(ResultSet leftResultSet, ResultSet rightResultSet,
			int[] leftColumns, int[] rightColumns, HashJoinImplType joinType,
			long blockSize) throws ResultSetException {
		this.joinType = joinType;
		this.blockSize = blockSize;

		this.ignoreOuterColumns = new ArrayList<Integer>();
		this.ignoreInnerColumns = new ArrayList<Integer>();

		// If the left result set is smaller or equal to the right result set it
		// becomes the outer result set, and the right result set becomes the
		// inner one. Otherwise the outer result set is set to the right result
		// set and the inner result set is set to the left result set.
		if (leftResultSet.getSize() <= rightResultSet.getSize()) {
			this.outerResultSet = leftResultSet;
			this.outerColumns = leftColumns;
			this.innerResultSet = rightResultSet;
			this.innerColumns = rightColumns;
			this.isLeftOuterResultSet = true;
		} else {
			this.outerResultSet = rightResultSet;
			this.outerColumns = rightColumns;
			this.innerResultSet = leftResultSet;
			this.innerColumns = leftColumns;
			this.isLeftOuterResultSet = false;
		}

		this.innerDataTypes = new PrimitiveDataType[this.innerColumns.length];
		this.outerDataTypes = new PrimitiveDataType[this.outerColumns.length];

		int counter = 0;
		for (int column : innerColumns) {
			this.innerDataTypes[counter] = this.innerResultSet
					.getColumn(column).getDataType();
			counter++;
		}
		counter = 0;
		for (int column : outerColumns) {
			this.outerDataTypes[counter] = this.outerResultSet
					.getColumn(column).getDataType();
			counter++;
		}

		hashFunction = Hashing.murmur3_128();
		hashMultiMap = LinkedHashMultimap.<HashCode, Row> create();
	}

	public ResultSet join(ResultSet output) throws ResultSetException,
			PersistableException {
		// Set up columns for output
		// Calculate offset
		int innerOffset = 0;
		int outerOffset = 0;
		List<String> columnNames = new ArrayList<String>();
		if (isLeftOuterResultSet) {

			for (int outerColumnIterator = 0; outerColumnIterator < outerResultSet
					.getColumnSize(); outerColumnIterator++) {
				Column column = outerResultSet.getColumn(outerColumnIterator);
				columnNames.add(column.getName());
				output.appendColumn(column);
			}

			for (int innerColumnIterator = 0; innerColumnIterator < innerResultSet
					.getColumnSize(); innerColumnIterator++) {
				Column column = innerResultSet.getColumn(innerColumnIterator);
				if (!columnNames.contains(column.getName())) {
					output.appendColumn(column);
				} else {
					ignoreInnerColumns.add(innerColumnIterator);
				}
			}
			innerOffset = outerResultSet.getColumnSize();
		} else {
			// Set up columns for output
			for (int innerColumnIterator = 0; innerColumnIterator < innerResultSet
					.getColumnSize(); innerColumnIterator++) {
				Column column = innerResultSet.getColumn(innerColumnIterator);
				columnNames.add(column.getName());
				output.appendColumn(column);
			}

			for (int outerColumnIterator = 0; outerColumnIterator < outerResultSet
					.getColumnSize(); outerColumnIterator++) {

				Column column = outerResultSet.getColumn(outerColumnIterator);
				if (!columnNames.contains(column.getName())) {
					output.appendColumn(column);
				} else {
					ignoreOuterColumns.add(outerColumnIterator);
				}

			}

			outerOffset = innerResultSet.getColumnSize();
		}

		outerResultSet.beforeFirst();

		// Loop around outer result set until all outer result sets have been
		// hashed and compared
		while (outerResultSet.isLast()) {

			// Build the multi map from the inner for this block
			buildMultiMap();

			Set<HashCode> usedKeys = new HashSet<HashCode>();

			// Loop through the inner loop to check for matches
			innerResultSet.beforeFirst();
			while (innerResultSet.next()) {
				// Create hash of inner columns to match on
				HashCode innerHash = hashResultSetRow(innerColumns,
						innerDataTypes, innerResultSet);

				// Check if inner columns match on anything in the hash
				Set<Row> matchedRows = hashMultiMap.get(innerHash);

				// Loop through all matches
				for (Row row : matchedRows) {

					// Check values to ensure no hash collisions
					if (trueMatch(row, innerResultSet.getCurrentRow())) {
						// Write both output rows
						output.appendRow();
						writeRow(output, row, outerResultSet.getColumnSize(),
								outerOffset, ignoreOuterColumns);
						writeRow(output, innerResultSet.getCurrentRow(),
								innerResultSet.getColumnSize(), innerOffset,
								ignoreInnerColumns);

						usedKeys.add(innerHash);
					}

				}
				if (matchedRows.isEmpty()) {
					if ((joinType == HashJoinImplType.FULLOUTER)
							|| (!isLeftOuterResultSet && (joinType == HashJoinImplType.LEFTOUTER))
							|| (isLeftOuterResultSet && (joinType == HashJoinImplType.RIGHTOUTER))) {
						output.appendRow();
						writeRow(output, innerResultSet.getCurrentRow(),
								innerResultSet.getColumnSize(), innerOffset,
								ignoreInnerColumns);
					}
				}
			}
			if ((joinType == HashJoinImplType.FULLOUTER)
					|| (isLeftOuterResultSet && (joinType == HashJoinImplType.LEFTOUTER))
					|| (!isLeftOuterResultSet && (joinType == HashJoinImplType.RIGHTOUTER))) {
				for (HashCode innerHash : hashMultiMap.keySet()) {
					if (!usedKeys.contains(innerHash)) {
						output.appendRow();
						// Check if inner columns match on anything in the hash
						// Loop through all matches
						for (Row row : hashMultiMap.get(innerHash)) {
							writeRow(output, row,
									outerResultSet.getColumnSize(),
									outerOffset, ignoreOuterColumns);
						}
					}

				}
			}
		}

		return output;
	}

	private void writeRow(ResultSet output, Row row, int colSize, int offset,
			List<Integer> ignoreColumns) throws ResultSetException {
		int minus = 0;
		for (int i = 0; i < colSize; i++) {
			if (!ignoreColumns.contains(i)) {
				output.updateObject(i + offset - minus, row.getColumn(i));
			} else {
				minus++;
			}
		}
	}

	private boolean trueMatch(Row outerRow, Row innerRow) {

		for (int i = 0; i < innerColumns.length; i++) {
			Object outerObj = outerRow.getColumn(outerColumns[i]);
			Object innerObj = innerRow.getColumn(innerColumns[i]);

			if ((outerObj == null) ^ (innerObj == null)) {
				return false;
			} else if ((outerObj != null) && (innerObj != null)) {
				if (!outerObj.equals(innerObj)) {
					return false;
				}
			}
		}

		return true;
	}

	private void buildMultiMap() throws ResultSetException {
		hashMultiMap.clear();
		long counter = 0;
		while (outerResultSet.next() && counter < blockSize) {
			counter++;
			hashMultiMap.put(
					hashResultSetRow(outerColumns, outerDataTypes,
							outerResultSet), outerResultSet.getCurrentRow());
		}
	}

	private HashCode hashResultSetRow(int[] columns,
			PrimitiveDataType[] columnDataTypes, ResultSet resultSet)
			throws ResultSetException {
		Hasher columnHash = hashFunction.newHasher();

		for (int columnI = 0; columnI < columns.length; columnI++) {
			int column = columns[columnI];
			switch (columnDataTypes[columnI].getName()) {
			case "boolean":
				columnHash.putBoolean(resultSet.getBoolean(column));
				break;
			case "byte":
				columnHash.putByte(resultSet.getByte(column));
				break;
			case "double":
				columnHash.putDouble(resultSet.getDouble(column));
				break;
			case "float":
				columnHash.putFloat(resultSet.getFloat(column));
				break;
			case "integer":
				columnHash.putInt(resultSet.getInt(column));
				break;
			case "long":
				columnHash.putLong(resultSet.getLong(column));
				break;
			default:
				columnHash.putString(resultSet.getString(column),
						Charsets.UTF_8);
				break;
			}
		}

		return columnHash.hash();
	}
}
