


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


public class CsvReport
{
    
	
	private static final Logger LOGGER = Logger.getLogger( CsvReport.class.getName() );
	DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss"); 
	File file = new File( "Nhanes_puipaths_Check_Code_"+df.format(new Date())+".csv");
	public static int count;
	BufferedWriter bw = null;
	FileWriter fw = null;

	@SuppressWarnings("resource")
	
	@Test
	
	public void FileCreation() throws IOException
	{

		fw = new FileWriter(file.getAbsoluteFile(), true);
		bw = new BufferedWriter(fw);
		bw.write("PUIPath");
	}

	
    
	 /**  
	    * Retrieve the value of endpoint (baseURI) from pom.xml    
	 */  
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
	
	
	/*public void Filewrite(String pathtest)
	{

*/


	 /**  
	    * Check the status code of all the puis and gets the count of number of puis 
	 * @throws IOException 
	    * 
	  */
	
	
	@SuppressWarnings({ })
	public void checkcodegetpuis(String puipath,String puiaccesstoken) throws IOException 
	{

	{
		
						
			try{
		
					given().header("Authorization", puiaccesstoken).when().get(puipath).then().statusCode(200).log().ifValidationFails();
			    
				}
		
					catch(AssertionError e)
					{
						   LOGGER.assertLog(true,e.getMessage());
						  					  
					}
		
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

										try {
											
											bw.write(childpath);
											bw.newLine();
											System.out.println("Done");
						} catch (IOException e) {

											e.printStackTrace();

										} finally {

											try {

												if (bw != null)
													bw.close();

												if (fw != null)
													fw.close();

											} catch (IOException ex) {

												ex.printStackTrace();

											}
												
										checkcodegetpuis(childpath,puiaccesstoken);
											//Logger.getInstance("PUI testing"+childpath);	
										 }	 		 
						
							
				//System.out.println("----------------------------Number of puis--------------------------"+count);
										 
			}
	
	}	
	
}
	
}