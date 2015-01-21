package org.coursera.camppotlatch.service.commons;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {
	private SimpleDateFormat dateFormater = null;
	
	public DateUtils() {
		dateFormater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		dateFormater.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	public String convertToISO8601DateFormat(Date date) {
		// Example: 2014-10-27T09:44:55Z
		return dateFormater.format(date);
	}
	
	public Date parseISO8601DateFormat(String text) throws ParseException {
		return dateFormater.parse(text);
	}

    public Date convertToISO8601Date(Date date) throws ParseException {
        return parseISO8601DateFormat(convertToISO8601DateFormat(date));
    }	
}
