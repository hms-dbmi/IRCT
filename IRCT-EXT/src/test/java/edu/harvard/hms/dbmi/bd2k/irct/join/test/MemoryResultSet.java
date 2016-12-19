package edu.harvard.hms.dbmi.bd2k.irct.join.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.ResultSetImpl;

public class MemoryResultSet extends ResultSetImpl {
	private List<Map<Integer, Object>> results;

	@Override
	public boolean next() throws ResultSetException {
		if (getRowPosition() >= results.size() - 1) {
			return false;
		}

		this.setRowPosition(this.getRowPosition() + 1);

		return true;
	}

	@Override
	public void appendRow() throws ResultSetException, PersistableException {
		if (results == null) {
			results = new ArrayList<Map<Integer, Object>>();
		}

		results.add(new HashMap<Integer, Object>());
		setRowPosition(getRowPosition() + 1);
	}

	@Override
	public void updateObject(int columnIndex, Object obj)
			throws ResultSetException {
		results.get((int) this.getRowPosition()).put(columnIndex, obj);
	}

	@Override
	public Object getObject(int columnIndex) throws ResultSetException {
		return results.get((int) this.getRowPosition()).get(columnIndex);
	}

}
