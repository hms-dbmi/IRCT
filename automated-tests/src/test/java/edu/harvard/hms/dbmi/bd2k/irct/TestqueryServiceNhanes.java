
package edu.harvard.hms.dbmi.bd2k.irct;

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


import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

import org.apache.bcel.classfile.Constant;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

 * CheckStatusCodeNhanes.java class which check the response of End Points(PUIs),validate
 * it and counts the number of Puis under base project.  
 * @author Atul 
 * @Version 1.0	 */


public class TestqueryServiceNhanes
{
	//RestAssured.registerParser("text/plain", Parser.JSON);
	private static final Logger LOGGER = Logger.getLogger( TestqueryServiceNhanes.class.getName() );
    String baseUri;
    String accessToken;
    //RestAssured.defaultParser = Parser.JSON;
    
    /**
     * Retrieve the value of endpoint (baseURI) from pom.xml
     */
    
   @BeforeMethod
    public void setup()
    {
	   baseUri=RestUtils.BaseURIPath();
	   accessToken=RestUtils.AccessToken();
	   RestUtils.setContentType(ContentType.JSON);
    }
   
  
 public String generateStringFromResource(String path) throws IOException {

	    return new String(Files.readAllBytes(Paths.get(path)));

	}
   
 @Test  
public void runQuery() throws IOException{
  String APIUrl = baseUri+"queryService/runQuery/";
  System.out.println(APIUrl);
  String jsonBody = generateStringFromResource("/src/main/resources/Search.json");

  //RestAssured.registerParser("text/plain", Parser.TEXT);
  given()
  		.contentType("application/json")
  		.header("Authorization", accessToken)
  		.body(new HashMap<String, Object>() 
  		{{
  			put("Select", new HashMap<String, Object>()
  			
  			{{
  					put("field", new HashMap<String, Object>()
  						{{
				  		     put("pui", "/nhanes/Demo/laboratory/laboratory/pcbs/");
					         put("dataType", "STRING");
				         }});
  					put("alias", "pcb153");
  				}});
  			
  		}}).
  when().
  		post(APIUrl).
  then().
   		body("ResultId",  notNullValue());
  		//statusCode(200);
 }
	/*@Test
		public void httpPost() throws InterruptedException {
			
			//Initializing Rest API's URL
			String APIUrl = baseUri+"queryService/runQuery/";
			System.out.println(APIUrl);
			
			//Initializing payload or API body
			String APIBody = "{API Body}"; 
						
					// Building request using requestSpecBuilder
			RequestSpecBuilder builder = new RequestSpecBuilder();
			
			//Setting API's body
			builder.setBody(APIBody);
				
			//Setting content type as application/json or application/xml
			builder.setContentType("application/json; charset=UTF-8");
				
			RequestSpecification requestSpec = builder.build();

			//Making post request with authentication, leave blank in case there are no credentials- basic("","")
			Response response=given();
					
			Response response = given().authentication().preemptive().basic({username}, {password})
						.spec(requestSpec).when().post(APIUrl);
			JSONObject JSONResponseBody = new JSONObject(response.body().asString());

			//Fetching the desired value of a parameter
//			String result = JSONResponseBody.getString({key});
				
			//Asserting that result 
	//		Assert.assertEquals(result, "{expectedValue}");

			}
*/
}			




