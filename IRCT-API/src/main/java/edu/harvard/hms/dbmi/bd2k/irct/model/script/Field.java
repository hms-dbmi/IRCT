package edu.harvard.hms.dbmi.bd2k.irct.model.script;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Field {
	private String pui;
	private String dataType;
	public String getPui() {
		return pui;
	}
	public void setPui(String pui) {
		this.pui = pui;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
}
