package edu.harvard.hms.dbmi.bd2k.irct.util;

import edu.harvard.hms.dbmi.bd2k.irct.event.IRCTEventListener;

import javax.naming.InitialContext;
import javax.naming.NamingException;

public class Utilities {
	
	public static final class Naming {
		
		public static final class Whitelist{
			public static String JSON_NAME = "userid";
			public static String JSON_RESOURCES = "resources";
		}
	}
	
	public static IRCTEventListener getIRCTEventListener() {
		try {
			InitialContext ic = new InitialContext();
			return (IRCTEventListener) ic.lookup("java:module/IRCTEventListener");
		} catch (NamingException e) {
			e.printStackTrace();
		}
		return null;
	}
}
