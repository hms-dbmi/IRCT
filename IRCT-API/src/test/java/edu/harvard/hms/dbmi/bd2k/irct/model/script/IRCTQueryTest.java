package edu.harvard.hms.dbmi.bd2k.irct.model.script;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.util.IOUtils;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class IRCTQueryTest {

	@SuppressWarnings("unchecked")
	private void testSerialization(String path, Class type) throws IOException{
		ObjectMapper mapper = new ObjectMapper();
		InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(path);
		String json = new String(IOUtils.toByteArray(resourceAsStream));
		mapper.readValue(json, type);
	}
	
	@Test
	public void testIRCTQuerySerializationSupport() throws IOException {
		testSerialization("query_json/i2b2_tranSmart_query_1.json", ScriptedQuery.class);
	}

	@Test
	public void testAFLQuerySerializationSupport() throws IOException {
		testSerialization("query_json/AFLQuery1.json", AFLQuery.class);
	}

	
	@Test
	public void testAFLQuerySerializationSupportBothWays() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		AFLQuery query = new AFLQuery();
		List<Where> whereClause = new ArrayList<Where>();
		whereClause.add(new Where().setDataType("AFL").setSrc("SOME AFL"));
		query.setWhere(whereClause);
		String json = mapper.writeValueAsString(query);
		System.out.println(json);
		assertEquals(query, mapper.readValue(json, AFLQuery.class));		
	}
	
}
