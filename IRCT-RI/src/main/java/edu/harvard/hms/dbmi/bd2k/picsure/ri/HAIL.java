package edu.harvard.hms.dbmi.bd2k.picsure.ri;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import edu.harvard.hms.dbmi.bd2k.irct.IRCTApplication;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.find.FindInformationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Entity;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.WhereClause;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.Resource;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ResourceState;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.PathResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.QueryResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultStatus;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.PersistableException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.exception.ResultSetException;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.Column;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.FileResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;
import edu.harvard.hms.dbmi.bd2k.util.Utility;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import us.monoid.json.JSONObject;

import javax.json.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.*;

/**
 * A resource implementation of a data source that communicates with a HAIL proxy via HTTP
 */
public class HAIL implements QueryResourceImplementationInterface,
        PathResourceImplementationInterface {
    Logger logger = Logger.getLogger(this.getClass());


    private static final String PATH_NAME = "pui";

    protected String resourceName;
    protected String resourceURL;

    protected ResourceState resourceState;

//    Map<Entity> allPathEntities;

    @Override
    public void setup(Map<String, String> parameters) throws ResourceInterfaceException {

        if (logger.isDebugEnabled())
            logger.debug("setup for Hail" +
                    " Starting...");

        String errorString = "";
        this.resourceName = parameters.get("resourceName");
        if (this.resourceName == null) {
            logger.error( "setup() `resourceName` parameter is missing.");
            errorString += " resourceName";
        }

        String tempResourceURL = parameters.get("resourceURL");
        if (tempResourceURL == null) {
            logger.error( "setup() `resourceURL` parameter is missing.");
            errorString += " resourceURL";
        } else {
            resourceURL = (tempResourceURL.endsWith("/"))?tempResourceURL.substring(0, tempResourceURL.length()-1):tempResourceURL;
        }

        if (!errorString.isEmpty()) {
            throw new ResourceInterfaceException("Hail Interface setup() is missing:" + errorString);
        }

        resourceState = ResourceState.READY;
        logger.debug( "setup() for " + resourceName +
                " Finished. " + resourceName +
                " is in READY state.");
    }

    @Override
    public String getType() {
        return "Hail";
    }

    @Override
    public List<Entity> getPathRelationship(Entity path, OntologyRelationship relationship, User user) {
        logger.debug("getPathRelationship() Starting");
        List<Entity> entities = new ArrayList<Entity>();

        // Split the path into components. The first component is the Hail resource name, the rest is
        // a URL path like string.
        String p = path.getPui();
        logger.debug("getPathRelationship() pui:"+p);
        if (p.indexOf('/',2)==-1) {
            // This is a request for the root

            // Call the external URL
            InputStream is = simpleRestCall(this.resourceURL);

            // Parse the response, as JSON mime type
            ObjectMapper objectMapper = IRCTApplication.objectMapper;
            JsonNode responseJsonNode;
            try {
                responseJsonNode = objectMapper.readTree(is);
                String responseStatus = responseJsonNode.get("status").textValue();
                Entity e = new Entity();

                e.setPui("objectIdVal");
                e.setName("objectNameVal");
                e.setDisplayName("objectDisplayNameVal status:" + responseStatus);

                entities.add(e);
            } catch (JsonMappingException jme) {
                logger.error("getPathRelationship() Exception:"+jme.getMessage());
                throw new RuntimeException("Could not parse JSON response from `"+resourceName+"` resource");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        } else {
            String objectPath = p.substring(p.indexOf('/',2));
            logger.debug("getPathRelationship() objectPath: "+objectPath);

            Entity e = new Entity();

            e.setPui("objectIdVal");
            e.setName("objectNameVal");
            e.setDisplayName("objectDisplayNameVal");

            entities.add(e);
        }

        logger.debug("getPathRelationship() Finished");
        return entities;
    }


    private List<Entity> retrieveAllPathTree(){
        String urlString = resourceURL + "/tree";

        CloseableHttpClient httpClient = IRCTApplication.CLOSEABLE_HTTP_CLIENT;
        HttpGet httpGet = new HttpGet(urlString);

        CloseableHttpResponse response = null;

        List<Entity> entities = null;

        try {
            response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();

//            entities = parseAllHailPathJsonNode(IRCTApplication.objectMapper
//                    .readTree(entity
//                            .getContent()));

            EntityUtils.consume(entity);
        } catch (IOException ex ){
            logger.error("IOException when retrieving all path from Hail API:" + urlString +
                    " with exception message: " + ex.getMessage());
        } finally {
            try {
                if (response != null)
                    response.close();
            } catch (IOException ex) {
                logger.error("GNOME - IOExcpetion when closing http response: " + ex.getMessage());
            }
        }

        return entities;
    }

    /**
     *
     * @param pathNode
     * @return null if nothing
     */
    private TreeMap<String, JsonNode> parseAllHailPathJsonNode(JsonNode pathNode){
        TreeMap<String, JsonNode> entityTreeMap = null;
        return entityTreeMap;
    }

    @Override
    public List<Entity> find(Entity path, FindInformationInterface findInformation, User user) {
        logger.debug("find() starting");
        List<Entity> returns = new ArrayList<Entity>();
        logger.debug("find() finished");
        return returns;
    }

    @Override
    public Result runQuery(User user, Query query, Result result) {
        logger.debug("runQuery() starting");

        if (result == null)
            logger.debug("runQuery() `result` object is null, still.");

        try {
            List<WhereClause> whereClauses = query.getClausesOfType(WhereClause.class);
            result.setResultStatus(ResultStatus.CREATED);
            result.setMessage("Started running the query.");

            for (WhereClause whereClause : whereClauses) {
                // Convert the predicate fields into variables on the Hail request
                // These variables will be used when rendering the Hail template
                // into an actual script.
                Map<String,String> hailVariables = new HashMap<String, String>();
                for (String fieldName: whereClause.getStringValues().keySet()) {
                    hailVariables.put(fieldName, whereClause.getStringValues().get(fieldName));
                    logger.debug("runQuery() field:"+fieldName+"="+whereClause.getStringValues().get(fieldName));
                }

                // Convert the predicate value to a hail template
                String hailTemplate = whereClause.getPredicateType().getName();
                logger.debug("runQuery() hailTemplate:"+hailTemplate);
                hailVariables.put("template", hailTemplate);

                // Use the pui from the where clause to set the study name in the list of variables.
                hailVariables.put("study", Utility.getURLFromPui(whereClause
                        .getField()
                        .getPui(),resourceName));

                HailResponse hailResponse = hailJobSubmit(hailTemplate,hailVariables);

                if (hailResponse.isError()) {
                    result.setResultStatus(ResultStatus.ERROR);
                    result.setMessage(hailResponse.getErrorMessage());
                } else {
                    result.setResourceActionId(hailResponse.getJobUUID());
                    result.setResultStatus(ResultStatus.RUNNING);
                    result.setMessage(hailResponse.getMessage());
                }
            }
            logger.debug("runQuery() HTTPResponse has been interpreted. Updated `result` object.");

        } catch (Exception e) {
            logger.error(String.format("runQuery() UnhandledException: %s", e.getMessage()));

            result.setResultStatus(ResultStatus.ERROR);
            result.setMessage(String.valueOf(e.getMessage()));
        }

        logger.debug("runQuery() finished");
        return result;
    }

    @Override
    public Result getResults(User user, Result result) {
        logger.debug("getResults() starting");
        logger.debug("getResults() getting result for "+result.getResourceActionId());

        result.setResultStatus(ResultStatus.ERROR);
        result.setMessage("Result failed, on purpose.");

        return result;
    }

    @Override
    public ResourceState getState() {
        return resourceState;
    }

    @Override
    public ResultDataType getQueryDataType(Query query) {
        return ResultDataType.TABULAR;
    }

    private InputStream simpleRestCall(String urlString, Map<String, String> payload) {
        logger.debug("simpleRestCall() Starting");

        HttpEntity restEntity = null;
        CloseableHttpClient restClient = HttpClientBuilder.create().build();
        URIBuilder builder = null;
        HttpGet get = null;
        try {
            builder = new URIBuilder(urlString);
            for (String fieldName: payload.keySet()) {
                logger.debug("simpleRestCall() add `"+fieldName+"` to payload as `"+payload.get(fieldName)+"`.");
                builder.setParameter(fieldName, payload.get(fieldName));
            }
            get = new HttpGet(builder.build());
            get.addHeader("Content-Type", ContentType.APPLICATION_JSON.toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URL generated:"+urlString);
        }
        CloseableHttpResponse restResponse = null;
        try {
            restResponse = restClient.execute(get);
            restEntity = restResponse.getEntity();
            // https://stackoverflow.com/questions/15969037/why-did-the-author-use-entityutils-consumehttpentity#15970985
            EntityUtils.consume(restEntity);
            logger.debug("simpleRestCall() released entity resource.");
        } catch (IOException ex ){
            logger.error("simpleRestCall() IOException: Cannot execute POST with URL: " + urlString);
        } finally {
            try {
                if (restResponse != null)
                    restResponse.close();
            } catch (Exception ex) {
                logger.error("simpleRestCall() finallyException: " + ex.getMessage());
            }
        }
        logger.debug("simpleRestCall() finished.");

        if (restEntity!=null) {
            try {
                return restEntity.getContent();
            } catch (IOException ioex) {
                logger.error("simpleRestCall() Exception:"+ioex);

            } finally {

            }
        }
        return null;
    }

    private InputStream simpleRestCall(String urlString) {
        logger.debug("restCall() Starting");
        HttpEntity restEntity = null;
        CloseableHttpClient restClient = HttpClientBuilder.create().build();

        CloseableHttpResponse restResponse = null;
        try {
            HttpGet get = new HttpGet(urlString);
            get.addHeader("Content-Type", ContentType.APPLICATION_JSON.toString());

            restResponse = restClient.execute(get);
            restEntity = restResponse.getEntity();

            // https://stackoverflow.com/questions/15969037/why-did-the-author-use-entityutils-consumehttpentity#15970985
            EntityUtils.consume(restEntity);
            logger.debug("restCall() released entity resource.");
        } catch (IOException ex ){
            logger.error("restCall() IOException: Cannot execute POST with URL: " + urlString);
        } finally {
            try {
                if (restResponse != null)
                    restResponse.close();
            } catch (Exception ex) {
                logger.error("restCall() finallyException: " + ex.getMessage());
            }
        }
        logger.debug("restCall() finished.");

        if (restEntity!=null) {
            try {
                return restEntity.getContent();
            } catch (IOException ioex) {
                logger.error("restCall() Exception:"+ioex);

            } finally {

            }
        }
        return null;
    }

    // HTTP POST with JSON body, constructed from `payload` Map, and parse the
    // returned stream into a JsonNode object for later parsing. Protocoll errors
    // are captured as well.
    // Any protocol or parsing error will be thrown as RuntimeException
    private JsonNode restPOST(String urlString, Map<String, String> payload) {
        logger.debug("restPOST() Starting ");
        JsonNode responseObject = null;

        CloseableHttpResponse restResponse = null;
        try {
            ObjectMapper objectMapper = IRCTApplication.objectMapper;
            CloseableHttpClient restClient = HttpClientBuilder.create().build();
            // Convert payload into JSON object.
            JSONObject json = new JSONObject();
            for(String fieldName: payload.keySet()) {
                json.put(fieldName,payload.get(fieldName));
            }
            HttpPost post = new HttpPost((urlString));
            post.addHeader("Content-Type", ContentType.APPLICATION_JSON.toString());
            post.setEntity(new StringEntity(json.toString()));
            restResponse = restClient.execute(post);

            if (restResponse.getStatusLine().getStatusCode()!=200) {
                // Error status returned.
            }
            if (restResponse==null) {
                logger.error("restResponse is null");
            }
            HttpEntity restEntity = restResponse.getEntity();
            if (restEntity==null) {
                logger.error("restEntity is null");
            }
            // Convert JSON response into Java object
            responseObject = objectMapper
                    .readTree(restEntity
                            .getContent());
            logger.debug("restCall() finished parsing data");

            // https://stackoverflow.com/questions/15969037/why-did-the-author-use-entityutils-consumehttpentity#15970985
            EntityUtils.consume(restEntity);
            logger.debug("restPOST() released entity resource.");
        } catch (JsonParseException ex){
            logger.error("restPOST() JsonParseException: " + ex.getMessage());
        } catch (IOException ex ) {
            logger.error("restPOST() IOException: " + urlString);
        } catch (Exception ex) {
            logger.error("restPOST() UnhandledException:"+ex);
        } finally {
            try {
                if (restResponse != null)
                    restResponse.close();
            } catch (Exception ex) {
                logger.error("restPOST() finallyException: " + ex.getMessage());
            }
        }
        logger.debug("restPOST() finished.");
        return responseObject;
    }

    // HTTP GET, parse the returned stream into a JsonNode object for
    // later parsing.
    private JsonNode restGET(String urlString) {
        logger.debug("restPOST() Starting ");
        JsonNode responseObject = null;

        CloseableHttpResponse restResponse = null;
        try {
            ObjectMapper objectMapper = IRCTApplication.objectMapper;
            CloseableHttpClient restClient = HttpClientBuilder.create().build();

            HttpPost get = new HttpGet((urlString));
            restResponse = restClient.execute(get);

            if (restResponse.getStatusLine().getStatusCode()!=200) {
                // Error status returned.
            }
            if (restResponse==null) {
                logger.error("restResponse is null");
            }
            HttpEntity restEntity = restResponse.getEntity();
            if (restEntity==null) {
                logger.error("restEntity is null");
            }
            // Convert JSON response into Java object
            responseObject = objectMapper
                    .readTree(restEntity
                            .getContent());
            logger.debug("restCall() finished parsing data");

            // https://stackoverflow.com/questions/15969037/why-did-the-author-use-entityutils-consumehttpentity#15970985
            EntityUtils.consume(restEntity);
            logger.debug("restPOST() released entity resource.");
        } catch (JsonParseException ex){
            throw new ResourceInterfaceException("JSON Parsing exception: "+ex.getMessage());
        } catch (IOException ex ) {
            logger.error("restPOST() IOException: " + urlString);
        } finally {
            try {
                if (restResponse != null)
                    restResponse.close();
            } catch (Exception ex) {
                logger.error("restPOST() finallyException: " + ex.getMessage());
            }
        }
        logger.debug("restPOST() finished.");
        return responseObject;
    }

    private void restCall(String urlString, Result result) {
        logger.debug("restCall() Starting ");

        ObjectMapper objectMapper = IRCTApplication.objectMapper;

        CloseableHttpClient restClient = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(urlString);
        get.addHeader("Content-Type", ContentType.APPLICATION_JSON.toString());

        CloseableHttpResponse restResponse = null;

        try {
            result.setResultStatus(ResultStatus.RUNNING);

            restResponse = restClient.execute(get);
            HttpEntity restEntity = restResponse.getEntity();

            logger.debug("restCall() Response status is "+restResponse.getStatusLine().getStatusCode());
            if (restResponse.getStatusLine().getStatusCode() != 200) {
                result.setMessage(restResponse.getStatusLine().getReasonPhrase());
                result.setResultStatus(ResultStatus.ERROR);
            } else {
                // If response status is 200, then parse response data
                parseData(result, objectMapper
                        .readTree(restEntity
                                .getContent()));

                result.setMessage("Received data from datasource.");
                result.setResultStatus(ResultStatus.COMPLETE);
            }

            // https://stackoverflow.com/questions/15969037/why-did-the-author-use-entityutils-consumehttpentity#15970985
            EntityUtils.consume(restEntity);
            logger.debug("restCall() released entity resource.");

        } catch (PersistableException ex) {
            result.setResultStatus(ResultStatus.ERROR);
            logger.error("restCall() Persistable error: " + ex.getMessage() );
        } catch (ResultSetException ex) {
            result.setResultStatus(ResultStatus.ERROR);
            logger.error("restCall() Cannot append row: " + ex.getMessage());
        } catch (JsonParseException ex){
            result.setResultStatus(ResultStatus.ERROR);
            result.setMessage("Cannot parse response as JSON");
            logger.error("restCall() Cannot parse response as a JsonNode: " + ex.getMessage());
        } catch (IOException ex ){
            result.setResultStatus(ResultStatus.ERROR);
            result.setMessage("Cannot execute request to "+resourceName);
            logger.error("restCall() IOException: Cannot cannot execute POST with URL: " + urlString);
        } finally {
            try {
                if (restResponse != null)
                    restResponse.close();
            } catch (Exception ex) {
                logger.error("restCall() finallyException: " + ex.getMessage());
            }
        }
        logger.debug("restCall() finished.");
    }

    private void parseData(Result result, JsonNode responseJsonNode)
            throws PersistableException, ResultSetException{
        FileResultSet frs = (FileResultSet) result.getData();

        if (responseJsonNode.get("status")==null) {
            if (responseJsonNode.get("message")==null) {
                throw new RuntimeException("Unknown error.");
            } else {
                // If status is missing, but there is a message,
                // use that for error message
                throw new RuntimeException(responseJsonNode.get("message").textValue());
            }
        }

        String responseStatus = responseJsonNode.get("status").textValue();

        JsonNode matrixNode = responseJsonNode.get("matrix");
        if (responseStatus.equalsIgnoreCase("ok")){
            if (!matrixNode.getNodeType().equals(JsonNodeType.ARRAY)
                    || !matrixNode.get(0).getNodeType().equals(JsonNodeType.ARRAY)){
                String errorMessage = "Cannot parse response JSON from Hail: expecting an 2D array";
                result.setMessage(errorMessage);
                result.setResultStatus(ResultStatus.ERROR);
                throw new PersistableException(errorMessage);
            }

            // append columns
            for (JsonNode innerJsonNode : matrixNode.get(0)){
                if (!innerJsonNode.getNodeType().equals(JsonNodeType.STRING)){
                    String errorMessage = "Cannot parse response JSON from Hail: expecting a String in header array";
                    result.setMessage(errorMessage);
                    result.setResultStatus(ResultStatus.ERROR);
                    throw new PersistableException(errorMessage);
                }

                // how can I know what datatype it is for now?... just set it primitive string...
                frs.appendColumn(new Column(innerJsonNode.textValue(), PrimitiveDataType.STRING));
            }

            // append rows
            for (int i = 1; i < matrixNode.size(); i++){
                JsonNode jsonNode = matrixNode.get(i);
                if (!jsonNode.getNodeType().equals(JsonNodeType.ARRAY)){
                    String errorMessage = "Cannot parse response JSON from Hail: expecting an 2D array";
                    result.setMessage(errorMessage);
                    result.setResultStatus(ResultStatus.ERROR);
                    throw new PersistableException(errorMessage);
                }
                frs.appendRow();

                for (int j = 0; j<jsonNode.size(); j++){
                    // column datatype could be reset here by checking the json NodeType,
                    // but no PrimitiveDataType.NUMBER implemented yet, can't efficiently separate
                    // integer, double, just store everything as STRING for now
                    frs.updateString(frs.getColumn(j).getName(),
                            jsonNode.get(j).asText());
                }

            }
        } else {
            frs.appendColumn(new Column("status", PrimitiveDataType.STRING));
            frs.appendColumn(new Column("message", PrimitiveDataType.STRING));

            frs.appendRow();
            frs.updateString("status", responseStatus);
            frs.updateString("message", responseJsonNode.get("message").textValue());
        }

        result.setData(frs);
    }

    // Submit a Hail job, via the HailProxy RESTful API interface.
    private HailResponse hailJobSubmit(String templateName, Map<String, String> variables) {
        HailResponse resp = new HailResponse();

        try {
            JsonNode nd = restPOST(resourceURL+"/jobs", variables);

            // If all is well, the response will be HTTPStatus == 200
            // and

            if (nd == null) {
                resp.setError("Could not submit Hail job.");
            }


        } catch (Exception ex) {
            logger.error("hailJob() UnhandledException:"+ex.getMessage());
        }


        return resp;
    }

    // Submit a Hail job, via the HailProxy RESTful API interface.
    private HailResponse hailJobStatus(String jobUUID) {
        HailResponse resp = new HailResponse();
        try {
            JsonNode nd = restGET(resourceURL+"/jobs?id="+jobUUID);

            // If all is well, the response will be HTTPStatus == 200
            // and

            if (nd == null) throw new ResourceInterfaceException("Could not get Hail job information.");


        } catch (Exception ex) {
            logger.error("hailJob() UnhandledException:"+ex.getMessage());
        }
        return resp;
    }


    class PathElement {
        String pui;

    }

    class HailResponse {

        private String error_message = "";
        private String hail_message = "";

        public String getJobUUID() {
            return jobUUID;
        }

        public void setJobUUID(String jobUUID) {
            this.jobUUID = jobUUID;
        }

        private String jobUUID = "";

        public boolean isError() {
            return !this.error_message.isEmpty();
        }

        public String getErrorMessage() {
            return this.error_message;
        }
        public String getMessage() {
            return this.hail_message;
        }

        public void setError(String error_message) {
            this.error_message = error_message;

        }
    }
}
