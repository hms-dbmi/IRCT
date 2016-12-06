/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.monitoring.event.action;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.harvard.hms.dbmi.bd2k.irct.event.action.BeforeQuery;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

/**
 * Before a query write the Query information to the Action Monitoring log
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class MonitoringBeforeQuery implements BeforeQuery {
	
	private Log log;
	

	public void init(Map<String, String> parameters) {
		log = LogFactory.getLog("Action Monitoring");
	}

	public void fire(SecureSession session, Resource resource, Query query) {
		log.info(("QUERY: " + session.getUser().getName() + " : " + resource.getName() + " : " + query.toString()));
	}
	
	
}
