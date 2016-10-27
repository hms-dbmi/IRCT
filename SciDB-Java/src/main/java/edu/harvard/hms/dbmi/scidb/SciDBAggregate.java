package edu.harvard.hms.dbmi.scidb;

public class SciDBAggregate implements SciDBCommand {
	private String command;
	
	public SciDBAggregate(String command) {
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
