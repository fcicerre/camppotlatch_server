package org.coursera.camppotlatch.service.file;

import java.io.InputStream;
import java.io.OutputStream;

import org.coursera.camppotlatch.service.model.Gift;
import org.coursera.camppotlatch.service.model.User;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.S3Link;

public class UserImageFileHelper {
	public static final String CAMPPOTLACH_S3_BUCKET = "camppotlach-v0.1";
	
	public static final String JPEG_CONTENT_TYPE = "image/jpeg";
	public static final String JPEG_EXTENSION = ".jpg";
	
	private static final String USER_IMAGE_PREFIX = "user_image_";

	private S3FileManagerHelper s3FileManager;
	
	public UserImageFileHelper(DynamoDBMapper mapper) {
		s3FileManager = new S3FileManagerHelper(mapper);
	}
	
	public void saveUserImage(User user, InputStream inputStream) throws Exception {
		if (!user.getImageContentType().equals(JPEG_CONTENT_TYPE))
			throw new Exception("The user image is not " + JPEG_CONTENT_TYPE);
		
		String s3Bucket = CAMPPOTLACH_S3_BUCKET;
		String s3Key = USER_IMAGE_PREFIX + user.getLogin() + JPEG_EXTENSION;
		
		S3Link s3Link = s3FileManager.createS3Link(s3Bucket, s3Key, inputStream);
		
		user.setImageS3Link(s3Link);
	}
	
	public void loadUserImage(User user, OutputStream outputStream) throws Exception {
		if (!user.getImageContentType().equals(JPEG_CONTENT_TYPE))
			throw new Exception("The user image is not " + JPEG_CONTENT_TYPE);

		S3Link s3Link = user.getImageS3Link();
		s3FileManager.loadS3Link(s3Link, outputStream);
	}
	
	public void removeUserImage(User user) {
		S3Link s3Link = user.getImageS3Link();
		if (s3Link != null)
			s3FileManager.removeS3Link(s3Link);
	}	
}
