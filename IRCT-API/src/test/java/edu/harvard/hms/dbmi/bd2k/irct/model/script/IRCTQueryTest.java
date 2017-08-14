package edu.harvard.hms.dbmi.bd2k.irct.model.script;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.util.IOUtils;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class IRCTQueryTest {

	@Test
	public void testSerializationSupport() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("query_json/i2b2_tranSmart_query_1.json");
		String json = new String(IOUtils.toByteArray(resourceAsStream));
		mapper.readValue(json, ScriptedQuery.class);
	}

}
