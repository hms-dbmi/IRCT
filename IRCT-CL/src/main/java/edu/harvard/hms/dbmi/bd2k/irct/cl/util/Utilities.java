/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.cl.util;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

/**
 * A collection of static methods that provide shared functionality throughout
 * the IRCT-UI
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class Utilities {

	/**
	 * Returns all the first values from a MultiValue Map
	 * 
	 * @param multiValueMap
	 *            MultiValue Map
	 * @return First Values Map
	 */
	public static Map<String, String> getFirstFromMultiMap(
			MultivaluedMap<String, String> multiValueMap) {
		return getFirstFromMultiMap(multiValueMap, null);
	}

	/**
	 * Returns all the first values from a MultiValue Map if the key starts with
	 * a given string. If it is a match then the string is removed from the
	 * beginning of the key.
	 * 
	 * @param multiValueMap
	 *            MultiValue Map
	 * @param startsWith
	 *            Starts with
	 * @return First Values Map
	 */
	public static Map<String, String> getFirstFromMultiMap(
			MultivaluedMap<String, String> multiValueMap, String startsWith) {

		Map<String, String> firstMap = new HashMap<String, String>();

		for (String key : multiValueMap.keySet()) {
			if (startsWith == null) {
				firstMap.put(key, multiValueMap.getFirst(key));
			} else if (key.startsWith(startsWith)) {
				firstMap.put(key.substring(startsWith.length()),
						multiValueMap.getFirst(key));
			}
		}

		return firstMap;
	}
}
