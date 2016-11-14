/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
