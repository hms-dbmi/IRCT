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
public class Cast {

	public static void main(String[] args) {
		System.out.println("Cast Example");
		SciDB sciDB = new SciDB();
		// sciDB.connect("http://scidb.aws.dbmi.hms.harvard.edu:8080");

		SciDBArray newArray = new SciDBArray("winningTime");
		newArray.addAttribute("time", SciDBDataType.DOUBLE);
		newArray.addDimension("year", new SciDBDimension(1996, 2008, 1, 0));

		SciDBArray castArray = new SciDBArray("winningTimeCast");
		castArray.addAttribute("time_in_seconds", SciDBDataType.DOUBLE);
		castArray.addDimension("year", new SciDBDimension(1996, 2008, 1, 0));

		SciDBOperation castOperation = sciDB.cast(newArray, castArray);

		System.out.println(castOperation.toAFLQueryString());

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
