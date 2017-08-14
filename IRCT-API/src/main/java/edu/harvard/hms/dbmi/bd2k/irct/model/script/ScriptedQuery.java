package edu.harvard.hms.dbmi.bd2k.irct.model.script;

import java.util.List;

public class ScriptedQuery {
	
	private class IRCTQuery {
		public IRCTQuery(){
			
		}
		public Column[] getSelect() {
			return select;
		}
		public void setSelect(Column[] select) {
			this.select = select;
		}
		private Column[] select;
		private Predicate[] where;
	}
	private List<IRCTQuery> queries;
	private String script;
	
	public List<IRCTQuery> getQueries() {
		return queries;
	}
	public void setQueries(List<IRCTQuery> queries) {
		this.queries = queries;
	}
	
	public String getScript() {
		return script;
	}
	public void setScript(String script) {
		this.script = script;
	}
	
	public String toString(){
		return script + "\n" + queries;
	}
}
