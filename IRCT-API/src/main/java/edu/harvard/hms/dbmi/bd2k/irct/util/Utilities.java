package edu.harvard.hms.dbmi.bd2k.irct.util;

import edu.harvard.hms.dbmi.bd2k.irct.event.IRCTEventListener;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ApplicationException;
import org.apache.http.HttpResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Utilities {
	
	public static final class Naming {
		
		public static final class Whitelist{
			public static String JSON_NAME = "userid";
			public static String JSON_RESOURCES = "resources";
		}
	}
	
	public static IRCTEventListener getIRCTEventListener() throws ApplicationException{
		try {
			InitialContext ic = new InitialContext();
			return (IRCTEventListener) ic.lookup("java:module/IRCTEventListener");
		} catch (NamingException e) {
			throw new ApplicationException("Utilities.getIRCTEventListener() throws Naming Exception: " + e.getMessage()
					+ " with Resolved Name: " + e.getResolvedName() );
		}
	}

    /**
     *
     * @param httpResponse
     * @return
     *
     * @exception ApplicationException
     */
	public static String readFromHttpResponse(HttpResponse httpResponse) {
		String line;
		try (BufferedReader r = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()))){
			StringBuilder total = new StringBuilder();

			while ((line = r.readLine()) != null) {
				total.append(line);
			}
		} catch (IOException e){
			throw new ApplicationException("readFromHttpResponse() error " + e.getMessage() + " with response status code: "
					+ httpResponse.getStatusLine().getStatusCode()
					+ ", response entity length: " + httpResponse.getEntity().getContentLength() );
		}

		return line;
	}
}
