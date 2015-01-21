package org.coursera.camppotlatch.service.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

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

public class LikeMarkRepository {
	private static final int MAX_SCAN_SIZE = 1000;
	
	//@Autowired
	private AmazonDynamoDB dynamoDBClient;
	
	//@Autowired
	private AWSCredentialsProvider awsCredentialsProvider;

	public LikeMarkRepository(AmazonDynamoDB dynamoDBClient, AWSCredentialsProvider awsCredentialsProvider) {
		this.dynamoDBClient = dynamoDBClient;
		this.awsCredentialsProvider = awsCredentialsProvider;
	}
	
	// Find all
	public Collection<LikeMark> getAll(Integer offset, Integer limit) {
		DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient, awsCredentialsProvider);

		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        List<LikeMark> scanResult = mapper.scan(LikeMark.class, scanExpression);
		
		// Get like marks between the positions (page * size) and ((page + 1) * size) - 1
		int curCount = 0;
		Collection<LikeMark> result = new ArrayList<LikeMark>();
		for(LikeMark likeMark : scanResult) {
			if (offset >= 0 && limit >= 0) {
				if (curCount >= offset + limit)
					break;
				
				if (curCount >= offset) {
					result.add(likeMark);
				}
			} else {
				result.add(likeMark);
			}
			
			curCount++;
		}
		
		return result;
	}
	
	// Find like mark by gift id and user login
	public LikeMark findByGiftIdAndUserLogin(String giftId, String userLogin) {
		DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient, awsCredentialsProvider);
		
		/*
		LikeMark likeMarkKey = new LikeMark();
		likeMarkKey.setGiftId(giftId);
		
		Condition rangeKeyCondition = new Condition()
	    	.withComparisonOperator(ComparisonOperator.EQ.toString())
	    	.withAttributeValueList(new AttributeValue().withS(userLogin));
		
		DynamoDBQueryExpression<LikeMark> queryExpression = new DynamoDBQueryExpression<LikeMark>()
			    .withHashKeyValues(likeMarkKey)
			    .withRangeKeyCondition("userLogin", rangeKeyCondition);
		
		List<LikeMark> likeMarks = mapper.query(LikeMark.class, queryExpression);
		
		LikeMark likeMark = null;
		if (likeMarks.size() > 0) {
			likeMark = likeMarks.get(0);
		}
		*/
		
		LikeMark likeMark = mapper.load(LikeMark.class, giftId, userLogin);		
		
        return likeMark;
	}
	
	// Find all like marks by gift id
	public Collection<LikeMark> findAllByGiftId(String giftId, Integer offset, Integer limit) {
		DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient, awsCredentialsProvider);
		
		LikeMark likeMarkKey = new LikeMark();
		likeMarkKey.setGiftId(giftId);
		
		DynamoDBQueryExpression<LikeMark> queryExpression = new DynamoDBQueryExpression<LikeMark>()
			    .withHashKeyValues(likeMarkKey);
		
		List<LikeMark> queryResult = mapper.query(LikeMark.class, queryExpression);
		
        // Get likes sorted by creation date in descending order
        final Comparator<Date> dateDescComparator = new Comparator<Date>() {
			@Override
			public int compare(Date o1, Date o2) {
				return -o1.compareTo(o2);
			}
        };
        int curCount = 0;
        SortedMap<Date, LikeMark> sortedLikes = new TreeMap<Date, LikeMark>(dateDescComparator);
		for(LikeMark likeMark : queryResult) {
			curCount++;
			if (curCount > MAX_SCAN_SIZE)
				break;
			
			sortedLikes.put(likeMark.getCreateTime(), likeMark);
		}
		
		// Get like marks between the positions (page * size) and ((page + 1) * size) - 1
		curCount = 0;
		Collection<LikeMark> result = new ArrayList<LikeMark>();
		for(LikeMark likeMark : sortedLikes.values()) {
			if (offset >= 0 && limit >= 0) {
				if (curCount >= offset + limit)
					break;
				
				if (curCount >= offset) {
					result.add(likeMark);
				}
			} else {
				result.add(likeMark);
			}
			
			curCount++;
		}
		
        return result;
	}	
	
	// Save
	public LikeMark save(LikeMark likeMark) throws Exception {
		DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient, awsCredentialsProvider);
		
		String giftId = likeMark.getGiftId();
		String userLogin = likeMark.getUserLogin();
		
		if (giftId == null || userLogin == null)
			throw new Exception("The gift id and user login of a like mark must be defined");
		
		/*
		LikeMark retrievedLikeMark = findByGiftIdAndUserLogin(giftId, userLogin);
		if (retrievedLikeMark != null)
			throw new Exception("There already is a like mark with the same gift id and user login");
		*/
		
		mapper.save(likeMark);
		
		return likeMark;
	}
	
	// Delete
	public void delete(String giftId, String userLogin) {
		DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient, awsCredentialsProvider);
		
		LikeMark likeMark = findByGiftIdAndUserLogin(giftId, userLogin);
		
		if (likeMark == null)
			return;
				
		// Remove the like mark
		mapper.delete(likeMark);
	}
}
