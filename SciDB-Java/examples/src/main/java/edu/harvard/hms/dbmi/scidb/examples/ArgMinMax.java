/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.scidb.examples;

import edu.harvard.hms.dbmi.scidb.SciDB;
import edu.harvard.hms.dbmi.scidb.SciDBArray;

/**
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class ArgMinMax {
	
	public static void main(String[] args) {
		System.out.println("Arg Min Max Example");
		SciDB sciDB = new SciDB();
		sciDB.connect("http://scidb.aws.dbmi.hms.harvard.edu:8080");
		
//		SciDBArray x = sciDB.random(10, 5);
		
		
		sciDB.close();
	}

}
