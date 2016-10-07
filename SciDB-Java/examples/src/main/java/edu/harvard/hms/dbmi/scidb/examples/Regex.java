package edu.harvard.hms.dbmi.scidb.examples;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

import edu.harvard.hms.dbmi.scidb.SciDB;
import edu.harvard.hms.dbmi.scidb.SciDBArray;
import edu.harvard.hms.dbmi.scidb.SciDBCommand;
import edu.harvard.hms.dbmi.scidb.SciDBDataType;
import edu.harvard.hms.dbmi.scidb.SciDBDimension;
import edu.harvard.hms.dbmi.scidb.SciDBFunction;
import edu.harvard.hms.dbmi.scidb.SciDBFunctionFactory;
import edu.harvard.hms.dbmi.scidb.SciDBListElement;
import edu.harvard.hms.dbmi.scidb.exception.NotConnectedException;

public class Regex {

	public static void main(String[] args) {
		System.out.println("inegbinomcdf Example");
		SciDB sciDB = new SciDB();
		sciDB.connect("http://192.168.56.101:8080");

		try {
			//Create a 1-dimensional array called system_array | CREATE ARRAY system_array <system:double> [x=0:9,10,0];
//			SciDBArray systemArray = new SciDBArray();
//			systemArray.setName("system_array");
//			systemArray.addAttribute("system", SciDBDataType.DOUBLE);
//			systemArray.addDimension("x", new SciDBDimension("0", "9", "10", "0"));
//			String queryId = sciDB.executeQuery(sciDB.create(systemArray));
//			System.out.println("Query Id: " + queryId);
			
			//Put numerical values of 1–10 into the array | store(build(system_array, (x+1/1.0)),system_array)
//			sciDB.store(sciDB.build(systemArray, "(x+1/1.0"), systemArray);
			
			//Apply the negbinomcdf function to system | SELECT negbinomcdf(system,10.0,0.5) INTO prob_array FROM system_array
			String queryId = sciDB.executeQuery(sciDB.filter(sciDB.list(SciDBListElement.FUNCTIONS), SciDBFunctionFactory.regex("name", "(.*)q(.*)")), "csv");
			System.out.println("Query Id: " + queryId);
			System.out.println(inputStreamToString(sciDB.readLines()));
			
//			System.out.println(sciDB.version());
//			String queryId = sciDB.executeQuery(
//					sciDB.list(SciDBListElement.ARRAYS), "csv");
//			System.out.println("Query Id: " + queryId);
//			System.out.println(inputStreamToString(sciDB.readLines()));
//
//			queryId = sciDB.executeQuery(
//					sciDB.show(new SciDBArray("IHI_ACCELEROMETER")), "csv");
//			System.out.println("Query Id: " + queryId);
//			System.out.println(inputStreamToString(sciDB.readLines()));
//
//			System.out.println(sciDB.releaseSession());
		} catch (NotConnectedException | IOException e) {
			e.printStackTrace();
		}

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
