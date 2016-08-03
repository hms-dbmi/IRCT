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
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;

import edu.harvard.hms.dbmi.bd2k.irct.event.action.AfterExecutionPlan;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.executable.Executable;
import edu.harvard.hms.dbmi.bd2k.irct.executable.ExecutableStatus;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

public class S3AfterSaveResult implements AfterExecutionPlan {
	private AmazonS3 s3client;
	private Log log;
	private String bucketName;

	@Override
	public void init(Map<String, String> parameters) {
		log = LogFactory.getLog("AWS S3 Monitoring");
		bucketName = parameters.get("Bucket Name");
		
//		BasicAWSCredentials awsCreds = new BasicAWSCredentials(parameters.get("accessId"), parameters.get("accessKey"));
//		s3client = new AmazonS3Client(awsCreds);
//		s3client = new AmazonS3Client();
		s3client = new AmazonS3Client(new InstanceProfileCredentialsProvider());
//		s3client = new AmazonS3Client(new ProfileCredentialsProvider());
		
	}

	@Override
	public void fire(SecureSession session, Executable executable) {
		
		
		try {
			if(executable.getStatus() != ExecutableStatus.COMPLETED) {
				return;
			}
			
			Result result = executable.getResults();
			for (File resultFile : result.getData().getFileList()) {
				String keyName = "IRCT/result/" + result.getId() + "/" + resultFile.getName();
				// Copy the result into S3 if bucketName is not empty or null
				s3client.putObject(new PutObjectRequest(bucketName, keyName, resultFile));
				log.info("Moved " + result.getResultSetLocation() + " to " + bucketName + "/" + keyName);
				// Delete File
				resultFile.delete();
				log.info("Deleted " + resultFile.getName());
			}
			result.setResultSetLocation("S3://result/" + result.getId());
			
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
		} catch (ResourceInterfaceException e) {
			log.warn("Error Message: " + e.getMessage());
		}
	}

}
