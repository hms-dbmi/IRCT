/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.executable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import edu.harvard.hms.dbmi.bd2k.irct.action.Action;
import edu.harvard.hms.dbmi.bd2k.irct.event.IRCTEventListener;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultStatus;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;
import edu.harvard.hms.dbmi.bd2k.irct.util.Utilities;

/**
 * A child node in an execution tree that can be executed. It can have children of its own.
 */
public class ExecutableChildNode implements Executable {

	private User user;
	private boolean blocking;
	private Action action;
	private Map<String, Executable> children;
	private Map<String, Result> childrenResults;
	private ExecutableStatus state;
	
	private IRCTEventListener irctEventListener;

	@Override
	public void setup(User user) {
		this.user = user;
		this.children = new HashMap<String, Executable>();
		this.childrenResults = new HashMap<String, Result>();
		this.state = ExecutableStatus.CREATED;
		this.irctEventListener = Utilities.getIRCTEventListener();
	}

	@Override
	public void run() throws ResourceInterfaceException {
		irctEventListener.beforeAction(user, action);
		
		if (isBlocking() && !children.isEmpty()) {
			runSequentially();
		} else if (!children.isEmpty()){
			runConcurrently();
		}
		if(!childrenResults.isEmpty()) {
			action.updateActionParams(childrenResults);
		}
		
		this.state = ExecutableStatus.RUNNING;
		this.action.run(this.user);
		this.state = ExecutableStatus.COMPLETED;
		
		irctEventListener.afterAction(user, action);
	}

	private void runSequentially() throws ResourceInterfaceException {
		for (String key : this.children.keySet()) {
			Executable executable = this.children.get(key);
			executable.setup(this.user);
			executable.run();
			childrenResults.put(key, executable.getResults());
		}
	}

	private void runConcurrently() {
		List<ExecutorThread> myThreads = new ArrayList<ExecutorThread>();
		for (String key : this.children.keySet()) {
			myThreads.add(new ExecutorThread(key, this.children.get(key),
					this.user));
		}
		ExecutorService taskExecutor = Executors.newFixedThreadPool(10);
		for (ExecutorThread executorThread : myThreads) {
			taskExecutor.execute(executorThread);
		}
		taskExecutor.shutdown();
		try {
			taskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		for (ExecutorThread executorThread : myThreads) {
			childrenResults.put(executorThread.getId(),
					executorThread.getResult());
		}
	}

	@Override
	public ExecutableStatus getStatus() {
		return this.state;
	}

	@Override
	public Result getResults() throws ResourceInterfaceException {
		return this.action.getResults(this.user);
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
	 * @param action Action
	 */
	public void setAction(Action action) {
		this.action = action;
	}

	/**
	 * Returns if the actions should be run synchronously
	 * 
	 * TRUE - Synchronously
	 * FALSE - Asynchronously
	 * @return Blocking
	 */
	public boolean isBlocking() {
		return blocking;
	}

	/**
	 * Sets if the actions should be run synchronously
	 * 
	 * TRUE - Synchronously
	 * FALSE - Asynchronously
	 * 
	 * @param blocking Blocking
	 */
	public void setBlocking(boolean blocking) {
		this.blocking = blocking;
	}
}

/**
 * A subclass that implements a runnable execution thread for actions
 */
class ExecutorThread implements Runnable {

	private String id;
	private User user;
	private Executable executable;
	private Result result;

	/**
	 * Create the executor thread with the executable component
	 * 
	 * @param id Id of the executable
	 * @param executable Executable
	 * @param session Session to run it in
	 */
	public ExecutorThread(String id, Executable executable,
			User user) {
		this.user = user;
		this.executable = executable;
	}

	@Override
	public void run() {
		executable.setup(this.user);
		try {
			executable.run();
			this.result = executable.getResults();
		} catch (ResourceInterfaceException e) {
			result.setResultStatus(ResultStatus.ERROR);
		}
	}

	/**
	 * Returns the results
	 * 
	 * @return Results
	 */
	public Result getResult() {
		return this.result;
	}

	/**
	 * Sets the results
	 * 
	 * @return Results
	 */
	public String getId() {
		return this.id;
	}

}