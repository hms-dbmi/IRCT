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
public class CrossJoin {

	public static void main(String[] args) {
		System.out.println("Cross Join Example");
		SciDB sciDB = new SciDB();
		// sciDB.connect("http://scidb.aws.dbmi.hms.harvard.edu:8080");
		
		SciDBArray leftArray = new SciDBArray("leftArray");
		leftArray.addAttribute("attrB1", SciDBDataType.DOUBLE);
		leftArray.addDimension("i", new SciDBDimension(0, 3, 4, 0));
		leftArray.addDimension("j", new SciDBDimension(0, 3, 4, 0));
		SciDBOperation cbBCreateOperation = sciDB.create(leftArray);
		System.out.println(cbBCreateOperation.toAFLQueryString());

		SciDBArray rightArray = new SciDBArray("rightArray");
		rightArray.addAttribute("iLo", SciDBDataType.INT64);
		rightArray.addAttribute("jLo", SciDBDataType.INT64);
		rightArray.addAttribute("iHi", SciDBDataType.INT64);
		rightArray.addAttribute("jHi", SciDBDataType.INT64);
		rightArray.addDimension("rBi", new SciDBDimension(0, 2, 3, 0));

		SciDBOperation rBCreateOperation = sciDB.create(rightArray);
		System.out.println(rBCreateOperation.toAFLQueryString());

		SciDBOperation crossJoinOperation = sciDB.crossJoin(leftArray, "A", rightArray, "B", "A.j", "B.k");
		System.out.println(crossJoinOperation.toAFLQueryString());
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
