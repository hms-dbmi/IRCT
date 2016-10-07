package edu.harvard.hms.dbmi.scidb;

/**
 * A Java representation of a SciDB dimension. Changes to this object will not
 * be reflected in the SciDB instance.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class SciDBDimension {
	private String lowValue;
	private String highValue;
	private String chunkLength;
	private String chunkOverlap;
	
	public SciDBDimension() {
	}
	
	public SciDBDimension(String lowValue, String highValue, String chunkLength, String chunkOverlap) {
		this.lowValue = lowValue;
		this.highValue = highValue;
		this.chunkLength = chunkLength;
		this.chunkOverlap = chunkOverlap;
	}
	
	public SciDBDimension(int lowValue, int highValue, int chunkLength, int chunkOverlap) {
		this(Integer.toString(lowValue), Integer.toString(highValue), Integer.toString(chunkLength), Integer.toString(chunkOverlap));
	}

	

	public String toAFLQueryString() {
		String returns = "";
		
		if(lowValue != null) {
			returns += "=" + lowValue;
		}
		
		if (highValue != null) {
			returns += ":" + highValue;
		} else if(lowValue != null) {
			returns += ":*";
		}
		
		if (chunkLength != null) {
			returns += "," + chunkLength;
		} 
		
		if (chunkOverlap != null) {
			if(chunkLength == null) {
				returns += ",";
			}
			returns += "," + chunkOverlap;
		}
		
		return returns;
	}

	/*************************************************************************/
	/*** Setters and Getters                                               ***/
	/*************************************************************************/
	

	/**
	 * Returns the expression for the low value
	 * 
	 * @return the lowValue
	 */
	public String getLowValue() {
		return lowValue;
	}

	/**
	 * Sets the expression for the low value
	 * 
	 * @param lowValue
	 *            the lowValue to set
	 */
	public void setLowValue(String lowValue) {
		this.lowValue = lowValue;
	}

	/**
	 * Returns the expression for the high value
	 * 
	 * @return the highValue
	 */
	public String getHighValue() {
		return highValue;
	}

	/**
	 * Sets the expression for the high value
	 * 
	 * @param highValue
	 *            the highValue to set
	 */
	public void setHighValue(String highValue) {
		this.highValue = highValue;
	}

	/**
	 * Returns the chunk length
	 * 
	 * @return the chunkLength
	 */
	public String getChunkLength() {
		return chunkLength;
	}

	/**
	 * Sets the chunk length
	 * 
	 * @param chunkLength
	 *            the chunkLength to set
	 */
	public void setChunkLength(String chunkLength) {
		this.chunkLength = chunkLength;
	}

	/**
	 * Returns the number of overlapping dimensions
	 * 
	 * @return the chunkOverlap
	 */
	public String getChunkOverlap() {
		return chunkOverlap;
	}

	/**
	 * Sets the number of overlapping dimensions
	 * 
	 * @param chunkOverlap
	 *            the chunkOverlap to set
	 */
	public void setChunkOverlap(String chunkOverlap) {
		this.chunkOverlap = chunkOverlap;
	}

}
