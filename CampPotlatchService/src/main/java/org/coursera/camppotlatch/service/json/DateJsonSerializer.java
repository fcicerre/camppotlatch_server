package org.coursera.camppotlatch.service.json;

import java.io.IOException;
import java.util.Date;

import org.coursera.camppotlatch.service.commons.DateUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class DateJsonSerializer extends JsonSerializer<Date> {
	@Override
	public void serialize(Date value, JsonGenerator jgen,
			SerializerProvider provider) throws IOException,
			JsonProcessingException {
		if (value == null)
			jgen.writeNull();
		else
			jgen.writeString(new DateUtils().convertToISO8601DateFormat(value));		
	}
}
