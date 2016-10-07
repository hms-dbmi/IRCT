package edu.harvard.hms.dbmi.scidb.examples;

import edu.harvard.hms.dbmi.scidb.SciDBArray;

public class ArrayFromAFLResponseString {

	public static void main(String[] args) {
		String responseString = "IHI_ACCELEROMETER<acc_x:uint8,acc_y:uint8,acc_z:uint8,sleep:uint8> [subject=0:*,1,0,day=0:*,1,0,mil=0:86399999,86400000,600000]";
		
		SciDBArray array = SciDBArray.fromAFLResponseString(responseString);
		
		System.out.println("> " + responseString);
		System.out.println("< " + array.getName() + array.toAFLQueryString());
		
	}

}
