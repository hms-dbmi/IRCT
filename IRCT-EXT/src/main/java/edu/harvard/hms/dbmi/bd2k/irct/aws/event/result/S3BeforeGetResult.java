/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.aws.event.result;

import edu.harvard.hms.dbmi.bd2k.irct.event.result.BeforeGetResult;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;

public class S3BeforeGetResult implements BeforeGetResult {

	@Override
	public String getName() {
		return null;
	}

	@Override
	public void init() {

	}

	@Override
	public void fire(User user, Long resultId) {
		// TODO Auto-generated method stub
		
		// Check to see if the result is available locally
		// Copy the result from S3

	}

}
