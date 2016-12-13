package edu.harvard.hms.dbmi.bd2k.irct.cl.rest;

import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonValue.ValueType;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.harvard.hms.dbmi.bd2k.irct.controller.ExecutionController;
import edu.harvard.hms.dbmi.bd2k.irct.controller.JoinController;
import edu.harvard.hms.dbmi.bd2k.irct.exception.JoinException;
import edu.harvard.hms.dbmi.bd2k.irct.model.join.IRCTJoin;
import edu.harvard.hms.dbmi.bd2k.irct.model.join.Join;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Field;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

@Path("/joinService")
@ConversationScoped
@Named
public class JoinService implements Serializable {
	private static final long serialVersionUID = 5400469772999643934L;

	@Inject
	private JoinController jc;

	@Inject
	private ExecutionController ec;

	@Inject
	private HttpSession session;
	
	@GET
	@Path("/joins")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getJoins() {
		JsonArrayBuilder response = Json.createArrayBuilder();
		
		for(IRCTJoin join : jc.getSupportedJoins()) {
			response.add(join.toJson());
		}
		return Response.ok(response.build(), MediaType.APPLICATION_JSON).build();
	}

	@POST
	@Path("/runJoin")
	@Produces(MediaType.APPLICATION_JSON)
	public Response runJoin(String payload) {
		JsonObjectBuilder response = Json.createObjectBuilder();

		JsonReader jsonReader = Json.createReader(new StringReader(payload));
		JsonObject jsonJoin = jsonReader.readObject();
		jsonReader.close();

		Join join = null;
		try {
			join = convertJsonToJoin(jsonJoin);
		} catch (JoinException e) {
			response.add("status", "Invalid Request");
			response.add("message", e.getMessage());
			return Response.status(400).entity(response.build()).build();
		}

		try {
			response.add("resultId", ec.runJoin(join,
					(SecureSession) session.getAttribute("secureSession")));
		} catch (PersistableException e) {
			response.add("status", "Error running request");
			response.add("message", "An error occurred running this request");
			return Response.status(400).entity(response.build()).build();
		}

		return Response.ok(response.build(), MediaType.APPLICATION_JSON)
				.build();
	}

	private Join convertJsonToJoin(JsonObject jsonJoin) throws JoinException {
		jc.createJoin();

		String joinName = jsonJoin.getString("joinType");

		IRCTJoin irctJoin = jc.getIRCTJoin(joinName);
		
		if(irctJoin == null) {
			throw new JoinException("Unknown join type");
		}

		Map<String, Field> clauseFields = new HashMap<String, Field>();
		for (Field field : irctJoin.getFields()) {
			clauseFields.put(field.getPath(), field);
		}

		Map<String, Object> objectFields = new HashMap<String, Object>();
		Map<String, String> fields = new HashMap<String, String>();

		if (jsonJoin.containsKey("fields")) {
			JsonObject fieldObject = jsonJoin.getJsonObject("fields");
			objectFields = getObjectFields(clauseFields, fieldObject);
			fields = getStringFields(clauseFields, fieldObject);
		}

		jc.setup(irctJoin, fields, objectFields);

		return jc.getJoin();
	}

	private Map<String, Object> getObjectFields(
			Map<String, Field> clauseFields, JsonObject fieldObject)
			throws JoinException {
		Map<String, Object> objectFields = new HashMap<String, Object>();
		for (String key : fieldObject.keySet()) {
			ValueType vt = fieldObject.get(key).getValueType();

			if ((vt == ValueType.ARRAY)) {
				if (clauseFields.containsKey(key)
						&& (clauseFields.get(key).getDataTypes()
								.contains(PrimitiveDataType.ARRAY))) {

					JsonArray array = fieldObject.getJsonArray(key);
					String[] stringArray = new String[array.size()];
					for (int sa_i = 0; sa_i < array.size(); sa_i++) {
						stringArray[sa_i] = array.getString(sa_i);
					}
					objectFields.put(key, stringArray);
				} else {
					throw new JoinException(key
							+ " field does not support arrays.");
				}

			} else if (vt == ValueType.OBJECT) {
				throw new JoinException(key + " field does not support this type.");
			}
		}

		return objectFields;
	}

	private Map<String, String> getStringFields(
			Map<String, Field> clauseFields, JsonObject fieldObject) {
		Map<String, String> fields = new HashMap<String, String>();
		for (String key : fieldObject.keySet()) {
			ValueType vt = fieldObject.get(key).getValueType();
			if ((vt != ValueType.ARRAY) && (vt != ValueType.OBJECT)) {
				fields.put(key, fieldObject.getString(key));
			}
		}

		return fields;
	}
}
