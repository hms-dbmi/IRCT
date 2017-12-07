package edu.harvard.hms.dbmi.bd2k.irct.dataconverter;

import javax.ws.rs.core.StreamingOutput;

/**
 * An object that contains a data stream and information about a result
 */
public class ResultDataStream {
	private String mediaType;
	private StreamingOutput result;
	private String message;
	private String fileExtension;
	
	/**
	 * @return the mediaType
	 */
	public String getMediaType() {
		return mediaType;
	}
	/**
	 * @param mediaType the mediaType to set
	 */
	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}
	/**
	 * @return the result
	 */
	public StreamingOutput getResult() {
		if (result == null) {
			throw new RuntimeException("StreamingOutput `result` is not available.");
		}
		return result;
	}
	/**
	 * @param result the result to set
	 */
	public void setResult(StreamingOutput result) {
		this.result = result;
	}
	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	
	/**
	 * @param fileExtension the fileExtension to set
	 */
	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}
	
	/**
	 * Returns the file extension
	 * 
	 * @return File Extension
	 */
	public String getFileExtension() {
		return this.fileExtension;
	}
	
	
	
}
