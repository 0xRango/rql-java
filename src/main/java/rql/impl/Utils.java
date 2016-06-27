package rql.impl;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Utils {

	private static ObjectMapper OM = new ObjectMapper();

	public static ObjectMapper getObjectMapper() {
		return OM;
	}
}
