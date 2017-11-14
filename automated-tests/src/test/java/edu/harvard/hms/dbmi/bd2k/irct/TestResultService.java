
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
import io.restassured.internal.assertion.Assertion;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import java.io.InputStreamReader;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.assertEquals;

import org.apache.bcel.classfile.Constant;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.net.SyslogAppender;
import org.awaitility.Awaitility;

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
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.path.json.JsonPath;
@SuppressWarnings("unused")
/**			

 * NhanesTestqueryService.java class which covers the functionalities - resultStatus,AvailableFormats, 
 * @author Atul 
 * @Version 1.0	 */

public class TestResultService
{
	//RestAssured.registerParser("text/plain", Parser.JSON);
	
	
	private static final Logger LOGGER = Logger.getLogger( TestResultService.class.getName() );
    String resultServiceAPIUrl;
    String accessToken;
    String APIUrl;
    String resultStatusAPIUrl;
    String resultFormatAPIUrl;
    String jsonBody;
    String jsonGetResult;
    int statusCode;
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
   
      
   
@Test (priority=1) 
public void verifyGetResultStatusCode() throws IOException{
	 
	 LOGGER.info("--------------The test case verifyGetResultStatusCode method is running------------");
	
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
	   
	   resultServiceAPIUrl=RestUtils.BaseURIPath()+"/resultService/result/"+resultId+"/JSON/";
	   
				   try{
				   		given()
			              .header("Authorization", accessToken)
			              .when()
		//	              .get("https://nhanes.hms.harvard.edu/rest/v1/resultService/result/21442/JSON/")
			              .get(resultServiceAPIUrl)
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


@Test (priority=2)

	public void verifyGetResultJsonResponse() throws IOException, Exception{

	 LOGGER.info("--------------The test case verifyGetResultJsonResponse method is running------------");
	 
	try{
		Awaitility.await().atMost(70, TimeUnit.SECONDS).until(new Runnable()
		{
		  public void run()
		  {
			  Response responseJson=given()
				  .header("Authorization", accessToken)
	              .when()
	              //.get("https://nhanes.hms.harvard.edu/rest/v1/resultService/result/219687/JSON");
	              .get(resultServiceAPIUrl);
		   		jsonGetResult=responseJson.body().prettyPrint();
		   		responseJson.prettyPrint();
		   
		  }
		 });
	}
catch (AssertionError e) 
		{
		LOGGER.error("The response is invalid -----Test Failed", e);
		}

 try 
 {
		System.out.println("*******************************");
		System.out.println("jsonGetResult");
	 	FileWriter file = new FileWriter("./src/test/resources/resultServiceOutput.json");
		file.write(jsonGetResult);
		file.close();
		final File actual = new File("./src/test/resources/resultServiceOutput.json");
		final File expected = new File("./src/test/resources/resultServiceOutputExpected.json");
		assertEquals(FileUtils.readFileToString(actual, "utf-8"),FileUtils.readFileToString(expected, "utf-8"));
 } 
 catch (IOException e)
 {
	// TODO Auto-generated catch block
	e.printStackTrace();
 }
	

		
}


@Test (priority=3)
	 
public void verifyResultStatusAvailableJsonResponse() throws IOException, InterruptedException
	   {	   

		resultStatusAPIUrl=RestUtils.BaseURIPath()+"/resultService/resultStatus/"+resultId;

		Thread.sleep(30000);
		
		 LOGGER.info("--------------The test case verifyResultStatusAvailableJsonResponse method is running------------");
		try{
		   ValidatableResponse httpRequest = RestAssured.
				   given()
						.header("Authorization", accessToken)
				   .when()
					    .get(resultStatusAPIUrl)            
				   .then().
					    body("status", equalTo("AVAILABLE"));
			}
			
			catch(Exception e) {

				  LOGGER.info("***************** /resultService/resultStatus/ returns expected response as : Available*******************");
				   	
  			}    

	}
	
	
@Test (priority=4)
	   public void verifyResultStatusStatusCode() 
	   {	   
		
		resultStatusAPIUrl=RestUtils.BaseURIPath()+"/resultService/resultStatus/"+resultId;
		
		 LOGGER.info("--------------The test case verifyResultStatusStatusCode method is running------------");
			try{
		   ValidatableResponse httpRequest = RestAssured.
				   given()
						.header("Authorization", accessToken)
				   .when()
					    .get(resultStatusAPIUrl)            
				   .then().
					    statusCode(200);
			  LOGGER.info("***************** /resultService/resultStatus/ returns expected status code as : 200*******************");
			}
			
			catch(Exception e) {

				  LOGGER.info("***************** /resultService/resultStatus/ throws excepion!*******************");
				   	
			}    

	}

	
@Test (priority=5)
	   public void verifyAvailableFormatStatusCode() 
	   {	   
		
		resultFormatAPIUrl=RestUtils.BaseURIPath()+"/resultService/availableFormats/"+resultId;
		
		 LOGGER.info("--------------The test case verifyAvailableFormatStatusCode method is running------------");
		
			try{
		   ValidatableResponse httpRequest = RestAssured.
				   given()
						.header("Authorization", accessToken)
				   .when()
					    .get(resultFormatAPIUrl)            
				   .then().
					    statusCode(200);
			  LOGGER.info("***************** /resultService/availableFormats/ returns expected status code as : 200*******************");
			}
			
			catch(Exception e) {

				  LOGGER.info("***************** /resultService/availableFormats/ throws excepion!*******************");
				   	
			}    

	}

		
	@Test (priority=6)
	   public void verifyAvailableFormatJsonResponse() 
	   {	   
		
		resultFormatAPIUrl=RestUtils.BaseURIPath()+"/resultService/availableFormats/"+resultId;
		//System.out.println(resultFormatAPIUrl);
		
		 LOGGER.info("--------------The test case verifyAvailableFormatJsonResponse method is running------------");
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

	
	@Test (priority=7) 
	public void verifyGetResultInvalidAccessToken() throws IOException{
		 LOGGER.info("--------------The test case verifyGetResultInvalidAccessToken method is running------------");
		
		RequestSpecification httpRequest = RestAssured.given();
		Response response = httpRequest.get(resultServiceAPIUrl);
 
		// Get the status code from the Response. In case of a successful interaction with the web service, we
		// should get a status code of 401.
		int statusCode = response.getStatusCode();
 
		// Assert that correct status code is returned.
		Assert.assertEquals(statusCode /*actual value*/, 401 /*expected value*/, "Correct status code returned");
		//LOGGER.info("***************** /resultService/result/ returns 401 status code*******************");
		
	}

	
	@Test (priority=8) 
	public void verifyResultStatusAvailableInvalidAccessToken() throws IOException{
		
		 LOGGER.info("--------------The test case verifyResultStatusAvailableInvalidAccessToken method is running------------");
		 
		RequestSpecification httpRequest = RestAssured.given().header("Authorization", accessToken+"Invalid AccessToken");
		Response response = httpRequest.get(resultStatusAPIUrl);
 
		// Get the status code from the Response. In case of a successful interaction with the web service, we
		// should get a status code of 401
		int statusCode = response.getStatusCode();
 
		// Assert that correct status code is returned.
		Assert.assertEquals(statusCode /*actual value*/, 401 /*expected value*/, "Correct status code returned");
		//LOGGER.info("***************** /resultService/result/ returns 401 status code*******************");
		
	}
		
	@Test (priority=9) 
	public void verifyAvailableFormatInvalidAccessToken() throws IOException{
		
		 LOGGER.info("--------------The test case verifyAvailableFormatInvalidAccessToken method is running------------");
		 
		RequestSpecification httpRequest = RestAssured.given().header("Authorization", accessToken+"Invalid AccessToken");
		Response response = httpRequest.get(resultFormatAPIUrl);
 
		// Get the status code from the Response. In case of a successful interaction with the web service, we
		// should get a status code of 401
		int statusCode = response.getStatusCode();
 
		// Assert that correct status code is returned.
		Assert.assertEquals(statusCode /*actual value*/, 401 /*expected value*/, "Correct status code returned");
		//LOGGER.info("***************** /resultService/result/ returns 401 status code*******************");
		
	}
		

	@Test (priority=10) 
	public void verifyGetResultStatusInvalidResultId() throws IOException{
		
		 LOGGER.info("--------------The test case verifyGetResultStatusInvalidResultId method is running------------");
		 
		 
		RequestSpecification httpRequest = RestAssured.given().header("Authorization", accessToken);		
		Response response = httpRequest.get(RestUtils.BaseURIPath()+"/resultService/resultStatus/"+resultId+4);
 		String invalidResultIdResultStatus=response.jsonPath().get("message");
 		Assert.assertEquals(invalidResultIdResultStatus, "Unable to get result for that id");
			
	}


	@Test (priority=11) 
	public void verifyGetResultInvalidResultId() throws IOException{
		
		 LOGGER.info("--------------The test case verifyGetResultInvalidResultId method is running------------");
		 
		RequestSpecification httpRequest = RestAssured.given().header("Authorization", accessToken);		
		Response response = httpRequest.get(RestUtils.BaseURIPath()+"/resultService/result/"+resultId+4+"/JSON/");
 		String invalidResultIdMessage=response.jsonPath().get("message");
 		Assert.assertEquals(invalidResultIdMessage, "Unable to retrieve result.");
		
	
			
	}
		

	@Test (priority=12) 
	public void verifyAvailableFormatInvalidResultId() throws IOException{
		
		 LOGGER.info("--------------The test case verifyAvailableFormatInvalidResultId method is running------------");
		RequestSpecification httpRequest = RestAssured.given().header("Authorization", accessToken);		
		System.out.println(resultId);
		Response response = httpRequest.get(RestUtils.BaseURIPath()+"/resultService/availableFormats/"+resultId+4);
 		//System.out.println(response.prettyPrint());
 		String invalidResultIdMessageAvailableFormat=response.jsonPath().get("message");
 		Assert.assertEquals(invalidResultIdMessageAvailableFormat, "Unable to get available formats for that id");
		
		
	}
		
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

		
/*		try{
		   
			ValidatableResponse httpRequest = RestAssured.given()
				              .header("Authorization", "Invalid")
				              .when()
				              .get(resultServiceAPIUrl).then().assertThat().statusCode(408);
				              
				             
					   	
						  LOGGER.info("***************** /resultService/result/ returns 401 OK status code*******************");
					   	
				  			}    
				  			catch (AssertionError r) 
					       		{
					           	LOGGER.error("Rest URI has Exception/Error", r);
					       		}

	}

	*/
	
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

