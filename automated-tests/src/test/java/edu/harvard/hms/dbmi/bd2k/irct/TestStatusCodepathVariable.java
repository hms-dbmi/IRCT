package edu.harvard.hms.dbmi.bd2k.irct;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import io.restassured.RestAssured;
import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;


@SuppressWarnings("unused")

public class TestStatusCodepathVariable {

	  
		 @BeforeClass
		  public void setBaseUri () {

		    RestAssured.baseURI = "http://nhanes.hms.harvard.edu/rest/v1/resourceService";
		  }

       
@Test
  public void statusCodepath() 
{
	
	String path = "/path";
	
	given().header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0fGF2bGJvdEBkYm1pLmhtcy5oYXJ2YXJkLmVkdSIsImVtYWlsIjoiYXZsYm90QGRibWkuaG1zLmhhcnZhcmQuZWR1In0.51TYsm-uw2VtI8aGawdggbGdCSrPJvjtvzafd2Ii9NU").when().get(path).then().statusCode(200).log().all();
	

}


}
