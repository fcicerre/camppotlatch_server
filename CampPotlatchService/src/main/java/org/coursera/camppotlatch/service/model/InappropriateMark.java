package org.coursera.camppotlatch.service.model;

import java.util.Date;

import org.coursera.camppotlatch.service.json.DateJsonDeserializer;
import org.coursera.camppotlatch.service.json.DateJsonSerializer;
import org.coursera.camppotlatch.service.repository.DateTypeConverter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshalling;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBVersionAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Objects;
import com.google.gson.annotations.Expose;

@DynamoDBTable(tableName = "CampPotlach-Inappropriates")
public class InappropriateMark {
	
	@Expose
	private String giftId;
	@Expose
	private String userLogin;
	@Expose
	private Date createTime;
	
	private Long version;
	
	public InappropriateMark() {
	}
	
	public InappropriateMark(String giftId, String userLogin, Date createTime) {
		this.giftId = giftId;
		this.userLogin = userLogin;
		this.createTime = createTime;
	}
	
	@DynamoDBHashKey
	public String getGiftId() {
		return giftId;
	}
	public void setGiftId(String giftId) {
		this.giftId = giftId;
	}	
	
	@DynamoDBRangeKey
	public String getUserLogin() {
		return userLogin;
	}
	public void setUserLogin(String userLogin) {
		this.userLogin = userLogin;
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
	
	@DynamoDBVersionAttribute
	public Long getVersion() {
		return version;
	}
	public void setVersion(Long version) {
		this.version = version;
	}
	
	/**
	 * Two inappropriate marks will generate the same hashcode if they have exactly the same
	 * values for their giftId and userLogin.
	 * 
	 */
	@Override
	public int hashCode() {
		// Google Guava provides great utilities for hashing
		return Objects.hashCode(giftId, userLogin);
	}

	/**
	 * Two inappropriate marks are considered equal if they have exactly the same values for
	 * their giftId and userLogin.
	 * 
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof InappropriateMark) {
			InappropriateMark other = (InappropriateMark) obj;
			// Google Guava provides great utilities for equals too!
			return Objects.equal(giftId, other.giftId)
					&& Objects.equal(userLogin, other.userLogin);
		} else {
			return false;
		}
	}
}
