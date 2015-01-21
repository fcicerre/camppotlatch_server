package org.coursera.camppotlatch.client.json;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Date;

import org.coursera.camppotlatch.service.commons.DateUtils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class DateJsonSerializer implements JsonSerializer<Date>, JsonDeserializer<Date> {
	@Override
	public JsonElement serialize(Date src, Type typeOfSrc,
			JsonSerializationContext context) {
		if (src == null)
			return JsonNull.INSTANCE;
		else
			return new JsonPrimitive(new DateUtils().convertToISO8601DateFormat(src));
	}

	@Override
	public Date deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		try {
			if (json.isJsonNull())
				return null;
			else
				return new DateUtils().parseISO8601DateFormat(json.getAsJsonPrimitive().getAsString());
		} catch (ParseException ex) {
			throw new JsonParseException(ex);
		}
	}
}
