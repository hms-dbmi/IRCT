package edu.harvard.hms.dbmi.bd2k.irct;
import org.testng.annotations.Test;
//import org.testng.log4testng.Logger;
import org.apache.log4j.Logger;

import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.testng.annotations.BeforeTest;

public class CheckLog4j {
  
	   static Logger log = Logger.getLogger(CheckLog4j.class.getName());
	@Test
  public void  checkForLog4jConfigFile() 
  {

	  
	      log.debug("Hello this is a debug message");
	      log.info("Hello this is an info message");
	   }
	}