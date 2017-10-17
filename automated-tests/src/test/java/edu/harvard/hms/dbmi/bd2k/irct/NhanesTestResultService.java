
package edu.harvard.hms.dbmi.bd2k.irct;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
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
import org.apache.log4j.net.SyslogAppender;

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
import io.restassured.path.json.JsonPath;
@SuppressWarnings("unused")
/**			

 * NhanesTestqueryService.java class which covers the functionalities - resultStatus,AvailableFormats, 
 * @author Atul 
 * @Version 1.0	 */


public class NhanesTestResultService 
{
	//RestAssured.registerParser("text/plain", Parser.JSON);
	private static final Logger LOGGER = Logger.getLogger( NhanesTestResultService.class.getName() );
    String APIUrlResult;
    String accessToken;
    String APIUrl;
    int resultId;
    /**
     * Retrieve the value of endpoint (baseURI) from pom.xml
     */
    
   @BeforeMethod
    public void setup()
    {
	   
	   APIUrl=RestUtils.BaseURIPath()+"/queryService/runQuery/";
	   
	   System.out.println(APIUrl);
	   accessToken=RestUtils.AccessToken();
	   RestUtils.setContentType(ContentType.JSON);
	   System.out.println(accessToken);
    }
   
   public String generateStringFromResource(String path) throws IOException {

	    return new String(Files.readAllBytes(Paths.get(path)));
   }
   
   
   
   public void VerifyResultAvailableJsonResponse() throws IOException
   {
	   String root=System.getProperty("user.dir");
	   String abspathFile=root+"/src/test/resources/queryService.json";
	   String jsonBody = generateStringFromResource(abspathFile);
	   RestAssured.baseURI = "http://restapi.demoqa.com/utilities/weather/city";
   	   RequestSpecification httpRequest = RestAssured.given();
   	   Response response = httpRequest.get("/");
    
   	// First get the JsonPath object instance from the Response interface
   	JsonPath jsonPathEvaluator = response.jsonPath();
    
   	// Then simply query the JsonPath object to get a String value of the node
   	// specified by JsonPath: City (Note: You should not put $. in the Java code)
   	String Status = jsonPathEvaluator.get("Status");
    
   	// Let us print the city variable to see what we got
   	System.out.println("Report Status " + Status);
    
   	// Validate the response
   	Assert.assertEquals(Status, "Available", "Reports are Available");
    
   }
   
   
   
   
   
   
   
   
//@Test  
public void getResultStatusCode() throws IOException{
	 
	try{
	   String root=System.getProperty("user.dir");
	   String abspathFile=root+"/src/test/resources/queryService.json";
	   String jsonBody = generateStringFromResource(abspathFile);
	   System.out.println(APIUrl);
	   Response response=	(Response) RestAssured.given()
		   		.contentType("application/json")
		   		.header("Authorization", accessToken)
		   		.body(jsonBody)
		   		.when()
		   		.post(APIUrl)
		   		.then()
		   		.extract().response();
	   System.out.println(response.asString());
	   resultId=response.getBody().jsonPath().get("resultId");
	   System.out.println(resultId);
	   APIUrlResult=RestUtils.BaseURIPath()+"/resultService/result/"+resultId+"/JSON/";
	   System.out.println(APIUrlResult);
	   	
				   try{
				   		given()
			              .header("Authorization", accessToken)
			              .when()
		//	              .get("https://nhanes.hms.harvard.edu/rest/v1/resultService/result/21442/JSON/")
			              .get(APIUrlResult)
			              .then()
			              .statusCode(200);
				   	
					  LOGGER.info("***************** /resultService/result/ returns 200 OK status code*******************");
				   	
			  			}    
			  			catch (AssertionError r) 
				       		{
				           	LOGGER.error("Rest URI has Exception/Error", r);
				       		}

					}
	 catch (AssertionError e) 
			{
			LOGGER.error("The response is invalid -----Test Failed", e);
			}
}


//@Test  
	public void getResultResponse() throws IOException{
		 
		try{
		   String root=System.getProperty("user.dir");
		   String abspathFile=root+"/src/test/resources/queryService.json";
		   String jsonBody = generateStringFromResource(abspathFile);
		   System.out.println(APIUrl);
		   Response response=	(Response) RestAssured.given()
			   		.contentType("application/json")
			   		.header("Authorization", accessToken)
			   		.body(jsonBody)
			   		.when()
			   		.post(APIUrl)
			   		.then()
			   		.extract().response();
		   resultId=response.getBody().jsonPath().get("resultId");
		   APIUrlResult=RestUtils.BaseURIPath()+"/resultService/result/"+resultId+"/JSON/";
		   	
					   try{
					   		  given()
				              .header("Authorization", accessToken)
				              .when()
				              .get(APIUrlResult)            
				              .then()
				              .extract().response().getBody().jsonPath().get("testing");
				              
					   	
						  LOGGER.info("***************** /resultService/result/ returns valid Message*******************");
					   	
				  			}    
				  			catch (AssertionError r) 
					       		{
					           	LOGGER.error("Rest URI has Exception/Error", r);
					       		}
						

						}
		 catch (AssertionError e) 
		{
		LOGGER.error("The response is invalid -----Test Failed", e);
		}
	
		
		
		
		
}
}
/*
	try {
	   	
				given()
               .header("Authorization", accessToken)
               .when()
               .get("https://nhanes.hms.harvard.edu/rest/v1/resultService/result/21442/JSON/")
               //.get(APIUrlResult)
               .then()
               .statusCode(200);
	   	
		  LOGGER.info("***************** /resultService/result/ returns 200 OK status code*******************");
	   	
   		}    
   			catch (AssertionError r) 
	       		{
	           	LOGGER.error("Rest URI has Exception/Error", r);
	       		}
		
}

*/ 
 /*//@Test  
 public void getAvailalbleFormats() throws IOException{
   
 }
  
 
 //@Test  
 public void getResultStatus() throws IOException{
   
 }*/

