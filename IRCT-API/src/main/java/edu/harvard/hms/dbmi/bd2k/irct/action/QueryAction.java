/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.action;

import java.util.Date;
import java.util.Map;

import javax.naming.NamingException;

import org.apache.log4j.Logger;

import edu.harvard.hms.dbmi.bd2k.irct.event.IRCTEventListener;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.ClauseAbstract;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.WhereClause;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.QueryResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Persistable;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultStatus;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;
import edu.harvard.hms.dbmi.bd2k.irct.util.Utilities;

/**
 * Implements the Action interface to run a query on a specific instance
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class QueryAction implements Action {

	private Query query;
	private Resource resource;
	private ActionStatus status;
	private Result result;

	private IRCTEventListener irctEventListener;
	private Logger logger = Logger.getLogger(this.getClass());
	
	/**
	 * Sets up the action to run a given query on a resource
	 * 
	 * @param resource
	 *            Resource to run the query
	 * @param query
	 *            Run the query
	 */
	public void setup(Resource resource, Query query) {
		logger.debug("setup() Starting...");
		
		if (resource == null) {
			logger.warn("setup() resource is null");
		}
		this.query = query;
		this.resource = resource;
		this.status = ActionStatus.CREATED;
		this.irctEventListener = Utilities.getIRCTEventListener();
		logger.debug("setup() Finished.");
	}

	@Override
	public void updateActionParams(Map<String, Result> updatedParams) {
		logger.debug("updateActionParams() Starting...");
		
		for (String key : updatedParams.keySet()) {
			Long clauseId = Long.valueOf(key.split(".")[0]);
			String parameterId = key.split(".")[1];

			ClauseAbstract clause = this.query.getClauses().get(clauseId);
			if (clause instanceof WhereClause) {
				WhereClause whereClause = (WhereClause) clause;
				whereClause.getStringValues().put(parameterId,
						updatedParams.get(key).getId().toString());
			}
		}
		logger.debug("updateActionParams() Finished.");
	}

	@Override
	public void run(SecureSession session) {
		logger.debug("run() Starting...");
		
		logger.debug("run() calling `beforeQuery`");
		irctEventListener.beforeQuery(session, resource, query);
		logger.debug("run() set status to "+ActionStatus.RUNNING);
		this.status = ActionStatus.RUNNING;
		try {
			logger.debug("run() getting `QueryResourceImplementationInterface` object");
			QueryResourceImplementationInterface queryInterface = (QueryResourceImplementationInterface) resource
					.getImplementingInterface();
			
			if (queryInterface == null) {
				throw new RuntimeException("Unknown implementing interface for resource `"+resource.getName()+"`");
			}

			logger.debug("run() creating `result` field.");
			this.result = ActionUtilities.createResult(queryInterface.getQueryDataType(query));

			if (session != null) {
				logger.debug("run() setting `user` field of the `result`.");
				this.result.setUser(session.getUser());
			}
			
			logger.debug("run() calling runQuery() on the interface.");
			this.result = queryInterface.runQuery(session, query, result);

			// Update the result in the database
			logger.debug("run() calling ActionUtilities.mergeResult(). This would persist the result in the database.");
			ActionUtilities.mergeResult(this.result);
		} catch (Exception e) {
			logger.error("run() Exception: "+e.getMessage());
			this.status = ActionStatus.ERROR;
			if (this.result == null) {
				throw new RuntimeException(e);
			} else {
				this.result.setResultStatus(ResultStatus.ERROR);
				this.result.setMessage(e.getMessage());
			}
		}
		irctEventListener.afterQuery(session, resource, query);
	}

	@Override
	public Result getResults(SecureSession session)
			throws ResourceInterfaceException {
		try {
			this.result = ((QueryResourceImplementationInterface) resource
					.getImplementingInterface()).getResults(session, result);

			while ((this.result.getResultStatus() != ResultStatus.ERROR)
					&& (this.result.getResultStatus() != ResultStatus.COMPLETE)) {
				Thread.sleep(3000);
				this.result = ((QueryResourceImplementationInterface) resource
						.getImplementingInterface())
						.getResults(session, result);
			}

			if (this.result.getResultStatus() == ResultStatus.COMPLETE) {
				if (((Persistable) result.getData()).isPersisted()) {
					((Persistable) result.getData()).merge();
				} else {
					((Persistable) result.getData()).persist();
				}

			}

			result.getData().close();
		} catch (Exception e) {
			this.result.setResultStatus(ResultStatus.ERROR);
			this.result.setMessage(e.getMessage());
		}

		result.setEndTime(new Date());
		// Save the query Action
		try {
			ActionUtilities.mergeResult(result);
			this.status = ActionStatus.COMPLETE;
		} catch (NamingException e) {
			result.setMessage(e.getMessage());
			this.status = ActionStatus.ERROR;
		}

		return this.result;
	}

	/**
	 * Returns the query
	 * 
	 * @return Query
	 */
	public Query getQuery() {
		return this.query;

	}

	/**
	 * Sets the query
	 * 
	 * @param query
	 *            Query
	 */
	public void setQuery(Query query) {
		this.query = query;
	}

	@Override
	public ActionStatus getStatus() {
		return status;
	}
}
