
package edu.harvard.hms.dbmi.bd2k.irct;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import edu.harvard.hms.dbmi.bd2k.irct.Utils.RestUtils;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.LogConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.io.InputStreamReader;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

import org.apache.bcel.classfile.Constant;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
@SuppressWarnings("unused")
/**			

 * NhanesTestqueryService.java class which covers the functionalities - runQuery 
 * @author Atul 
 * @Version 1.0	 */


public class TestQueryService
{
	//RestAssured.registerParser("text/plain", Parser.JSON);
	private static final Logger LOGGER = Logger.getLogger( TestQueryService.class.getName() );
    String QueryServiceAPIUrl;
    String accessToken;
    String root;
    String abspathFile;
    String abspathFileInvalidMT;
    String abspathFileInvalidET;
    String jsonBody;
    String jsonBodyInvalidMT;
    String jsonBodyInvalidET;
    
    
    
    /**
     * Retrieve the value of endpoint (baseURI) from pom.xml
     */
    
					   @BeforeMethod
					    public void setup()
					    {
						   QueryServiceAPIUrl=RestUtils.BaseURIPath()+"/queryService/runQuery/";
						   accessToken=RestUtils.AccessToken();
						   RestUtils.setContentType(ContentType.JSON);
					    }
					   
					  
					 public String generateStringFromResource(String path) throws IOException {
					
						    return new String(Files.readAllBytes(Paths.get(path)));
					
						}
					   
		 @Test (priority=1)
					 
					public void verifyRunQueryStatusCode() throws IOException{
						 
					LOGGER.info("------------------The tese case verifyRunQueryStatusCode method is running--------------------");
					  
					  root=System.getProperty("user.dir");
					  abspathFile=root+"/src/test/resources/queryService.json";
					  jsonBody = generateStringFromResource(abspathFile);
					  
								  try{
								  		given()
								  		.contentType("application/json")
								  		.header("Authorization", accessToken)
								  		.body(jsonBody)
								  		.when()
								  		.post(QueryServiceAPIUrl)
								  		.then()
								  		.statusCode(200)
								  		.log()
								  		.all();
								  	LOGGER.info("The Status code is verified successfully");
								 	}
								  catch (AssertionError e) 
										{
								 	LOGGER.error("The Status code is not as expected -----Test Failed", e);
										}
								
								 }
		 @Test (priority=2)  
					 
					 public void verifyRunQueryResponseCheck() throws IOException{
					   
					   String root=System.getProperty("user.dir");
					   String abspathFile=root+"/src/test/resources/queryService.json";
					   String jsonBody = generateStringFromResource(abspathFile);
					   
					   LOGGER.info("--------------The test case verifyRunQueryResponseCheck method is running------------");
					  				   try{
										   
					  					 Response response=	(Response) RestAssured.given()
					 					   		.contentType("application/json")
					 					   		.header("Authorization", accessToken)
					 					   		.body(jsonBody)
					 					   		.when()
					 					   		.post(QueryServiceAPIUrl)
					 					   		.then().
					 					   		body("resultId",is(notNullValue())).extract().response();
					  					 
										   LOGGER.info("The response of queryService is verified successfully"       +response.asString());
										   		 }
										   
										   catch (AssertionError e) 
								       		{
											   
											   LOGGER.error("The Response is not as expected -----Test Failed", e);
								       		}
					   
					 		}
					
					
		@Test (priority=3) 
					public void verifyRunQueryStatusCodeInvalidAccessToken() throws IOException{
					
						LOGGER.info("--------------The test case verifyRunQueryStatusCodeInvalidAccessToken method is running------------");
						RequestSpecification httpRequest = (RequestSpecification) RestAssured.given().header("Authorization", accessToken+1);
						Response response = httpRequest.body(jsonBody).post(QueryServiceAPIUrl);
						int statusCode=response.getStatusCode();
						Assert.assertEquals(statusCode /*actual value*/, 401 /*expected value*/, "Correct status code returned");
						LOGGER.info("--------------Invalid access token returns 401 unauthorized code------------");
							
					}
					
		
		@Test (priority=4) 
					public void verifyRunQueryStatusCodeInvalidJsonBodyMissingTag() throws IOException{
						
						  root=System.getProperty("user.dir");
					 	  abspathFileInvalidMT=root+"/src/test/resources/queryServiceInvalidMissingTag.json";
						  jsonBodyInvalidMT = generateStringFromResource(abspathFileInvalidMT);
						  LOGGER.info("--------------The test case verifyRunQueryStatusCodeInvalidJsonBodyMissingTag method is running------------");
					
						RequestSpecification httpRequest = (RequestSpecification) RestAssured.given().header("Authorization", accessToken);
						Response response = httpRequest.body(jsonBodyInvalidMT).post(QueryServiceAPIUrl);
						int statusCode=response.getStatusCode();	
						
						System.out.println(statusCode);
						Assert.assertEquals(statusCode /*actual value*/, 500 /*expected value*/, "Correct status code returned");
						LOGGER.info("--------------Invalid JsonBody returns 500 Internal Server Error------------");
							
					}
					
	
		@Test (priority=5) 
		public void verifyRunQueryResponseInvalidJsonBodyEmptyTag() throws IOException{
			
			  root=System.getProperty("user.dir");
		 	  abspathFileInvalidET=root+"/src/test/resources/queryServiceInvalidEmptyField.json";
			  jsonBodyInvalidET = generateStringFromResource(abspathFileInvalidET);
			  LOGGER.info("--------------The test case verifyRunQueryResponseInvalidJsonBodyEmptyTag method is running------------");
		
			RequestSpecification httpRequest = (RequestSpecification) RestAssured.given().header("Authorization", accessToken);
			Response response = httpRequest.body(jsonBodyInvalidET).post(QueryServiceAPIUrl);
			String emptyfieldResponse=response.jsonPath().get("status");
			Assert.assertEquals(emptyfieldResponse , "Invalid Request");
			LOGGER.info("--------------Invalid JsonBody empty field response says Invalid Request------------");
				
		}

		@Test (priority=6) 
					public void verifyRunQueryResponseInvalidAccessToken() throws IOException{
					
						LOGGER.info("--------------The test case verifyRunQueryResponseInvalidAccessToken method is running------------");
						RequestSpecification httpRequest = (RequestSpecification) RestAssured.given().header("Authorization", accessToken+1);
						Response response = httpRequest.body(jsonBody).post(QueryServiceAPIUrl);
						String invalidAccessTokenResponse=response.jsonPath().get("message");
						Assert.assertEquals(invalidAccessTokenResponse , "Could not establish the user identity from request headers. HTTP 401 Unauthorized");
						LOGGER.info("--------------Invalid access token returns :  Could not establish the user identity from request headers. HTTP 401 Unauthorized-----------");
							
					}
					



}

