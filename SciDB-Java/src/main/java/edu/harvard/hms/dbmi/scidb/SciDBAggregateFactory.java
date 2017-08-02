/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.scidb;

public class SciDBAggregateFactory {
	private static SciDBAggregate create(String aggregate, String attribute) {
		return new SciDBAggregate(aggregate + "(" + attribute + ")");
	}
	
	public static SciDBAggregate count() {
		return count("*");
	}
	public static SciDBAggregate count(String attribute) {
		return create("count", attribute);
	}
}
