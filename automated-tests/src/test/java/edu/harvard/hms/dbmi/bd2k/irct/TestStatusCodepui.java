package edu.harvard.hms.dbmi.bd2k.irct;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ResponseBodyData;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

import java.util.List;
public class TestStatusCodepui {
  
	 @BeforeClass
	  public void setBaseUri () {

	    RestAssured.baseURI = "http://nhanes.hms.harvard.edu/rest/v1/resourceService/path";
	  }
	  		
		@Test
		public void statuscodecheckpui() {
		    
			
			
			// get("http://ergast.com/api/f1/2017/circuits.json").then().
		        //assertThat().body("MRData.CircuitTable.Circuits.circuitId",hasSize(20));
		        //body("circuitId", null);
		        //statusCode(200).log().all();
		   //Response res=(Response) given().header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0fGF2bGJvdEBkYm1pLmhtcy5oYXJ2YXJkLmVkdSIsImVtYWlsIjoiYXZsYm90QGRibWkuaG1zLmhhcnZhcmQuZWR1In0.51TYsm-uw2VtI8aGawdggbGdCSrPJvjtvzafd2Ii9NU").when().get("/path/nhanes/Demo/demographics/demographics").then().assertThat().body("pui[0]",equalToIgnoringCase("/nhanes/Demo/demographics/demographics/AGE/")).log().all();
		        
		    //given().header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0fGF2bGJvdEBkYm1pLmhtcy5oYXJ2YXJkLmVkdSIsImVtYWlsIjoiYXZsYm90QGRibWkuaG1zLmhhcnZhcmQuZWR1In0.51TYsm-uw2VtI8aGawdggbGdCSrPJvjtvzafd2Ii9NU").when().get("http://nhanes.hms.harvard.edu/rest/v1/resourceService/path/nhanes/Demo/demographics/demographics").then().extract();
			
			

			//int chatNumber = JsonPath.with(myBlob).get("guest.chatNumber");
			//Response res=(Response)given().header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0fGF2bGJvdEBkYm1pLmhtcy5oYXJ2YXJkLmVkdSIsImVtYWlsIjoiYXZsYm90QGRibWkuaG1zLmhhcnZhcmQuZWR1In0.51TYsm-uw2VtI8aGawdggbGdCSrPJvjtvzafd2Ii9NU").when().get("/path/nhanes/Demo/demographics/demographics").then().extract().path("pui[]");
			//System.out.println(((ResponseBodyData) res).asString());
						//String s=res.path("pui[i]");
			//String a = res.asString();
			//System.out.println(a);
//*******/nhanes/Demo/demographics/demographics********//
			Response res=(Response)given().header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0fGF2bGJvdEBkYm1pLmhtcy5oYXJ2YXJkLmVkdSIsImVtYWlsIjoiYXZsYm90QGRibWkuaG1zLmhhcnZhcmQuZWR1In0.51TYsm-uw2VtI8aGawdggbGdCSrPJvjtvzafd2Ii9NU").when()
				     .get("/nhanes/Demo/demographics/demographics")
				     .then()
				     .assertThat()
				     .statusCode(200)
				     .assertThat()
				     .extract().response();
			 
			 System.out.println(res.asString());
			 
			 List<String> p=res.getBody().jsonPath().getList("pui");
			 System.out.println(p.size());
			 for (int i=0;i<p.size();i++)
			 {
			 System.out.println(p.get(i));
			 
			 
			 given().header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0fGF2bGJvdEBkYm1pLmhtcy5oYXJ2YXJkLmVkdSIsImVtYWlsIjoiYXZsYm90QGRibWkuaG1zLmhhcnZhcmQuZWR1In0.51TYsm-uw2VtI8aGawdggbGdCSrPJvjtvzafd2Ii9NU").when().get(p.get(i)).then().statusCode(200).log().all();
			 
			 }
		}
  }

