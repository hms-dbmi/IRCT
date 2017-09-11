/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.executable;


import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultStatus;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;
import edu.harvard.hms.dbmi.bd2k.irct.util.Utilities;

import org.apache.log4j.Logger;

import edu.harvard.hms.dbmi.bd2k.irct.event.IRCTEventListener;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;

/**
 * An execution plan is series of executable processes that are run by the IRCT.
 * An execution plan is run each time a query, join, or process request for
 * execution is made.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class ExecutionPlan {
	private ExecutableStatus status;
	private Executable executable;
	private Result results;
	private SecureSession session;
	
	private Logger logger = Logger.getLogger(this.getClass());

	private IRCTEventListener irctEventListener;
	
	/**
	 * Setup the execution plan with the base executable
	 * 
	 * @param executable
	 *            Base executable
	 * @param session Secure session to run in            
	 */
	public void setup(Executable executable, SecureSession session) {
		this.executable = executable;
		this.session = session;
		this.status = ExecutableStatus.CREATED;
		this.results = null;
		
		this.irctEventListener = Utilities.getIRCTEventListener();
	}

	/**
	 * Run the base execution plan
	 */
	public void run() {
		logger.debug("run() `executable` "+executable.toString());
		
		irctEventListener.beforeExecutionPlan(session, executable);
		logger.debug("run() finished beforeExecutionPlan()");
		
		this.status = ExecutableStatus.RUNNING;
		try {
			logger.debug("run() `executable` setup");
			this.executable.setup(session);
			logger.debug("run() `executable` run()");
			this.executable.run();
			logger.debug("run() `executable` getResults()");
			this.results = this.executable.getResults();
			logger.debug("run() done. Setting status to "+ExecutableStatus.COMPLETED);
			this.status = ExecutableStatus.COMPLETED;
		} catch (ResourceInterfaceException e) {
			logger.error("run() ResourceInterfaceException:"+e.getMessage());
			this.status = ExecutableStatus.TERMINATED;
			this.results.setResultStatus(ResultStatus.ERROR);
			this.results.setMessage("ResourceInterfaceException:"+e.getMessage());
		} catch (Exception e) {
			logger.error("run() Exception:"+e.getMessage());
			
			this.status = ExecutableStatus.TERMINATED;
			if (this.results == null) {
				throw new RuntimeException("OtherException executing query: "+e.getMessage());
			} else {
				this.results.setResultStatus(ResultStatus.ERROR);
				this.results.setMessage("OtherException:"+e.getMessage());
			}
		}		
		irctEventListener.afterExecutionPlan(session, executable);
	}

	/**
	 * Return the results of the execution plan if they are available
	 * 
	 * @return Results
	 */
	public Result getResults() {
		return this.results;
	}

	/**
	 * Returns the current execution state
	 * 
	 * @return Execution state
	 */
	public ExecutableStatus getState() {
		return this.status;
	}

}
