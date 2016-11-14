package edu.harvard.hms.dbmi.scidb.examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import edu.harvard.hms.dbmi.scidb.SciDB;
import edu.harvard.hms.dbmi.scidb.SciDBArray;
import edu.harvard.hms.dbmi.scidb.SciDBFilterFactory;
import edu.harvard.hms.dbmi.scidb.SciDBFunction;
import edu.harvard.hms.dbmi.scidb.exception.NotConnectedException;

public class Nested {

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		System.out.println("Nested Example");
		SciDB sciDB = new SciDB();

		String arrayName = "IHI_ACCELEROMETER";

		SciDBFunction whereOperation = SciDBFilterFactory.greaterThan("acc_x", 100);
		SciDBFunction whereOperation2 = SciDBFilterFactory.equal("subject", 2);
		try {
			String queryId = sciDB.executeQuery(
					sciDB.project(sciDB.filter(sciDB.filter(new SciDBArray(arrayName), whereOperation), whereOperation2),
					"acc_x", "acc_y", "acc_z"), "dcsv");
			System.out.println("Query Id: " + queryId);
			BufferedReader in = new BufferedReader(
					new InputStreamReader(sciDB.readLines()));
			String line = null;
			while ((line = in.readLine()) != null) {
				System.out.println(line);
			}
			
		} catch (NotConnectedException | IOException e) {
			e.printStackTrace();
		}
		long stopTime = System.currentTimeMillis();
		System.out.println("Time: " + (stopTime - startTime));
		sciDB.close();
	}
}
