package edu.harvard.hms.dbmi.scidb.exception;

public class NotConnectedException extends Exception {

	private static final long serialVersionUID = -6574534436204125841L;

	public NotConnectedException() {
		super("Not connected to SciDB instance");
	}
	
	public NotConnectedException(String message, Throwable cause) {
		super(message, cause);
	}
}
