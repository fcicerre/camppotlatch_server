package org.coursera.camppotlatch.service.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.CopyOption;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.coursera.camppotlatch.service.model.Gift;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResourceLoader;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.S3Link;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.Region;
import com.amazonaws.services.s3.transfer.TransferManager;

public class S3FileManagerHelper {
	public static final String CAMPPOTLACH_DIR = ".camppotlach";
	public static final String TEMP_DIR = "temp";
	
    @Value("${amazon.s3.endpoint}")
    private String amazonS3Endpoint;
    
	private DynamoDBMapper mapper;

	public S3FileManagerHelper(DynamoDBMapper mapper) {
		this.mapper = mapper;
	}
	
	public S3Link createS3Link(String s3Bucket, String s3Key, InputStream inputStream) throws IOException {
		S3Link s3Link = mapper.createS3Link(Region.fromValue(amazonS3Endpoint), s3Bucket, s3Key);
		
		saveS3Link(s3Link, inputStream);
		
		return s3Link;
	}
	
	public void saveS3Link(S3Link s3Link, InputStream inputStream) throws IOException {
		byte[] buffer = IOUtils.toByteArray(inputStream);
		s3Link.uploadFrom(buffer);
		
		/*
		String tempFileName = UUID.randomUUID().toString() + ".temp";
		FileSystem fileSystem = FileSystems.getDefault();
		Path tempPath = fileSystem.getPath(CAMPPOTLACH_DIR, TEMP_DIR, tempFileName);
		
		try {
			Files.copy(inputStream, tempPath, StandardCopyOption.REPLACE_EXISTING);
		
			//PutObjectResult s3Object = s3Link.uploadFrom(tempPath.toFile());
			s3Link.uploadFrom(tempPath.toFile());
		} finally {
			Files.deleteIfExists(tempPath);
		}
		*/
	}
	
	public void loadS3Link(S3Link s3Link, OutputStream outputStream) {
		s3Link.downloadTo(outputStream);
	}
	
	public void removeS3Link(S3Link s3Link) {
		AmazonS3Client s3Client = s3Link.getAmazonS3Client();
		s3Client.deleteObject(s3Link.getBucketName(), s3Link.getKey());
	}
}
