/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.scidb.examples;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;

import edu.harvard.hms.dbmi.scidb.SciDB;
import edu.harvard.hms.dbmi.scidb.SciDBArray;
import edu.harvard.hms.dbmi.scidb.SciDBListElement;
import edu.harvard.hms.dbmi.scidb.exception.NotConnectedException;

/**
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class ListFunctions {

	public static void main(String[] args) {
		String[] argNames = new String[] {"first", "second", "third", "fourth", "fifth"};
		
		System.out.println("List Example");
		SciDB sciDB = new SciDB();
		sciDB.connect("http://192.168.56.101:8080");

		try {
			System.out.println(sciDB.version());
			String queryId = sciDB.executeQuery(sciDB.list(SciDBListElement.ARRAYS), "csv");
			System.out.println("Query Id: " + queryId);
			System.out.println(inputStreamToString(sciDB.readLines()));
			//			String[] rawLines = inputStreamToString(sciDB.readLines()).split("\n");
			
//			System.out.println(rawLines.length);
//			for(String rawLine : rawLines) {
//				if(rawLine.contains("iif(")) {
//					rawLine = "'iif','<any> iif(bool,<any>,<any>)',true,'scidb'";
//				}
//				String[] parts = rawLine.split("\',");
//				String call = parts[1].split(" ")[1];
//				String functionName = call.split("\\(")[0];
//
//				String[] functionParams = call.substring(call.indexOf('(') + 1, call.indexOf(')')).split(",");
//
//				if(functionParams[0].isEmpty()) {
//					functionParams = new String[] {};
//				}
//				
//				System.out.print("public static SciDBFunction " + functionName + "(");
//				
//				String createCall = "\t create(\"" + functionName + "\"";
//				for(int i = 0; i <= functionParams.length - 1; i++) {
//					String functionType = functionParams[i];
//					String argName = argNames[i];
//					if(i == 0) {
//						System.out.print("String " + argName);
//						createCall += ", " + argName;
//					} else {
//						System.out.print(", " + functionType.substring(0, 1).toUpperCase() + functionType.substring(1) + " " + argName);
//						if(functionType.equalsIgnoreCase("string")) {
//							createCall += ", " + argName;	
//						} else {
//							createCall += ", " + functionType.toUpperCase() + ".toString(" + argName + ")";
//						}
//					}
//					
//				}
//				createCall += ")";
//				
////				for(String functionParam : functionParams) {
////					System.out.print(" " + functionParam);
////				}
				
				
//				System.out.print(") {\n");
//				System.out.println("\treturn create(\"" + functionName + "\", first, SOMETHING.toString(second))");
//				System.out.println(createCall);
//				System.out.print("}\n");
//			}
//			CSVParser parser = new CSVParser(new InputStreamReader(sciDB.readLines()), CSVFormat.TDF);
//			for (CSVRecord csvRecord : parser) {
//				String name = csvRecord.get(1);
//				name = name.substring(1, name.length());
//			     System.out.println(name);
//			 }
			
//			queryId = sciDB.executeQuery(sciDB.show(new SciDBArray("IHI_ACCELEROMETER")), "csv");
//			System.out.println("Query Id: " + queryId);
//			System.out.println(inputStreamToString(sciDB.readLines()));
			
			System.out.println(sciDB.releaseSession());
		} catch (NotConnectedException | IOException e) {
			e.printStackTrace();
		}

		sciDB.close();
	}
	
	
	private static String inputStreamToString(InputStream inputStream) throws IOException {
		StringWriter writer = new StringWriter();
		IOUtils.copy(inputStream, writer, Charset.defaultCharset());
		inputStream.close();
		return writer.toString();
	}

}
