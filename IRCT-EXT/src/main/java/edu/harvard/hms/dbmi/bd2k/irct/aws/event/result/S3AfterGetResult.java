/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package edu.harvard.hms.dbmi.bd2k.irct.aws.event.result;

import java.io.File;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import edu.harvard.hms.dbmi.bd2k.irct.event.result.AfterGetResult;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;

/**
 * The S3 After Get Result checks to see if a result is available localy and if
 * it is not then retrieve it from the S3 server. This depends on the instance
 * to be set up to support S3 permissions.
 * 
 * Configurable Database Parameters
 * Bucket Name - Name of the bucket to save into
 * resultDataFolder - Name of the local folder to save to
 * 
 * @author Jeremy R. Easton-Marks
 *
 */
public class S3AfterGetResult implements AfterGetResult {

	private AmazonS3 s3client;
	private String bucketName;
	private Log log;
	private String irctSaveLocation;

	@Override
	public void init(Map<String, String> parameters) {
		log = LogFactory.getLog("AWS S3 Monitoring");
		bucketName = parameters.get("Bucket Name");
		irctSaveLocation = parameters.get("resultDataFolder");

		s3client = new AmazonS3Client(new InstanceProfileCredentialsProvider());

	}

	@Override
	public void fire(Result result) {
		if (!result.getResultSetLocation().startsWith("S3://")) {
			File temp = new File(result.getResultSetLocation());
			if (temp.exists()) {
				return;
			} else {
				result.setResultSetLocation("S3://result"
						+ result.getResultSetLocation().replaceAll(
								irctSaveLocation, ""));
			}
		}
		String location = result.getResultSetLocation().substring(5);
		// List the files in that bucket path
		try {

			final ListObjectsV2Request req = new ListObjectsV2Request()
					.withBucketName(bucketName).withPrefix(
							"tmp/IRCT/" + location);

			// Loop Through all the files
			ListObjectsV2Result s3Files;
			do {
				s3Files = s3client.listObjectsV2(req);
				for (S3ObjectSummary objectSummary : s3Files
						.getObjectSummaries()) {
					// Download the files to the directory specified
					String keyName = objectSummary.getKey();
					String fileName = irctSaveLocation
							+ keyName.replace("tmp/IRCT/" + location, "");
					log.info("Downloading: " + keyName + " --> " + fileName);
					s3client.getObject(
							new GetObjectRequest(bucketName, keyName),
							new File(fileName));
				}
				req.setContinuationToken(s3Files.getNextContinuationToken());
			} while (s3Files.isTruncated() == true);

			// Update the result set id
			result.setResultSetLocation(irctSaveLocation
					+ location.replace("result", ""));
		} catch (AmazonServiceException ase) {
			log.warn("Caught an AmazonServiceException, which "
					+ "means your request made it "
					+ "to Amazon S3, but was rejected with an error response"
					+ " for some reason.");
			log.warn("Error Message:    " + ase.getMessage());
			log.warn("HTTP Status Code: " + ase.getStatusCode());
			log.warn("AWS Error Code:   " + ase.getErrorCode());
			log.warn("Error Type:       " + ase.getErrorType());
			log.warn("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			log.warn("Caught an AmazonClientException, which "
					+ "means the client encountered "
					+ "an internal error while trying to "
					+ "communicate with S3, "
					+ "such as not being able to access the network.");
			log.warn("Error Message: " + ace.getMessage());
		}
	}

}
