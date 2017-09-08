package edu.harvard.hms.dbmi.bd2k.irct.model.script;

import java.util.ArrayList;
import java.util.List;

public class AFLQuery {
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((where == null) ? 0 : where.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AFLQuery other = (AFLQuery) obj;
		if (where == null) {
			if (other.where != null)
				return false;
		} else if (!where.equals(other.where))
			return false;
		return true;
	}

	public AFLQuery(){
		
	}
	

	private List<Where> where = new ArrayList<Where>();

	public List<Where> getWhere() {
		return where;
	}

	public AFLQuery setWhere(List<Where> where) {
		this.where = where;
		return this;
	}

}