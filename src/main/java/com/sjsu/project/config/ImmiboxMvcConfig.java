package com.sjsu.project.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.resource.ResourceUrlProvider;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

@Configuration
@EnableWebMvc
public class ImmiboxMvcConfig extends WebMvcConfigurationSupport  {

	Logger LOG = LoggerFactory.getLogger(ImmiboxMvcConfig.class);

@Value("${aws.access.key.id}")
public String awsId;
	
@Value("${aws.secret.access.key}")
private String awsKey;

@Value("${aws.region}")
private String awsRegion;

@Bean
public MultipartResolver multipartResolver() {
   StandardServletMultipartResolver resolver = new StandardServletMultipartResolver();
   return resolver;
}	

@Override
public ResourceUrlProvider mvcResourceUrlProvider() {
	// TODO Auto-generated method stub
	return super.mvcResourceUrlProvider();
}




/*
 * Connect to AWS S3 client
 */
@Bean
public AmazonS3 s3client()
{
	BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsId, awsKey);
	AmazonS3  s3client  = AmazonS3ClientBuilder.standard()
							.withCredentials(new AWSStaticCredentialsProvider(awsCreds))
							.withRegion(awsRegion)
							.build();
	
	LOG.info("AWSS3 s3client={}", s3client);
	return s3client;
}

/*
 * Connect to AWS Cognito client
 */
@Bean
public AWSCognitoIdentityProvider getAmazonCognitoIdentityClient()
{
	 BasicAWSCredentials awsCreds = new BasicAWSCredentials(awsId, awsKey);

     return AWSCognitoIdentityProviderClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                           .withRegion(awsRegion)
                           .build();

 }

}