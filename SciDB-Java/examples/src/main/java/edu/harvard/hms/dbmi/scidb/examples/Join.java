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
public class Join {

	public static void main(String[] args) {
		System.out.println("Join Example");
		SciDB sciDB = new SciDB();
		// sciDB.connect("http://scidb.aws.dbmi.hms.harvard.edu:8080");
		
		SciDBArray arrayA = new SciDBArray("A");
		arrayA.addAttribute("attrB1", SciDBDataType.DOUBLE);
		arrayA.addDimension("i", new SciDBDimension(0, 3, 4, 0));
		arrayA.addDimension("j", new SciDBDimension(0, 3, 4, 0));
		SciDBOperation arrayACreateOperation = sciDB.create(arrayA);
		System.out.println(arrayACreateOperation.toAFLQueryString());
		
		SciDBArray arrayB = new SciDBArray("B");
		arrayB.addAttribute("attrB1", SciDBDataType.DOUBLE);
		arrayB.addDimension("i", new SciDBDimension(0, 3, 4, 0));
		arrayB.addDimension("j", new SciDBDimension(0, 3, 4, 0));
		SciDBOperation arrayBCreateOperation = sciDB.create(arrayB);
		System.out.println(arrayBCreateOperation.toAFLQueryString());
		

		
		

		SciDBOperation joinOperation = sciDB.join(arrayA, arrayB);
		
		System.out.println(joinOperation.toAFLQueryString());
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
