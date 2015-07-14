/*
 *  This file is part of Inter-Resource Communication Tool (IRCT).
 *
 *  IRCT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  IRCT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with IRCT.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.message.pm;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.message.pm.xml.ApplicationType;
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.message.pm.xml.FacilityType;
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.message.pm.xml.BodyType;
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.message.pm.xml.GetUserConfigurationType;
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.message.pm.xml.MessageHeaderType;
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.message.pm.xml.ObjectFactory;
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.message.pm.xml.RequestHeaderType;
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.message.pm.xml.RequestMessageType;
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.message.pm.xml.ResponseMessageType;
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.message.pm.xml.SecurityType;

/**
 * The Project Management Cell communication class. Makes requests to the i2b2
 * Project Management Cell via XML and returns a corresponding representation of
 * an object
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class PMCell {
	private static JAXBContext pmJC;
	private static Marshaller pmMarshaller;
	private static ObjectFactory pmOF;

	private String connectionURL;
	private Map<String, String> parameters;

	/**
	 * Sets up all the needed parameters to communicate with the Ontology
	 * Management Cell
	 * 
	 * @param parameters
	 *            Setup parameters
	 * @throws JAXBException
	 *             An Exception Occurred
	 */
	public void setup(Map<String, String> parameters) throws JAXBException {
		// Setup Parameters
		connectionURL = parameters.get("PMConnectionURL");
		// Setup System
		pmOF = new ObjectFactory();
		pmJC = JAXBContext
				.newInstance("edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.message.pm.xml");
		pmMarshaller = pmJC.createMarshaller();
		pmMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		this.parameters = parameters;
	}

	/**
	 * Returns a list of cells and or roles associated with that client
	 * 
	 * @param client
	 *            HTTP Client
	 * @return The response message returned from the server
	 * @throws JAXBException
	 * @throws ClientProtocolException
	 *             A Client Protocol Exception occurred
	 * @throws IOException
	 *             An IO Exception occurred
	 */
	public ResponseMessageType getUserConfiguration(HttpClient client)
			throws JAXBException, ClientProtocolException, IOException {

		// Create Post
		HttpPost post = new HttpPost(connectionURL);
		// Set Header
		post.setHeader("Content-Type", "text/xml");

		// Create the XML Object
		RequestMessageType rmt = createMinimumBaseMessage();

		GetUserConfigurationType gut = pmOF.createGetUserConfigurationType();
		gut.getProject().add("undefined");
		rmt.getMessageBody().getAny().add(pmOF.createGetUserConfiguration(gut));

		// Mashall the XML to String and attach it to the post request
		StringWriter sw = new StringWriter();
		pmMarshaller.marshal(pmOF.createRequest(rmt), sw);
		post.setEntity(new StringEntity(sw.toString()));

		// Execute the post and get the response
		HttpResponse response = client.execute(post);

		// Unmarshall the String to XML and return it
		return JAXB.unmarshal(response.getEntity().getContent(),
				ResponseMessageType.class);

	}

	/**
	 * Creates the minimum message needed to send a request to the i2b2 server
	 * 
	 * @return Request Message Base
	 */
	private RequestMessageType createMinimumBaseMessage() {
		RequestMessageType rmt = pmOF.createRequestMessageType();

		// Create Message Header Type
		MessageHeaderType mht = pmOF.createMessageHeaderType();

		// Set Sending Application
		ApplicationType sat = pmOF.createApplicationType();
		sat.setApplicationName("IRCT");
		sat.setApplicationVersion("1.0");

		mht.setSendingApplication(sat);

		// Set Sending Facility
		FacilityType ft = pmOF.createFacilityType();
		ft.setFacilityName("IRCT");

		mht.setSendingFacility(ft);

		// Create Security Type
		SecurityType st = pmOF.createSecurityType();
		st.setDomain(parameters.get("domain"));
		st.setUsername(parameters.get("username"));
		st.setPassword(parameters.get("password"));
		mht.setSecurity(st);

		rmt.setMessageHeader(mht);

		// Create Request Header Type
		RequestHeaderType rht = pmOF.createRequestHeaderType();
		rht.setResultWaittimeMs(180000);
		rmt.setRequestHeader(rht);

		// Create Body Type
		BodyType bt = pmOF.createBodyType();
		rmt.setMessageBody(bt);

		return rmt;
	}

}
