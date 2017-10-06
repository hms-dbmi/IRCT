
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

 * NhanesTestqueryService.java class which covers the functionalities - resultStatus,AvailableFormats, 
 * @author Atul 
 * @Version 1.0	 */


public class NhanesTestResultService
{
	//RestAssured.registerParser("text/plain", Parser.JSON);
	private static final Logger LOGGER = Logger.getLogger( NhanesTestResultService.class.getName() );
    String APIUrl;
    String accessToken;
     String resultId="23232";
    //RestAssured.defaultParser = Parser.JSON;
    
    /**
     * Retrieve the value of endpoint (baseURI) from pom.xml
     */
    
   @BeforeMethod
    public void setup()
    {
	   APIUrl=RestUtils.BaseURIPath()+"/resultService/"+resultId+"/CSV";;
	   System.out.println(APIUrl);
	   accessToken=RestUtils.AccessToken();
	   RestUtils.setContentType(ContentType.JSON);
    }
   
  
    
 @Test  
public void getresultId() throws IOException{
  
}
 
 @Test  
 public void getAvailalbleFormats() throws IOException{
   
 }
  
 
 @Test  
 public void getResultStatus() throws IOException{
   
 }
}