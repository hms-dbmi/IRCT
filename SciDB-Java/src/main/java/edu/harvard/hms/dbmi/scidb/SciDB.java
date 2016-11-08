/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.scidb;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;

import edu.harvard.hms.dbmi.scidb.exception.NotConnectedException;
import edu.harvard.hms.dbmi.scidb.exception.SciDBOperationException;

/**
 * An implementation that uses the SciDB Shim interface to communicate with a
 * SciDB server. It is partially modeled after the SciDB-Python interface.
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class SciDB {
	private boolean connected;
	private String url;
	private String sessionId;
	private HttpClient client;
	

	/**
	 * Connect to a local SciDB instance running on port 8080. This command is
	 * the same as connect("http://localhost:8080");
	 * 
	 */
	public void connect() {
		connect("http://localhost:8080");
	}

	/*************************************************************************/
	/*** Methods ***/
	/*************************************************************************/

	public SciDBOperation aggregate(SciDBCommand operation,
			SciDBAggregate aggregate) {
		return aggregate(operation, aggregate, null);
	}
	public SciDBOperation aggregate(SciDBCommand operation,
			SciDBAggregate aggregate, String dimension) {
		
		return aggregate(operation, aggregate, dimension, null);
	}
	
	public SciDBOperation aggregate(SciDBCommand operation,
			SciDBAggregate aggregate, String dimension, String alias) {
		SciDBOperation aggregateOperation = new SciDBOperation("aggregate");
		aggregateOperation.setCommand(operation);
		
		String postFix = aggregate.toAFLQueryString();
		if(alias != null) {
			postFix += " AS " + alias;
		}
		if(dimension != null) {
			postFix += "," + dimension;
		}
		aggregateOperation.setPostFix(postFix);
		return aggregateOperation;
	}
	
	/**
	 * Apply a method to an array
	 * 
	 * @param array
	 *            Array
	 * @param attributeExpressions
	 *            Attribute Expression Array
	 * @return SciDB Operation
	 * @throws SciDBOperationException
	 *             An exception occurred
	 */
	public SciDBOperation apply(SciDBArray array,
			String... attributeExpressions) throws SciDBOperationException {

		if (attributeExpressions.length % 2 == 1) {
			throw new SciDBOperationException(
					"Wrong number of attribute expressions");
		}

		SciDBOperation applyOperation = new SciDBOperation("apply");

		applyOperation.setCommandString(array.getName());

		String postFix = "";
		for (String attributeExpression : attributeExpressions) {
			postFix += attributeExpression + ",";
		}
		postFix = postFix.substring(0, postFix.length() - 1);
		applyOperation.setPostFix(postFix);

		return applyOperation;
	}

	/**
	 * Return the attributes of an array
	 * 
	 * @param array
	 *            Array
	 * @return SciDB Operation
	 */
	public SciDBOperation attributes(SciDBArray array) {
		SciDBOperation attribtueOperation = new SciDBOperation("attributes",
				array.getName());
		return attribtueOperation;
	}

	/**
	 * Runs the average rank operator of an array
	 * 
	 * @param array
	 *            Array
	 * @param attribute
	 *            Attributes
	 * @param dimensions
	 *            Dimensions
	 * @return SciDB Operation
	 */
	public SciDBOperation avgRank(SciDBArray array, String attribute,
			String... dimensions) {
		SciDBOperation avgRankOperation = new SciDBOperation("avg_rank",
				array.getName());

		String postFix = "";
		if (attribute != null) {
			postFix += attribute;
		}

		if (dimensions != null) {
			for (String dimension : dimensions) {
				postFix += "," + dimension;
			}
		}
		if (!postFix.isEmpty()) {
			avgRankOperation.setPostFix(postFix);
		}

		return avgRankOperation;
	}

	/**
	 * Creates a Bernoulli operation
	 * 
	 * @param array
	 *            Array
	 * @param probability
	 *            Probability
	 * @return SciDB Operation
	 */
	public SciDBOperation bernoulli(SciDBArray array, Double probability) {
		return bernoulli(array, probability, null);
	}

	/**
	 * Creates a Bernoulli operation with a random seed
	 * 
	 * @param array
	 *            Array
	 * @param probability
	 *            Probability
	 * @param seed
	 *            Seed
	 * @return SciDB Operation
	 */
	public SciDBOperation bernoulli(SciDBArray array, Double probability,
			Integer seed) {
		SciDBOperation bernoulliOperation = new SciDBOperation("bernoulli",
				array.getName());

		String postFix = Double.toString(probability);

		if (seed != null) {
			postFix += "," + seed;
		}
		bernoulliOperation.setPostFix(postFix);
		return bernoulliOperation;
	}

	/**
	 * Creates a between operation
	 * 
	 * @param array
	 *            Array
	 * @param lowCoordinate
	 *            Low coordinate
	 * @param highCoordinate
	 *            High coordinate
	 * @return SciDB Operation
	 * @throws SciDBOperationException
	 *             An exception occurred
	 */
	public SciDBOperation between(SciDBArray array, int lowCoordinate,
			int highCoordinate) throws SciDBOperationException {
		return between(array, new int[] { lowCoordinate },
				new int[] { highCoordinate });
	}

	/**
	 * Creates a between operation
	 * 
	 * @param array
	 *            Array
	 * @param lowCoordinates
	 *            Low coordinates
	 * @param highCoordinate
	 *            High coordinate
	 * @return SciDB Operation
	 * @throws SciDBOperationException
	 *             An exception occurred
	 */
	public SciDBOperation between(SciDBCommand array, int[] lowCoordinates,
			int highCoordinate) throws SciDBOperationException {
		return between(array, lowCoordinates, new int[] { highCoordinate });
	}

	/**
	 * Creates a between operation
	 * 
	 * @param array
	 *            Array
	 * @param lowCoordinate
	 *            Low coordinate
	 * @param highCoordinates
	 *            High coordinates
	 * @return SciDB Operation
	 * @throws SciDBOperationException
	 *             An exception occurred
	 */
	public SciDBOperation between(SciDBCommand array, int lowCoordinate,
			int[] highCoordinates) throws SciDBOperationException {
		return between(array, new int[] { lowCoordinate }, highCoordinates);
	}

	/**
	 * Creates a between operation
	 * 
	 * @param array
	 *            Array
	 * @param lowCoordinates
	 *            Low coordinates
	 * @param highCoordinates
	 *            High coordinates
	 * @return SciDB Operation
	 * @throws SciDBOperationException
	 *             An exception occurred
	 */
	public SciDBOperation between(SciDBArray array, int[] lowCoordinates,
			int[] highCoordinates) throws SciDBOperationException {
		SciDBOperation betweenOperation = new SciDBOperation("between",
				array.getName());

		// Check low coordinates
		if (lowCoordinates.length != array.getDimensions().size()) {
			throw new SciDBOperationException(
					"Lower coordinates is not equal to the size of the array");
		}

		// Check high coordinates
		if ((highCoordinates != null)
				&& (highCoordinates.length != array.getDimensions().size())) {
			throw new SciDBOperationException(
					"High coordinates is not equal to the size of the array");
		}

		String postFix = "";
		for (int lowCoordinate : lowCoordinates) {
			postFix += Integer.toString(lowCoordinate) + ",";
		}

		if (highCoordinates == null) {
			for (@SuppressWarnings("unused")
			int lowCoordinatate : lowCoordinates) {
				postFix += "null,";
			}
		} else {
			for (int highCoordinate : highCoordinates) {
				postFix += Integer.toString(highCoordinate) + ",";
			}
		}

		postFix = postFix.substring(0, postFix.length() - 1);

		betweenOperation.setPostFix(postFix);

		return betweenOperation;
	}
	
	/**
	 * Creates a between operation
	 * 
	 * @param array
	 *            Array
	 * @param lowCoordinates
	 *            Low coordinates
	 * @param highCoordinates
	 *            High coordinates
	 * @return SciDB Operation
	 * @throws SciDBOperationException
	 *             An exception occurred
	 */
	public SciDBOperation between(SciDBCommand array, int[] lowCoordinates,
			int[] highCoordinates) {
		SciDBOperation betweenOperation = new SciDBOperation("between",
				array.toAFLQueryString());

		String postFix = "";
		for (int lowCoordinate : lowCoordinates) {
			postFix += Integer.toString(lowCoordinate) + ",";
		}

		if (highCoordinates == null) {
			for (@SuppressWarnings("unused")
			int lowCoordinatate : lowCoordinates) {
				postFix += "null,";
			}
		} else {
			for (int highCoordinate : highCoordinates) {
				postFix += Integer.toString(highCoordinate) + ",";
			}
		}

		postFix = postFix.substring(0, postFix.length() - 1);

		betweenOperation.setPostFix(postFix);

		return betweenOperation;
	}

	/**
	 * Builds a new array with the parameters passed
	 * 
	 * @param newArray
	 *            New Array
	 * @return SciDB Operation
	 */
	public SciDBOperation build(SciDBArray newArray) {
		return build(newArray, null);
	}

	/**
	 * Builds a new array with the parameters passed with the expression
	 * 
	 * @param newArray
	 *            New Array
	 * @param expression SciDB Expression
	 * @return SciDB Operation
	 */
	public SciDBOperation build(SciDBArray newArray, String expression) {
		SciDBOperation buildOperation = new SciDBOperation("build");

		if (newArray.getOrigin() == null) {
			newArray.setOrigin(this);
			buildOperation.setCommand(newArray);
		} else {
			buildOperation.setCommandString(newArray.getName());
		}

		if (expression != null) {
			buildOperation.setPostFix(expression);
		}

		return buildOperation;
	}

	/**
	 * Cancels a given query id
	 * 
	 * @param queryId
	 *            The Query ID
	 * @return SciDB Operation
	 */
	public SciDBOperation cancel(String queryId) {
		SciDBOperation cancelOperation = new SciDBOperation("cancel");
		cancelOperation.setCommandString(queryId);
		return cancelOperation;
	}

	/**
	 * Casts a given array to a new template
	 * 
	 * @param array
	 *            Original Array
	 * @param template
	 *            Template Array
	 * @return SciDB Operation
	 */
	public SciDBOperation cast(SciDBArray array, SciDBArray template) {
		SciDBOperation castOperation = new SciDBOperation("cast");
		castOperation.setCommandString(array.getName());

		String postFix = "";
		if (template.getOrigin() == null) {
			postFix += template.toAFLQueryString();
		} else {
			postFix += template.getName();
		}
		castOperation.setPostFix(postFix);
		return castOperation;
	}

	/**
	 * Scans all the data in the input array
	 * 
	 * @param array
	 *            Array
	 * @return SciDB Operation
	 */
	public SciDBOperation consume(SciDBCommand array) {
		return consume(array, null);
	}

	/**
	 * Scans all the data in the input array with optional parameter which
	 * determines the number of attributes to scan as a group
	 * 
	 * @param array
	 *            Array
	 * @param numAttributesToScan
	 *            Number of attributes to scan
	 * @return SciDB Operation
	 */
	public SciDBOperation consume(SciDBCommand array, Integer numAttributesToScan) {
		SciDBOperation consumeOperation = new SciDBOperation("consume");

		consumeOperation.setCommand(array);

		if (numAttributesToScan != null) {
			consumeOperation.setPostFix(Integer.toString(numAttributesToScan));
		}

		return consumeOperation;
	}

	/**
	 * Create a new array
	 * 
	 * @param newArray Array to create
	 * @return SciDB Operation
	 */
	public SciDBOperation create(SciDBArray newArray) {
		SciDBOperation createOperation = new SciDBOperation();
		createOperation.setPrefix("create array " + newArray.getName());
		createOperation.setCommand(newArray);
		newArray.setOrigin(this);

		return createOperation;
	}

	/**
	 * Performs a cross between the two arrays
	 * 
	 * @param sourceArray
	 *            Source Array
	 * @param regionArray
	 *            Region Array
	 * @return SciDB Operation
	 */
	public SciDBOperation crossBetween(SciDBArray sourceArray,
			SciDBArray regionArray) {
		SciDBOperation crossBetweenOperation = new SciDBOperation(
				"cross_between");

		crossBetweenOperation.setCommandString(sourceArray.getName());
		crossBetweenOperation.setPostFix(regionArray.getName());

		return crossBetweenOperation;
	}

	/**
	 * Run a Cross Join Operation
	 * 
	 * @param leftArray
	 *            Left Array
	 * @param rightCommand
	 *            Right Array
	 * @param leftDimension
	 *            Left Dimension
	 * @param rightDimension
	 *            Right Dimension
	 * @return SciDB Operation
	 */
	public SciDBOperation crossJoin(SciDBCommand leftArray,
			SciDBCommand rightCommand, String leftDimension, String rightDimension) {
		return crossJoin(leftArray, null, rightCommand, null, leftDimension,
				rightDimension);
	}
	
	
	public SciDBOperation crossJoin(SciDBCommand leftCommand, String leftAlias, SciDBCommand rightCommand, String rightAlias, String... dimensions) {
		SciDBOperation crossJoinOperation = new SciDBOperation("cross_join");
		
		String command = "";
		if(leftCommand instanceof SciDBArray) {
			command = ((SciDBArray) leftCommand).getName();
		} else {
			command = leftCommand.toAFLQueryString();
		}
		if (leftAlias != null) {
			command += " as " + leftAlias;
		}
		
		if(rightCommand instanceof SciDBArray) {
			command += "," + ((SciDBArray) rightCommand).getName();
		} else {
			command += "," + rightCommand.toAFLQueryString();
		}
		
		if (rightAlias != null) {
			command += " as " + rightAlias;
		}
		
		for(String dimension : dimensions) {
			command += "," + dimension;	
		}
		

		crossJoinOperation.setCommandString(command);
		
		return crossJoinOperation;
	}
	
	/**
	 * Run a Cross Join Operation
	 * 
	 * @param leftCommand
	 *            Left Command
	 * @param leftAlias
	 *            Left Array Alias
	 * @param rightCommand
	 *            Right Command
	 * @param rightAlias
	 *            Right Array Alias
	 * @param leftDimension
	 *            Left Dimension
	 * @param rightDimension
	 *            Right Dimension
	 * @return SciDB Operation
	 */
	public SciDBOperation crossJoin(SciDBCommand leftCommand, String leftAlias,
			SciDBCommand rightCommand, String rightAlias, String leftDimension,
			String rightDimension) {
		SciDBOperation crossJoinOperation = new SciDBOperation("cross_join");
		
		String command = "";
		if(leftCommand instanceof SciDBArray) {
			command = ((SciDBArray) leftCommand).getName();
			if (leftAlias != null) {
				command += " as " + leftAlias;
			}
		} else {
			command = leftCommand.toAFLQueryString();
		}
		
		if(rightCommand instanceof SciDBArray) {
			command += "," + ((SciDBArray) rightCommand).getName();
			if (rightAlias != null) {
				command += " as " + rightAlias;
			}
		} else {
			command += "," + rightCommand.toAFLQueryString();
		}
		command += "," + leftDimension + "," + rightDimension;

		crossJoinOperation.setCommandString(command);
		return crossJoinOperation;
	}

	/**
	 * Runs a cumulate operation on an array with the given aggregates along the
	 * first dimension
	 * 
	 * @param array
	 *            Array
	 * @param aggregates
	 *            Aggregates
	 * @return SciDB Operation
	 */
	public SciDBOperation cumulate(SciDBArray array,
			SciDBOperation... aggregates) {
		return cumulate(array, null, aggregates);
	}

	/**
	 * Creates a cumulate operation on an array with the given aggregates along
	 * the given dimension
	 * 
	 * @param array
	 *            Array
	 * @param dimension
	 *            Dimension
	 * @param aggregates
	 *            Aggregates
	 * @return SciDB Operation
	 */
	public SciDBOperation cumulate(SciDBArray array, String dimension,
			SciDBOperation... aggregates) {
		SciDBOperation cumulateOperation = new SciDBOperation("cumulate");

		cumulateOperation.setCommandString(array.getName());

		String postFix = "";
		for (SciDBOperation aggregate : aggregates) {
			postFix += aggregate.toAFLQueryString() + ",";
		}
		postFix = postFix.substring(0, postFix.length() - 1);
		if (dimension != null) {
			postFix += "," + dimension;
		}

		cumulateOperation.setPostFix(postFix);
		return cumulateOperation;
	}

	/**
	 * Creates an operation that returns the dimensions of an array
	 * 
	 * @param array
	 *            Array
	 * @return SciDB Operation
	 */
	public SciDBOperation dimensions(SciDBArray array) {
		SciDBOperation dimensionsOperation = new SciDBOperation("dimensions");
		dimensionsOperation.setCommandString(array.getName());
		return dimensionsOperation;
	}

	/**
	 * Create an operation to filter an array given a SciDB function
	 * 
	 * @param array
	 *            Array
	 * @param function
	 *            Function
	 * @return SciDB Operation
	 */
	public SciDBOperation filter(SciDBArray array, SciDBCommand function) {
		return filter(array, function.toAFLQueryString());
	}

	/**
	 * Create an operation to filter an array given an expression
	 * 
	 * @param array
	 *            Array
	 * @param expression
	 *            Expression
	 * @return SciDB Operation
	 */
	public SciDBOperation filter(SciDBArray array, String expression) {
		SciDBOperation filterOperation = new SciDBOperation("filter");
		filterOperation.setCommandString(array.getName());
		filterOperation.setPostFix(expression);
		return filterOperation;
	}

	/**
	 * Create an operation to filter on a command that creates a list given a
	 * function
	 * 
	 * @param operation
	 *            List operation
	 * @param function
	 *            Function
	 * @return SciDB Operation
	 */
	public SciDBOperation filter(SciDBOperation operation, SciDBFunction function) {
		return filter(operation, function.toAFLQueryString());
	}
	
	/**
	 * Create an operation to filter on a command that creates a list given a
	 * function
	 * 
	 * @param operation
	 *            List operation
	 * @param function
	 *            Function
	 * @return SciDB Operation
	 */
	public SciDBCommand filter(SciDBCommand operation, SciDBCommand function) {
		return filter(operation, function.toAFLQueryString());
	}

	/**
	 * Create an operation to filter on a command that creates a list with a
	 * given expression
	 * 
	 * @param operation
	 *            List operation
	 * @param expression
	 *            Expression
	 * @return SciDB Operation
	 */
	public SciDBOperation filter(SciDBCommand operation, String expression) {
		SciDBOperation filterOperation = new SciDBOperation("filter");
		if(operation instanceof SciDBArray) {
			filterOperation.setCommandString(((SciDBArray) operation).getName());
		} else {
			filterOperation.setCommandString(operation.toAFLQueryString());
		}
		filterOperation.setPostFix(expression);
		return filterOperation;
	}

	/**
	 * Creates an SciDB Operation to run a gemm matrix multiplication across
	 * three arrays
	 * 
	 * @param array1
	 *            Array 1
	 * @param array2
	 *            Array 2
	 * @param array3
	 *            Array 3
	 * @return SciDB Operation
	 */
	public SciDBOperation gemm(SciDBArray array1, SciDBArray array2,
			SciDBArray array3) {
		return gemm(array1, array2, array3, new String[] {});
	}

	/**
	 * Creates an SciDB Operation to run a gemm matrix multiplication across
	 * three arrays with a given option
	 * 
	 * @param array1
	 *            Array 1
	 * @param array2
	 *            Array 2
	 * @param array3
	 *            Array 3
	 * @param options
	 *            Option
	 * @return SciDB Operation
	 */
	public SciDBOperation gemm(SciDBArray array1, SciDBArray array2,
			SciDBArray array3, String... options) {
		SciDBOperation gemmOperation = new SciDBOperation("gemm");
		gemmOperation.setCommandString(array1.getName());
		String postFix = array2.getName() + "," + array3.getName();

		for(String option : options) {
			postFix += "," + option;
		}

		gemmOperation.setPostFix(postFix);

		return gemmOperation;
	}

	/**
	 * Creates a gesvd operation given an array and a factor. The factor must be
	 * of the following values: - U (or 'left'): the matrix of left-singular
	 * vectors. - S (or 'values'): a vector that contains the singular values in
	 * decreasing numerical order. - VT (or 'right'): the transpose of the
	 * matrix of right-singular vectors.
	 * 
	 * @param array
	 *            Array
	 * @param factor
	 *            Factor
	 * @return SciDB Operation
	 * @throws SciDBOperationException
	 *             Factor is not U, S, or VT
	 */
	public SciDBOperation gesvd(SciDBArray array, String factor)
			throws SciDBOperationException {
		SciDBOperation gesvdOperation = new SciDBOperation("gesvd");
		gesvdOperation.setCommandString(array.getName());
		if (!(factor.equals("U") || factor.equals("S") || factor.equals("VT"))) {
			throw new SciDBOperationException("Factor is not U, S, or VT");
		}
		gesvdOperation.setPostFix(factor);

		return gesvdOperation;
	}

	/**
	 * Creates a GLM operation given a matrix, response, weights, distribution,
	 * and link
	 * 
	 * @param matrix
	 *            Matrix
	 * @param response
	 *            Response
	 * @param weights
	 *            Weights
	 * @param distribution
	 *            Distribution
	 * @param link
	 *            Link
	 * @return SciDB Operation
	 */
	public SciDBOperation glm(SciDBArray matrix, SciDBArray response,
			SciDBArray weights, String distribution, String link) {
		SciDBOperation glmOperation = new SciDBOperation("glm");
		glmOperation.setCommandString(matrix.getName());

		String postFix = response.getName() + "," + weights.getName() + ","
				+ distribution + "," + link;
		glmOperation.setPostFix(postFix);

		return glmOperation;
	}

	/**
	 * Returns an operation that returns the coordinate value of an attribute
	 * 
	 * @param array
	 *            Array
	 * @param indexArray
	 *            Index Array
	 * @param attributeName
	 *            Attribute Name
	 * @param outputAttributeName
	 *            Output Attribute Name
	 * @param indexSorted
	 *            Index Sorted
	 * @return SciDB Operation
	 */
	public SciDBOperation indexLookup(SciDBArray array, SciDBArray indexArray,
			String attributeName, String outputAttributeName,
			Boolean indexSorted) {
		return indexLookup(array, indexArray, attributeName,
				outputAttributeName, indexSorted, null);

	}

	/**
	 * Returns an operation that returns the coordinate value of an attribute
	 * 
	 * @param array
	 *            Array
	 * @param indexArray
	 *            Index Array
	 * @param attributeName
	 *            Attribute Name
	 * @param outputAttributeName
	 *            Output Attribute Name
	 * @return SciDB Operation
	 */
	public SciDBOperation indexLookup(SciDBArray array, SciDBArray indexArray,
			String attributeName, String outputAttributeName) {
		return indexLookup(array, indexArray, attributeName,
				outputAttributeName, null, null);

	}

	/**
	 * Returns an operation that returns the coordinate value of an attribute
	 * 
	 * @param array
	 *            Array
	 * @param indexArray
	 *            Index Array
	 * @param attributeName
	 *            Attribute Name
	 * @return SciDB Operation
	 */
	public SciDBOperation indexLookup(SciDBArray array, SciDBArray indexArray,
			String attributeName) {
		return indexLookup(array, indexArray, attributeName, null, null, null);

	}

	/**
	 * Returns an operation that returns the coordinate value of an attribute
	 * 
	 * @param array
	 *            Array
	 * @param indexArray
	 *            Index Array
	 * @param attributeName
	 *            Attribute Name
	 * @param outputAttributeName
	 *            Output Attribute Name
	 * @param indexSorted
	 *            Index Sorted
	 * @param memoryLimit
	 *            Memory Limit
	 * @return SciDB Operation
	 */
	public SciDBOperation indexLookup(SciDBArray array, SciDBArray indexArray,
			String attributeName, String outputAttributeName,
			Boolean indexSorted, Long memoryLimit) {
		SciDBOperation indexLookupOperation = new SciDBOperation("index_lookup");
		indexLookupOperation.setCommandString(array.getName());

		String postFix = indexArray.getName() + "," + attributeName;

		if (outputAttributeName != null) {
			postFix += "," + outputAttributeName;
		}
		if (indexSorted != null) {
			postFix += "," + indexSorted;
		}
		if (memoryLimit != null) {
			postFix += "," + memoryLimit;
		}

		indexLookupOperation.setPostFix(postFix);

		return indexLookupOperation;
	}

	/**
	 * Creates a list operation on an element
	 * 
	 * @param element
	 *            Element
	 * @return SciDB Operation
	 */
	public SciDBOperation list(SciDBListElement element) {
		return list(element, null);
	}

	/**
	 * Creates a list operation on an element with a version
	 * 
	 * @param element
	 *            Element
	 * @param version
	 *            Version
	 * @return SciDB Operation
	 */
	public SciDBOperation list(SciDBListElement element, Boolean version) {

		SciDBOperation listOperation = new SciDBOperation("list");
		listOperation.setCommandString("'" + element.name().toLowerCase() + "'");
		if (version != null) {
			listOperation.setPostFix(version.toString());
		}

		return listOperation;
	}

	/**
	 * Creates an insert operation between two arrays
	 * 
	 * @param array1
	 *            Array 1
	 * @param array2
	 *            Array 2
	 * @return SciDB Operation
	 */
	public SciDBOperation insert(SciDBArray array1, SciDBArray array2) {
		SciDBOperation insertOperation = new SciDBOperation("insert");
		insertOperation.setCommandString(array1.getName());
		insertOperation.setPostFix(array2.getName());
		return insertOperation;
	}

	/**
	 * Creates a join operation between two arrays
	 * 
	 * @param array1
	 *            Array 1
	 * @param array2
	 *            Array 2
	 * @return SciDB Operation
	 */
	public SciDBOperation join(SciDBArray array1, SciDBArray array2) {
		SciDBOperation insertOperation = new SciDBOperation("join");
		insertOperation.setCommandString(array1.getName());
		insertOperation.setPostFix(array2.getName());
		return insertOperation;
	}

	/**
	 * Creates a kendall operation between two arrays
	 * 
	 * @param array1
	 *            Array 1
	 * @param array2
	 *            Array 2
	 * @return SciDB Operation
	 */
	public SciDBOperation kendall(SciDBArray array1, SciDBArray array2) {
		SciDBOperation insertOperation = new SciDBOperation("kendall");
		insertOperation.setCommandString(array1.getName());
		insertOperation.setPostFix(array2.getName());
		return insertOperation;
	}

	/**
	 * Creates an operation that will return the name physical instance that the
	 * individual arrays reside on.
	 * 
	 * @return SciDB Operation
	 */
	public SciDBOperation listArrayResidency() {
		SciDBOperation listArrayResidencyOperation = new SciDBOperation(
				"list_array_residency");

		return listArrayResidencyOperation;
	}

	/**
	 * Creates an operation that will return the list the instance configuration
	 * 
	 * @return SciDB Operation
	 */
	public SciDBOperation listInstances() {
		SciDBOperation listInstancesOperation = new SciDBOperation(
				"list_instances");
		return listInstancesOperation;
	}

	/**
	 * Creates an operation that will load a library
	 * 
	 * @param libraryName
	 *            Library Name
	 * @return SciDB Operation
	 */
	public SciDBOperation loadLibrary(String libraryName) {
		SciDBOperation loadLibraryOperation = new SciDBOperation("load_library");
		loadLibraryOperation.setCommandString(libraryName);

		return loadLibraryOperation;
	}

	/**
	 * Creates an operation that will load a module
	 * 
	 * @param moduleName
	 *            Module Name
	 * @return SciDB Operation
	 */
	public SciDBOperation loadModule(String moduleName) {
		SciDBOperation loadModuleOperation = new SciDBOperation("load_module");
		loadModuleOperation.setCommandString(moduleName);

		return loadModuleOperation;
	}

	
	/**
	 * Creates a merge operation between two arrays
	 * 
	 * @param leftArray Left Array
	 * @param rightArray Right Array
	 * @return SciDB Operation
	 */
	public SciDBOperation merge(SciDBArray leftArray, SciDBArray rightArray) {
		SciDBOperation insertOperation = new SciDBOperation("merge");
		insertOperation.setCommandString(leftArray.getName());
		insertOperation.setPostFix(rightArray.getName());
		return insertOperation;
	}

	/**
	 * Creates an operation that will initialize the mpi infrastructure
	 * 
	 * @return SciDB Operation
	 */
	public SciDBOperation mpiInit() {
		SciDBOperation mpiInitOperation = new SciDBOperation("mpi_init");
		return mpiInitOperation;
	}

	/**
	 * Creates a pearson operation
	 * 
	 * @param array1
	 *            Array 1
	 * @param array2
	 *            Array 2
	 * @return SciDB Operation
	 */
	public SciDBOperation pearson(SciDBArray array1, SciDBArray array2) {
		return pearson(array1, array2, null);
	}

	/**
	 * Creates a pearson operation with a flag
	 * 
	 * @param array1
	 *            Array 1
	 * @param array2
	 *            Array 2
	 * @param flag
	 *            Flag
	 * @return SciDB Operation
	 */
	public SciDBOperation pearson(SciDBArray array1, SciDBArray array2,
			Boolean flag) {
		SciDBOperation pearsonOperation = new SciDBOperation("pearson");
		pearsonOperation.setCommandString(array1.getName());
		String postFix = array2.getName();

		if (flag != null) {
			postFix += "," + flag;
		}

		pearsonOperation.setPostFix(postFix);

		return pearsonOperation;
	}

	/**
	 * Creates a project operation
	 * 
	 * @param array
	 *            Array
	 * @param attributes
	 *            Attributes
	 * @return SciDB Operation
	 */
	public SciDBOperation project(SciDBArray array, String... attributes) {
		SciDBOperation projectOperation = new SciDBOperation("project");
		projectOperation.setCommandString(array.getName());
		String postFix = "";
		for (String attribute : attributes) {
			postFix += attribute + ",";
		}
		postFix = postFix.substring(0, postFix.length() - 1);
		projectOperation.setPostFix(postFix);
		return projectOperation;
	}
	
	/**
	 * Creates a project operation
	 * 
	 * @param operation Operation
	 * @param attributes attributes
	 * @return SciDB Operation
	 */
	public SciDBOperation project(SciDBCommand operation, String... attributes) {
		SciDBOperation projectOperation = new SciDBOperation("project");
		projectOperation.setCommandString(operation.toAFLQueryString());
		String postFix = "";
		for (String attribute : attributes) {
			postFix += attribute + ",";
		}
		postFix = postFix.substring(0, postFix.length() - 1);
		projectOperation.setPostFix(postFix);
		return projectOperation;
	}

	/**
	 * Creates quantile operation
	 * 
	 * @param array
	 *            Array
	 * @param quantiles
	 *            Quantile
	 * @return SciDB Operation
	 */
	public SciDBOperation quantile(SciDBArray array, int quantiles) {
		return quantile(array, quantiles, null, null);
	}

	/**
	 * Creates quantile operation
	 * 
	 * @param array
	 *            Array
	 * @param quantiles
	 *            Quantile
	 * @param attribute
	 *            Attribute
	 * @return SciDB Operation
	 */
	public SciDBOperation quantile(SciDBArray array, int quantiles,
			String attribute) {
		return quantile(array, quantiles, attribute, null);
	}

	/**
	 * Creates quantile operation
	 * 
	 * @param array
	 *            Array
	 * @param quantile
	 *            Quantile
	 * @param attribute
	 *            Attribute
	 * @param dimension
	 *            Dimension
	 * @return SciDB Operation
	 */
	public SciDBOperation quantile(SciDBArray array, int quantile,
			String attribute, String dimension) {
		SciDBOperation quantileOperation = new SciDBOperation("quantile");
		quantileOperation.setCommandString(array.getName());

		String postFix = Integer.toString(quantile);

		if (attribute != null) {
			postFix += "," + attribute;
		}

		if (dimension != null) {
			postFix += "," + dimension;
		}

		quantileOperation.setPostFix(postFix);

		return quantileOperation;
	}
	
	/**
	 * Creates a rank operation
	 * 
	 * @param array Array 
	 * @return SciDB Operation
	 */
	public SciDBOperation rank(SciDBArray array) {
		return rank(array, null, new String[] {});
	}
	
	/**
	 * Creates a rank operation
	 * 
	 * @param array Array 
	 * @param attribute Attribute
	 * @return SciDB Operation
	 */
	public SciDBOperation rank(SciDBArray array, String attribute) {
		return rank(array, attribute, new String[] {});
	}
	
	/**
	 * Creates a rank operation
	 * 
	 * @param array Array 
	 * @param attribute Attribute
	 * @param dimensions Dimensions
	 * @return SciDB Operation
	 */
	public SciDBOperation rank(SciDBArray array, String attribute, String... dimensions) {
		SciDBOperation rankOperation = new SciDBOperation("rank");
		rankOperation.setCommandString(array.getName());
		
		String postFix = "";
		if(attribute != null) {
			postFix += attribute;
		}
		
		if(dimensions != null) {
			for(String dimension : dimensions) {
				postFix += "," + dimension;
			}
		}
		
		if(!postFix.isEmpty()) {
			rankOperation.setPostFix(postFix);
		}
		
		return rankOperation;
	}
	
	/**
	 * Creates a redimension operation
	 * 
	 * @param array Array
	 * @return SciDB Operation
	 */
	public SciDBOperation redimension(SciDBArray array) {
		return redimension(array, null, new String[] {});
	}
	
	/**
	 * Creates a redimension operation
	 * 
	 * @param array Array
	 * @param isStrict is Strict
	 * @return SciDB Operation
	 */
	public SciDBOperation redimension(SciDBArray array, Boolean isStrict) {
		return redimension(array, isStrict, new String[] {});
	}
	
	/**
	 * Creates a redimension operation
	 * 
	 * @param array Array
	 * @param isStrict is Strict
	 * @param aggregateCalls Aggregate Calls
	 * @return SciDB Operation
	 */
	public SciDBOperation redimension(SciDBArray array, Boolean isStrict, String... aggregateCalls) {
		SciDBOperation redimensionOperation = new SciDBOperation("redimension");
		
		if(array.getOrigin() == null) {
			redimensionOperation.setCommand(array);
		} else {
			redimensionOperation.setCommandString(array.toAFLQueryString());
		}
		String postFix = "";
		
		if(isStrict != null) {
			postFix += isStrict;
		}
		
		if(aggregateCalls != null) {
			for(String aggregateCall : aggregateCalls) {
				postFix += "," + aggregateCall;
			}
		}
		
		if(!postFix.isEmpty()) {
			redimensionOperation.setPostFix(postFix);
		}
		
		return redimensionOperation;
	}

	/**
	 * Creates an operation that will remove an array
	 * 
	 * @param array
	 *            Array
	 * @return SciDB Operation
	 */
	public SciDBOperation remove(SciDBArray array) {
		SciDBOperation removeOperation = new SciDBOperation("remove");
		removeOperation.setCommandString(array.getName());
		return removeOperation;
	}

	/**
	 * Creates an remove version operation
	 * 
	 * @param array
	 *            Array
	 * @param versionId
	 *            Version Id
	 * @return SciDB Operation
	 */
	public SciDBOperation removeVersions(SciDBArray array, int versionId) {
		SciDBOperation removeVersionOperation = new SciDBOperation(
				"remove_versions");
		removeVersionOperation.setCommandString(array.getName());
		removeVersionOperation.setPostFix(Integer.toString(versionId));
		return removeVersionOperation;
	}

	/**
	 * Creates a rename operation
	 * 
	 * @param array
	 *            Array
	 * @param name
	 *            Name
	 * @return SciDB Operation
	 */
	public SciDBOperation rename(SciDBArray array, String name) {
		SciDBOperation renameOperation = new SciDBOperation("rename");
		renameOperation.setCommandString(array.getName());
		renameOperation.setPostFix(name);

		return renameOperation;
	}

	/**
	 * Creates a repart operation
	 * 
	 * @param sourceArray
	 *            Source
	 * @param template
	 *            Template
	 * @return SciDB Operation
	 */
	public SciDBOperation repart(SciDBArray sourceArray, SciDBArray template) {
		SciDBOperation repartOperation = new SciDBOperation("repart");
		repartOperation.setCommandString(sourceArray.getName());

		if (template.getOrigin() == null) {
			repartOperation.setPostFix(template.toAFLQueryString());
		} else {
			repartOperation.setPostFix(template.getName());
		}

		return repartOperation;
	}

	/**
	 * Creates a reshape operation
	 * 
	 * @param sourceArray
	 *            Source
	 * @param template
	 *            Template
	 * @return SciDB Operation
	 */
	public SciDBOperation reshape(SciDBArray sourceArray, SciDBArray template) {
		SciDBOperation reshapeOperation = new SciDBOperation("reshape");
		reshapeOperation.setCommandString(sourceArray.getName());

		if (template.getOrigin() == null) {
			reshapeOperation.setPostFix(template.toAFLQueryString());
		} else {
			reshapeOperation.setPostFix(template.getName());
		}

		return reshapeOperation;
	}
	
	/**
	 * Creates a RNG Uniform operation
	 *  
	 * @param array Array or Schema
	 * @return SciDB Operation
	 */
	public SciDBOperation rngUniform(SciDBArray array) {
		return rngUniform(array, null, null, null, null);
	}
	
	/**
	 * Creates a RNG Uniform operation
	 *  
	 * @param array Array or Schema
	 * @param min Min
	 * @param max Max
	 * @return SciDB Operation
	 */
	public SciDBOperation rngUniform(SciDBArray array, Double min, Double max) {
		return rngUniform(array, min, max, null, null);
	}
	
	/**
	 * Creates a RNG Uniform operation
	 *  
	 * @param array Array or Schema
	 * @param min Min
	 * @return SciDB Operation
	 */
	public SciDBOperation rngUniform(SciDBArray array, Double min) {
		return rngUniform(array, min, null, null, null);
	}
	
	/**
	 * Creates a RNG Uniform operation
	 *  
	 * @param array Array or Schema
	 * @param min Min
	 * @param max Max
	 * @param generatorName Generator
	 * @return SciDB Operation
	 */
	public SciDBOperation rngUniform(SciDBArray array, Double min, Double max, String generatorName) {
		return rngUniform(array, min, max, generatorName, null);
	}
	
	/**
	 * Creates a RNG Uniform operation
	 *  
	 * @param array Array or Schema
	 * @param min Min
	 * @param max Max
	 * @param generatorName Generator
	 * @param seed Seed
	 * @return SciDB Operation
	 */
	public SciDBOperation rngUniform(SciDBArray array, Double min, Double max, String generatorName, Integer seed) {
		SciDBOperation rngUniformOperation = new SciDBOperation("rng_uniform");
		if(array.getOrigin() == null) {
			rngUniformOperation.setCommand(array);
		} else {
			rngUniformOperation.setCommandString(array.getName());
		}
		
		String postFix = "";
		if(min != null) {
			postFix += "," + min;
		}
		if(max != null) {
			postFix += "," + max;
		}
		if(generatorName != null) {
			postFix += "," + generatorName;
		}
		if(seed != null) {
			postFix += "," + seed;
		}
		
		if(!postFix.isEmpty()) {
			rngUniformOperation.setPostFix(postFix);
		}
		return rngUniformOperation;
		
	}

	/**
	 * Creates a save operation
	 * 
	 * @param array
	 *            Array
	 * @param filePath
	 *            File path
	 * @param instanceId
	 *            Instance Id
	 * @return SciDB Operation
	 */
	public SciDBOperation save(SciDBArray array, String filePath, int instanceId) {
		return save(array, filePath, instanceId, null);
	}

	/**
	 * Creates a save operation
	 * 
	 * @param array
	 *            Array
	 * @param filePath
	 *            File path
	 * @return SciDB Operation
	 */
	public SciDBOperation save(SciDBArray array, String filePath) {
		return save(array, filePath, null, null);
	}

	/**
	 * Creates a save operation
	 * 
	 * @param array
	 *            Array
	 * @param filePath
	 *            File path
	 * @param instanceId
	 *            Instance Id
	 * @param format
	 *            Format
	 * @return SciDB Operation
	 */
	public SciDBOperation save(SciDBArray array, String filePath,
			Integer instanceId, String format) {
		SciDBOperation saveOperation = new SciDBOperation("save");
		saveOperation.setCommandString(array.getName());
		String postFix = filePath;
		if (instanceId != null) {
			postFix += "," + instanceId;
		}
		if (format != null) {
			postFix += "," + format;
		}

		saveOperation.setPostFix(postFix);

		return saveOperation;
	}

	/**
	 * Creates an operation that will scan an array
	 * 
	 * @param array
	 *            Array
	 * @return SciDB Operation
	 */
	public SciDBOperation scan(SciDBArray array) {
		SciDBOperation scanOperation = new SciDBOperation("scan");
		scanOperation.setCommandString(array.getName());
		return scanOperation;
	}

	/**
	 * Creates a show operation that will return the properties of an array
	 * 
	 * @param array
	 *            Array
	 * @return SciDB Operation
	 */
	public SciDBOperation show(SciDBArray array) {
		SciDBOperation showOperation = new SciDBOperation("show");
		showOperation.setCommandString(array.getName());
		return showOperation;
	}

	/**
	 * Creates a slice operation that will return a subarray
	 * 
	 * @param array
	 *            Array
	 * @param dimensionValues
	 *            Dimension Values
	 * @return SciDB Operation
	 */
	public SciDBOperation slice(SciDBArray array, String... dimensionValues) {
		SciDBOperation sliceOperation = new SciDBOperation("slice");
		sliceOperation.setCommandString(array.getName());

		String postFix = "";
		for (String dimensionValue : dimensionValues) {
			postFix += dimensionValue + ",";
		}
		postFix = postFix.substring(0, postFix.length() - 1);
		sliceOperation.setPostFix(postFix);
		return sliceOperation;
	}

	/**
	 * Creates a sort operation on an array that will sorted by the first
	 * attribute.
	 * 
	 * @param array Array
	 * @return SciDB Operation
	 */
	public SciDBOperation sort(SciDBArray array) {
		return sort(array, new String[] {});
	}

	/**
	 * Creates a sort operation on an array given a set of attributes. The
	 * attributes may include asc or dsc to identify which sort order to take.
	 * If the attributes parameter is null then array will be sorted by the
	 * first attribute.
	 * 
	 * @param array
	 *            Array
	 * @param attributes
	 *            Atributes
	 * @return SciDB Operation
	 */
	public SciDBOperation sort(SciDBArray array, String... attributes) {
		SciDBOperation sortOperation = new SciDBOperation("sort");
		sortOperation.setCommandString(array.getName());

		if ((attributes != null) && (attributes.length >= 1)) {
			String postFix = "";
			for (String attribute : attributes) {
				postFix += attribute + ",";
			}
			postFix = postFix.substring(0, postFix.length() - 1);
			sortOperation.setPostFix(postFix);
		}

		return sortOperation;
	}

	/**
	 * Creates a spearman operation between two arrays
	 * 
	 * @param array1
	 *            Array 1
	 * @param array2
	 *            Array 2
	 * @return SciDB Operation
	 */
	public SciDBOperation spearman(SciDBArray array1, SciDBArray array2) {
		SciDBOperation spearmanOperation = new SciDBOperation("spearman");
		spearmanOperation.setCommandString(array1.getName());
		spearmanOperation.setPostFix(array2.getName());
		return spearmanOperation;
	}

	/**
	 * Creates a spgemm operation between two arrays
	 * 
	 * @param array1
	 *            Array 1
	 * @param array2
	 *            Array 2
	 * @return SciDB Operation
	 */
	public SciDBOperation spgemm(SciDBArray array1, SciDBArray array2) {
		SciDBOperation spgemmOperation = new SciDBOperation("spgemm");
		spgemmOperation.setCommandString(array1.getName());
		spgemmOperation.setPostFix(array2.getName());
		return spgemmOperation;
	}

	/**
	 * Creates a store operation that stores an array
	 * 
	 * @param operation
	 *            Array or Operation
	 * @param array
	 *            New or Existing Array
	 * @return SciDB Array
	 */
	public SciDBOperation store(SciDBCommand operation, SciDBArray array) {
		SciDBOperation storeOperation = new SciDBOperation("store");
		storeOperation.setCommandString(operation.toAFLQueryString());
		storeOperation.setPostFix(array.getName());

		return storeOperation;
	}

	/**
	 * Creates a subArray operation that produces a sub array of an array given
	 * the high and low coordinates
	 * 
	 * @param array
	 *            Array
	 * @param coordinates
	 *            Coordinates
	 * @return SciDB Operation
	 */
	public SciDBOperation subArray(SciDBArray array, int... coordinates) {
		SciDBOperation subArrayOperation = new SciDBOperation("subarray");
		subArrayOperation.setCommandString(array.getName());
		String postFix = "";
		for (int i : coordinates) {
			postFix += Integer.toString(i) + ",";
		}
		postFix = postFix.substring(0, postFix.length() - 1);
		subArrayOperation.setPostFix(postFix);
		return subArrayOperation;
	}

	/**
	 * Creates an substitute operation given a nullable array and substitute
	 * array
	 * 
	 * @param nullableArray
	 *            Nullable Array
	 * @param substituteArray
	 *            Substitute Array
	 * @return SciDB Operation
	 */
	public SciDBOperation substitute(SciDBArray nullableArray,
			SciDBArray substituteArray) {
		return substitute(nullableArray, substituteArray, new String[] {});
	}

	/**
	 * Creates an substitute operation given a nullable array, substitute array
	 * and attribtues
	 * 
	 * @param nullableArray
	 *            Nullable Array
	 * @param substituteArray
	 *            Substitute Array
	 * @param attributes
	 *            Attributes
	 * @return SciDB Operation
	 */
	public SciDBOperation substitute(SciDBArray nullableArray,
			SciDBArray substituteArray, String... attributes) {
		SciDBOperation substituteOperation = new SciDBOperation("substitute");
		substituteOperation.setCommandString(nullableArray.getName());
		String postFix = substituteArray.getName();
		if (attributes != null) {
			postFix += ",";
			for (String attribute : attributes) {
				postFix += attribute + ",";
			}
			postFix = postFix.substring(0, postFix.length() - 1);
		}
		substituteOperation.setPostFix(postFix);
		return substituteOperation;
	}

	/**
	 * Creates an operation that will check the instance liveness
	 * 
	 * @return SciDB Operation
	 */
	public SciDBOperation sync() {
		SciDBOperation syncOperation = new SciDBOperation("sync");
		return syncOperation;
	}

	/**
	 * Creates an operation that will transpose an array
	 * 
	 * @param array
	 *            Array
	 * @return SciDB Operation
	 */
	public SciDBOperation transpose(SciDBArray array) {
		SciDBOperation transposeOperation = new SciDBOperation("transpose");
		transposeOperation.setCommandString(array.getName());
		return transposeOperation;
	}

	/**
	 * Returns an operation that will estimates the truncated singular value
	 * decomposition.
	 * 
	 * @param array
	 *            Array
	 * @param arrayTranspose
	 *            Array Transpose
	 * @param n
	 *            Singular Values
	 * @param tolerance
	 *            Tolerance
	 * @param iteration
	 *            Iterations
	 * @param initialVector
	 *            Initial Vector
	 * @param left
	 *            Left
	 * @return SciDB Operation
	 */
	public SciDBOperation tsvd(SciDBArray array, SciDBArray arrayTranspose,
			int n, double tolerance, int iteration, SciDBArray initialVector,
			SciDBArray left) {
		return tsvd(array, arrayTranspose, n, tolerance, iteration,
				initialVector, left, null);
	}

	/**
	 * Returns an operation that will estimates the truncated singular value
	 * decomposition.
	 * 
	 * @param array
	 *            Array
	 * @param arrayTranspose
	 *            Array Transpose
	 * @param n
	 *            Singular Values
	 * @param tolerance
	 *            Tolerance
	 * @param iteration
	 *            Iterations
	 * @param initialVector
	 *            Initial Vector
	 * @return SciDB Operation
	 */
	public SciDBOperation tsvd(SciDBArray array, SciDBArray arrayTranspose,
			int n, double tolerance, int iteration, SciDBArray initialVector) {
		return tsvd(array, arrayTranspose, n, tolerance, iteration,
				initialVector, null, null);
	}

	/**
	 * Returns an operation that will estimates the truncated singular value
	 * decomposition.
	 * 
	 * @param array
	 *            Array
	 * @param arrayTranspose
	 *            Array Transpose
	 * @param n
	 *            Singular Values
	 * @param tolerance
	 *            Tolerance
	 * @param iteration
	 *            Iterations
	 * @return SciDB Operation
	 */
	public SciDBOperation tsvd(SciDBArray array, SciDBArray arrayTranspose,
			int n, double tolerance, int iteration) {
		return tsvd(array, arrayTranspose, n, tolerance, iteration, null, null,
				null);
	}

	/**
	 * Returns an operation that will estimates the truncated singular value
	 * decomposition.
	 * 
	 * @param array
	 *            Array
	 * @param arrayTranspose
	 *            Array Transpose
	 * @param n
	 *            Singular Values
	 * @param tolerance
	 *            Tolerance
	 * @return SciDB Operation
	 */
	public SciDBOperation tsvd(SciDBArray array, SciDBArray arrayTranspose,
			int n, double tolerance) {
		return tsvd(array, arrayTranspose, n, tolerance, null, null, null, null);
	}

	/**
	 * Returns an operation that will estimates the truncated singular value
	 * decomposition.
	 * 
	 * @param array
	 *            Array
	 * @param arrayTranspose
	 *            Array Transpose
	 * @param n
	 *            Singular Values
	 * @return SciDB Operation
	 */
	public SciDBOperation tsvd(SciDBArray array, SciDBArray arrayTranspose,
			Integer n) {
		return tsvd(array, arrayTranspose, n, null, null, null, null, null);
	}

	/**
	 * Returns an operation that will estimates the truncated singular value
	 * decomposition.
	 * 
	 * @param array
	 *            Array
	 * @param arrayTranspose
	 *            Array Transpose
	 * @param n
	 *            Singular Values
	 * @param tolerance
	 *            Tolerance
	 * @param iteration
	 *            Iterations
	 * @param initialVector
	 *            Initial Vector
	 * @param left
	 *            Left
	 * @param right
	 *            Right
	 * @return SciDB Operation
	 */
	public SciDBOperation tsvd(SciDBArray array, SciDBArray arrayTranspose,
			Integer n, Double tolerance, Integer iteration,
			SciDBArray initialVector, SciDBArray left, SciDBArray right) {
		SciDBOperation tsvdOperation = new SciDBOperation("tsvd");
		tsvdOperation.setCommandString(array.getName());
		String postFix = arrayTranspose.getName() + "," + n;

		if (tolerance != null) {
			postFix += "," + tolerance;
		}
		if (iteration != null) {
			postFix += "," + iteration;
		}
		if (initialVector != null) {
			postFix += "," + initialVector.getName();
		}
		if (left != null) {
			postFix += "," + left.getName();
		}
		if (right != null) {
			postFix += "," + right.getName();
		}

		tsvdOperation.setPostFix(postFix);
		return tsvdOperation;
	}

	/**
	 * Creates an operation that will return an array into an n+1 dimensions
	 * greater than the input array
	 * 
	 * @param array
	 *            Array
	 * @return SciDB Operation
	 */
	public SciDBOperation unfold(SciDBArray array) {
		SciDBOperation unfoldOperation = new SciDBOperation("unfold");
		unfoldOperation.setCommandString(array.getName());
		return unfoldOperation;
	}

	/**
	 * Creates an operation that will remove all duplicates from an array
	 * 
	 * @param array
	 *            Array
	 * @return SciDB Operation
	 */
	public SciDBOperation uniq(SciDBArray array) {
		return uniq(array, null);
	}

	/**
	 * Creates an operation that will remove all duplicates from an array
	 * 
	 * @param array
	 *            Array
	 * @param chunkSize
	 *            Chunk Size
	 * @return SciDB Operation
	 */
	public SciDBOperation uniq(SciDBArray array, Integer chunkSize) {
		SciDBOperation uniqOperation = new SciDBOperation("uniq");
		uniqOperation.setCommandString(array.getName());

		if (chunkSize != null) {
			uniqOperation.setPostFix(chunkSize.toString());
		}
		return uniqOperation;
	}

	/**
	 * Creates an operation that will unload a given library
	 * 
	 * @param libraryName
	 *            Library Name
	 * @return SciDB Operation
	 */
	public SciDBOperation unloadLibrary(String libraryName) {
		SciDBOperation unloadLibraryOperation = new SciDBOperation(
				"unload_library");
		unloadLibraryOperation.setCommandString(libraryName);
		return unloadLibraryOperation;
	}

	/**
	 * Creates an operation that will create a one dimensional array from a
	 * multidimensional array
	 * 
	 * @param array
	 *            Array
	 * @param dimension
	 *            Dimension
	 * @return SciDB Operation
	 */
	public SciDBOperation unpack(SciDBArray array, String dimension) {
		return unpack(array, dimension, null);
	}

	/**
	 * Creates an operation that will create a one dimensional array from a
	 * multidimensional array
	 * 
	 * @param array
	 *            Array
	 * @param dimension
	 *            Dimension
	 * @param chunkSize
	 *            Chunk Size
	 * @return SciDB Operation
	 */
	public SciDBOperation unpack(SciDBArray array, String dimension,
			Integer chunkSize) {
		SciDBOperation unpackOperation = new SciDBOperation("unpack");
		unpackOperation.setCommandString(dimension);
		String postFix = dimension;

		if (chunkSize != null) {
			postFix += "," + chunkSize;
		}
		unpackOperation.setPostFix(postFix);
		return unpackOperation;
	}

	/**
	 * Creates an operation that will return all the versions of an array
	 * 
	 * @param array
	 *            Array
	 * @return SciDB Operation
	 */
	public SciDBOperation versions(SciDBArray array) {
		SciDBOperation versionsOperation = new SciDBOperation("versions");
		versionsOperation.setCommandString(array.getName());
		return versionsOperation;
	}

	/**
	 * Creates a xgrid operation given an array and set of scales
	 * 
	 * @param array
	 *            Array
	 * @param scale
	 *            Scales
	 * @return SciDB Operation
	 */
	public SciDBOperation xgrid(SciDBArray array, int... scale) {
		SciDBOperation xgridOperation = new SciDBOperation("xgrid");
		xgridOperation.setCommandString(array.getName());
		String postFix = "";
		for (int i : scale) {
			postFix += Integer.toString(i) + ",";
		}
		postFix = postFix.substring(0, postFix.length() - 1);
		xgridOperation.setPostFix(postFix);

		return xgridOperation;
	}

	/*************************************************************************/
	/***
	 * SHIM Connection /
	 *************************************************************************/

	/**
	 * Connect to a SciDB instance at the given URL
	 * 
	 * @param url SciDB URL
	 * @return Operation Status
	 */
	public boolean connect(String url) {
		HttpClientBuilder cb = HttpClientBuilder.create();
		return connect(cb.build(), url);
	}
	
	/**
	 * Connect to a SciDB instance at the given URL
	 * 
	 * @param client HTTP Client
	 * @param url SciDB URL
	 * @return Operation Status
	 */
	public boolean connect(HttpClient client, String url) {
		this.client = client;
		this.url = url;
		
		try {
			URIBuilder uriBuilder = new URIBuilder(this.url + "/new_session");
			
			URI uri = uriBuilder.build();
			System.out.println(uri.toASCIIString());
			
			HttpResponse response = client.execute(new HttpGet(uri));
			
			this.sessionId = inputStreamToString(response.getEntity()
					.getContent());
			this.connected = true;
			return true;
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public String version() throws NotConnectedException {
		if (!this.connected) {
			throw new NotConnectedException();
		}
		

		try {
			URIBuilder uriBuilder = new URIBuilder(this.url + "/version");
			
			URI uri = uriBuilder.build();
			System.out.println(uri.toASCIIString());
			HttpGet runQuery = new HttpGet(uri);
			HttpResponse response = client.execute(runQuery);
			return inputStreamToString(response.getEntity().getContent());
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public String newSession() throws NotConnectedException {
		if (!this.connected) {
			throw new NotConnectedException();
		}

		try {
			URIBuilder uriBuilder = new URIBuilder(this.url + "/new_session");
			
			URI uri = uriBuilder.build();
			System.out.println(uri.toASCIIString());
			HttpGet runQuery = new HttpGet(uri);
			HttpResponse response = client.execute(runQuery);
			return inputStreamToString(response.getEntity().getContent());
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public String releaseSession() throws NotConnectedException {
		return releaseSession(this.sessionId);
	}
	public String releaseSession(String sessionId) throws NotConnectedException {
		if (!this.connected) {
			throw new NotConnectedException();
		}

		try {
			URIBuilder uriBuilder = new URIBuilder(this.url + "/release_session");
			uriBuilder.addParameter("id", sessionId);
			URI uri = uriBuilder.build();
			System.out.println(uri.toASCIIString());
			HttpGet runQuery = new HttpGet(uri);
			HttpResponse response = client.execute(runQuery);
			return inputStreamToString(response.getEntity().getContent());
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}

		return null;
	}

	public String executeQuery(SciDBOperation operation)
			throws NotConnectedException {
		return executeQuery(operation, null);
	}

	public String executeQuery(SciDBCommand operation, String save)
			throws NotConnectedException {
		if (!this.connected) {
			throw new NotConnectedException();
		}

		try {
			URIBuilder uriBuilder = new URIBuilder(this.url + "/execute_query");
			uriBuilder.addParameter("id", this.sessionId);
			uriBuilder.addParameter("query", operation.toAFLQueryString());
			if (save != null) {
				uriBuilder.addParameter("save", save);
			}

			URI uri = uriBuilder.build();
			System.out.println(uri.toASCIIString());
			HttpGet runQuery = new HttpGet(uri);
			HttpResponse response = client.execute(runQuery);
			return inputStreamToString(response.getEntity().getContent());
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}

		return null;
	}

	public InputStream readLines() throws NotConnectedException {
		return readLines(this.sessionId);
	}
	
	public InputStream readLines(String sessionID) throws NotConnectedException {
		if (!this.connected) {
			throw new NotConnectedException();
		}

		try {
			URIBuilder uriBuilder = new URIBuilder(this.url + "/read_lines")
					.addParameter("id", sessionID);
			
			URI uri = uriBuilder.build();
			System.out.println(uri.toASCIIString());
			
			HttpGet runQuery = new HttpGet(uri);
			HttpResponse response = client.execute(runQuery);
			return response.getEntity().getContent();
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Close the connection to a SciDB instance
	 * @return Operation Status
	 */
	public boolean close() {
		try {
			URI uri = new URIBuilder(this.url + "/release_session")
					.addParameter("id", this.sessionId).build();
			HttpGet closeSession = new HttpGet(uri);
			client.execute(closeSession);
			this.connected = false;
			return true;
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private String inputStreamToString(InputStream inputStream)
			throws IOException {
		StringWriter writer = new StringWriter();
		IOUtils.copy(inputStream, writer, Charset.defaultCharset());
		inputStream.close();
		return writer.toString();
	}

	public String getSessionId() {
		return this.sessionId;
	}
}
