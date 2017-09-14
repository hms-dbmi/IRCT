package edu.harvard.hms.dbmi.bd2k.irct;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import io.restassured.RestAssured;
import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;



@SuppressWarnings("unused")

public class TestStatusCodes {
  
/*@BeforeTest
    public void setUp() {
		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
		RestAssured.authentication=preemptive().basic(userName, password);
}
*/        
        
@Test
  public void statusCodePath() 
{
	//given().when().get("http://restcountries.eu/rest/v1/name/austria").then().statusCode(200).log().all();
	//
	//given().auth().oauth2("Authorization:Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0fGF2bGJvdEBkYm1pLmhtcy5oYXJ2YXJkLmVkdSIsImVtYWlsIjoiYXZsYm90QGRibWkuaG1zLmhhcnZhcmQuZWR1In0.51TYsm-uw2VtI8aGawdggbGdCSrPJvjtvzafd2Ii9NU").get("http://nhanes.hms.harvard.edu/rest/v1/resourceService/path").then().statusCode(200).log().all();
	
	given().header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0fGF2bGJvdEBkYm1pLmhtcy5oYXJ2YXJkLmVkdSIsImVtYWlsIjoiYXZsYm90QGRibWkuaG1zLmhhcnZhcmQuZWR1In0.51TYsm-uw2VtI8aGawdggbGdCSrPJvjtvzafd2Ii9NU").when().get("http://nhanes.hms.harvard.edu/rest/v1/resourceService/path").then().statusCode(200).log().all();
	

}

@Test
public void statusCodePathNhanes() 
{
	given().header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0fGF2bGJvdEBkYm1pLmhtcy5oYXJ2YXJkLmVkdSIsImVtYWlsIjoiYXZsYm90QGRibWkuaG1zLmhhcnZhcmQuZWR1In0.51TYsm-uw2VtI8aGawdggbGdCSrPJvjtvzafd2Ii9NU").when().get("http://nhanes.hms.harvard.edu/rest/v1/resourceService/path/nhanes").then().statusCode(200).log().all();
	

}

@Test
public void statusCodeDemo() 
{
	
	given().header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0fGF2bGJvdEBkYm1pLmhtcy5oYXJ2YXJkLmVkdSIsImVtYWlsIjoiYXZsYm90QGRibWkuaG1zLmhhcnZhcmQuZWR1In0.51TYsm-uw2VtI8aGawdggbGdCSrPJvjtvzafd2Ii9NU").when().get("http://nhanes.hms.harvard.edu/rest/v1/resourceService/path/nhanes/Demo").then().statusCode(200);
	

}

@Test
public void statusCodeDemographics() 
{
	
	given().header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0fGF2bGJvdEBkYm1pLmhtcy5oYXJ2YXJkLmVkdSIsImVtYWlsIjoiYXZsYm90QGRibWkuaG1zLmhhcnZhcmQuZWR1In0.51TYsm-uw2VtI8aGawdggbGdCSrPJvjtvzafd2Ii9NU").when().get("http://nhanes.hms.harvard.edu/rest/v1/resourceService/path/nhanes/Demo/demographics/demographics").then().statusCode(200);
	

}			
@Test
public void statusCodeExamination() 
{
	
	given().header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0fGF2bGJvdEBkYm1pLmhtcy5oYXJ2YXJkLmVkdSIsImVtYWlsIjoiYXZsYm90QGRibWkuaG1zLmhhcnZhcmQuZWR1In0.51TYsm-uw2VtI8aGawdggbGdCSrPJvjtvzafd2Ii9NU").when().get("http://nhanes.hms.harvard.edu/rest/v1/resourceService/path/nhanes/Demo/examination/examination").then().statusCode(200);
	

}			



}
