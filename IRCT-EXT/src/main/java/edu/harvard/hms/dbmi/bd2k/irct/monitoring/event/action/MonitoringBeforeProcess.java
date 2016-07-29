/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.monitoring.event.action;


import edu.harvard.hms.dbmi.bd2k.irct.event.action.BeforeProcess;
import edu.harvard.hms.dbmi.bd2k.irct.model.process.IRCTProcess;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

public class MonitoringBeforeProcess implements BeforeProcess {
	
	
	public String getName() {
		return null;
	}

	public void init() {
	
	}

	@Override
	public void fire(SecureSession session, IRCTProcess process) {
		System.out.println("PROCESS: " + session.getUser().getName() + " : " + process.toString());
		
	}
	
	
}
