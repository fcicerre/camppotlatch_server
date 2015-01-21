package org.coursera.camppotlatch.service.model;

import java.util.Date;

import org.coursera.camppotlatch.service.json.DateJsonDeserializer;
import org.coursera.camppotlatch.service.json.DateJsonSerializer;
import org.coursera.camppotlatch.service.repository.DateTypeConverter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshalling;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBVersionAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.S3Link;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Objects;
import com.google.gson.annotations.Expose;

@DynamoDBTable(tableName = "CampPotlach-Users")
public class User {
	
	public static final int THUMBNAIL_WIDTH = 128;
	public static final int THUMBNAIL_HEIGHT = 128;

	@Expose
	private String login;
	@Expose
	private String password;
	@Expose
	private String roles;
	@Expose
	private String name;
	@Expose
	private String email;
	
	@Expose
	private String imageId;
	
	private String imageContentType;
	private String imagePath;
	private S3Link imageS3Link;
	
	@Expose
	private String city;
	@Expose
	private String country;
	@Expose
	private Date createTime;
	@Expose
	private Boolean disableInappropriate;
	@Expose
	private Integer giftsReloadPeriod;
	@Expose
	private Integer likesCount;
	@Expose
	private Integer inappropriatesCount;
	@Expose
	private Integer postedGiftsCount;
	
	private Long version;
	
	public User() {
		this(null, null, null);
	}
	
	public User(String login, String password, String roles) {
		this.login = login;
		this.password = password;
		this.roles = roles;
		
		this.disableInappropriate = false;
		this.giftsReloadPeriod = 5;
		this.likesCount = 0;
		this.inappropriatesCount = 0;
		this.postedGiftsCount = 0;
	}
	
	@DynamoDBHashKey
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
	
	@DynamoDBAttribute
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	@DynamoDBAttribute
	public String getRoles() {
		return roles;
	}
	public void setRoles(String roles) {
		this.roles = roles;
	}
	
	@DynamoDBAttribute
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@DynamoDBAttribute
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	@DynamoDBAttribute
	public String getImageId() {
		return imageId;
	}
	public void setImageId(String imageId) {
		this.imageId = imageId;
	}
	
	@JsonIgnore
	@DynamoDBAttribute
	public String getImageContentType() {
		return imageContentType;
	}
	public void setImageContentType(String imageContentType) {
		this.imageContentType = imageContentType;
	}

	@JsonIgnore
	@DynamoDBIgnore
	public String getImagePath() {
		return imagePath;
	}
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	@JsonIgnore
	@DynamoDBAttribute
	public S3Link getImageS3Link() {
		return imageS3Link;
	}
	public void setImageS3Link(S3Link imageS3Link) {
		this.imageS3Link = imageS3Link; 
	}

	@DynamoDBAttribute
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	
	@DynamoDBAttribute
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	
	@DynamoDBAttribute
	@DynamoDBMarshalling(marshallerClass = DateTypeConverter.class)
	@JsonSerialize(using=DateJsonSerializer.class)
	@JsonDeserialize(using=DateJsonDeserializer.class)
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	@DynamoDBAttribute
	public Boolean getDisableInappropriate() {
		return disableInappropriate;
	}
	public void setDisableInappropriate(Boolean disableInappropriate) {
		this.disableInappropriate = disableInappropriate;
	}
	
	@DynamoDBAttribute
	public Integer getGiftsReloadPeriod() {
		return giftsReloadPeriod;
	}
	public void setGiftsReloadPeriod(Integer giftsReloadPeriod) {
		this.giftsReloadPeriod = giftsReloadPeriod;
	}
	
	@DynamoDBAttribute
	public Integer getLikesCount() {
		return likesCount;
	}
	public void setLikesCount(Integer likesCount) {
		this.likesCount = likesCount;
	}
	
	@DynamoDBAttribute
	public Integer getInappropriatesCount() {
		return inappropriatesCount;
	}
	public void setInappropriatesCount(Integer inappropriatesCount) {
		this.inappropriatesCount = inappropriatesCount;
	}
	
	@DynamoDBAttribute
	public Integer getPostedGiftsCount() {
		return postedGiftsCount;
	}
	public void setPostedGiftsCount(Integer postedGiftsCount) {
		this.postedGiftsCount = postedGiftsCount;
	}
	
	@DynamoDBVersionAttribute
	public Long getVersion() {
		return version;
	}
	public void setVersion(Long version) {
		this.version = version;
	}
	
	/**
	 * Two users will generate the same hashcode if they have exactly the same
	 * values for their name.
	 * 
	 */
	@Override
	public int hashCode() {
		// Google Guava provides great utilities for hashing
		return Objects.hashCode(name);
	}

	/**
	 * Two users are considered equal if they have exactly the same values for
	 * their name.
	 * 
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof User) {
			User other = (User) obj;
			// Google Guava provides great utilities for equals too!
			return Objects.equal(name, other.name);
		} else {
			return false;
		}
	}	
}
