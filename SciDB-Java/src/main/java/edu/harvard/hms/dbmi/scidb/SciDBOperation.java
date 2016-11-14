/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.scidb;

public class SciDBOperation implements SciDBCommand {
	private String commandString;
	private SciDBCommand command;
	private String action;
	private String prefix;
	private String postFix;
	
	public SciDBOperation() {
		
	}
	
	public SciDBOperation(String action, SciDBCommand command) {
		this.action = action;
		this.command = command;
	}
	
	public SciDBOperation(String action) {
		this.action = action;
	}
	
	public SciDBOperation(String action, String prefix, String postFix, SciDBCommand command) {
		this.action = action;
		this.prefix = prefix;
		this.postFix = postFix;
		this.command = command;
	}
	

	public SciDBOperation(String action, String commandString) {
		this.action = action;
		this.commandString = commandString;
	}

	@Override
	public String toAFLQueryString() {
		String returns = "";
		if(this.action != null) {
			returns += this.action + "(";
		}
		if(this.prefix != null) {
			returns += this.prefix + " ";
		}
		if(this.command != null) {
			returns += this.command.toAFLQueryString();
		} else if(this.commandString != null) {
			returns += this.commandString;
		}
		
		if(this.postFix != null) {
			returns += "," + this.postFix;
		}
		if(this.action != null) {
			returns += ")";
		}
		return returns;
	}

	/**
	 * @return the commandString
	 */
	public String getCommandString() {
		return commandString;
	}

	/**
	 * @param commandString the commandString to set
	 */
	public void setCommandString(String commandString) {
		this.commandString = commandString;
	}

	/**
	 * @return the command
	 */
	public SciDBCommand getCommand() {
		return command;
	}

	/**
	 * @param command the command to set
	 */
	public void setCommand(SciDBCommand command) {
		this.command = command;
	}

	/**
	 * @return the action
	 */
	public String getAction() {
		return action;
	}

	/**
	 * @param action the action to set
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * @return the prefix
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * @param prefix the prefix to set
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	/**
	 * @return the postFix
	 */
	public String getPostFix() {
		return postFix;
	}

	/**
	 * @param postFix the postFix to set
	 */
	public void setPostFix(String postFix) {
		this.postFix = postFix;
	}
	
	

}
