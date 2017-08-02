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
public class Dimensions {

	public static void main(String[] args) {
		System.out.println("Dimension Example");
		SciDB sciDB = new SciDB();
		// sciDB.connect("http://scidb.aws.dbmi.hms.harvard.edu:8080");
		
		SciDBArray array = new SciDBArray("array1");
		array.addAttribute("attrB1", SciDBDataType.DOUBLE);
		array.addDimension("i", new SciDBDimension(0, 3, 4, 0));
		array.addDimension("j", new SciDBDimension(0, 3, 4, 0));
		SciDBOperation cbBCreateOperation = sciDB.create(array);
		System.out.println(cbBCreateOperation.toAFLQueryString());

		
		

		SciDBOperation dimensionsOperation = sciDB.dimensions(array);
		System.out.println(dimensionsOperation.toAFLQueryString());
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
