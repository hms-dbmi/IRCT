/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.scidb.exception;

public class SciDBOperationException extends Exception {
	private static final long serialVersionUID = -4580645786675954993L;

	public SciDBOperationException(String message) {
		super(message);
	}

}
