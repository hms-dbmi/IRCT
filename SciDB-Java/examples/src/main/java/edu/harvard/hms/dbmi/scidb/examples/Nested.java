package edu.harvard.hms.dbmi.scidb.examples;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

import edu.harvard.hms.dbmi.scidb.SciDB;
import edu.harvard.hms.dbmi.scidb.SciDBArray;
import edu.harvard.hms.dbmi.scidb.SciDBFilterFactory;
import edu.harvard.hms.dbmi.scidb.SciDBFunction;
import edu.harvard.hms.dbmi.scidb.SciDBFunctionFactory;
import edu.harvard.hms.dbmi.scidb.exception.NotConnectedException;

public class Nested {

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		System.out.println("Nested Example");
		SciDB sciDB = new SciDB();
		sciDB.connect("http://192.168.56.101:8080");

		String arrayName = "IHI_ACCELEROMETER";

		SciDBFunction whereOperation = SciDBFilterFactory.greaterThan("acc_x", 100);
		SciDBFunction whereOperation2 = SciDBFilterFactory.equal("subject", 2);
		try {
			String queryId = sciDB.executeQuery(
					sciDB.project(sciDB.filter(sciDB.filter(new SciDBArray(arrayName), whereOperation), whereOperation2),
					"acc_x", "acc_y", "acc_z"), "dcsv");
			System.out.println("Query Id: " + queryId);
			String[] rawLines = inputStreamToString(sciDB.readLines()).split("\n");
			for(int i = 0; i <= 25; i++) {
				System.out.println(rawLines[i]);
			}
		} catch (NotConnectedException | IOException e) {
			e.printStackTrace();
		}
		long stopTime = System.currentTimeMillis();
		System.out.println("Time: " + (stopTime - startTime));
		sciDB.close();
	}

	private static String inputStreamToString(InputStream inputStream)
			throws IOException {
		StringWriter writer = new StringWriter();
		IOUtils.copy(inputStream, writer, Charset.defaultCharset());
		inputStream.close();
		return writer.toString();
	}
}
