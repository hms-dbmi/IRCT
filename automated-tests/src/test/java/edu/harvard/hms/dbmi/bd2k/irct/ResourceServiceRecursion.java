package edu.harvard.hms.dbmi.bd2k.irct;

import static io.restassured.RestAssured.given;

import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class ResourceServiceRecursion 
{
  
String path = "http://nhanes.hms.harvard.edu/rest/v1/resourceService/path";
	
@Test
  public void recursionpath(String path) 
		{
	//String path = "http://nhanes.hms.harvard.edu/rest/v1/resourceService/path";
    	
	Response res1=(Response)given().header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0fGF2bGJvdEBkYm1pLmhtcy5oYXJ2YXJkLmVkdSIsImVtYWlsIjoiYXZsYm90QGRibWkuaG1zLmhhcnZhcmQuZWR1In0.51TYsm-uw2VtI8aGawdggbGdCSrPJvjtvzafd2Ii9NU").when()
		     .get(path)
		     .then()
		     .assertThat()
		     .statusCode(200)
		     .assertThat()
		     .extract().response();
	 
	 System.out.println(res1.asString());

		
		}


}
