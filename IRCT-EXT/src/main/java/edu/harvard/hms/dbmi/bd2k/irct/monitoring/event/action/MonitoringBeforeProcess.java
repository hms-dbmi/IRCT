/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.monitoring.event.action;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;

import edu.harvard.hms.dbmi.bd2k.irct.event.action.BeforeProcess;
import edu.harvard.hms.dbmi.bd2k.irct.model.process.IRCTProcess;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

public class MonitoringBeforeProcess implements BeforeProcess {
	
	private Log log;
	
	public String getName() {
		return null;
	}

	public void init() {
		InitialContext ic;
		try {
			ic = new InitialContext();
			this.log = (Log) ic.lookup("java:module/Log");
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void fire(SecureSession session, IRCTProcess process) {
		log.info("PROCESS: " + session.getUser().getName() + " : " + process.toString());
		
	}
	
	
}
