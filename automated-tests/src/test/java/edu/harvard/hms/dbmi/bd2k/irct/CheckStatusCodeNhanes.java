package edu.harvard.hms.dbmi.bd2k.irct;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import junit.framework.Assert;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

import java.util.List;

@SuppressWarnings("unused")

public class CheckStatusCodeNhanes
{
    
	
	String endpoint=System.getProperty("path");	
	public static int count;
	
	@Test (timeOut = 30000000 )
	public void getpathaccesstoken()
				{
			
					String accesstoken=System.getProperty("accesstoken");
					
					//System.out.println(endpoint);
					//System.out.println(accesstoken);
					checkcodegetpuis(endpoint, accesstoken);
				}
	
	
	public void checkcodegetpuis(String puipath,String puiaccesstoken) 
					{
						
		try{
	//					System.out.println("*****************pui path is ************"   +puipath);
						given().header("Authorization", puiaccesstoken).when().get(puipath).then().statusCode(200).log().ifError();
		}
		
		catch(Error e)
		{
			System.out.println("Error Occured");
		}
		
						Response res=(Response)given().header("Authorization", puiaccesstoken).when()
										     .get(puipath)
										     .then()				          
										     .extract().response();
						
						List<String> pui=res.getBody().jsonPath().getList("pui");
		
						count++;
					
						System.out.println("===========================PUIS======================================="+count);
				
						
									if (pui==null || pui.size()==0)
										{
											return;	
										}
									
									for (int i=0;i<pui.size();i++)
										 {
											
											String childpath=endpoint+pui.get(i);
										System.out.println("*********************PUI child path is *******************\n"+childpath);
											checkcodegetpuis(childpath,puiaccesstoken);
										
										
								}	 		 
						
				System.out.println("----------------------------Number of puis--------------------------"+count);
										 
						}

		
	
	
}
	
