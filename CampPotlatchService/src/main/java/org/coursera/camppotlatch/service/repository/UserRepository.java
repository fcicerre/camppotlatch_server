package org.coursera.camppotlatch.service.repository;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.coursera.camppotlatch.service.file.UserImageFileHelper;
import org.coursera.camppotlatch.service.model.User;
import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.google.common.base.Objects;

public class UserRepository {
	private static final int MAX_SCAN_SIZE = 1000;
	
	//@Autowired
	private AmazonDynamoDB dynamoDBClient;
	
	//@Autowired
	private AWSCredentialsProvider awsCredentialsProvider;
	
	public UserRepository(AmazonDynamoDB dynamoDBClient, AWSCredentialsProvider awsCredentialsProvider) {
		this.dynamoDBClient = dynamoDBClient;
		this.awsCredentialsProvider = awsCredentialsProvider;
	}
	
	// Find all
	public Collection<User> getAll(Integer offset, Integer limit) {
		DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient, awsCredentialsProvider);

		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        List<User> scanResult = mapper.scan(User.class, scanExpression);
		
		// Get users between the positions (page * size) and ((page + 1) * size) - 1
		int curCount = 0;
		Collection<User> result = new ArrayList<User>();
		for(User user : scanResult) {
			if (offset >= 0 && limit >= 0) {
				if (curCount >= offset + limit)
					break;
				
				if (curCount >= offset) {
					result.add(user);
				}
			} else {
				result.add(user);
			}
			
			curCount++;
		}
		
		return result;
	}
	
	// Find by login
	public User findByLogin(String login) {
		DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient, awsCredentialsProvider);
		
		User userKey = new User();
		userKey.setLogin(login);
		
		User user = mapper.load(userKey);
        	
        return user;
	}
	
	/**
	 * Find all users filtered by name part and ordered by creation time descending
	 */
	public Collection<User> findAll(String namePart, Integer offset, Integer limit) {
		DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient, awsCredentialsProvider);
		
		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        List<User> scanResult = mapper.scan(User.class, scanExpression);
        
        // Filter users by name part and sorted by name in ascending order
        final Comparator<String> nameAscComparator = new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return -o1.compareTo(o2);
			}
        };
        int curCount = 0;
        SortedMap<String, User> sortedUsers = new TreeMap<String, User>(nameAscComparator);
        Pattern patName = Pattern.compile(".*" + namePart + ".*", Pattern.CASE_INSENSITIVE);
		for(User user : scanResult) {
			curCount++;
			if (curCount > MAX_SCAN_SIZE)
				break;
			
			// Discard users which name does not match the name part
			if (namePart != null && !namePart.equals("") && 
					!patName.matcher(user.getName()).matches())
				continue;
			
			sortedUsers.put(user.getName(), user);
		}
		
		// Get users between the positions (page * size) and ((page + 1) * size) - 1
		curCount = 0;
		Collection<User> result = new ArrayList<User>();
		for(User user : sortedUsers.values()) {
			if (offset >= 0 && limit >= 0) {
				if (curCount >= offset + limit)
					break;
				
				if (curCount >= offset) {
					result.add(user);
				}
			} else {
				result.add(user);
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
	 * Find top users gift givers filtered by name part which are ordered by likes descending and creation time descending
	 */
	public Collection<User> findTopGiftGivers(String namePart, Integer offset, Integer limit) {
		DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient, awsCredentialsProvider);
		
		DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        List<User> scanResult = mapper.scan(User.class, scanExpression);
        
        // Filter users by name part and sorted by likes and creation date in descending order
        final Comparator<LikesCreationDate> topUsersDescDateDescComparator = new Comparator<LikesCreationDate>() {
			@Override
			public int compare(LikesCreationDate o1, LikesCreationDate o2) {
				return -o1.compareTo(o2);
			}
        };
        int curCount = 0;
        SortedMap<LikesCreationDate, User> sortedUsers = 
        		new TreeMap<LikesCreationDate, User>(topUsersDescDateDescComparator);
        Pattern patName = Pattern.compile(".*" + namePart + ".*", Pattern.CASE_INSENSITIVE);
		for(User user : scanResult) {
			curCount++;
			if (curCount > MAX_SCAN_SIZE)
				break;
			
			// Discard users which name does not match the name part
			if (namePart != null && !namePart.equals("") && 
					!patName.matcher(user.getName()).matches())
				continue;
			
			sortedUsers.put(new LikesCreationDate(user.getLikesCount(), user.getCreateTime()), user);
		}
		
		// Get users between the positions (page * size) and ((page + 1) * size) - 1
		curCount = 0;
		Collection<User> result = new ArrayList<User>();
		for(User user : sortedUsers.values()) {
			if (offset >= 0 && limit >= 0) {
				if (curCount >= offset + limit)
					break;
				
				if (curCount >= offset) {
					result.add(user);
				}
			} else {
				result.add(user);
			}
			
			curCount++;
		}
		
		return result;		
	}
	
	// Save
	public User save(User user) {
		DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient, awsCredentialsProvider);
		
		mapper.save(user);
		
		return user;
	}
	
	// Delete
	public void delete(String login) {
		DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient, awsCredentialsProvider);
		
		User userKey = new User();
		userKey.setLogin(login);
		
		User user = mapper.load(userKey);
		
		// Remove the user image
		UserImageFileHelper userImageHelper = new UserImageFileHelper(mapper);
		userImageHelper.removeUserImage(user);
		
		// Remove the user
		mapper.delete(user);
	}

	/**
	 * Save image to S3
	 */
	public void saveImageFile(User user, InputStream inputStream) throws Exception {
		DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient, awsCredentialsProvider);
		
		UserImageFileHelper userImageHelper = new UserImageFileHelper(mapper);
		userImageHelper.saveUserImage(user, inputStream);
		
		mapper.save(user);
	}
	
	/**
	 * Load image from S3
	 */
	public void loadImageFile(User user, OutputStream outputStream) throws Exception {
		DynamoDBMapper mapper = new DynamoDBMapper(dynamoDBClient, awsCredentialsProvider);
		
		UserImageFileHelper userImageHelper = new UserImageFileHelper(mapper);
		userImageHelper.loadUserImage(user, outputStream);
	}
}
