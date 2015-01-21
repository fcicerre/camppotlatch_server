package org.coursera.camppotlatch.service.repository;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.coursera.camppotlatch.service.commons.DateUtils;
import org.coursera.camppotlatch.service.file.GiftImageFileHelper;
import org.coursera.camppotlatch.service.file.S3FileManagerHelper;
import org.coursera.camppotlatch.service.model.Gift;
import org.coursera.camppotlatch.service.model.OperationResult;
import org.coursera.camppotlatch.service.model.OperationResult.OperationResultState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.google.common.base.Objects;

public class GiftRepository {
	private static final int MAX_SCAN_SIZE = 1000;
	
	/*
	@Autowired
	private GiftPageableRepository giftRepository;
	*/
	
	//@Autowired
	private AmazonDynamoDB dynamoDBClient;
	
	//@Autowired
	private AWSCredentialsProvider awsCredentialsProvider;

	public GiftRepository(AmazonDynamoDB dynamoDBClient, AWSCredentialsProvider awsCredentialsProvider) {
		this.dynamoDBClient = dynamoDBClient;
		this.awsCredentialsProvider = awsCredentialsProvider;
	}
	
	/*
	// Find all
	public Collection<Gift> getAll(Integer page, Integer size) {
		DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient, awsCredentialsProvider);

		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        List<Gift> scanResult = mapper.scan(Gift.class, scanExpression);
		
		// Get gifts between the positions (page * size) and ((page + 1) * size) - 1
		int curCount = 0;
		Collection<Gift> result = new ArrayList<Gift>();
		for(Gift gift : scanResult) {
			if (page >= 0 && size > 0) {
				if (curCount >= ((page + 1) * size))
					break;
				
				if (curCount >= (page * size)) {
					result.add(gift);
				}
			} else {
				result.add(gift);
			}
			
			curCount++;
		}
		
		return result;
	}
	*/

	// Find all
	public Collection<Gift> getAll(Integer offset, Integer limit) {
		DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient, awsCredentialsProvider);

		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        List<Gift> scanResult = mapper.scan(Gift.class, scanExpression);
		
		// Get gifts between the positions (page * size) and ((page + 1) * size) - 1
		int curCount = 0;
		Collection<Gift> result = new ArrayList<Gift>();
		for(Gift gift : scanResult) {
			if (offset >= 0 && limit >= 0) {
				if (curCount >= offset + limit)
					break;
				
				if (curCount >= offset) {
					result.add(gift);
				}
			} else {
				result.add(gift);
			}
			
			curCount++;
		}
		
		return result;
	}
	
	// Find by id
	public Gift findById(String id) {
		DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient, awsCredentialsProvider);
		
		Gift giftKey = new Gift();
		giftKey.setId(id);
		
		Gift gift = mapper.load(giftKey);
        	
        return gift;
	}
	
	/**
	 * Find all caption gifts filtered by title part and ordered by creation time descending
	 */
	public Collection<Gift> findAll(Date minCreationTime, Date maxCreationTime, String titlePart, Boolean filterInappropriate, String userLogin, Integer offset, Integer limit) {
		DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient, awsCredentialsProvider);
		
		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
		
		// Scans caption gifts by creation time range
		if (minCreationTime != null && maxCreationTime != null) {
			DateUtils dateUtils = new DateUtils();
			String minCreationTimeStr = dateUtils.convertToISO8601DateFormat(minCreationTime);
			String maxCreationTimeStr = dateUtils.convertToISO8601DateFormat(maxCreationTime);
			
			Condition betweenCreationTimeCondition = new Condition()
	    	.withComparisonOperator(ComparisonOperator.BETWEEN.toString())
	    	.withAttributeValueList(new AttributeValue().withS(minCreationTimeStr), new AttributeValue().withS(maxCreationTimeStr));
			
	        scanExpression.addFilterCondition("createTime", betweenCreationTimeCondition);
		}
		
		Condition captionGiftIdEqualsNullCondition = new Condition()
    	.withComparisonOperator(ComparisonOperator.NULL);
		
        scanExpression.addFilterCondition("captionGiftId", captionGiftIdEqualsNullCondition);
        
        List<Gift> scanResult = mapper.scan(Gift.class, scanExpression);
        
        // Filter gifts by title part and sorted by creation date in descending order
        final Comparator<Date> dateDescComparator = new Comparator<Date>() {
			@Override
			public int compare(Date o1, Date o2) {
				return -o1.compareTo(o2);
			}
        };
        int curCount = 0;
        SortedMap<Date, Gift> sortedGifts = new TreeMap<Date, Gift>(dateDescComparator);
        Pattern patTitle = Pattern.compile(".*" + titlePart + ".*", Pattern.CASE_INSENSITIVE);
		for(Gift gift : scanResult) {
			curCount++;
			if (curCount > MAX_SCAN_SIZE)
				break;
			
			// Discard related gifts (gifts in a chain)
			if (gift.getCaptionGiftId() != null)
				continue;
			
			// Discard gifts which the title does not match the title part
			if (titlePart != null && !titlePart.equals("") && 
					!patTitle.matcher(gift.getTitle()).matches())
				continue;
			
			// Filter inappropriate from other users
			if (filterInappropriate && userLogin != null 
					&& !gift.getCreatorLogin().equals(userLogin) && gift.getInappropriateCount() > 0)
				continue;
			
			sortedGifts.put(gift.getCreateTime(), gift);
		}
		
		// Get gifts between the positions (page * size) and ((page + 1) * size) - 1
		curCount = 0;
		Collection<Gift> result = new ArrayList<Gift>();
		for(Gift gift : sortedGifts.values()) {
			if (offset >= 0 && limit >= 0) {
				if (curCount >= offset + limit)
					break;
				
				if (curCount >= offset) {
					result.add(gift);
				}
			} else {
				result.add(gift);
			}
			
			curCount++;
		}
		
		return result;
	}
	
	// Likes and creation date class
	private class LikesCreationDate implements Comparable<LikesCreationDate> {
		public Integer likes;
		public Date creationDate;
		public LikesCreationDate(Integer likes, Date creationDate) {
			this.likes = likes;
			this.creationDate = creationDate;
		}

		@Override
		public int hashCode() {
			// Google Guava provides great utilities for hashing
			return Objects.hashCode(likes, creationDate);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof LikesCreationDate) {
				LikesCreationDate other = (LikesCreationDate) obj;
				// Google Guava provides great utilities for equals too!
				return Objects.equal(likes, other.likes)
						&& Objects.equal(creationDate, other.creationDate);
			} else {
				return false;
			}
		}

		@Override
		public int compareTo(LikesCreationDate o) {
			if (likes != o.likes)
				return (likes < o.likes) ? -1 : 1;
			else if (!creationDate.equals(o.creationDate))
				return creationDate.compareTo(o.creationDate);
			else
				return 0;
		}	
	}
	
	/**
	 * Find top caption gifts filtered by title part and ordered by likes descending and creation time descending
	 */
	public Collection<Gift> findTopGifts(Date minCreationTime, Date maxCreationTime, String titlePart, 
			Boolean filterInappropriate, String userLogin, Integer offset, Integer limit) {
		DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient, awsCredentialsProvider);
		
		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
		
		if (minCreationTime != null && maxCreationTime != null) {
			DateUtils dateUtils = new DateUtils();
			String minCreationTimeStr = dateUtils.convertToISO8601DateFormat(minCreationTime);
			String maxCreationTimeStr = dateUtils.convertToISO8601DateFormat(maxCreationTime);
			
			// Scans gifts by creation time range
			Condition betweenCreationTimeCondition = new Condition()
	    	.withComparisonOperator(ComparisonOperator.BETWEEN.toString())
	    	.withAttributeValueList(new AttributeValue().withS(minCreationTimeStr), new AttributeValue().withS(maxCreationTimeStr));
	
			scanExpression.addFilterCondition("createTime", betweenCreationTimeCondition);
		}
		
		Condition captionGiftIdEqualsNullCondition = new Condition()
    	.withComparisonOperator(ComparisonOperator.NULL);
		
        scanExpression.addFilterCondition("captionGiftId", captionGiftIdEqualsNullCondition);
        
        List<Gift> scanResult = mapper.scan(Gift.class, scanExpression);
        
        // Filter gifts by title part and sorted by likes and creation date in descending order
        final Comparator<LikesCreationDate> topGiftsDescDateDescComparator = new Comparator<LikesCreationDate>() {
			@Override
			public int compare(LikesCreationDate o1, LikesCreationDate o2) {
				return -o1.compareTo(o2);
			}
        };
        int curCount = 0;
        SortedMap<LikesCreationDate, Gift> sortedGifts = 
        		new TreeMap<LikesCreationDate, Gift>(topGiftsDescDateDescComparator);
        Pattern patTitle = Pattern.compile(".*" + titlePart + ".*", Pattern.CASE_INSENSITIVE);
		for(Gift gift : scanResult) {
			curCount++;
			if (curCount > MAX_SCAN_SIZE)
				break;
			
			// Discard related gifts (gifts in a chain)
			if (gift.getCaptionGiftId() != null)
				continue;
			
			// Discard gifts which the title does not match the title part
			if (titlePart != null && !titlePart.equals("") && 
					!patTitle.matcher(gift.getTitle()).matches())
				continue;
			
			// Filter inappropriate from other users
			if (filterInappropriate && userLogin != null 
					&& !gift.getCreatorLogin().equals(userLogin) && gift.getInappropriateCount() > 0)
				continue;
			
			sortedGifts.put(new LikesCreationDate(gift.getLikesCount(), gift.getCreateTime()), gift);
		}
		
		// Get gifts between the positions (page * size) and ((page + 1) * size) - 1
		curCount = 0;
		Collection<Gift> result = new ArrayList<Gift>();
		for(Gift gift : sortedGifts.values()) {
			if (offset >= 0 && limit >= 0) {
				if (curCount >= offset + limit)
					break;
				
				if (curCount >= offset) {
					result.add(gift);
				}
			} else {
				result.add(gift);
			}
			
			curCount++;
		}
		
		return result;		
	}
	
	/**
	 * Find caption gifts of a creator, filtered by title part and ordered by creation time descending
	 */
	public Collection<Gift> findByCreatorLogin(String creatorLogin, Date minCreationTime, Date maxCreationTime, 
			String titlePart, boolean includeRelatedGifts, 
			Boolean filterInappropriate, String userLogin, Integer offset, Integer limit) throws Exception {
		DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient, awsCredentialsProvider);
		
		if (creatorLogin == null)
			throw new Exception("The creator login is null");
		
		Gift giftKey = new Gift();
		giftKey.setCreatorLogin(creatorLogin);
		
		DynamoDBQueryExpression<Gift> queryExpression = new DynamoDBQueryExpression<Gift>();
		queryExpression.setIndexName(Gift.CREATOR_LOGIN_INDEX);
		//queryExpression.setRangeKeyConditions(rangeKeyConditions);
		queryExpression.setHashKeyValues(giftKey);
		queryExpression.setConsistentRead(false);
		//queryExpression.setScanIndexForward(false);
        
        List<Gift> queryResult = mapper.query(Gift.class, queryExpression);
        
        // Filter gifts by title part and sorted by creation date in descending order
        final Comparator<Date> dateDescComparator = new Comparator<Date>() {
			@Override
			public int compare(Date o1, Date o2) {
				return -o1.compareTo(o2);
			}
        };
        int curCount = 0;
        SortedMap<Date, Gift> sortedGifts = new TreeMap<Date, Gift>(dateDescComparator);
        Pattern patTitle = Pattern.compile(".*" + titlePart + ".*", Pattern.CASE_INSENSITIVE);
		for(Gift gift : queryResult) {
			curCount++;
			if (curCount > MAX_SCAN_SIZE)
				break;
			
			// Discard related gifts (gifts in a chain)
			if (!includeRelatedGifts && gift.getCaptionGiftId() != null)
				continue;
			
			if (minCreationTime != null && maxCreationTime != null) {
				// Discard gifts out of creation date range
				if (gift.getCreateTime().compareTo(minCreationTime) < 0 || gift.getCreateTime().compareTo(maxCreationTime) > 0)
					continue;
			}
			
			// Discard gifts which the title does not match the title part
			if (titlePart != null && !titlePart.equals("") && 
					!patTitle.matcher(gift.getTitle()).matches())
				continue;
			
			// Filter inappropriate from other users
			if (filterInappropriate && userLogin != null 
					&& !gift.getCreatorLogin().equals(userLogin) && gift.getInappropriateCount() > 0)
				continue;
			
			sortedGifts.put(gift.getCreateTime(), gift);
		}
		
		// Get gifts between the positions (page * size) and ((page + 1) * size) - 1
		curCount = 0;
		Collection<Gift> result = new ArrayList<Gift>();
		for(Gift gift : sortedGifts.values()) {
			if (offset >= 0 && limit >= 0) {
				if (curCount >= offset + limit)
					break;
				
				if (curCount >= offset) {
					result.add(gift);
				}
			} else {
				result.add(gift);
			}
			
			curCount++;
		}
		
		return result;		
	}
	
	/**
	 * Find caption gifts which refers to a caption gift (gifts in a chain), 
	 * filtered by title part and ordered by creation time descending
	 */
	public Collection<Gift> findByCaptionGiftId(String captionGiftId, Date minCreationTime, Date maxCreationTime, 
			String titlePart, Boolean filterInappropriate, String userLogin, Integer offset, Integer limit) throws Exception {
		DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient, awsCredentialsProvider);
		
		if (captionGiftId == null)
			throw new Exception("The caption gift id is null");
		
		Gift giftKey = new Gift();
		giftKey.setCaptionGiftId(captionGiftId);
		DynamoDBQueryExpression<Gift> queryExpression = new DynamoDBQueryExpression<Gift>();
		queryExpression.setIndexName(Gift.CAPTION_GIFT_ID_INDEX);
		//queryExpression.setRangeKeyConditions(rangeKeyConditions);
		queryExpression.setHashKeyValues(giftKey);
		queryExpression.setConsistentRead(false);
		//queryExpression.setScanIndexForward(false);
        
        List<Gift> queryResult = mapper.query(Gift.class, queryExpression);
        
        // Filter gifts by title part and sorted by creation date in descending order
        final Comparator<Date> dateDescComparator = new Comparator<Date>() {
			@Override
			public int compare(Date o1, Date o2) {
				return -o1.compareTo(o2);
			}
        };
        int curCount = 0;
        SortedMap<Date, Gift> sortedGifts = new TreeMap<Date, Gift>(dateDescComparator);
        Pattern patTitle = Pattern.compile(".*" + titlePart + ".*", Pattern.CASE_INSENSITIVE);
		for(Gift gift : queryResult) {
			curCount++;
			if (curCount > MAX_SCAN_SIZE)
				break;
			
			if (minCreationTime != null && maxCreationTime != null) {
				// Discard gifts out of creation date range
				if (gift.getCreateTime().compareTo(minCreationTime) < 0 || gift.getCreateTime().compareTo(maxCreationTime) > 0)
					continue;
			}
			
			// Discard gifts which the title does not match the title part
			if (titlePart != null && !titlePart.equals("") && 
					!patTitle.matcher(gift.getTitle()).matches())
				continue;
			
			// Filter inappropriate from other users
			if (filterInappropriate && userLogin != null 
					&& !gift.getCreatorLogin().equals(userLogin) && gift.getInappropriateCount() > 0)
				continue;
			
			sortedGifts.put(gift.getCreateTime(), gift);
		}
		
		// Get gifts between the positions (page * size) and ((page + 1) * size) - 1
		curCount = 0;
		Collection<Gift> result = new ArrayList<Gift>();
		for(Gift gift : sortedGifts.values()) {
			if (offset >= 0 && limit >= 0) {
				if (curCount >= offset + limit)
					break;
				
				if (curCount >= offset) {
					result.add(gift);
				}
			} else {
				result.add(gift);
			}
			
			curCount++;
		}
		
		return result;		
	}
	
	// Save
	public Gift save(Gift gift) {
		DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient, awsCredentialsProvider);
		
		mapper.save(gift);
		
		return gift;
	}
	
	// Delete
	public void delete(String id) {
		DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient, awsCredentialsProvider);
		
		Gift giftKey = new Gift();
		giftKey.setId(id);
		
		Gift gift = mapper.load(giftKey);
		
		// Remove the gift image
		GiftImageFileHelper giftImageHelper = new GiftImageFileHelper(mapper);
		giftImageHelper.removeGiftImage(gift);
		
		// Remove the gift
		mapper.delete(gift);
	}

	/**
	 * Save image to S3
	 */
	public void saveImageFile(Gift gift, InputStream inputStream) throws Exception {
		DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient, awsCredentialsProvider);
		
		GiftImageFileHelper giftImageHelper = new GiftImageFileHelper(mapper);
		giftImageHelper.saveGiftImage(gift, inputStream);
		
		mapper.save(gift);
	}
	
	/**
	 * Load image from S3
	 */
	public void loadImageFile(Gift gift, OutputStream outputStream) throws Exception {
		DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient, awsCredentialsProvider);
		
		GiftImageFileHelper giftImageHelper = new GiftImageFileHelper(mapper);
		giftImageHelper.loadGiftImage(gift, outputStream);
	}
}
