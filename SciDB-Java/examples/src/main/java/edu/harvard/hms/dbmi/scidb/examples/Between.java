/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.scidb.examples;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

import edu.harvard.hms.dbmi.scidb.SciDB;
import edu.harvard.hms.dbmi.scidb.SciDBArray;
import edu.harvard.hms.dbmi.scidb.SciDBDataType;
import edu.harvard.hms.dbmi.scidb.SciDBDimension;
import edu.harvard.hms.dbmi.scidb.SciDBOperation;
import edu.harvard.hms.dbmi.scidb.exception.NotConnectedException;
import edu.harvard.hms.dbmi.scidb.exception.SciDBOperationException;

/**
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class Between {

	public static void main(String[] args) {
		System.out.println("Bernoulli Example");
		SciDB sciDB = new SciDB();
		// sciDB.connect("http://scidb.aws.dbmi.hms.harvard.edu:8080");
		try {
			SciDBArray newArray = new SciDBArray("between_array");
			newArray.addAttribute("val", SciDBDataType.DOUBLE);
			newArray.addDimension("i", new SciDBDimension(0, 3, 4, 0));
			newArray.addDimension("j", new SciDBDimension(0, 3, 4, 0));
			SciDBOperation createOperation = sciDB.create(newArray);
			System.out.println(createOperation.toAFLQueryString());

			SciDBOperation betweenOperation1 = sciDB.between(newArray,
					new int[] { 2, 2 }, new int[] { 3, 3 });
			System.out.println(betweenOperation1.toAFLQueryString());
			SciDBOperation betweenOperation2 = sciDB.between(newArray,
					new int[] { 2, 2 }, null);

			System.out.println(betweenOperation2.toAFLQueryString());
		} catch (SciDBOperationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// sciDB.close();
	}

	private static String inputStreamToString(InputStream inputStream)
			throws IOException {
		StringWriter writer = new StringWriter();
		IOUtils.copy(inputStream, writer, Charset.defaultCharset());
		inputStream.close();
		return writer.toString();
	}

}
