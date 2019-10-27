package com.sjsu.project.services;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminAddUserToGroupRequest;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.iterable.S3Versions;
import com.amazonaws.services.s3.model.BucketVersioningConfiguration;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.S3VersionSummary;

@Component
@Configuration
public class S3Services {

	private Logger logger = LoggerFactory.getLogger(S3Services.class);

	@Autowired
	private AmazonS3 s3client;

	@Autowired
	private AWSCognitoIdentityProvider cognitoclientprovider;

	@Value("${aws.s3.bucket}")
	public String bucketName;

	@Value("${aws.userpool.id}")
	public String userPoolID;
	
	

	
	public S3Services() {

	}

	/**
	 * get objects from folder for users
	 * @param folderKey
	 * @return
	 */
	public List<S3ObjectSummary> getObjectslistFromFolder(String folderKey) {
		logger.info("BUCKET NAME: bucketName={}", bucketName);
		if (StringUtils.isEmpty(bucketName)) {
			logger.error("Bucket Name is empty. Please check the configurations");
			throw new RuntimeException("Bucket Name is empty. Please check the configurations");
		}
		List<S3ObjectSummary> objectList = new ArrayList<>();
		ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
		listObjectsRequest.withBucketName(bucketName.trim());
		listObjectsRequest.withPrefix(folderKey + "/");

		ObjectListing objectListing;
		do {

			objectListing = s3client.listObjects(listObjectsRequest);
			for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
				logger.info(" - " + objectSummary.getKey() + "  " + "(size = " + objectSummary.getSize() + ")");
				objectList.add(objectSummary);

			}
			listObjectsRequest.setMarker(objectListing.getNextMarker());
		} while (objectListing.isTruncated());

		return objectList;
	}

	/**
	 * get all user files for admin
	 * @return
	 */
	public List<S3ObjectSummary> getAllUserFiles() {
		logger.info("BUCKET NAME: bucketName={}", bucketName);
		if (StringUtils.isEmpty(bucketName)) {
			logger.error("Bucket Name is empty. Please check the configurations");
			throw new RuntimeException("Bucket Name is empty. Please check the configurations");
		}
		List<S3ObjectSummary> objectList = new ArrayList<>();
		ListObjectsV2Result result;

		result = s3client.listObjectsV2(bucketName.trim());
		for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
			logger.info(" - " + objectSummary.getKey() + "  " + "(size = " + objectSummary.getSize() + ")");
			objectList.add(objectSummary);
		}

		return objectList;
	}
	
	/**
	 * delete user files functionality
	 * @param keyName
	 */
	public void deleteUserFiles(String keyName)
	{
		logger.info("BUCKET NAME: keyname={}", keyName);
		if (StringUtils.isEmpty(bucketName)) {
			logger.error("Bucket Name is empty. Please check the configurations");
			throw new RuntimeException("Bucket Name is empty. Please check the configurations");
		}
		try {
		 
		 String bucketVersionStatus = s3client.getBucketVersioningConfiguration(bucketName.trim()).getStatus();
		 if (!bucketVersionStatus.equals(BucketVersioningConfiguration.ENABLED)) {
			 System.out.printf("Bucket %s is not versioning-enabled.", bucketName);
		 }
		 else
		 {
			 System.out.println("Deleting versioned object " + keyName);
			 
			 for ( S3VersionSummary version : S3Versions.forKey(s3client, bucketName.trim(), keyName) ) {
				 
				    String versionId = version.getVersionId();          
				    s3client.deleteVersion(bucketName.trim(), keyName, versionId);
				}
		 }
		}
		catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
        }
	}
	
    
	/**
	 * get CloudFront Url for user files
	 * @param keyName
	 * @return
	 */
	public String getFileURL(String keyName) {
		try {
			
			URL fileurl = s3client.getUrl(bucketName.trim(), keyName);
			String oldurl = fileurl.toString();
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(oldurl);
			String newurl = builder.host("d1t14z5wgf455n.cloudfront.net").toUriString();
			logger.info("FILE URL: fileurl={}", newurl);
			
			return newurl;
			
		} catch (AmazonServiceException ase) {
			logger.info("Caught an AmazonServiceException from PUT requests, rejected reasons:");
			logger.info("Error Message:    " + ase.getMessage());
			logger.info("HTTP Status Code: " + ase.getStatusCode());
			logger.info("AWS Error Code:   " + ase.getErrorCode());
			logger.info("Error Type:       " + ase.getErrorType());
			logger.info("Request ID:       " + ase.getRequestId());
			throw ase;
		} catch (AmazonClientException ace) {
			logger.info("Caught an AmazonClientException: ");
			logger.info("Error Message: " + ace.getMessage());
			throw ace;
		}
	}

	
	/**
	 * Upload files to bucket functionality
	 * @param keyName
	 * @param file
	 * @param fileDescription
	 * @param firstName
	 * @param lastName
	 * @return
	 */
	public String uploadFile(String keyName, MultipartFile file, String fileDescription, String firstName, String lastName) {
		
		try {
			
			
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength(file.getSize());
			metadata.addUserMetadata("file-description", fileDescription);
			metadata.addUserMetadata("user-firstname", firstName);
			metadata.addUserMetadata("user-lastname", lastName);

			if (StringUtils.isEmpty(bucketName)) {
				logger.error("Bucket Name is empty. Please check the configurations");
				throw new RuntimeException("Bucket Name is empty. Please check the configurations");
			}

			logger.info("BUCKET NAME: bucketName={}", bucketName);
			
			PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName.trim(), keyName, file.getInputStream(), metadata);
			s3client.putObject(putObjectRequest);
					
			//s3client.putObject(bucketName.trim(), keyName, file.getInputStream(), metadata);
								

			URL fileurl = s3client.getUrl(bucketName.trim(), keyName);

			return fileurl.toString();

		} catch (IOException ioe) {
			logger.error("IOException: " + ioe.getMessage());
		} catch (AmazonServiceException ase) {
			logger.info("Caught an AmazonServiceException from PUT requests, rejected reasons:");
			logger.info("Error Message:    " + ase.getMessage());
			logger.info("HTTP Status Code: " + ase.getStatusCode());
			logger.info("AWS Error Code:   " + ase.getErrorCode());
			logger.info("Error Type:       " + ase.getErrorType());
			logger.info("Request ID:       " + ase.getRequestId());
			throw ase;
		} catch (AmazonClientException ace) {
			logger.info("Caught an AmazonClientException: ");
			logger.info("Error Message: " + ace.getMessage());
			throw ace;
		}

		return null;
	}
	
	
	/**
	 * add users and admin to groups functionality
	 * @param username
	 * @param groupname
	 */
	public void addUserToGroup(String username, String groupname) {

		AdminAddUserToGroupRequest addUserToGroupRequest = new AdminAddUserToGroupRequest().withGroupName(groupname)
				.withUserPoolId(userPoolID).withUsername(username);

		cognitoclientprovider.adminAddUserToGroup(addUserToGroupRequest);

		cognitoclientprovider.shutdown();

	}
	
	
	/**
	 * get S3 object metadata functionality
	 * @param keyName
	 * @return
	 */
	public ObjectMetadata getFileMetaData(String keyName)
	{
		ObjectMetadata objectMetadata = s3client.getObject(bucketName.trim(), keyName).getObjectMetadata();
		return objectMetadata;
	}

}
