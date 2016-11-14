/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
