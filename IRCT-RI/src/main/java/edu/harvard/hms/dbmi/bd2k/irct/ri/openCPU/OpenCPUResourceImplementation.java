/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.ri.openCPU;

import java.util.List;
import java.util.Map;

import javax.json.JsonObject;

import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.action.ActionState;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Path;
import edu.harvard.hms.dbmi.bd2k.irct.model.process.IRCTProcess;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ProcessResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ResourceState;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultSet;

/**
 * An implementation of a resource that communicates with an open CPU instance.
 * 
 * NOTE: Still in active development
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class OpenCPUResourceImplementation implements
		ProcessResourceImplementationInterface {
	String baseURL;

	@Override
	public void setup(Map<String, String> parameters) {

	}

	@Override
	public String getType() {
		return "openCPU";
	}

	@Override
	public List<Path> getReturnEntity() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ActionState runProcess(IRCTProcess pep) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResultSet getResults(ActionState actionState) throws ResourceInterfaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResourceState getState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonObject toJson() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonObject toJson(int depth) {
		// TODO Auto-generated method stub
		return null;
	}
}
