/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.ri.openCPU;

import java.util.Arrays;
import java.util.Map;

import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.process.IRCTProcess;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ResourceState;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.ProcessResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

/**
 * An implementation of a resource that communicates with an open CPU instance.
 * 
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class OpenCPUResourceImplementation implements
		ProcessResourceImplementationInterface {
	private String resourceName;
	private String resourceURL;
	private ResourceState resourceState;

	@Override
	public void setup(Map<String, String> parameters)
			throws ResourceInterfaceException {
		String[] strArray = { "resourceName", "resourceURL" };
		if (!parameters.keySet().containsAll(Arrays.asList(strArray))) {
			throw new ResourceInterfaceException("Missing parameters");
		}
		
		this.resourceName = parameters.get("resourceName");
		this.resourceURL = parameters.get("resourceURL");
		
		resourceState = ResourceState.READY;		
	}

	@Override
	public Result runProcess(SecureSession session, IRCTProcess pep,
			Result result) throws ResourceInterfaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result getResults(SecureSession session, Result result)
			throws ResourceInterfaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getType() {
		 return "elasticSearch";
	}

	@Override
	public ResourceState getState() {
		return resourceState;
	}

	@Override
	public ResultDataType getProcessDataType(IRCTProcess process) {
		return ResultDataType.TABULAR;
	}
}
