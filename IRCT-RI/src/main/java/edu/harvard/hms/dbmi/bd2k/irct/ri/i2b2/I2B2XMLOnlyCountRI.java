/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.ri.i2b2;

import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;
import edu.harvard.hms.dbmi.i2b2.api.crc.xml.psm.ResultOutputOptionType;

/**
 * A resource implementation of a resource that communicates with the i2b2
 * servers via XML
 */
public class I2B2XMLOnlyCountRI
		extends I2B2XMLResourceImplementation {

	@Override
	public String getType() {
		return "i2b2XML_countOnly";
	}

	@Override
	public Result runQuery(User user, Query query, Result result) throws ResourceInterfaceException {
		query.getMetaData().put("only_count", "");
		return i2b2XMLRIRunQuery_runRequest(user, query, result, ResultOutputOptionType.PATIENT_COUNT_XML);
	}

}
