package org.coursera.camppotlatch.service.repository;

import java.util.Date;

import org.coursera.camppotlatch.service.commons.DateUtils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshaller;

public class DateTypeConverter implements DynamoDBMarshaller<Date> {
	public final DateUtils dateUtils = new DateUtils(); 
	
	@Override
    public String marshall(Date value) {
        String dateStr = null;
        if (value != null) {
        	dateStr = dateUtils.convertToISO8601DateFormat(value);
        }
        
        return dateStr; 
    }

    @Override
    public Date unmarshall(Class<Date> dimensionType, String value) {
    	Date date = null;
        try {
        	if (value != null) {
        		date = dateUtils.parseISO8601DateFormat(value);
        	}
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return date;
    }
}
