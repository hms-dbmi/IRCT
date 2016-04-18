/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2transmart;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;

import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Entity;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ResourceState;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.QueryResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultStatus;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.I2B2XMLResourceImplementation;
import edu.harvard.hms.dbmi.i2b2.api.crc.CRCCell;
import edu.harvard.hms.dbmi.i2b2.api.exception.I2B2InterfaceException;

/**
 * An implementation of a resource that communicates with the tranSMART
 * instance. It extends the i2b2 XML resource implementation.
 * 
 */
public class I2B2TranSMARTResourceImplementation extends
		I2B2XMLResourceImplementation {
	private String resourceName;
	private ResourceState resourceState;
	private String transmartURL;
	private String i2b2URL;
	private CRCCell crcCell;

	private String domain;
	private boolean useProxy;
	private String proxyURL;
	private String userName;
	private String password;

	@Override
	public void setup(Map<String, String> parameters)
			throws ResourceInterfaceException {
		String[] strArray = { "resourceName", "resourceURL", "resourceI2b2URL",
				"domain" };
		if (!parameters.keySet().containsAll(Arrays.asList(strArray))) {
			throw new ResourceInterfaceException("Missing parameters");
		}

		this.resourceName = parameters.get("resourceName");
		this.domain = parameters.get("domain");
		this.proxyURL = parameters.get("proxyURL");

		if (this.proxyURL == null) {
			this.useProxy = false;
			this.userName = parameters.get("username");
			this.password = parameters.get("password");
		} else {
			this.useProxy = true;
		}

		this.transmartURL = parameters.get("resourceURL");
		this.i2b2URL = parameters.get("resourceI2b2URL");
		parameters.replace("resourceURL", parameters.get("resourceI2b2URL"));

		super.setup(parameters);

		// Setup Cells
		try {
			crcCell = new CRCCell();
			crcCell.setup();
		} catch (JAXBException e) {
			throw new ResourceInterfaceException(e);
		}

		resourceState = ResourceState.READY;
	}

	@Override
	public List<Entity> getPathRelationship(Entity path,
			OntologyRelationship relationship, SecureSession session) throws ResourceInterfaceException {
		List<Entity> returns = super.getPathRelationship(path, relationship, session);
		
		//Get the counts from the tranSMART server
		
		return returns;
	}

	@Override
	public Result runQuery(SecureSession session, Query qep, Result result)
			throws ResourceInterfaceException {
		result = super.runQuery(session, qep, result);

		if (result.getResultStatus() != ResultStatus.ERROR) {
			String resultInstanceId = result.getResourceActionId();
			String projectId = resultInstanceId.split("\\|")[0];
			String queryId = resultInstanceId.split("\\|")[1];
			String resultId = resultInstanceId.split("\\|")[2];
			try {
				// Wait for it to be either ready or fail
				crcCell = createCRCCell(projectId, session.getUser().getName());
				// Loop through the select clauses to build up the select string
				result = super.getResults(session, result);
				while ((result.getResultStatus() != ResultStatus.ERROR)
						&& (result.getResultStatus() != ResultStatus.COMPLETE)) {
					Thread.sleep(5000);
					result = super.getResults(session, result);
				}
				// Call the tranSMART API to get the dataset

				// Convert the dataset to Tabular format

				// Set the status to complete
				result.setResultStatus(ResultStatus.COMPLETE);
			} catch (JAXBException | InterruptedException e) {
				result.setResultStatus(ResultStatus.ERROR);
				System.out.println(e.getMessage());
			}
		}
		return result;
	}

	@Override
	public Result getResults(SecureSession session, Result result)
			throws ResourceInterfaceException {
		// This method only exists so the results for i2b2XML do not get called
		return result;
	}

	@Override
	public ResourceState getState() {
		return resourceState;
	}

	@Override
	public ResultDataType getQueryDataType() {
		return ResultDataType.TABULAR;
	}

	@Override
	public String getType() {
		return "i2b2/tranSMART";
	}

	private HttpClient createi2b2Client(SecureSession session) {
		HttpClientBuilder returns = HttpClientBuilder.create();
		List<Header> defaultHeaders = new ArrayList<Header>();
		if (session != null) {
			defaultHeaders.add(new BasicHeader("Authorization", session
					.getToken().toString()));
		}
		defaultHeaders.add(new BasicHeader("Content-Type",
				"application/x-www-form-urlencoded"));
		returns.setDefaultHeaders(defaultHeaders);

		return returns.build();
	}

	private HttpClient createClient(SecureSession session) {
		HttpClientBuilder returns = HttpClientBuilder.create();
		List<Header> defaultHeaders = new ArrayList<Header>();
		if (session != null) {
			defaultHeaders.add(new BasicHeader("Authorization", session
					.getToken().toString()));
		}

		returns.setDefaultHeaders(defaultHeaders);

		return returns.build();
	}

	private CRCCell createCRCCell(String projectId, String userName)
			throws JAXBException {
		if (this.useProxy) {
			crcCell.setupConnection(this.i2b2URL, this.domain, userName, "",
					projectId, this.useProxy, this.proxyURL
							+ "/QueryToolService");
		} else {
			crcCell.setupConnection(this.i2b2URL, this.domain, this.userName,
					this.password, projectId, false, null);
		}
		return crcCell;
	}
}
