/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.monitoring.event.action;


import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;

import edu.harvard.hms.dbmi.bd2k.irct.controller.ResultController;
import edu.harvard.hms.dbmi.bd2k.irct.event.action.BeforeQuery;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

public class MonitoringBeforeQuery implements BeforeQuery {
	
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

	public void fire(SecureSession session, Resource resource, Query query) {
		System.out.println("QUERY: " + session.getUser().getName() + " : " + resource.getName() + " : " + query.toString());
		System.out.println(log == null);
	}
	
	
}
