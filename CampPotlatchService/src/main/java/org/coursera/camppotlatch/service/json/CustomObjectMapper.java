package org.coursera.camppotlatch.service.json;

import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class CustomObjectMapper extends ObjectMapper {
	private static final long serialVersionUID = -3903552428778234200L;
	
	public CustomObjectMapper() {
		SimpleModule module = new SimpleModule();
		module.addSerializer(Date.class, new DateJsonSerializer());
		module.addDeserializer(Date.class, new DateJsonDeserializer());
		registerModule(module);
	}
}
