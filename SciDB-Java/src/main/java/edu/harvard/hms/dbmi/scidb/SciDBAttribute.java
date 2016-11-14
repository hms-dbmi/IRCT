/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.scidb;

/**
 * A Java representation of a SciDB array attribute. Changes to this object will
 * not be reflected in the SciDB instance.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class SciDBAttribute {
	private SciDBDataType dataType;
	private boolean nullable;
	private String defaultValue;
	private String compressionType;

	public SciDBAttribute(SciDBDataType dataType) {
		this.dataType = dataType;
	}

	public String toAFLQueryString() {
		String returns = "";
		
		if(dataType != null) {
			returns += ":" + dataType.getSciDBName();
		}
		
		if (nullable) {
			returns += " NULL";
		}
		
		if (defaultValue != null) {
			returns += " DEFAULT " + defaultValue;
		} 
		
		if (compressionType != null) {
			returns += " COMPRESSION " + compressionType; 
		}
		
		return returns;
	}

	/*************************************************************************/
	/*** Setters and Getters                                               ***/
	/*************************************************************************/
	
	/**
	 * Returns the data type of the attribute
	 * 
	 * @return the dataType
	 */
	public SciDBDataType getDataType() {
		return dataType;
	}

	/**
	 * Sets the data type of the attribute
	 * 
	 * @param dataType
	 *            the dataType to set
	 */
	public void setDataType(SciDBDataType dataType) {
		this.dataType = dataType;
	}

	/**
	 * Returns if the attribute is nullable
	 * 
	 * @return the nullable
	 */
	public boolean isNullable() {
		return nullable;
	}

	/**
	 * Sets if the attribute is nullable
	 * 
	 * @param nullable
	 *            the nullable to set
	 */
	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}

	/**
	 * Returns the default value of the attribute
	 * 
	 * @return the defaultValue
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Sets the default attribute of the value
	 * 
	 * @param defaultValue
	 *            the defaultValue to set
	 */
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * Returns the compression type
	 * 
	 * @return the compressionType
	 */
	public String getCompressionType() {
		return compressionType;
	}

	/**
	 * Sets the compression type
	 * 
	 * @param compressionType
	 *            the compressionType to set
	 */
	public void setCompressionType(String compressionType) {
		this.compressionType = compressionType;
	}

}
