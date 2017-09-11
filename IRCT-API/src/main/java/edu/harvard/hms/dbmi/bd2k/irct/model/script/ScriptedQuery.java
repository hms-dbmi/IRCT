package edu.harvard.hms.dbmi.bd2k.irct.model.script;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScriptedQuery {
	private Map<String, Integer> resultSets;
	private String script;
	private Map<String, String> scriptOptions;
	
	public Map<String, Integer> getResultSets() {
		return resultSets;
	}
	public void setResultSets(Map<String, Integer> resultSets) {
		this.resultSets = resultSets;
	}
	public String getScript() {
		return script;
	}
	public void setScript(String script) {
		this.script = script;
	}
	public Map<String, String> getScriptOptions() {
		return scriptOptions;
	}
	public void setScriptOptions(Map<String, String> scriptOptions) {
		this.scriptOptions = scriptOptions;
	}
}
