/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.scidb;

import java.util.HashMap;
import java.util.Map;

/**
 * A Java representation of a SciDB array. The data is not stored locally.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class SciDBArray implements SciDBCommand {
	private String name;
	private boolean temp;
	private String using;

	private SciDB origin;

	private Map<String, SciDBDimension> dimensions;
	private Map<String, SciDBAttribute> attributes;

	public SciDBArray(String name) {
		this.name = name;
		this.dimensions = new HashMap<String, SciDBDimension>();
		this.attributes = new HashMap<String, SciDBAttribute>();
	}
	public SciDBArray() {
		this.dimensions = new HashMap<String, SciDBDimension>();
		this.attributes = new HashMap<String, SciDBAttribute>();
	}

	@Override
	public String toAFLQueryString() {
		String returns = "";

		if (!this.attributes.isEmpty()) {
			returns += "<";
			for (String attributeName : this.attributes.keySet()) {
				returns += attributeName
						+ this.attributes.get(attributeName).toAFLQueryString()
						+ ",";
			}
			returns = returns.substring(0, returns.length() - 1);
			returns += ">";
		}

		if (!this.dimensions.isEmpty()) {
			returns += "[";
			for (String dimensionName : this.dimensions.keySet()) {
				returns += dimensionName
						+ this.dimensions.get(dimensionName).toAFLQueryString()
						+ ",";
			}
			returns = returns.substring(0, returns.length() - 1);
			returns += "]";
		}
		return returns;
	}
	
	public static SciDBArray fromAFLResponseString(String line) {
		SciDBArray returns = new SciDBArray();
		String name = line.substring(0, line.indexOf("<"));
		returns.setName(name);
		
		String attributeString = line.substring(line.indexOf("<") + 1, line.indexOf(">"));
		String attribtues[] = attributeString.split(",");
		
		for(String attribute : attribtues) {
			String[] attributeComponents = attribute.split(":");
			returns.addAttribute(attributeComponents[0], SciDBDataType.valueOf(attributeComponents[1].split(" ")[0].toUpperCase()));
		}
		
		String dimensionString = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
		
		String[] segments = dimensionString.split(",");
		
		SciDBDimension newDimension = null;
		String dimensionName = "";
		
		int segmentPosition = 0;
		for(String segment : segments) {
			
			if(segment.contains("=")) {
				if(newDimension != null) {
					returns.addDimension(dimensionName, newDimension);
				}
				newDimension = new SciDBDimension();
				segmentPosition = 1;				
				dimensionName = segment.substring(0, segment.indexOf("="));
				segment = segment.substring(segment.indexOf("=") + 1);
				newDimension.setLowValue(segment.substring(0, segment.indexOf(":")));
				newDimension.setHighValue(segment.substring(segment.indexOf(":") + 1));
			} else if(segmentPosition == 1) {
				newDimension.setChunkLength(segment);
				segmentPosition = 2;
			} else if(segmentPosition == 2) {
				newDimension.setChunkOverlap(segment);
				segmentPosition = 3;
			}
			
		}
		returns.addDimension(dimensionName, newDimension);
		
		return returns;
	}

	/*************************************************************************/
	/*** Setters and Getters ***/
	/*************************************************************************/

	/**
	 * Returns the unique name of the array
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the array
	 * 
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Is this a temporary table
	 * 
	 * @return the temp
	 */
	public boolean isTemp() {
		return temp;
	}

	/**
	 * Sets if this is a temporary table
	 * 
	 * @param temp
	 *            the temp to set
	 */
	public void setTemp(boolean temp) {
		this.temp = temp;
	}

	/**
	 * Returns the existing array
	 * 
	 * @return the using
	 */
	public String getUsing() {
		return using;
	}

	/**
	 * Set the existing array
	 * 
	 * @param using
	 *            the using to set
	 */
	public void setUsing(String using) {
		this.using = using;
	}

	/**
	 * Returns the SciDB instance that created the array
	 * 
	 * @return the origin
	 */
	public SciDB getOrigin() {
		return origin;
	}

	/**
	 * Sets the SciDB instance that created the array
	 * 
	 * @param origin
	 *            the origin to set
	 */
	public void setOrigin(SciDB origin) {
		this.origin = origin;
	}

	/**
	 * Returns a map of the array dimensions where the key is the name of the
	 * dimension and the value is the attributes of that dimension.
	 * 
	 * @return the dimensions
	 */
	public Map<String, SciDBDimension> getDimensions() {
		return dimensions;
	}

	/**
	 * Add a dimension to an array
	 * 
	 * @param name Name of the dimension
	 * @param newDimension Dimension to add
	 */
	public void addDimension(String name, SciDBDimension newDimension) {
		dimensions.put(name, newDimension);
	}

	/**
	 * Sets a map of the array dimension where the key is the name of the
	 * dimension and the value is the attributes of that dimension.
	 * 
	 * @param dimensions
	 *            the dimensions to set
	 */
	public void setDimensions(Map<String, SciDBDimension> dimensions) {
		this.dimensions = dimensions;
	}

	/**
	 * Returns a map of attributes of the array where the key is the name of the
	 * attribute and the value is its attributes
	 * 
	 * @return the attributes
	 */
	public Map<String, SciDBAttribute> getAttributes() {
		return attributes;
	}

	/**
	 * Sets the map of attributes of the array where the key is the name of the
	 * attribute and the value is its attributes
	 * 
	 * @param attributes
	 *            the attributes to set
	 */
	public void setAttributes(Map<String, SciDBAttribute> attributes) {
		this.attributes = attributes;
	}

	public void addAttribute(String attribtueName, SciDBDataType dataType) {
		this.attributes.put(attribtueName, new SciDBAttribute(dataType));
		
	}
}
