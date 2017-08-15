package edu.harvard.hms.dbmi.bd2k.irct.cl.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.http.HttpSession;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.harvard.hms.dbmi.bd2k.irct.controller.ResultController;
import edu.harvard.hms.dbmi.bd2k.irct.dataconverter.ResultDataStream;
import edu.harvard.hms.dbmi.bd2k.irct.model.script.IRCTQuery;
import edu.harvard.hms.dbmi.bd2k.irct.model.script.ScriptedQuery;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;
import edu.harvard.hms.dbmi.bd2k.irct.ws.rs.resultconverter.JSONTabularDataConverter;

@Path("/script")
@RequestScoped
public class ScriptService {

	@Inject
	private HttpSession session;

	@Inject
	private QueryService queryService;
	@Inject
	private ResultService resultService;
	@Inject
	private ResultController resultController;
	
	// TODO : This is only required because the IRCT does not properly use the JaxRS providers framework to handle serialization.
	private ObjectMapper mapper = new ObjectMapper();
		
	private Logger logger = Logger.getLogger(getClass());
		
	@POST
	public Response submitScriptQuery(String scriptedQueryJson) throws JsonParseException, JsonMappingException, IOException{
		logger.error(scriptedQueryJson);
		ScriptedQuery scriptedQuery = mapper.readValue(scriptedQueryJson, ScriptedQuery.class);
		logger.error(scriptedQuery);
		logger.error(queryService);
		HashMap<IRCTQuery, Integer> resultMap = new HashMap<IRCTQuery, Integer>();
		for(IRCTQuery q : scriptedQuery.getQueries()) {
			Response response = queryService.runQuery(mapper.writeValueAsString(q));
			logger.error(response.getEntity());
			resultMap.put(q, getResultId(response));
		}
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
		try {
			engine.eval(scriptedQuery.getScript());
			Invocable iEngine = (Invocable)engine;
			ArrayList<Object> results = new ArrayList<Object>(resultMap.size());
			for(int x : resultMap.values()){
				while(getResultStatus(resultService.resultStatus((long)x)).contentEquals("RUNNING")){
					Thread.yield();
				}
				ResultDataStream resultData = resultController.getResultDataStream((User)session.getAttribute("user"), (long)x, "JSON");
				ByteArrayOutputStream result = new ByteArrayOutputStream();
				resultData.getResult().write(result);
				logger.error("RESULT DATA : " + result.toString("UTF-8").substring(0, 1000));
				results.add(result.toString("UTF-8"));
			}
			String result = ((String)iEngine.invokeFunction("transform", results.toArray()));
			return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Response.serverError().build();
	}

	private Integer getResultId(Response response) throws IOException, JsonParseException, JsonMappingException {
		return (Integer) mapper.readValue(response.getEntity().toString(), Map.class).get("resultId");
	}
	
	private String getResultStatus(Response response) throws IOException, JsonParseException, JsonMappingException {
		return (String) mapper.readValue(response.getEntity().toString(), Map.class).get("status");
	}
}
