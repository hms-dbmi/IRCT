package edu.harvard.hms.dbmi.bd2k.irct.cl.rest;

import java.io.IOException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.harvard.hms.dbmi.bd2k.irct.model.script.ScriptedQuery;

@Path("/script")
public class ScriptService {
	
	private Logger logger = Logger.getLogger(getClass());
		
	@POST
	public Response submitScriptQuery(String scriptedQuery) throws JsonParseException, JsonMappingException, IOException{
		ObjectMapper mapper = new ObjectMapper();
		logger.error(scriptedQuery);
		logger.error(mapper.readValue(scriptedQuery, ScriptedQuery.class));
		return Response.ok().build();
	}
}
