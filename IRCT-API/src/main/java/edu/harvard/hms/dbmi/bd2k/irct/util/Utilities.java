package edu.harvard.hms.dbmi.bd2k.irct.util;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import edu.harvard.hms.dbmi.bd2k.irct.event.IRCTEventListener;

public class Utilities {
	
	private static final Logger logger = Logger.getLogger(Utilities.class);
	
	public static IRCTEventListener getIRCTEventListener() {
		logger.info("userless method");
		try {
			InitialContext ic = new InitialContext();
			return (IRCTEventListener) ic.lookup("java:module/IRCTEventListener");
		} catch (NamingException e) {
			e.printStackTrace();
		}
		logger.info("useless method, returning null");
		return null;
	}
}
