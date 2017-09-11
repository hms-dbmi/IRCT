package edu.harvard.hms.dbmi.bd2k.irct.cl.rest;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.ejb.Startup;
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
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheLoader.InvalidCacheLoadException;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.cache.Weigher;

import edu.harvard.hms.dbmi.bd2k.irct.controller.ResultController;
import edu.harvard.hms.dbmi.bd2k.irct.dataconverter.ResultDataStream;
import edu.harvard.hms.dbmi.bd2k.irct.model.script.ScriptedQuery;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;

@Path("/scriptService")
@Startup
public class ScriptService {

	@Inject
	private HttpSession session;

	@Inject
	private ResultService resultService;
	@Inject
	private ResultController resultController;

	private static final ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

	static {
		try {
			engine.eval("this.transform = {};load('https://cdnjs.cloudflare.com/ajax/libs/underscore.js/1.6.0/underscore-min.js');");
		} catch (ScriptException e) {
			throw new RuntimeException(e);
		}
	}

	// TODO : This is only required because the IRCT does not properly use the JaxRS providers framework to handle serialization.
	private ObjectMapper mapper = new ObjectMapper();

	private Logger logger = Logger.getLogger(ScriptService.class);

	ArrayList<Integer> functionHashList = new ArrayList<Integer>();
	
	private LoadingCache<Long, byte[]> cache = 
		CacheBuilder.newBuilder()
		.removalListener(new RemovalListener<Long, byte[]>() {

			@Override
			public void onRemoval(RemovalNotification<Long, byte[]> arg0) {
				logger.info("Removed : " + arg0.getKey());
			}
		})
		.maximumWeight(1048576 * 80)
		.weigher(new Weigher<Long, byte[]>(){
			public int weigh(Long arg0, byte[] arg1) {
				return arg1.length;
			}
		}).build(new CacheLoader<Long, byte[]>(){
			public byte[] load(Long id) {
				logger.info("loading : " + id);
				try {
					String resultStatus = getResultStatus(resultService.resultStatus(id));
					while(resultStatus.contentEquals("RUNNING")){
						Thread.yield();
						resultStatus = getResultStatus(resultService.resultStatus(id));
					}
					if(resultStatus.contentEquals("AVAILABLE")){
						ResultDataStream resultData = resultController.getResultDataStream(
								(User)session.getAttribute("user"), id, "JSON");
						ByteArrayOutputStream result = new ByteArrayOutputStream();
						resultData.getResult().write(new BufferedOutputStream(result, 1048576));
						ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
						ObjectOutputStream bytes = new ObjectOutputStream(byteStream);
						bytes.writeObject(mapper.readValue(new String(result.toByteArray(),"UTF-8"), Map.class));
						bytes.close();
						return byteStream.toByteArray();
					}
					return null;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
		});
	
	@POST
	@Path("/script")
	public Response submitScriptQuery(String scriptedQueryJson) throws JsonParseException, JsonMappingException, IOException{
		long startTime = System.currentTimeMillis();
		ScriptedQuery scriptedQuery = mapper.readValue(scriptedQueryJson, ScriptedQuery.class);
		long deserialized = System.currentTimeMillis();
		try {
			//			engine.eval(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("/META-INF/resources/webjars/underscorejs/1.8.3/underscore-min.js")));
			String sessionFunctionName = "transform_"+Math.abs(scriptedQueryJson.hashCode());
			if(!functionHashList.contains(sessionFunctionName.hashCode())){
				String sessionSafetyWrapper = sessionFunctionName + " = " + scriptedQuery.getScript() + ";";
				engine.eval(sessionSafetyWrapper);
				functionHashList.add(sessionFunctionName.hashCode());
			}
			logger.error("Function Name : " + sessionFunctionName);
			Invocable iEngine = (Invocable)engine;
			long engineInit = System.currentTimeMillis();
			HashMap<String, Object> results = new HashMap<String, Object>();
			for(String key : scriptedQuery.getResultSets().keySet()){
				long id = scriptedQuery.getResultSets().get(key);
				byte[] cachedResult;
				synchronized(cache){
					try{
						cachedResult = cache.get(id);
					}catch(InvalidCacheLoadException e){
						HashMap<String, String> response = new HashMap<String, String>();
						response.put("message", "RESULT SET ERROR");
						e.printStackTrace();
						return Response.serverError().entity(response).type(MediaType.APPLICATION_JSON).build();
					}
				}
				results.put(key, new ObjectInputStream(new ByteArrayInputStream(cachedResult)).readObject());
			}
			long resultsGathered = System.currentTimeMillis();
			HashMap<String, Object> options = new HashMap<String, Object>();
			options.put("resultSets", results);
			options.put("scriptOptions", scriptedQuery.getScriptOptions());
			long optionsBuilt = System.currentTimeMillis();
			Object scriptReturnValue = iEngine.invokeFunction(sessionFunctionName, mapper.writeValueAsString(options));
			long functionExecuted = System.currentTimeMillis();
			String resultJSON = mapper.writeValueAsString(scriptReturnValue);
			long serialized = System.currentTimeMillis();
			logger.info("Timings : \n\t" 
			+ (deserialized - startTime) + "\n\t" 
			+ (engineInit - deserialized) + "\n\t" 
			+ (resultsGathered - engineInit) + "\n\t" 
			+ (optionsBuilt - resultsGathered) + "\n\t" 
			+ (functionExecuted - optionsBuilt) + "\n\t" 
			+ (serialized - functionExecuted));
			return Response.ok(resultJSON, MediaType.APPLICATION_JSON_TYPE).build();
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Response.serverError().build();
	}

	private String getResultStatus(Response response) throws IOException, JsonParseException, JsonMappingException {
		return (String) mapper.readValue(response.getEntity().toString(), Map.class).get("status");
	}
}
