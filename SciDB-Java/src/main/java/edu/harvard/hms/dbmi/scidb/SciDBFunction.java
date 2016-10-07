package edu.harvard.hms.dbmi.scidb;

public class SciDBFunction implements SciDBCommand {
	private String command;
	
	public SciDBFunction(String command) {
		this.command = command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	@Override
	public String toAFLQueryString() {
		return this.command;
	}

}
