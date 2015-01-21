package org.coursera.camppotlatch.service.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.coursera.camppotlatch.service.model.InappropriateMark;
import org.coursera.camppotlatch.service.model.LikeMark;
import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;

public class InappropriateMarkRepository {
	private static final int MAX_SCAN_SIZE = 1000;
	
	//@Autowired
	private AmazonDynamoDB dynamoDBClient;
	
	//@Autowired
	private AWSCredentialsProvider awsCredentialsProvider;

	public InappropriateMarkRepository(AmazonDynamoDB dynamoDBClient, AWSCredentialsProvider awsCredentialsProvider) {
		this.dynamoDBClient = dynamoDBClient;
		this.awsCredentialsProvider = awsCredentialsProvider;
	}

	// Find all
	public Collection<InappropriateMark> getAll(Integer offset, Integer limit) {
		DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient, awsCredentialsProvider);

		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        List<InappropriateMark> scanResult = mapper.scan(InappropriateMark.class, scanExpression);
		
		// Get inappropriate marks between the positions (page * size) and ((page + 1) * size) - 1
		int curCount = 0;
		Collection<InappropriateMark> result = new ArrayList<InappropriateMark>();
		for(InappropriateMark inappropriateMark : scanResult) {
			if (offset >= 0 && limit >= 0) {
				if (curCount >= offset + limit)
					break;
				
				if (curCount >= offset) {
					result.add(inappropriateMark);
				}
			} else {
				result.add(inappropriateMark);
			}
			
			curCount++;
		}
		
		return result;
	}
	
	// Find inappropriate mark by gift id and user login
	public InappropriateMark findByGiftIdAndUserLogin(String giftId, String userLogin) {
		DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient, awsCredentialsProvider);
		
		/*
		InappropriateMark inappropriateMarkKey = new InappropriateMark();
		inappropriateMarkKey.setGiftId(giftId);
		
		Condition rangeKeyCondition = new Condition()
	    	.withComparisonOperator(ComparisonOperator.EQ.toString())
	    	.withAttributeValueList(new AttributeValue().withS(userLogin));
		
		DynamoDBQueryExpression<InappropriateMark> queryExpression = 
				new DynamoDBQueryExpression<InappropriateMark>()
			    	.withHashKeyValues(inappropriateMarkKey)
			    	.withRangeKeyCondition("userLogin", rangeKeyCondition);
		
		List<InappropriateMark> inappropriateMarks = mapper.query(InappropriateMark.class, queryExpression);
		
		InappropriateMark inappropriateMark = null;
		if (inappropriateMarks.size() > 0) {
			inappropriateMark = inappropriateMarks.get(0);
		}
		*/
		
		InappropriateMark inappropriateMark = mapper.load(InappropriateMark.class, giftId, userLogin);		
		
        return inappropriateMark;
	}
	
	// Find all inappropriate marks by gift id
	public Collection<InappropriateMark> findAllByGiftId(String giftId, Integer offset, Integer limit) {
		DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient, awsCredentialsProvider);
		
		InappropriateMark inappropriateMarkKey = new InappropriateMark();
		inappropriateMarkKey.setGiftId(giftId);
		
		DynamoDBQueryExpression<InappropriateMark> queryExpression = 
				new DynamoDBQueryExpression<InappropriateMark>()
			    	.withHashKeyValues(inappropriateMarkKey);
		
		List<InappropriateMark> queryResult = mapper.query(InappropriateMark.class, queryExpression);
		
        // Get inappropriate marks sorted by creation date in descending order
        final Comparator<Date> dateDescComparator = new Comparator<Date>() {
			@Override
			public int compare(Date o1, Date o2) {
				return -o1.compareTo(o2);
			}
        };
        int curCount = 0;
        SortedMap<Date, InappropriateMark> sortedMarks = new TreeMap<Date, InappropriateMark>(dateDescComparator);
		for(InappropriateMark inappropriateMark : queryResult) {
			curCount++;
			if (curCount > MAX_SCAN_SIZE)
				break;
			
			sortedMarks.put(inappropriateMark.getCreateTime(), inappropriateMark);
		}
		
		// Get inappropriate marks between the positions (page * size) and ((page + 1) * size) - 1
		curCount = 0;
		Collection<InappropriateMark> result = new ArrayList<InappropriateMark>();
		for(InappropriateMark inappropriateMark : sortedMarks.values()) {
			if (offset >= 0 && limit >= 0) {
				if (curCount >= offset + limit)
					break;
				
				if (curCount >= offset) {
					result.add(inappropriateMark);
				}
			} else {
				result.add(inappropriateMark);
			}
			
			curCount++;
		}
		
        return result;
	}	
	
	// Create
	public InappropriateMark save(InappropriateMark inappropriateMark) throws Exception {
		DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient, awsCredentialsProvider);
		
		String giftId = inappropriateMark.getGiftId();
		String userLogin = inappropriateMark.getUserLogin();
		
		if (giftId == null || userLogin == null)
			throw new Exception("The gift id and user login of a inappropriate mark must be defined");
		
		/*
		InappropriateMark retrievedInappropriateMark = findByGiftIdAndUserLogin(giftId, userLogin);
		if (retrievedInappropriateMark != null)
			throw new Exception("There already is a inappropriate mark with the same gift id and user login");
		*/
		
		mapper.save(inappropriateMark);
		
		return inappropriateMark;
	}
	
	// Delete
	public void delete(String giftId, String userLogin) {
		DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient, awsCredentialsProvider);
		
		InappropriateMark inappropriateMark = findByGiftIdAndUserLogin(giftId, userLogin);
		
		if (inappropriateMark == null)
			return;
				
		// Remove the inappropriate mark
		mapper.delete(inappropriateMark);
	}
}
