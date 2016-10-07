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
public class CrossBetween {

	public static void main(String[] args) {
		System.out.println("Cross Between Example");
		SciDB sciDB = new SciDB();
		// sciDB.connect("http://scidb.aws.dbmi.hms.harvard.edu:8080");
		
		SciDBArray cbB = new SciDBArray("cbB");
		cbB.addAttribute("attrB1", SciDBDataType.DOUBLE);
		cbB.addDimension("i", new SciDBDimension(0, 3, 4, 0));
		cbB.addDimension("j", new SciDBDimension(0, 3, 4, 0));
		SciDBOperation cbBCreateOperation = sciDB.create(cbB);
		System.out.println(cbBCreateOperation.toAFLQueryString());

		SciDBArray rB = new SciDBArray("rB");
		rB.addAttribute("iLo", SciDBDataType.INT64);
		rB.addAttribute("jLo", SciDBDataType.INT64);
		rB.addAttribute("iHi", SciDBDataType.INT64);
		rB.addAttribute("jHi", SciDBDataType.INT64);
		rB.addDimension("rBi", new SciDBDimension(0, 2, 3, 0));

		SciDBOperation rBCreateOperation = sciDB.create(rB);
		System.out.println(rBCreateOperation.toAFLQueryString());

		SciDBOperation crossBetweenOperation = sciDB.crossBetween(cbB, rB);
		System.out.println(crossBetweenOperation.toAFLQueryString());
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
