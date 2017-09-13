package edu.harvard.hms.dbmi.bd2k.irct;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import junit.framework.Assert;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;



@SuppressWarnings("unused")

public class ReadingSystemVariable 
{
         
	@Test
	  public void statusCode_path() 
	{
		 String pathtest=System.getProperty("path");	
		 String accesstokentest=System.getProperty("accesstoken");
		  System.out.println(pathtest);
		  System.out.println(accesstokentest);
	Response res1=(Response)given().header("Authorization", accesstokentest).when()
				     .get(pathtest)
				     .then()
				     .assertThat()
				     .statusCode(200)				     
				     .extract().response();
		
		//  Response res1=(Response) given().header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0fGF2bGJvdEBkYm1pLmhtcy5oYXJ2YXJkLmVkdSIsImVtYWlsIjoiYXZsYm90QGRibWkuaG1zLmhhcnZhcmQuZWR1In0.51TYsm-uw2VtI8aGawdggbGdCSrPJvjtvzafd2Ii9NU").when().get(pathtest).then().statusCode(200);
//		  int statuscode=res1.statusCode();
	//	  Assert.assertEquals(statuscode, 200);

  }
	



}
