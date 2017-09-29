
package edu.harvard.hms.dbmi.bd2k.irct;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.LogConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import junit.framework.Assert;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

import org.apache.bcel.classfile.Constant;
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


public class CheckStatusCodeNhanesRunQuery
{
    
	private static final Logger LOGGER = Logger.getLogger( CheckStatusCodeNhanesRunQuery.class.getName() );
	

	
	@SuppressWarnings("resource")
	
@Test

	
	
		public void httpPost() throws InterruptedException {
			
			//Initializing Rest API's URL
			String APIUrl = "http://{API URL}";
				
			//Initializing payload or API body
			String APIBody = "{API Body}"; //e.g.- "{\"key1\":\"value1\",\"key2\":\"value2\"}"
						
			// Building request using requestSpecBuilder
			RequestSpecBuilder builder = new RequestSpecBuilder();
				
			//Setting API's body
			builder.setBody(APIBody);
				
			//Setting content type as application/json or application/xml
			builder.setContentType("application/json; charset=UTF-8");
				
			RequestSpecification requestSpec = builder.build();

			//Making post request with authentication, leave blank in case there are no credentials- basic("","")
/*			Response response=given();
					
			Response response = given().authentication().preemptive().basic({username}, {password})
						.spec(requestSpec).when().post(APIUrl);
			JSONObject JSONResponseBody = new JSONObject(response.body().asString());
*/
			//Fetching the desired value of a parameter
//			String result = JSONResponseBody.getString({key});
				
			//Asserting that result of Norway is Oslo
	//		Assert.assertEquals(result, "{expectedValue}");

			}

}			




