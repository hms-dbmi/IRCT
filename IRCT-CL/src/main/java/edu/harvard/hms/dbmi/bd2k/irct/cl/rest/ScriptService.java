package edu.harvard.hms.dbmi.bd2k.irct.cl.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.http.HttpSession;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.harvard.hms.dbmi.bd2k.irct.controller.ResultController;
import edu.harvard.hms.dbmi.bd2k.irct.dataconverter.ResultDataStream;
import edu.harvard.hms.dbmi.bd2k.irct.model.script.IRCTQuery;
import edu.harvard.hms.dbmi.bd2k.irct.model.script.ScriptedQuery;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;

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

	private static final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

	static {
		try {
			engine.eval("this.transform = {};load('https://cdnjs.cloudflare.com/ajax/libs/underscore.js/1.6.0/underscore-min.js');");
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// TODO : This is only required because the IRCT does not properly use the JaxRS providers framework to handle serialization.
	private ObjectMapper mapper = new ObjectMapper();

	private Logger logger = Logger.getLogger(getClass());

	@POST
	public Response submitScriptQuery(String scriptedQueryJson) throws JsonParseException, JsonMappingException, IOException{
		logger.error(scriptedQueryJson);
		ScriptedQuery scriptedQuery = mapper.readValue(scriptedQueryJson, ScriptedQuery.class);
		logger.error(scriptedQuery);
		logger.error(queryService);
		try {
			//			engine.eval(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("/META-INF/resources/webjars/underscorejs/1.8.3/underscore-min.js")));
			String sessionFunctionName = "transform_"+Math.abs(session.getId().hashCode());
			String sessionSafetyWrapper = sessionFunctionName + " = " + scriptedQuery.getScript() + ";";
			engine.eval(sessionSafetyWrapper);
			logger.error(sessionFunctionName);
			engine.eval("print("+ sessionFunctionName +");");
			Invocable iEngine = (Invocable)engine;
			logger.error("Gathering results : " + System.currentTimeMillis());
			ArrayList<Object> results = new ArrayList<Object>(scriptedQuery.getResultSets().size());
			for(int x : scriptedQuery.getResultSets().values()){
				logger.error("Gathering results : " + System.currentTimeMillis());
				while(getResultStatus(resultService.resultStatus((long)x))
						.contentEquals("RUNNING")){
					Thread.yield();
				}
				logger.error("Gathering results : " + System.currentTimeMillis());
				ResultDataStream resultData = resultController.getResultDataStream(
						(User)session.getAttribute("user"), (long)x, "JSON");
				logger.error("Gathering results : " + System.currentTimeMillis());
				ByteArrayOutputStream result = new ByteArrayOutputStream();
				logger.error("Gathering results : " + System.currentTimeMillis());
				resultData.getResult().write(result);
				logger.error("Gathering results : " + System.currentTimeMillis());
				results.add(result.toString("UTF-8"));
			}
			logger.error("Processing Script : " + System.currentTimeMillis());
			Object scriptReturnValue = iEngine.invokeFunction(sessionFunctionName, results);
			logger.error("Building JSON     : " + System.currentTimeMillis());
			String resultJSON = mapper.writeValueAsString(scriptReturnValue);
			logger.error("Sending Response  : " + System.currentTimeMillis());
			return Response.ok(resultJSON, MediaType.APPLICATION_JSON_TYPE).build();
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
