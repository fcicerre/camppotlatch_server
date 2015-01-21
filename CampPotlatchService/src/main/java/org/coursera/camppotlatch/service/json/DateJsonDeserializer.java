package org.coursera.camppotlatch.service.json;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import org.coursera.camppotlatch.service.commons.DateUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class DateJsonDeserializer extends JsonDeserializer<Date> {
	@Override
	public Date deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		try {
			if (jp.getText().equals("null"))
				return null;
			else
				return new DateUtils().parseISO8601DateFormat(jp.getText());
		} catch (ParseException ex) {
			throw new IOException(ex);
		}
	}

}
