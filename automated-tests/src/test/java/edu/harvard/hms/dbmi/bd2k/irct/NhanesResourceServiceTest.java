
package edu.harvard.hms.dbmi.bd2k.irct;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.response.Response;


import static io.restassured.RestAssured.*;

import org.apache.bcel.classfile.Constant;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

@SuppressWarnings("unused")

/**		
 * CheckStatusCodeNhanes.java class which check the response of End Points(PUIs),validate
 * it and counts the number of Puis under base project.  
 * @author Atul 
 * @Version 1.0	BufferedWriter bw = null;
	FileWriter fw = null;
 */


public class NhanesResourceServiceTest
{
    
	
	private static final Logger LOGGER = Logger.getLogger( NhanesResourceServiceTest.class.getName() );
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss"); 
	File file = new File( "Nhanes_puipaths_Check_Code_"+df.format(new Date())+".csv");
	public static int countPui;

	
	@SuppressWarnings("resource")
	
    
	 /**  
	    * Retrieve the value of endpoint (baseURI) from pom.xml    
	 */  

	String baseUri=System.getProperty("path");	//Getting  the value from pom.xml
	
    
	
	 /**  
	    * Retrieve the value of accessToken from pom.xml and set timeout of  30000000 milliseonds for getting
	     the response.
	 * @throws IOException i
	    * 
	  */  
	
	
	@Test (timeOut = 30000000 )
	public void ResourceServiceCheckStatusCode() throws IOException
				{
			
					String accessToken=System.getProperty("accessToken");
					resourceServiceStatusCodePuis(baseUri, accessToken);
				}
	


	/**  
	    * Check the status code of all the puis and gets the count of number of puis 
	 * @throws IOException 
	    * 
	  */
	
	
	@SuppressWarnings({ })
	
	public void resourceServiceStatusCodePuis(String puiPath,String puiAccessToken) throws IOException 
	{

		{
		
						
			try{
		
					given().header("Authorization", puiAccessToken).when().get(puiPath).then().statusCode(200).log().ifValidationFails();
			    
				}
		
					catch(AssertionError e)
					{
						   
						   LOGGER.info("Rest URI has Exception/Error"+e.getStackTrace());
		
					}
		
						Response res=(Response)given().header("Authorization", puiAccessToken).when()
										     .get(puiPath)
										     .then()				          
										     .extract().response();
						
						
						List<String> pui=res.getBody().jsonPath().getList("pui");
						
						if (pui==null || pui.size()==0)
						{
						int noPui=0;
						LOGGER.info("=========================== No Puis====================      : "+ noPui);	
						}
						{
						LOGGER.info("***************PUIs in response************************      : "+pui.toString());
						LOGGER.info("***************Count of child puis*********************      : "+pui.size());
						int puiCount=pui.size();					
						}
						
						countPui++;
											   
			//System.out.println("===========================PUIS======================================="+count);
				
					
									if (pui==null || pui.size()==0)
										{
											return;	
										}
									
									for (int i=0;i<pui.size();i++)
										 {
									
									String childPuiPath=baseUri+pui.get(i);
									
									//System.out.println("*********************PUI child path is *******************\n"+childpuipath);
									//LOGGER.info("PUI path  number :                                       "+countpui);
									
									
									LOGGER.info("-----------------------------------------------------------------------------------------------");
									LOGGER.info("Path Unique Identifier with baseURI             :" +countPui+"  : "+childPuiPath);
									LOGGER.info("-----------------------------------------------------------------------------------------------");
									resourceServiceStatusCodePuis(childPuiPath,puiAccessToken);
									
										
						}	 		 
						
							
				//System.out.println("----------------------------Number of puis--------------------------"+count);
								
			}
	
	}	
	
}
	
			