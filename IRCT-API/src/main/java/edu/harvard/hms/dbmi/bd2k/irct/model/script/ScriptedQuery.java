package edu.harvard.hms.dbmi.bd2k.irct.model.script;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScriptedQuery {
	
	private List<IRCTQuery> queries;
	private List<Integer> resultIds;
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
	public List<Integer> getResultIds() {
		return resultIds;
	}
	public void setResultIds(List<Integer> resultIds) {
		this.resultIds = resultIds;
	}
}
