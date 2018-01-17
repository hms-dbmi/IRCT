package edu.harvard.hms.dbmi.bd2k.irct.model.result.json;

import com.fasterxml.jackson.databind.JsonNode;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Data;

/**
 * The json result implementation interface provides an extension of the data
 * interface to allow a json object to be stored.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public interface JSONResultImpl extends Data {
	/**
	 * Close the json result implementation
	 */
	void close();

	/**
	 * Is the json result implementation closed
	 * @return If it is closed
	 */
	boolean isClosed();

	/**
	 * Set the json object to save
	 * 
	 * @param jsonNode Jackson JSON Node
	 */
	JSONResultImpl setJsonNode(JsonNode jsonNode);


}
