package edu.harvard.hms.dbmi.bd2k.irct.model.script;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class IRCTQuery {

	private Column[] select;
	private Predicate[] where;
	
	public IRCTQuery(){

	}
	public Column[] getSelect() {
		return select;
	}
	public void setSelect(Column[] select) {
		this.select = select;
	}
	public Predicate[] getWhere() {
		return where;
	}
	public void setWhere(Predicate[] where) {
		this.where = where;
	}
}
