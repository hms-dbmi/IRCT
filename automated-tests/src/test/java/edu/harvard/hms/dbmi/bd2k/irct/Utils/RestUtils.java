package edu.harvard.hms.dbmi.bd2k.irct.Utils;

import org.testng.annotations.Test;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class RestUtils {
    //Global Setup Variables

	public static Response path; //Rest request path
    /*
    ***Sets Base URI***
    Before starting the test, we should set the RestAssured.baseURI
    */
 /*   public static void setBaseURIAccessToken (){
       //RestAssured.baseURI = baseURI;
        //System.err.println();
        //RestAssured.baseURI = Systiem.getProperty("path");    //Getting  the value from pom.xml
    	String baseURI=System.getProperty("path");
 
        }*/
    
      public static String BaseURIPath()
      {
    	  String baseURI=System.getProperty("basepath");
    	  return baseURI;
          
      }/*
      public static String queryServiceBaseURI()
      {
    	  String baseURI=System.getProperty("pathquery");
    	  return baseURI;
          
      }*/
      
      public static String AccessToken()
      {
          String accessTokenPom = System.getProperty("accessToken");
    	  return accessTokenPom;
          
      }

    

    /*
    ***Sets base path***
    Before starting the test, we should set the RestAssured.basePath
    */
    public static void setBasePath(String basePathTerm){
        RestAssured.basePath = basePathTerm;
    }

    /*
    ***Reset Base URI (after test)***
    After the test, we should reset the RestAssured.baseURI
    */
    public static void resetBaseURI (){
        RestAssured.baseURI = null;
    }

    /*
    ***Reset base path (after test)***
    After the test, we should reset the RestAssured.basePath
    */
    public static void resetBasePath(){
        RestAssured.basePath = null;
    }

    /*
    ***Sets ContentType***
    We should set content type as JSON or XML before starting the test
    */
    
    public static void setContentType(ContentType Type)
    {
    	RestAssured.given().contentType(Type);
    }
    
    
    /*
        ***Returns response***
    We send "path" as a parameter to the Rest Assured'a "get" method
    and "get" method returns response of API
    */
    public static Response getResponse() {
        //System.out.print("path: " + path +"\n");
//        return get(path);
        return (path);
    }

    /*
    ***Returns JsonPath object***
    * First convert the API's response to String type with "asString()" method.
    * Then, send this String formatted json response to the JsonPath class and return the JsonPath
    */
    public static JsonPath getJsonPath (Response res) {
        String json = res.asString();
        //System.out.print("returned json: " + json +"\n");
        return new JsonPath(json);
    }
}


