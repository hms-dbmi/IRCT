package edu.harvard.hms.dbmi.bd2k.irct.cl.rest;

import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.harvard.hms.dbmi.bd2k.irct.model.script.IRCTQuery;
import edu.harvard.hms.dbmi.bd2k.irct.model.script.ScriptedQuery;

@Path("/script")
public class ScriptService {
	
	@Inject
	private QueryService queryService;
	
	// TODO : This is only required because the IRCT does not properly use the JaxRS providers framework to handle serialization.
	private ObjectMapper mapper = new ObjectMapper();
		
	private Logger logger = Logger.getLogger(getClass());
		
	@POST
	public Response submitScriptQuery(String scriptedQueryJson) throws JsonParseException, JsonMappingException, IOException{
		logger.error(scriptedQueryJson);
		ScriptedQuery scriptedQuery = mapper.readValue(scriptedQueryJson, ScriptedQuery.class);
		logger.error(scriptedQuery);
		logger.error(queryService);
		for(IRCTQuery q : scriptedQuery.getQueries()) {
			Response response = queryService.runQuery(mapper.writeValueAsString(q));
			logger.error(response.getEntity());
		}
		return Response.ok().build();
	}
}
