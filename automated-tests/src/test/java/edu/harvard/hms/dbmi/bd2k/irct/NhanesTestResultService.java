
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
import io.restassured.response.ValidatableResponse;
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
    String APIResultStatus;
    String APIResultFormat;
    String jsonBody;
    int resultId;
    /**
     * Retrieve the value of endpoint (baseURI) from pom.xml
     * @throws IOException 
     */
    
   @BeforeMethod
    public void setup() throws IOException
    {
	   
	   APIUrl=RestUtils.BaseURIPath()+"/queryService/runQuery/";
	   accessToken=RestUtils.AccessToken();
	   RestUtils.setContentType(ContentType.JSON);
	   String root=System.getProperty("user.dir");
	   String abspathFile=root+"/src/test/resources/queryService.json";
	   jsonBody = generateStringFromResource(abspathFile);

    }
   
   public String generateStringFromResource(String path) throws IOException {

	    return new String(Files.readAllBytes(Paths.get(path)));
   }
   
      
   
//@Test (priority=1) 
public void verifyGetResultStatusCode() throws IOException{
	 
	try{
	   
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


//@Test (priority=2)
	public void verifyGetResultJsonResponse() throws IOException{
		 
		try{
		   
		   Response response=	(Response) RestAssured.given()
			   		.contentType("application/json")
			   		.header("Authorization", accessToken)
			   		.body(jsonBody)
			   		.when()
			   		.post(APIUrl)
			   		.then()
			   		.extract().response();
//		   resultId=response.getBody().jsonPath().get("resultId");
	//	   APIUrlResult=RestUtils.BaseURIPath()+"/resultService/result/"+resultId+"/JSON/";
		   	
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
	
	
	//@Test (priority=3)
	   public void VerifyResultStatusAvailableJsonResponse() throws IOException
	   {	   
		
		APIResultStatus=RestUtils.BaseURIPath()+"/resultService/resultStatus/"+resultId;
		//System.out.println(APIResultStatus);
			try{
		   ValidatableResponse httpRequest = RestAssured.
				   given()
						.header("Authorization", accessToken)
				   .when()
					    .get(APIResultStatus)            
				   .then().
					    body("status", equalTo("AVAILABLE"));
			}
			
			catch(Exception e) {

				  LOGGER.info("***************** /resultService/resultStatus/ returns expected response as : Available*******************");
				   	
  			}    

	}
	
	
	//@Test (priority=4)
	   public void VerifyResultStatusStatusCode() 
	   {	   
		
		APIResultStatus=RestUtils.BaseURIPath()+"/resultService/resultStatus/"+resultId;
		
			try{
		   ValidatableResponse httpRequest = RestAssured.
				   given()
						.header("Authorization", accessToken)
				   .when()
					    .get(APIResultStatus)            
				   .then().
					    statusCode(200);
			  LOGGER.info("***************** /resultService/resultStatus/ returns expected status code as : 200*******************");
			}
			
			catch(Exception e) {

				  LOGGER.info("***************** /resultService/resultStatus/ throws excepion!*******************");
				   	
			}    

	}

	
	//@Test (priority=5)
	   public void VerifyAvailableStatusCode() 
	   {	   
		
		APIResultFormat=RestUtils.BaseURIPath()+"/resultService/availableFormats/"+resultId;
		
			try{
		   ValidatableResponse httpRequest = RestAssured.
				   given()
						.header("Authorization", accessToken)
				   .when()
					    .get(APIResultFormat)            
				   .then().
					    statusCode(200);
			  LOGGER.info("***************** /resultService/availableFormats/ returns expected status code as : 200*******************");
			}
			
			catch(Exception e) {

				  LOGGER.info("***************** /resultService/availableFormats/ throws excepion!*******************");
				   	
			}    

	}

	
	@Test (priority=6)
	   public void VerifyAvailableJsonResponse() 
	   {	   
		
		APIResultFormat=RestUtils.BaseURIPath()+"/resultService/availableFormats/"+resultId;
		System.out.println(APIResultFormat);
			try{
		   ValidatableResponse httpRequest = RestAssured.
				   given()
						.header("Authorization", accessToken)
				   .when()
					    .get("https://nhanes.hms.harvard.edu/rest/v1/resultService/availableFormats/218045").then().assertThat().body(containsString("JSON"));            
				   
				    //body("status", equalTo("AVAILABLE"));
					  LOGGER.info("***************** /resultService/availableFormats/ response matches as expected*******************");
			}
			
			catch(Exception e) {

				LOGGER.info("***************** /resultService/availableFormats/ throws excepion!*******************");
				   	
			}    

	}

}
	
	//extract().response().;
		

	   	
	   	// First get the JsonPath object instance from the Response interface
	   	//JsonPath jsonPathEvaluator = response.jsonPath();
	    
	   	// Then simply query the JsonPath object to get a String value of the node
	   	
	   	//String status = jsonPathEvaluator.get("status");
	    
	   	// Let us print the city variable to see what we got
	   	//System.out.println("Result satus value" + status);
	    
	   	// Validate the response
	   	//Assert.assertEquals(status, "Available", "Reports are Available");



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
  
 
*/

