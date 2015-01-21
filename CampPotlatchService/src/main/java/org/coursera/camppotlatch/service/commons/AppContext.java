package org.coursera.camppotlatch.service.commons;

import javax.servlet.MultipartConfigElement;

import org.coursera.camppotlatch.service.repository.GiftRepository;
import org.coursera.camppotlatch.service.repository.InappropriateMarkRepository;
import org.coursera.camppotlatch.service.repository.LikeMarkRepository;
import org.coursera.camppotlatch.service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.MultiPartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

public class AppContext {
	private static final String MAX_REQUEST_SIZE = "150MB";
		
	public static final String AMAZON_AWS_ACCESSKEY_KEY = "amazon.aws.accesskey";
	public static final String AMAZON_AWS_SECRETKEY_KEY = "amazon.aws.secretkey";
	public static final String AMAZON_DYNAMODB_ENDPOINT_KEY = "amazon.dynamodb.endpoint";
	public static final String AMAZON_S3_ENDPOINT_KEY = "amazon.s3.endpoint";
	
	private static MultipartConfigElement multipartConfigElement;
	private static BasicAWSCredentials amazonAWSCredentials;
	private static AmazonDynamoDB amazonDynamoDB;
	private static AWSCredentialsProvider amazonAWSCredentialsProvider;
	
	private static GiftRepository giftRepository;
	private static UserRepository userRepository;
	private static LikeMarkRepository likeMarkRepository;
	private static InappropriateMarkRepository inappropriateMarkRepository;
	
    public static MultipartConfigElement multipartConfigElement() {
    	if (multipartConfigElement == null) {
			// Setup the application container to be accept multipart requests
			final MultiPartConfigFactory factory = new MultiPartConfigFactory();
			
			// Place upper bounds on the size of the requests to ensure that
			// clients don't abuse the web container by sending huge requests
			factory.setMaxFileSize(MAX_REQUEST_SIZE);
			factory.setMaxRequestSize(MAX_REQUEST_SIZE);
	
			// Return the configuration to setup multipart in the container
			multipartConfigElement = factory.createMultipartConfig();
    	}
    	
    	return multipartConfigElement;
	}
    
    public static AWSCredentials amazonAWSCredentials() {
    	if (amazonAWSCredentials == null) {    	
			String amazonAWSAccessKey = System.getProperty(AMAZON_AWS_ACCESSKEY_KEY);
			String amazonAWSSecretKey = System.getProperty(AMAZON_AWS_SECRETKEY_KEY);
			
			// Setting corresponding Java system properties
			System.setProperty("aws.accessKeyId", amazonAWSAccessKey);
			System.setProperty("aws.secretKey", amazonAWSSecretKey);    	
			
			amazonAWSCredentials = new BasicAWSCredentials(amazonAWSAccessKey, amazonAWSSecretKey);
    	}
    	
		return amazonAWSCredentials;
    }
	
    public static AmazonDynamoDB amazonDynamoDB() {
    	if (amazonDynamoDB == null) {
	    	String amazonDynamoDBEndpoint = System.getProperty(AMAZON_DYNAMODB_ENDPOINT_KEY);
	    	
	        amazonDynamoDB = new AmazonDynamoDBClient(
	                amazonAWSCredentials());
	        
	        if (!StringUtils.isEmpty(amazonDynamoDBEndpoint)) {
	            amazonDynamoDB.setEndpoint(amazonDynamoDBEndpoint);
	        }
    	}
    	
        return amazonDynamoDB;
    }
    
    public static AWSCredentialsProvider amazonAWSCredentialsProvider() {
    	if (amazonAWSCredentialsProvider == null) {
    		amazonAWSCredentialsProvider = new SystemPropertiesCredentialsProvider();
    	}
    	
    	return amazonAWSCredentialsProvider;
    }
    
    public static GiftRepository giftRepository() {
    	if (giftRepository == null) {
    		giftRepository = new GiftRepository(amazonDynamoDB(), amazonAWSCredentialsProvider());
    	}
    	
    	return giftRepository;
    }
    
    public static UserRepository userRepository() {
    	if (userRepository == null) {
    		userRepository = new UserRepository(amazonDynamoDB(), amazonAWSCredentialsProvider());
    	}
    	
    	return userRepository;
    }
    
    public static LikeMarkRepository likeMarkRepository() {
    	if (likeMarkRepository == null) {
    		likeMarkRepository = 
    				new LikeMarkRepository(amazonDynamoDB(), amazonAWSCredentialsProvider());
    	}
    	
    	return likeMarkRepository;
    }
    
    public static InappropriateMarkRepository inappropriateMarkRepository() {
    	if (inappropriateMarkRepository == null) {
    		inappropriateMarkRepository = 
    				new InappropriateMarkRepository(amazonDynamoDB(), amazonAWSCredentialsProvider());
    	}
    	
    	return inappropriateMarkRepository;
    }
}
