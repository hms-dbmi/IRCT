/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.i2b2.api;

import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 * Provides an implementation that describes the API for setting up connectors
 * to individuals i2b2 cells.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public abstract class Cell {
	
	/*
	 * Since JAXBContext instances are expensive to create, we maintain a cache of them.
	 */
	private static HashMap<String, JAXBContext> jaxbContexts = new HashMap<String, JAXBContext>();

	private JAXBContext jaxbContext(String packageName) throws JAXBException{
		JAXBContext jaxbContext = jaxbContexts.get(packageName);
		if(jaxbContext == null){
			jaxbContext = JAXBContext.newInstance(packageName);
			jaxbContexts.put(packageName, jaxbContext);
		}
		return jaxbContext;
	};
	
	/**
	 * Each Cell implementation needs to marshall XML for each request sent to I2B2. Since
	 * the Marshaller class is not thread-safe, we must create a new one for each request
	 * and for each package used in that request.
	 * 
	 * @param packageName
	 * @return
	 * @throws JAXBException
	 */
	protected final Marshaller marshaller(String packageName) throws JAXBException{
		Marshaller pdoMarshaller = jaxbContext(packageName).createMarshaller();
		pdoMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		return pdoMarshaller;
	};
	
	/**
	 * Sets up all needed parameters to communicate with the implementing
	 * classes cell
	 * 
	 * @param connectionURL
	 *            URL of the cell
	 * @param domain
	 *            Domain of the user
	 * @param userName
	 *            User Name
	 * @param password
	 *            Password
	 * @param projectId
	 *            Project ID
	 * @param useProxy Use a proxy
	 * @param proxyURL URL of the proxy if used
	 * @throws JAXBException
	 *             An Exception Occurred
	 */
	public abstract void setup(String connectionURL, String domain, String userName,
			String password, String projectId, boolean useProxy, String proxyURL) throws JAXBException;

	/**
	 * Sets up all needed parameters to communicate with the implementing
	 * classes cell
	 * 
	 * @param connectionURL
	 *            URL of the cell
	 * @param domain
	 *            Domain of the user
	 * @param userName
	 *            User Name
	 * @param token
	 *            i2b2 Token
	 * @param timeout
	 *            i2b2 token timeout time
	 * @param projectId
	 *            Project Id
	 * @param useProxy Use a proxy
	 * @param proxyURL URL of the proxy if used
	 * @throws JAXBException
	 *             An Exception Occurred
	 */
	public abstract void setup(String connectionURL, String domain, String userName,
			String token, long timeout, String projectId, boolean useProxy, String proxyURL) throws JAXBException;

	/**
	 * Sets/Updates the connection information.
	 * 
	 * @param connectionURL URL of the cell
	 * @param domain Domain of the user
	 * @param userName User Name
	 * @param password Password
	 * @param projectId Project id
	 * @param useProxy Use a proxy
	 * @param proxyURL URL of the proxy if used
	 */
	protected abstract void setupConnection(String connectionURL, String domain, String userName,
			String password, String projectId, boolean useProxy, String proxyURL);
}
