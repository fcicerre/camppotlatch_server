package org.coursera.camppotlatch.service;

import javax.servlet.MultipartConfigElement;

import org.coursera.camppotlatch.service.auth.OAuth2SecurityConfiguration;
import org.coursera.camppotlatch.service.commons.AppContext;
import org.coursera.camppotlatch.service.repository.GiftRepository;
import org.coursera.camppotlatch.service.repository.InappropriateMarkRepository;
import org.coursera.camppotlatch.service.repository.LikeMarkRepository;
import org.coursera.camppotlatch.service.repository.UserRepository;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.MultiPartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.SystemPropertiesCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

//Tell Spring to automatically inject any dependencies that are marked in
//our classes with @Autowired
@EnableAutoConfiguration
//Tell Spring to automatically create a JPA implementation of our
//VideoRepository
//@EnableDynamoDBRepositories(basePackages = "org.coursera.camppotlach")
@EnableDynamoDBRepositories()
//Tell Spring to turn on WebMVC (e.g., it should enable the DispatcherServlet
//so that requests can be routed to our Controllers)
@EnableWebMvc
//Tell Spring that this object represents a Configuration for the
//application
@Configuration
//Tell Spring to go and scan our controller package (and all sub packages) to
//find any Controllers or other components that are part of our application.
//Any class in this package that is annotated with @Controller is going to be
//automatically discovered and connected to the DispatcherServlet.
@ComponentScan
@Import(OAuth2SecurityConfiguration.class)
public class Application {
	//private static final String MAX_REQUEST_SIZE = "150MB";

	// Tell Spring to launch our app!
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
		
	/*
	@Value("${amazon.aws.accesskey}")
    private String amazonAWSAccessKey;

    @Value("${amazon.aws.secretkey}")
    private String amazonAWSSecretKey;	
	
	@Value("${amazon.dynamodb.endpoint}")
    private String amazonDynamoDBEndpoint;

    @Value("${amazon.s3.endpoint}")
    private String amazonS3Endpoint;
	 */
	
    /*
    @Value("${AWS_ACCESS_KEY_ID}")
    private String amazonAWSAccessKey;

    @Value("${AWS_SECRET_KEY}")
    private String amazonAWSSecretKey;
     */


	// This configuration element adds the ability to accept multipart
	// requests to the web container.
	@Bean
    public MultipartConfigElement multipartConfigElement() {
		return AppContext.multipartConfigElement();
	}
	
    @Bean
    public AWSCredentialsProvider amazonAWSCredentialsProvider() {
    	return AppContext.amazonAWSCredentialsProvider();
    }
    
    @Bean
    public AWSCredentials amazonAWSCredentials() {
    	return AppContext.amazonAWSCredentials();
    }
    
    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
    	return AppContext.amazonDynamoDB();
    }
    
    @Bean
    public GiftRepository giftRepository() {
    	return AppContext.giftRepository();
    }
    
    @Bean
    public UserRepository userRepository() {
    	return AppContext.userRepository();
    }
    
    @Bean
    public LikeMarkRepository likeMarkRepository() {
    	return AppContext.likeMarkRepository();
    }
    
    @Bean
    public InappropriateMarkRepository inappropriateMarkRepository() {
    	return AppContext.inappropriateMarkRepository();
    }
}
