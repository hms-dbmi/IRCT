
package edu.harvard.hms.dbmi.bd2k.irct;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.response.Response;
import junit.framework.Assert;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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


public class CheckStatusCodeNhanesReport
{
    
	
	public static int count;
	static DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss"); 
	static File file = new File( "Nhanes_puipaths_Check_Code_"+df.format(new Date())+".csv");

	//Delimiter used in CSV file
	private static final String COMMA_DELIMITER = ",";
	private static final String NEW_LINE_SEPARATOR = "\n";
	
	//CSV file header
	private static final String FILE_HEADER = "PUI Path,Pass or fail,HTTP Status Code,Count";

	@SuppressWarnings("resource")
	
    String endpoint=System.getProperty("path");	//Getting  the value from pom.xml
	
	
	 /**  
	    * Retrieve the value of accesstoken from pom.xml and set timeout of  30000000 milliseonds for getting
	     the response.
	 * @throws IOException 
	    * 
	  */  
	
	
	@Test (timeOut = 30000000 )
	public void getpathaccesstoken() throws IOException
				{
			
					String accesstoken=System.getProperty("accesstoken");
					checkcodegetpuis(endpoint, accesstoken);
				}
	
		 /**  
	    * Check the status code of all the puis and gets the count of number of puis 
	 * @throws IOException 
	    * 
	  */
	
	
	
	@SuppressWarnings({ })
	public void checkcodegetpuis(String puipath,String puiaccesstoken) throws IOException 
	{

		    FileWriter fileWriter = null;
		

			fileWriter = new FileWriter(file);

			//Write the CSV file header
			fileWriter.append(FILE_HEADER.toString());
			
			//Add a new line separator after the header
			fileWriter.append(NEW_LINE_SEPARATOR);
					
					try
							{
					
								given().header("Authorization", puiaccesstoken).when().get(puipath).then().statusCode(200).log().ifValidationFails();
						    
							}
					
							catch(AssertionError e)
							{
									 System.out.println("Error");
									  					  
							}
					
			
					try
						{
							
						
						Response res=(Response)given().header("Authorization", puiaccesstoken).when()
													     .get(puipath)
													     .then()				          
													     .extract().response();
									
									List<String> pui=res.getBody().jsonPath().getList("pui");
					
									count++;
								
									System.out.println("===========================PUIS======================================="+count);
							
								
												if (pui==null || pui.size()==0)
													{
														return;	
													}
												
												for (int i=0;i<pui.size();i++)
													 {
														
													String childpath=endpoint+pui.get(i);
													System.out.println("*********************PUI child path is *******************\n"+childpath);
													fileWriter.append(childpath);
													fileWriter.append(COMMA_DELIMITER);
													fileWriter.append("200");
													fileWriter.append(COMMA_DELIMITER);
													checkcodegetpuis(childpath,puiaccesstoken);
													 }
												
												
								}
		
													catch (Exception e) {
														System.out.println("Error in CsvFileWriter !!!");
														e.printStackTrace();
													} finally {

														fileWriter.append("test");
														try {
															fileWriter.flush();
															fileWriter.close();
														} catch (IOException e) {
															System.out.println("Error while flushing/closing fileWriter !!!");
												            e.printStackTrace();
														}
																					
								}	
													
							

			}
	
	
	
	}


	
