package edu.harvard.hms.dbmi.bd2k.irct;
import org.testng.annotations.Test;
//import org.testng.log4testng.Logger;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.testng.annotations.BeforeTest;

public class CheckLog4j {
  
	@Test
	public void  CVSUtilExample() throws IOException
	{
	/*
	String csvFile = "D://abc.csv";
    FileWriter writer = new FileWriter(csvFile);
    CSVUtils.writeLine(writer, Arrays.asList("a", "b", "c", "d"));
    
	}
	*/
	
		 String text = "Hello world";
	        BufferedWriter output = null;
	        try {
	            File file = new File("example.txt");
	            output = new BufferedWriter(new FileWriter(file));
	            output.write(text);
	        } catch ( IOException e ) {
	            e.printStackTrace();
	        } finally {
	          if ( output != null ) {
	            output.close();
	          }
	        }
	
	
	}
	
	
}
	 ///static Logger log = Logger.getLogger(CheckLog4j.class.getName());

//	@Test
  /*public void  checkForLog4jConfigFile() 
  {

	  
	      log.debug("Hello this is a debug message");
	      log.info("Hello this is an info message");
	   }
*/	