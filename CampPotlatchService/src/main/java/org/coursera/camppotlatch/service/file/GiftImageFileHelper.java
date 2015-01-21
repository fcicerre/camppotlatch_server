package org.coursera.camppotlatch.service.file;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.coursera.camppotlatch.service.model.Gift;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.S3Link;
import com.amazonaws.services.s3.model.PutObjectResult;

public class GiftImageFileHelper {
	public static final String CAMPPOTLACH_S3_BUCKET = "camppotlach-v0.1";
	
	public static final String JPEG_CONTENT_TYPE = "image/jpeg";
	public static final String JPEG_EXTENSION = ".jpg";
	
	private static final String GIFT_IMAGE_PREFIX = "gift_image_";

	//private DynamoDBMapper mapper;
	private S3FileManagerHelper s3FileManager;
	
	public GiftImageFileHelper(DynamoDBMapper mapper) {
		//this.mapper = mapper;
		s3FileManager = new S3FileManagerHelper(mapper);
	}
	
	public void saveGiftImage(Gift gift, InputStream inputStream) throws Exception {
		if (!gift.getImageContentType().equals(JPEG_CONTENT_TYPE))
			throw new Exception("The gift image is not " + JPEG_CONTENT_TYPE);
		
		String s3Bucket = CAMPPOTLACH_S3_BUCKET;
		String s3Key = GIFT_IMAGE_PREFIX + gift.getId() + JPEG_EXTENSION;
		
		S3Link s3Link = s3FileManager.createS3Link(s3Bucket, s3Key, inputStream);
		
		gift.setImageS3Link(s3Link);
	}
	
	public void loadGiftImage(Gift gift, OutputStream outputStream) throws Exception {
		if (!gift.getImageContentType().equals(JPEG_CONTENT_TYPE))
			throw new Exception("The gift image is not " + JPEG_CONTENT_TYPE);

		S3Link s3Link = gift.getImageS3Link();
		s3FileManager.loadS3Link(s3Link, outputStream);
	}
	
	public void removeGiftImage(Gift gift) {
		S3Link s3Link = gift.getImageS3Link();
		if (s3Link != null)
			s3FileManager.removeS3Link(s3Link);
	}
}
