package edu.harvard.hms.dbmi.bd2k.irct.model.ontology;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataTypeForJson {
	private String name;
	
	public String getName() {
		return name;
	}
	public DataTypeForJson setName(String name) {
		this.name = name;
		return this;
	}
	
	public String getPattern() {
		return pattern;
	}
	public DataTypeForJson setPattern(String pattern) {
		this.pattern = pattern;
		return this;
	}
	
	public String getDescription() {
		return description;
	}
	public DataTypeForJson setDescription(String description) {
		this.description = description;
		return this;
	}
	
	public String getTypeof() {
		return typeof;
	}
	public DataTypeForJson setTypeof(String typeof) {
		this.typeof = typeof;
		return this;
	}
	
	private String pattern;
	private String description;
	private String typeof;
}
