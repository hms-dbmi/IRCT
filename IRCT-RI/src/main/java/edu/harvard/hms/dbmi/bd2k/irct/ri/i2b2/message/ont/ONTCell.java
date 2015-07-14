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
package edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.message.ont;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import edu.harvard.hms.dbmi.bd2k.irct.ri.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.message.ont.xml.BodyType;
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.message.ont.xml.ConceptsType;
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.message.ont.xml.GetCategoriesType;
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.message.ont.xml.GetChildrenType;
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.message.ont.xml.MessageHeaderType;
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.message.ont.xml.ObjectFactory;
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.message.ont.xml.RequestHeaderType;
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.message.ont.xml.RequestMessageType;
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.message.ont.xml.ResponseMessageType;
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.message.ont.xml.SecurityType;
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.message.ont.xml.ApplicationType;
import edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.message.ont.xml.FacilityType;

/**
 * The Ontology Management Cell communication class. Makes requests to the i2b2
 * Ontology Management Cell via XML and returns a corresponding representation
 * of an object/
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class ONTCell {
	private static JAXBContext ontJC;
	private static Marshaller ontMarshaller;
	private static ObjectFactory ontOF;

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
		connectionURL = parameters.get("ONTConnectionURL");
		// Setup System
		ontOF = new ObjectFactory();
		ontJC = JAXBContext
				.newInstance("edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2.message.ont.xml");
		ontMarshaller = ontJC.createMarshaller();
		ontMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		this.parameters = parameters;
	}

	/**
	 * Returns a list of categories available for a given user.
	 * 
	 * @param client
	 *            HTTP Client
	 * @param blobCategories
	 *            Return data stored as Blob or Clob
	 * @param hiddenCategories
	 *            Return hidden data
	 * @param synonymsCategories
	 *            Return synonyms
	 * @param typeCategories
	 *            Sets type of categories
	 * @return A concepts Type that contains concepts
	 * @throws JAXBException
	 *             An XML Processing Exception occurred
	 * @throws ClientProtocolException
	 *             A Client Protocol Exception occurred
	 * @throws IOException
	 *             An IO Exception occurred
	 * @throws ResourceInterfaceException
	 *             An error occurred on the i2b2 server
	 */
	public ConceptsType getCategories(HttpClient client,
			boolean blobCategories, boolean hiddenCategories,
			boolean synonymsCategories, String typeCategories)
			throws JAXBException, ClientProtocolException, IOException,
			ResourceInterfaceException {

		// Create Post
		HttpPost post = new HttpPost(connectionURL + "/getCategories");
		// Set Header
		post.setHeader("Content-Type", "text/xml");

		// Create the XML Object
		RequestMessageType rmt = createMinimumBaseMessage();

		GetCategoriesType gct = ontOF.createGetCategoriesType();
		gct.setBlob(blobCategories);
		gct.setHiddens(hiddenCategories);
		gct.setSynonyms(synonymsCategories);
		if (typeCategories != null) {
			gct.setType(typeCategories);
		}

		rmt.getMessageBody().getAny().add(ontOF.createGetCategories(gct));

		// Mashall the XML to String and attach it to the post request
		StringWriter sw = new StringWriter();
		ontMarshaller.marshal(ontOF.createRequest(rmt), sw);

		post.setEntity(new StringEntity(sw.toString()));

		// Execute the post and get the response
		HttpResponse response = client.execute(post);

		// Unmarshall the Response Entity to XML and return it
		ResponseMessageType responseMessage = JAXB.unmarshal(response
				.getEntity().getContent(), ResponseMessageType.class);

		return getConceptTypes(responseMessage);
	}

	/**
	 * Returns a list of children from a given category
	 * 
	 * @param client
	 *            HTTP Client
	 * @param parentKey
	 *            The parent of the children to be returned
	 * @param hidden
	 *            Return hidden data
	 * @param blob
	 *            Return data stored as Blob or Clob
	 * @param synonyms
	 *            Return synonyms
	 * @param max
	 *            Max number to return
	 * @param type
	 *            Sets type of categories
	 * @return A concepts Type that contains concepts
	 * @throws JAXBException
	 *             An XML Processing Exception occurred
	 * @throws ClientProtocolException
	 *             A Client Protocol Exception occurred
	 * @throws IOException
	 *             An IO Exception occurred
	 * @throws ResourceInterfaceException
	 *             An error occurred on the i2b2 server
	 */
	public ConceptsType getChildren(HttpClient client, String parentKey,
			boolean hidden, boolean blob, boolean synonyms, int max, String type)
			throws JAXBException, ClientProtocolException, IOException,
			ResourceInterfaceException {
		// Create Post
		HttpPost post = new HttpPost(connectionURL + "/getChildren");
		// Set Header
		post.setHeader("Content-Type", "text/xml");

		// Create the XML Object
		RequestMessageType rmt = createMinimumBaseMessage();

		GetChildrenType gct = ontOF.createGetChildrenType();
		gct.setParent(parentKey);
		gct.setHiddens(hidden);
		gct.setBlob(blob);
		gct.setSynonyms(synonyms);
		if (max != -1) {
			gct.setMax(max);
		}
		if (type != null) {
			gct.setType(type);
		}

		rmt.getMessageBody().getAny().add(ontOF.createGetChildren(gct));

		// Mashall the XML to String and attach it to the post request
		StringWriter sw = new StringWriter();
		ontMarshaller.marshal(ontOF.createRequest(rmt), sw);

		post.setEntity(new StringEntity(sw.toString()));

		// Execute the post and get the response
		HttpResponse response = client.execute(post);

		// Unmarshall the Response Entity to XML and return it
		ResponseMessageType responseMessage = JAXB.unmarshal(response
				.getEntity().getContent(), ResponseMessageType.class);

		return getConceptTypes(responseMessage);
	}

	/**
	 * Parses the Response Message
	 * 
	 * @param responseMessage
	 *            Response Message
	 * @return Concepts Type that contains the categories
	 * @throws ResourceInterfaceException
	 *             An error occurred on the i2b2 server
	 */
	@SuppressWarnings("unchecked")
	private ConceptsType getConceptTypes(ResponseMessageType responseMessage)
			throws ResourceInterfaceException {
		if (responseMessage.getResponseHeader().getResultStatus().getStatus()
				.getType().equals("ERROR")) {
			throw new ResourceInterfaceException(responseMessage
					.getResponseHeader().getResultStatus().getStatus()
					.getValue());

		}
		return ((JAXBElement<ConceptsType>) responseMessage.getMessageBody()
				.getAny().get(0)).getValue();

	}

	/**
	 * Creates the minimum message needed to send a request to the i2b2 server
	 * 
	 * @return Request Message Base
	 */
	private RequestMessageType createMinimumBaseMessage() {
		RequestMessageType rmt = ontOF.createRequestMessageType();

		// Create Message Header Type
		MessageHeaderType mht = ontOF.createMessageHeaderType();

		// Set Sending Application
		ApplicationType sat = ontOF.createApplicationType();
		sat.setApplicationName("IRCT");
		sat.setApplicationVersion("1.0");

		mht.setSendingApplication(sat);

		// Set Sending Facility
		FacilityType ft = ontOF.createFacilityType();
		ft.setFacilityName("IRCT");

		mht.setSendingFacility(ft);

		// Create Security Type
		SecurityType st = ontOF.createSecurityType();
		st.setDomain(parameters.get("domain"));
		st.setUsername(parameters.get("username"));
		st.setPassword(parameters.get("password"));
		mht.setSecurity(st);

		mht.setProjectId(parameters.get("projectID"));
		rmt.setMessageHeader(mht);

		// Create Request Header Type
		RequestHeaderType rht = ontOF.createRequestHeaderType();
		rht.setResultWaittimeMs(180000);
		rmt.setRequestHeader(rht);

		// Create Body Type
		BodyType bt = ontOF.createBodyType();
		rmt.setMessageBody(bt);

		return rmt;
	}

}
