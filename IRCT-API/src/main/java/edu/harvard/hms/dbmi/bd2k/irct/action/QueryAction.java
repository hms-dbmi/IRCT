/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.action;

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
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;
import edu.harvard.hms.dbmi.bd2k.irct.util.Utilities;
import org.apache.log4j.Logger;

import javax.naming.NamingException;
import java.util.Date;
import java.util.Map;

/**
 * Implements the Action interface to run a query on a specific instance
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
		this.query = query;
		this.resource = resource;
		this.status = ActionStatus.CREATED;
		this.irctEventListener = Utilities.getIRCTEventListener();
	}

	@Override
	public void updateActionParams(Map<String, Result> updatedParams) {
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
	}

	@Override
	public void run(User user) {
		irctEventListener.beforeQuery(user, resource, query);
		this.status = ActionStatus.RUNNING;
		try {
			QueryResourceImplementationInterface queryInterface = (QueryResourceImplementationInterface) resource
					.getImplementingInterface();

			this.result = ActionUtilities.createResult(queryInterface.getQueryDataType(query));
			this.result.setUser(user);
			logger.debug("run() starting query");
			this.result = queryInterface.runQuery(user, query, result);
			logger.debug("run() finished query");

			// Update the result in the database
			logger.debug("run() persisting result to database");
			ActionUtilities.mergeResult(this.result);
			logger.debug("run() persisted result to database");
		} catch (Exception e) {
			logger.error("run() Exception:"+e.getMessage());
			
			if (this.result == null) {
				logger.error("run() `this.result` is null");
			} else {
				this.result.setMessage(e.getMessage());
				this.result.setResultStatus(ResultStatus.ERROR);
			}
			this.status = ActionStatus.ERROR;
		}
		irctEventListener.afterQuery(user, resource, query);
	}

	@Override
	public Result getResults(User user){
		logger.debug("getResults() starting");

		try {
			this.result = ((QueryResourceImplementationInterface) resource
					.getImplementingInterface()).getResults(user, result);

			if (this.result == null)
				throw new ResourceInterfaceException("getResults() after retrieving result, result is null");

			while ((this.result.getResultStatus() != ResultStatus.ERROR)
					&& (this.result.getResultStatus() != ResultStatus.COMPLETE)) {
				Thread.sleep(3000);
				this.result = ((QueryResourceImplementationInterface) resource
						.getImplementingInterface())
						.getResults(user, result);
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
			logger.debug("getResults() merge result to the database.");
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
