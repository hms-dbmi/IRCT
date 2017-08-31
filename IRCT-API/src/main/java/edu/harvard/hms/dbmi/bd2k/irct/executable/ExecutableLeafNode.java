/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.executable;

import org.apache.log4j.Logger;

import edu.harvard.hms.dbmi.bd2k.irct.action.Action;
import edu.harvard.hms.dbmi.bd2k.irct.event.IRCTEventListener;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;
import edu.harvard.hms.dbmi.bd2k.irct.util.Utilities;

/**
 * A leaf node in an execution tree that can be executed. It does not have any
 * children
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class ExecutableLeafNode implements Executable {
	private Logger logger = Logger.getLogger(this.getClass());

	private SecureSession session;
	private Action action;
	private ExecutableStatus state;
	
	private IRCTEventListener irctEventListener;

	@Override
	public void setup(SecureSession secureSession) {
		logger.debug("setup() starting");
		this.session = secureSession;
		this.state = ExecutableStatus.CREATED;
		logger.debug("setup() got `IRCTEventListener`");
		this.irctEventListener = Utilities.getIRCTEventListener();
		logger.debug("setup() Finished");
	}

	@Override
	public void run() throws ResourceInterfaceException {
		logger.debug("run() Starting...");
		
		logger.debug("run() call beforeAction() `action` "+action.toString()+" "+this.getAction().getClass().toString());
		irctEventListener.beforeAction(session, action);
		
		this.state = ExecutableStatus.RUNNING;
		logger.debug("run() call run on `action` "+this.getAction().toString());
		this.action.run(this.session);
		logger.debug("run() returned from run()");
		
		logger.debug("run() set status to "+ExecutableStatus.COMPLETED);
		this.state = ExecutableStatus.COMPLETED;
		
		logger.debug("run() call afterAction()");
		irctEventListener.afterAction(session, action);
		logger.debug("run() Finished.");
	}

	@Override
	public ExecutableStatus getStatus() {
		return this.state;
	}

	@Override
	public Result getResults() throws ResourceInterfaceException {
		return this.action.getResults(this.session);
	}

	/**
	 * Returns the action that is to be executed
	 * 
	 * @return Action
	 */
	public Action getAction() {
		return action;
	}

	/**
	 * Sets the action that is to be executed
	 * 
	 * @param action
	 *            Action
	 */
	public void setAction(Action action) {
		this.action = action;
	}

}
