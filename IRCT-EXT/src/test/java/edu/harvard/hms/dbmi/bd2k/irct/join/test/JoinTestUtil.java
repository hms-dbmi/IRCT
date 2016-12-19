package edu.harvard.hms.dbmi.bd2k.irct.join.test;

import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.Column;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.ResultSet;

public class JoinTestUtil {
	
	public static void printResultSet(ResultSet resultSet) throws ResultSetException {
		System.out.print("|");
		for(Column column : resultSet.getColumns()) {
			System.out.print(column.getName() + "\t|");
		}
		
		resultSet.beforeFirst();
		while(resultSet.next()) {
			System.out.print("\n|");
			for(int columnIndex = 0; columnIndex < resultSet.getColumnSize(); columnIndex++) {
				Object obj = resultSet.getObject(columnIndex);
				if(obj != null) {
					System.out.print(obj.toString());	
				}
				System.out.print("\t|");
			}
		}
		System.out.println("\n");
	}
	
	public static boolean isEqual(ResultSet base, ResultSet comparator) throws ResultSetException {
		//Check input
		if((base == null) || (comparator == null)) {
			return false;
		}
		
		//Check Columns
		if(base.getColumnSize() != comparator.getColumnSize()) {
			return false;
		}
		
		for(int columnIndex = 0; columnIndex < base.getColumnSize(); columnIndex++) {
			if(!base.getColumn(columnIndex).equals(comparator.getColumn(columnIndex))) {
				return false; 
			}
		}
		
		//Check Row Size
		if(base.getSize() != comparator.getSize()) {
			return false;
		}
		
		//Check internal data
		base.beforeFirst();
		comparator.beforeFirst();
		while(base.next()) {
			comparator.next();
			
			for(int columnIndex = 0; columnIndex < base.getColumnSize(); columnIndex++) {
				Object baseObj = base.getObject(columnIndex);
				Object comparatorObj = comparator.getObject(columnIndex); 
				if((baseObj == null) != (comparatorObj == null)) {
					return false;
				}
				if(baseObj != null && !baseObj.equals(comparatorObj)) {
					return false;
				}
			}
			
		}
		
		return true;
	}
	
	public static ResultSet createResultSet(final Column[] columns, final Object[] objects) throws ResultSetException, PersistableException {
		MemoryResultSet mrs = new MemoryResultSet();
		
		for(Column column : columns) {
			mrs.appendColumn(column);
		}
		final int rows = objects.length / columns.length;
		mrs.beforeFirst();
		for(int row = 0; row < rows; row++) {
			mrs.appendRow();
			for(int column = 0; column < columns.length; column++) {
				int objLoc = (row * columns.length) + column;
				if(objects[objLoc] != null) {
					mrs.updateObject(column, objects[objLoc]);
				}
			}
		}
		mrs.beforeFirst();
		return mrs;
	}

}
