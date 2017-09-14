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
public class ResourceServiceStatusCode {
  
	 @BeforeClass
	  public void setBaseUri () {

	    RestAssured.baseURI = "http://nhanes.hms.harvard.edu/rest/v1/resourceService/path";
	  }
	  		
		@Test
		public void statuscodecheckpuitest1() {
		    

			//Verifying status code for URI and all PUIs of the response *******/nhanes/********//
			
			Response res1=(Response)given().header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0fGF2bGJvdEBkYm1pLmhtcy5oYXJ2YXJkLmVkdSIsImVtYWlsIjoiYXZsYm90QGRibWkuaG1zLmhhcnZhcmQuZWR1In0.51TYsm-uw2VtI8aGawdggbGdCSrPJvjtvzafd2Ii9NU").when()
				     .get("/nhanes/")
				     .then()
				     .assertThat()
				     .statusCode(200)
				     .assertThat()
				     .extract().response();
			 
			 System.out.println(res1.asString());
			 
			 List<String> p1=res1.getBody().jsonPath().getList("pui");
			 
			 System.out.println("Number of puis in response :"+p1.size());
			 
			 for (int i=0;i<p1.size();i++)
			 {
			 System.out.println("PUI    :" +p1.get(i));
			 				 
			 given().header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0fGF2bGJvdEBkYm1pLmhtcy5oYXJ2YXJkLmVkdSIsImVtYWlsIjoiYXZsYm90QGRibWkuaG1zLmhhcnZhcmQuZWR1In0.51TYsm-uw2VtI8aGawdggbGdCSrPJvjtvzafd2Ii9NU").when().get(p1.get(i)).then().statusCode(200).log().all();
			 
			 }


			
//Verifying status code for URI and all PUIs of the response *******/nhanes/Demo********//
	Response res2=(Response)given().header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0fGF2bGJvdEBkYm1pLmhtcy5oYXJ2YXJkLmVkdSIsImVtYWlsIjoiYXZsYm90QGRibWkuaG1zLmhhcnZhcmQuZWR1In0.51TYsm-uw2VtI8aGawdggbGdCSrPJvjtvzafd2Ii9NU").when()
					     .get("/nhanes/Demo")
					     .then()
					     .assertThat()
					     .statusCode(200)
					     .assertThat()
					     .extract().response();
				 
				 System.out.println(res2.asString());
				 
				 List<String> p2=res2.getBody().jsonPath().getList("pui");
				 System.out.println("Number of PUis in /nhanes/demo" + p2.size());
				 for (int i=0;i<p2.size();i++)
				 {
				 System.out.println("PUI in /nhanes/Demo          "+ p2.get(i));
				 				 
				 given().header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0fGF2bGJvdEBkYm1pLmhtcy5oYXJ2YXJkLmVkdSIsImVtYWlsIjoiYXZsYm90QGRibWkuaG1zLmhhcnZhcmQuZWR1In0.51TYsm-uw2VtI8aGawdggbGdCSrPJvjtvzafd2Ii9NU").when().get(p2.get(i)).then().statusCode(200).log().all();
				 
				 }

//Verifying status code for URI and all PUIs of the response *******/nhanes/Demo/demographics/demographics********//
					Response dg=(Response)given().header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0fGF2bGJvdEBkYm1pLmhtcy5oYXJ2YXJkLmVkdSIsImVtYWlsIjoiYXZsYm90QGRibWkuaG1zLmhhcnZhcmQuZWR1In0.51TYsm-uw2VtI8aGawdggbGdCSrPJvjtvzafd2Ii9NU").when()
						     .get("/nhanes/Demo/demographics/demographics")
						     .then()
						     .assertThat()
						     .statusCode(200)
						     .assertThat()
						     .extract().response();
					 
					 System.out.println(dg.asString());
					 
					 List<String> p3=dg.getBody().jsonPath().getList("pui");
					 System.out.println("Number of puis in /nhanes/Demo/demographics/demographics"+p3.size());
					 for (int i=0;i<p3.size();i++)
					 {
					
						 System.out.println("PUI in /nhanes/Demo/Demographics/Demographics/          "+ p3.get(i));
					 
					 
					 given().header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0fGF2bGJvdEBkYm1pLmhtcy5oYXJ2YXJkLmVkdSIsImVtYWlsIjoiYXZsYm90QGRibWkuaG1zLmhhcnZhcmQuZWR1In0.51TYsm-uw2VtI8aGawdggbGdCSrPJvjtvzafd2Ii9NU").when().get(p3.get(i)).then().statusCode(200).log().all();
					 
					 }
					
				
	//Verifying status code for URI and all PUIs of the response *******/nhanes/Demo/examination/examination/********//
						Response ex=(Response)given().header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0fGF2bGJvdEBkYm1pLmhtcy5oYXJ2YXJkLmVkdSIsImVtYWlsIjoiYXZsYm90QGRibWkuaG1zLmhhcnZhcmQuZWR1In0.51TYsm-uw2VtI8aGawdggbGdCSrPJvjtvzafd2Ii9NU").when()
							     .get("nhanes/Demo/examination/examination/")
							     .then()
							     .assertThat()
							     .statusCode(200)
							     .assertThat()
							     .extract().response();
						 
						 System.out.println(ex.asString());
						 
						 List<String> p4=dg.getBody().jsonPath().getList("pui");
						 System.out.println("Number of puis in nhanes/Demo/examination/examination/"+p4.size());
						 for (int i=0;i<p4.size();i++)
						 {
						
							 System.out.println("PUI in nhanes/Demo/examination/examination/          "+ p4.get(i));
						 
						 
						 given().header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0fGF2bGJvdEBkYm1pLmhtcy5oYXJ2YXJkLmVkdSIsImVtYWlsIjoiYXZsYm90QGRibWkuaG1zLmhhcnZhcmQuZWR1In0.51TYsm-uw2VtI8aGawdggbGdCSrPJvjtvzafd2Ii9NU").when().get(p4.get(i)).then().statusCode(200).log().all();
						 
						 }			 
						 
	//Verifying status code for URI and all PUIs of the response *******/nhanes/Demo/laboratory/laboratory/********//
							Response lb=(Response)given().header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0fGF2bGJvdEBkYm1pLmhtcy5oYXJ2YXJkLmVkdSIsImVtYWlsIjoiYXZsYm90QGRibWkuaG1zLmhhcnZhcmQuZWR1In0.51TYsm-uw2VtI8aGawdggbGdCSrPJvjtvzafd2Ii9NU").when()
								     .get("/nhanes/Demo/laboratory/laboratory/")
								     .then()
								     .assertThat()
								     .statusCode(200)
								     .assertThat()
								     .extract().response();
							 
							 System.out.println(ex.asString());
							 
							 List<String> p5=dg.getBody().jsonPath().getList("pui");
							 System.out.println("Number of puis in /nhanes/Demo/laboratory/laboratory/"+p5.size());
							 for (int i=0;i<p5.size();i++)
							 {
							
								 System.out.println("PUI in /nhanes/Demo/laboratory/laboratory/          "+ p5.get(i));
							 
							 
							 given().header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0fGF2bGJvdEBkYm1pLmhtcy5oYXJ2YXJkLmVkdSIsImVtYWlsIjoiYXZsYm90QGRibWkuaG1zLmhhcnZhcmQuZWR1In0.51TYsm-uw2VtI8aGawdggbGdCSrPJvjtvzafd2Ii9NU").when().get(p5.get(i)).then().statusCode(200).log().all();
							 
							 }			 	 
						 		 
					 

					 
		}
  }

