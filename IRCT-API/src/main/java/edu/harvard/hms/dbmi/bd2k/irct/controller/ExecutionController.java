/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.controller;

import java.util.Date;
import java.util.concurrent.Callable;

import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.apache.log4j.Logger;

import edu.harvard.hms.dbmi.bd2k.irct.action.JoinAction;
import edu.harvard.hms.dbmi.bd2k.irct.action.ProcessAction;
import edu.harvard.hms.dbmi.bd2k.irct.action.QueryAction;
import edu.harvard.hms.dbmi.bd2k.irct.executable.ExecutableLeafNode;
import edu.harvard.hms.dbmi.bd2k.irct.executable.ExecutionPlan;
import edu.harvard.hms.dbmi.bd2k.irct.model.join.Join;
import edu.harvard.hms.dbmi.bd2k.irct.model.process.IRCTProcess;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Persistable;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultStatus;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

/**
 * The execution controller is a stateless controller that manages the
 * executions of different processes, queries, and joins by creating an
 * execution plan and running it.
 *
 *
 * @author Jeremy R. Easton-Marks
 *
 */
@Stateless
public class ExecutionController {

	Logger logger = Logger.getLogger(this.getClass());

	@PersistenceContext(unitName = "primary")
	EntityManager entityManager;

	@Resource(name = "DefaultManagedExecutorService")
	private ManagedExecutorService mes;

	@Inject
	private ResourceController rc;

	/**
	 * Runs the process
	 *
	 * @param process
	 *            Process to run
	 * @param secureSession Session to run it in
	 * @return result id
	 * @throws PersistableException
	 *             Persistable exception occurred
	 */
	public Long runProcess(IRCTProcess process, SecureSession secureSession)
			throws PersistableException {
		Result newResult = new Result();
		newResult.setJobType("EXECUTION");
		if(secureSession != null) {
			newResult.setUser(secureSession.getUser());
		}

		newResult.setResultStatus(ResultStatus.RUNNING);
		entityManager.persist(newResult);

		ProcessAction pa = new ProcessAction();
		pa.setup(process.getResources().get(0), process);

		ExecutableLeafNode eln = new ExecutableLeafNode();
		eln.setAction(pa);

		ExecutionPlan exp = new ExecutionPlan();
		exp.setup(eln, secureSession);

		runExecutionPlan(exp, newResult);

		return newResult.getId();
	}

	/**
	 * Run a query by creating an execution plan
	 *
	 * @param query
	 *            Query
	 * @param secureSession Session to run it in
	 * @return Result Id
	 * @throws PersistableException
	 *             An error occurred
	 */
	public Long runQuery(Query query, SecureSession secureSession)
			throws PersistableException {
		logger.debug("runQuery() Starting...");

		Result newResult = new Result();
		newResult.setJobType("EXECUTION");
		logger.debug("runQuery() Set `newResult` as type:EXECUTION");

		// Add the current user to the query.
		newResult.setUser(secureSession.getUser());
		logger.debug("runQuery() Set `newResult` as user:"+newResult.getUser().getUserId());
		
		newResult.setResultStatus(ResultStatus.RUNNING);
		logger.debug("runQuery() Set `newResult` as type:EXECUTION");

		entityManager.persist(newResult);
		logger.debug("runQuery() persited `newResult`");

		QueryAction qa = new QueryAction();
		
		edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource resource = (edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource) query.getResources().toArray()[0];
		logger.debug("runQuery() `QueryAction` and `Resource` are set.");
		
		if(!resource.isSetup()) {
			logger.debug("runQuery() Resource `"+resource.getName()+"` is not set up, yet.");
			resource = rc.getResource(resource.getName());
		} else {
			logger.debug("runQuery() Resource is already set up. "+resource.getName());
		}
		logger.debug("runQuery() Resource `"+resource.getName()+"` is now should be all set.");
		
		logger.debug("runQuery() Setting up queryaction with resource and query.");
		qa.setup(resource, query);
		logger.debug("runQuery() `QueryAction` setup is now complete.");

		ExecutableLeafNode eln = new ExecutableLeafNode();
		eln.setAction(qa);
		logger.debug("runQuery() Added `QueryAction` to `ExecutableLeafNode`.");

		ExecutionPlan exp = new ExecutionPlan();
		exp.setup(eln, secureSession);
		logger.debug("runQuery() `ExecutionPlan` is all set up. Try runExecutionPlan() now");
		
		runExecutionPlan(exp, newResult);
		logger.debug("runQuery() returning `newResult.id` as "+newResult.getId());
		return newResult.getId();
	}

	/**
	 * Run a join by creating an execution plan
	 *
	 * @param join
	 *            Join to run
	 * @param secureSession Session to run it in
	 * @return Result Id
	 * @throws PersistableException
	 *             An error occurred
	 */
	public Long runJoin(Join join, SecureSession secureSession)
			throws PersistableException {
		Result newResult = new Result();
		newResult.setJobType("EXECUTION");
		if(secureSession != null) {
			newResult.setUser(secureSession.getUser());
		}

		newResult.setResultStatus(ResultStatus.RUNNING);
		entityManager.persist(newResult);

		JoinAction ja = new JoinAction();
		ja.setup(join);

		ExecutableLeafNode eln = new ExecutableLeafNode();
		eln.setAction(ja);

		ExecutionPlan exp = new ExecutionPlan();
		exp.setup(eln, secureSession);
		runExecutionPlan(exp, newResult);

		return newResult.getId();
	}

	/**
	 * Runs an execution plan
	 *
	 * @param executionPlan
	 *            Execution Plan
	 * @param result
	 *            Result
	 * @throws PersistableException
	 *             A persistable exception occurred
	 */
	@Asynchronous
	public void runExecutionPlan(final ExecutionPlan executionPlan,
			final Result result) throws PersistableException {
		
		logger.debug("runExectionPlan() Starting...");
		Callable<Result> runPlan = new Callable<Result>() {
			@Override
			public Result call() {
				logger.debug("Callable.call() starting in inner class");
				
				try {
					result.setStartTime(new Date());
					logger.debug("Callable.call() Calling run() on `executionPlan` "+executionPlan.getClass().getName());
					executionPlan.run();

					Result finalResult = executionPlan.getResults();

					if ((finalResult.getResultStatus() == ResultStatus.COMPLETE) && (finalResult.getData() instanceof Persistable)) {
						result.setDataType(finalResult.getDataType());
						result.setData(finalResult.getData());
						result.setResultSetLocation(finalResult.getResultSetLocation());
						result.setMessage(finalResult.getMessage());

						if(((Persistable) result.getData()).isPersisted()) {
							((Persistable) result.getData()).merge();
						} else {
							((Persistable) result.getData()).persist();
						}
						result.setResultStatus(ResultStatus.AVAILABLE);
					} else {
						result.setResultStatus(ResultStatus.ERROR);
						result.setMessage(finalResult.getMessage());
					}

					result.setEndTime(new Date());
					
					UserTransaction userTransaction = lookup();
					
					userTransaction.begin();
					entityManager.merge(result);
					userTransaction.commit();
					
				} catch (PersistableException e) {
					logger.error("Callable.call() PersistableException:"+e.getMessage());
					result.setResultStatus(ResultStatus.ERROR);
					result.setMessage("Callable.call() "+e.getMessage());
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("Callable.call() Exception:"+e.getMessage());
					result.setResultStatus(ResultStatus.ERROR);
				} finally {
					logger.debug("Callable.call() done with the method");
				}
				logger.debug("Callable.call() Finished. Returning `result` with status "+result.getResultStatus());
				return result;
			}
		};
		logger.debug("runExecutionPlan() created `Callable`. calling submit() of ManagedExecutionService");
		try {
			mes.submit(runPlan);
		} catch (Exception e) {
			logger.error("runExecutionPlan() >>>Exception: "+e.getMessage());
		} finally {
			logger.debug("runExecutionPlan() Finished.");
		}
	}

	private UserTransaction lookup() throws NamingException {
		InitialContext ic = new InitialContext();
		return (UserTransaction) ic.lookup("java:comp/UserTransaction");
	}
}
