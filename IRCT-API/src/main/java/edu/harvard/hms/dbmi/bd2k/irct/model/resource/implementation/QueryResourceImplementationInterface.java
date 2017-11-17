/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation;


import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ResourceState;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;

/**
 * Provides an implementation that describes the API for any resource that has
 * the ability to handle a query
 */
public interface QueryResourceImplementationInterface extends
		ResourceImplementationInterface {
	/**
	 * Runs the given query
	 * 
     * @param session Session to run in
	 * @param qep Query to be run
	 * @param result Results       
	 * @return The id of the query that is running
	 * @throws ResourceInterfaceException A resource exception occurred
	 */
	Result runQuery(User user, Query qep, Result result) throws ResourceInterfaceException;

	/**
	 * Parses and runs a raw string.
	 * 
	 * @param query A `String` object, that hopefully includes the resource being queried and the credentials that are allowed to query that particular resource
	 *
	 * @return A `Result` object, that is the response from the queried resource.
	 * @throws ResourceInterfaceException An `Exception` that refers to the parsing, querying (asyncronous) and result processing portion of this request.
	 */
	Result runRawQuery(String queryString) throws ResourceInterfaceException;

	/**
	 * Returns the results of the query if they are available
	 * 
	 * @param user Credentials for getting the results
	 * @param query The query, who's results we want 
	 * @return Results
	 * @throws ResourceInterfaceException A resource exception occurred
	 */
	Result getResults(User user, Query query) throws ResourceInterfaceException;

	/**
	 * Returns the results of the query if they are available
	 * 
	 * @param session Session to run in
	 * @param result Results     
	 * @return Results
	 * @throws ResourceInterfaceException A resource exception occurred
	 */
	Result getResults(User user, Result result) throws ResourceInterfaceException;

	/**
	 * Returns the state of the resource
	 * 
	 * @return Resource State
	 */
	ResourceState getState();
	
	/**
	 * Returns the result data type
	 * @param query Query to run
	 * @return Result data type
	 */
	ResultDataType getQueryDataType(Query query);
}
