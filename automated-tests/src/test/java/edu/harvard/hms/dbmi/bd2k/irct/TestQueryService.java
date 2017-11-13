
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
    String abspathFileInvalid;
    String jsonBody;
    String jsonBodyInvalid;
    
    //RestAssured.defaultParser = Parser.JSON;
    
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
					  
					  root=System.getProperty("user.dir");
					  abspathFile=root+"/src/test/resources/queryService.json";
					  jsonBody = generateStringFromResource(abspathFile);
					  
					  
					  //RestAssured.registerParser("text/plain", Parser.TEXT);
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
					
						RequestSpecification httpRequest = (RequestSpecification) RestAssured.given().header("Authorization", accessToken+1);
						Response response = httpRequest.body(jsonBody).post(QueryServiceAPIUrl);
						int statusCode=response.getStatusCode();
						Assert.assertEquals(statusCode /*actual value*/, 401 /*expected value*/, "Correct status code returned");
						
							
					}
					
					@Test (priority=4) 
					public void verifyRunQueryStatusCodeInvalidJsonBody() throws IOException{
						
						  root=System.getProperty("user.dir");
						  abspathFileInvalid=root+"/src/test/resources/queryService.json";
						  jsonBodyInvalid = generateStringFromResource(abspathFile);
					
						RequestSpecification httpRequest = (RequestSpecification) RestAssured.given().header("Authorization", accessToken);
						Response response = httpRequest.body(abspathFileInvalid).post(QueryServiceAPIUrl);
						int statusCode=response.getStatusCode();
						Assert.assertEquals(statusCode /*actual value*/, 500 /*expected value*/, "Correct status code returned");
						
							
					}
					
					
					@Test (priority=5) 
					public void verifyRunQueryResponseInvalidAccessToken() throws IOException{
					
						RequestSpecification httpRequest = (RequestSpecification) RestAssured.given().header("Authorization", accessToken+1);
						Response response = httpRequest.body(jsonBody).post(QueryServiceAPIUrl);
						String invalidAccessTokenResponse=response.jsonPath().get("message");
						Assert.assertEquals(invalidAccessTokenResponse , "Could not establish the user identity from request headers. HTTP 401 Unauthorized");
						
							
					}
					



}

