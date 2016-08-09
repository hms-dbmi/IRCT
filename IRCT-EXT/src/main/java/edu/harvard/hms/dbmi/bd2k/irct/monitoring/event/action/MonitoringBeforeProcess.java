/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.monitoring.event.action;


import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.hms.dbmi.bd2k.irct.event.action.BeforeProcess;
import edu.harvard.hms.dbmi.bd2k.irct.model.process.IRCTProcess;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

public class MonitoringBeforeProcess implements BeforeProcess {
	
	private Log log;
	
	public void init(Map<String, String> parameters) {
		log = LogFactory.getLog("Action Monitoring");
	}

	@Override
	public void fire(SecureSession session, IRCTProcess process) {
		log.info("PROCESS: " + session.getUser().getName() + " : " + process.toString());
		
	}
	
	
}
