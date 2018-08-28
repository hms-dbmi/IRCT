package edu.harvard.hms.dbmi.bd2k.picsure.ri;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.harvard.hms.dbmi.bd2k.irct.IRCTApplication;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.find.FindInformationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Entity;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.WhereClause;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ResourceState;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.PathResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.QueryResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Data;
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
import us.monoid.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * A resource implementation of a data source that communicates with a HAIL proxy via HTTP
 */
@SuppressWarnings("Duplicates")
public class LivyHAIL implements QueryResourceImplementationInterface,
        PathResourceImplementationInterface {
    Logger logger = Logger.getLogger(this.getClass());

    private static final String PATH_NAME = "pui";

    protected String resourceName;
    protected String resourceURL;
    protected ResourceState resourceState;

    protected String sessionID;

    protected String inputFileDir = "/app/data/";
    protected String outputFileDir = "/app/data/output/";
    // Choose your desired input file and output file name
    protected String inputFileName = "/example_data_PMC.maf";
    protected String outputFileName = "BRCA2_benign";

//    Map<Entity> allPathEntities;

    @Override
    public void setup(Map<String, String> parameters) throws ResourceInterfaceException {

        if (logger.isDebugEnabled())
            logger.debug("setup for Hail" +
                    " Starting...");

        String errorString = "";
        this.resourceName = parameters.get("resourceName");
        if (this.resourceName == null) {
            logger.error("setup() `resourceName` parameter is missing.");
            errorString += " resourceName";
        }

        String tempResourceURL = parameters.get("resourceURL");
        if (tempResourceURL == null) {
            logger.error("setup() `resourceURL` parameter is missing.");
            errorString += " resourceURL";
        } else {
            resourceURL = (tempResourceURL.endsWith("/")) ? tempResourceURL.substring(0, tempResourceURL.length() - 1) : tempResourceURL;
        }

        if (!errorString.isEmpty()) {
            throw new ResourceInterfaceException("Hail Interface setup() is missing:" + errorString);
        }

        // Initialize a new session
        HashMap<String, String> kindSpecified = new HashMap<>();
        kindSpecified.put("kind" , "pyspark");

        JsonNode sessionResponse = restPOST(this.resourceURL + "/sessions", kindSpecified);
        sessionID = sessionResponse.get("id").toString();

        resourceState = ResourceState.READY;
        logger.debug("setup() for " + resourceName +
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
        logger.debug("getPathRelationship() pui:" + p);
        if (p.indexOf('/', 2) == -1) {
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
                logger.error("getPathRelationship() Exception:" + jme.getMessage());
                throw new RuntimeException("Could not parse JSON response from `" + resourceName + "` resource");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else {
            String objectPath = p.substring(p.indexOf('/', 2));
            logger.debug("getPathRelationship() objectPath: " + objectPath);

            Entity e = new Entity();

            e.setPui("objectIdVal");
            e.setName("objectNameVal");
            e.setDisplayName("objectDisplayNameVal");

            entities.add(e);
        }

        logger.debug("getPathRelationship() Finished");
        return entities;
    }

    private List<Entity> retrieveAllPathTree() {
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
        } catch (IOException ex) {
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
     * @param pathNode
     * @return null if nothing
     */
    private TreeMap<String, JsonNode> parseAllHailPathJsonNode(JsonNode pathNode) {
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
    public Result runQuery(User user, Query query, Result result) throws ResourceInterfaceException {
        logger.debug("runQuery() *** STARTING ***");

        if (result == null)
            logger.error("runQuery() `result` object is null, still.");

        try {
            // Wait for it to be either ready or fail
            while (resourceState != ResourceState.COMPLETE) {
                Thread.sleep(3000);
                updateResourceState();
            }
        } catch (InterruptedException | UnsupportedOperationException e) {
            result.setResultStatus(ResultStatus.ERROR);
            result.setMessage(e.getMessage());
        }

        List<WhereClause> whereClauses = query.getClausesOfType(WhereClause.class);
        result.setResultStatus(ResultStatus.CREATED);
        result.setMessage("Started running the query.");

        // Convert the predicate fields into variables on the Hail request
        // These variables will be used when rendering the Hail template into an actual script.
        Map<String, String> hailVariables = new HashMap<String, String>();
        for (WhereClause whereClause : whereClauses) {

            for (String fieldName : whereClause.getStringValues().keySet()) {
                hailVariables.put(fieldName, whereClause.getStringValues().get(fieldName));
                logger.debug("runQuery() field:" + fieldName + "=" + whereClause.getStringValues().get(fieldName));
            }

            // Convert the predicate value to a hail template
            String hailTemplate = whereClause.getPredicateType().getName();
            logger.debug("runQuery() hailTemplate:" + hailTemplate);
            hailVariables.put("template", hailTemplate);

            // Use the pui from the where clause to set the study name in the list of variables.
            hailVariables.put("study", Utility.getURLFromPui(whereClause
                    .getField()
                    .getPui(), resourceName));

            // Add the desired input file and the output file name to the haiLVariables
            String inputFile = inputFileDir + inputFileName;
            String outputFile = outputFileDir + outputFileName;
            hailVariables.put("dataset", inputFile);
            hailVariables.put("output_name", outputFile);
        }

        // hailVariables now contains at least 'template' and 'study' fields, but not necessarily with valid values
        // Send the JSON request to the remote datasource, as an HTTP POST, with `variables` as the body of the request.
        logger.debug("runQuery() starting hail job submission");
        Date starttime = new Date();

        // Read the PySpark template file
        Map<java.lang.String, java.lang.String> filledTemplate = generateQuery(hailVariables);

        JsonNode nd = restPOST(this.resourceURL + "/sessions/" + sessionID + "/statements", filledTemplate);

        logger.debug("runQuery() hail job submission finished");

        // Parse JSON and evaluate if this is an error, or whatnot
        HailResponse hailResponse = new HailResponse(nd);
        if (hailResponse.isError()) {
            logger.error("runQuery() Hail job failed, due to " + hailResponse.getErrorMessage() + ".");
            result.setResultStatus(ResultStatus.ERROR);
            result.setMessage(hailResponse.getErrorMessage());
        } else {
            logger.debug("runQuery() Hail job started. UUID:" + hailResponse.getJobUUID());
            result.setStartTime(starttime);
            result.setResourceActionId(hailResponse.getJobUUID());
            result.setResultStatus(ResultStatus.RUNNING);
            result.setMessage(hailResponse.getHailMessage());
        }

        logger.debug("runQuery() finished");
        return result;
    }

    @Override
    public Result getResults(User user, Result result) throws ResourceInterfaceException {
        logger.debug("getResults() starting");

        String hailJobUUID = result.getResourceActionId();
        logger.debug("getResults() getting result for " + hailJobUUID);

        JsonNode nd = restGET(resourceURL + "/sessions/" + sessionID + "/statements/" + hailJobUUID);

        HailResponse hailResponse = new HailResponse(nd);
        logger.debug("getResults() finished parsing Hail response.");

        if (hailResponse.isError()) {
            logger.debug("getResults() Hail error message:" + hailResponse.getErrorMessage());
            result.setResultStatus(ResultStatus.ERROR);
            result.setMessage(hailResponse.getErrorMessage());
        } else {
            logger.debug("getResults() jobStatus: " + hailResponse.getJobStatus());

            if (hailResponse.getJobStatus().equalsIgnoreCase("RUNNING")) {
                result.setResultStatus(ResultStatus.RUNNING);
            }
            if (hailResponse.getJobStatus().equalsIgnoreCase("AVAILABLE")) {
                logger.debug("getResults() setting result status to COMPLETE");
                result.setResultStatus(ResultStatus.COMPLETE);

                logger.debug("getResults() parsing returned actual data");
                try {
                    parseData(result, nd);
                } catch (PersistableException pe) {
                    logger.error("getResults() Unable to persist data");
                    result.setResultStatus(ResultStatus.ERROR);
                    result.setMessage(pe.getMessage());
                } catch (ResultSetException re) {
                    logger.error("getResults() Cannot parse HAIL response");
                    result.setResultStatus(ResultStatus.ERROR);
                    result.setMessage(re.getMessage());
                }
            }
        }
        logger.debug("getResults() finished");
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
            for (String fieldName : payload.keySet()) {
                logger.debug("simpleRestCall() add `" + fieldName + "` to payload as `" + payload.get(fieldName) + "`.");
                builder.setParameter(fieldName, payload.get(fieldName));
            }
            get = new HttpGet(builder.build());
            get.addHeader("Content-Type", ContentType.APPLICATION_JSON.toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URL generated:" + urlString);
        }
        CloseableHttpResponse restResponse = null;
        try {
            restResponse = restClient.execute(get);
            restEntity = restResponse.getEntity();
            // https://stackoverflow.com/questions/15969037/why-did-the-author-use-entityutils-consumehttpentity#15970985
            EntityUtils.consume(restEntity);
            logger.debug("simpleRestCall() released entity resource.");
        } catch (IOException ex) {
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

        if (restEntity != null) {
            try {
                return restEntity.getContent();
            } catch (IOException ioex) {
                logger.error("simpleRestCall() Exception:" + ioex);

            } finally {

            }
        }
        return null;
    }

    private InputStream simpleRestCall(String urlString) {
        logger.debug("restCall() Starting");
        HttpEntity restEntity = null;
        CloseableHttpClient restClient = IRCTApplication.CLOSEABLE_HTTP_CLIENT;

        HttpGet get = new HttpGet(urlString);
        get.addHeader("Content-Type", ContentType.APPLICATION_JSON.toString());

        try (CloseableHttpResponse restResponse = restClient.execute(get)) {

            restEntity = restResponse.getEntity();

            // https://stackoverflow.com/questions/15969037/why-did-the-author-use-entityutils-consumehttpentity#15970985
            EntityUtils.consume(restEntity);
            logger.debug("restCall() released entity resource.");
        } catch (IOException ex) {
            logger.error("restCall() IOException: Cannot execute POST with URL: " + urlString);
        }
        logger.debug("restCall() finished.");

        if (restEntity != null) {
            try {
                return restEntity.getContent();
            } catch (IOException ioex) {
                logger.error("restCall() Exception:" + ioex);

            } finally {

            }
        }
        return null;
    }

    /**
     * HTTP POST with JSON body, constructed from `payload` Map, and parse the
     * returned stream into a JsonNode object for later parsing. Protocoll errors
     * are captured as well.
     * Any protocol or parsing error will be thrown as RuntimeException
     *
     * @param urlString
     * @param payload
     * @return
     * @throws JSONException
     * @throws IOException
     */

    private JsonNode restPOST(String urlString, Map<String, String> payload) {
        logger.debug("restPOST() Starting ");
        JsonNode responseObject = null;

        ObjectMapper objectMapper = IRCTApplication.objectMapper;
        CloseableHttpClient restClient = IRCTApplication.CLOSEABLE_HTTP_CLIENT;

        HttpPost post = new HttpPost((urlString));
        post.addHeader("Content-Type", ContentType.APPLICATION_JSON.toString());

        try {
            post.setEntity(
                    new StringEntity(objectMapper
                            .writeValueAsString(payload)));
        } catch (JsonProcessingException e) {
            throw new ResourceInterfaceException("Hail - restPOST() cannot parse payload map to json string for request body: " + payload + ", with message: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            throw new ResourceInterfaceException("Hail - restPOST() the encoding is not supported by apache httppost: " + e.getMessage());
        }

        try (CloseableHttpResponse restResponse = restClient.execute(post)) {

            if (restResponse.getStatusLine().getStatusCode() != 200) {
                logger.error("restPost() Error status response from RESTful call:" + restResponse.getStatusLine().getStatusCode());
            }
            if (restResponse == null) {
                logger.error("restPOST() restResponse is null");
            }
            HttpEntity restEntity = restResponse.getEntity();
            if (restEntity == null) {
                logger.error("restEntity is null");
            }

            if (restResponse.getLastHeader("Content-type").getValue().equalsIgnoreCase("application/json")) {
                // Convert JSON response into Java object
                responseObject = objectMapper
                        .readTree(restEntity
                                .getContent());

            } else {
                logger.debug("restPOST() Response is not JSON mime type.");
                JsonNodeFactory factory = JsonNodeFactory.instance;
                ObjectNode rootNode = factory.objectNode();

                rootNode.put("status", "ok");
                rootNode.put("message", "nonJSON data received");
                rootNode.put("data", EntityUtils.toString(restEntity));

            }
            logger.debug("restPOST() finished parsing data");

            // https://stackoverflow.com/questions/15969037/why-did-the-author-use-entityutils-consumehttpentity#15970985
            EntityUtils.consume(restEntity);
            logger.debug("restPOST() released entity resource.");
        } catch (IOException e) {
            throw new ResourceInterfaceException("Hail - restPost() Could not connect to resource URL." + e.getMessage());
        }

        logger.debug("restPOST() finished.");
        return responseObject;
    }

    /**
     * HTTP GET, parse the returned stream into a JsonNode object for
     * later parsing.
     *
     * @param urlString
     * @return
     * @throws ResourceInterfaceException
     */
    private JsonNode restGET(String urlString) throws ResourceInterfaceException {
        logger.debug("restGET() Starting");
        JsonNode responseObject = null;

        CloseableHttpResponse restResponse = null;
        try {
            ObjectMapper objectMapper = IRCTApplication.objectMapper;
            CloseableHttpClient restClient = HttpClientBuilder.create().build();

            HttpGet get = new HttpGet((urlString));
            restResponse = restClient.execute(get);

            if (restResponse == null) {
                logger.error("restResponse is null");
                throw new ResourceInterfaceException("Could not get Hail response");
            }

            if (restResponse.getStatusLine().getStatusCode() != 200) {
                throw new ResourceInterfaceException("Could not get Hail response (" + restResponse.getStatusLine().getStatusCode() + ")");
            }
            HttpEntity restEntity = restResponse.getEntity();
            if (restEntity == null) {
                logger.error("restEntity is null");
                throw new ResourceInterfaceException("Could not get Hail response");
            }

            if (restEntity.getContentType().getValue().equalsIgnoreCase("application/json")) {
                // Convert JSON response into Java object
                responseObject = objectMapper
                        .readTree(restEntity
                                .getContent());
            } else {
                // Process non JSON data, which happens if we are streaming down actual data
                System.out.println(EntityUtils.toString(restEntity));
            }
            logger.debug("restGET() finished parsing data");

            // https://stackoverflow.com/questions/15969037/why-did-the-author-use-entityutils-consumehttpentity#15970985
            EntityUtils.consume(restEntity);
            logger.debug("restGET() released entity resource.");
        } catch (IOException ex) {
            logger.error("restGET() IOException: " + urlString + " " + ex.getMessage());
            throw new ResourceInterfaceException("Could not get Hail response (" + ex.getMessage() + ")");
        } finally {
            try {
                if (restResponse != null)
                    restResponse.close();
            } catch (Exception ex) {
                logger.error("restGET() finallyException: " + ex.getMessage());
            }
        }
        logger.debug("restGET() finished.");
        return responseObject;
    }

    private void parseData(Result result, JsonNode responseJsonNode)
            throws PersistableException, ResultSetException {
        FileResultSet frs = (FileResultSet) result.getData();

        //Expected to be a string in TSV format.
        //If not, a ResultSetException will most likely occur while appending rows or columns
        String tsv = responseJsonNode.get("output").get("data").asText();

        //first line is header, data starts at second line
        String[] allRows = tsv.split("\n");
        String[] headers = allRows[0].split("\t");
        for (int i = 0; i < headers.length; i++) {
            frs.appendColumn(new Column(headers[i], PrimitiveDataType.STRING));
        }
        for (int i = 1; i < allRows.length; i++) {
            String[] row = allRows[i].split("\t");
            frs.appendRow();
            for (int j = 0; j < row.length; j++) {
                frs.updateString(j, row[j]);
            }
        }

        result.setData(frs);
        result.setDataType(ResultDataType.TABULAR);
        result.setMessage("Data has been downloaded");
    }

    private java.util.Map generateQuery(Map variables) {

        // Get the specified variables out of the HashMap hailVariables
        Object dataSet = variables.get("dataset");
        Object gene = variables.get("gene");
        Object significance = variables.get("significance");
        Object subjectIds = variables.get("subject_id");
        Object outputDir = variables.get("output_name");

        // Fill in the template with the desired variables
        String template = "import hail as hl\n" +
                "hl.init(sc, quiet=True, idempotent=True)\n" +
                "mt = hl.import_table('" + dataSet + "')\n" +
                "new_data = mt.filter((mt.Hugo_Symbol=='" + gene + "')&" +
                "(mt.CLIN_SIG=='" + significance + "')&" +
                "(mt.Tumor_Sample_Barcode=='" + subjectIds + "'))\n" +
                "new_data.export(output='" + outputDir + "', types_file='maf')";

        // Create a HashMap to specify where the data code can be found for the post request
        HashMap<java.lang.String, java.lang.String> postTemplate = new HashMap<>();
        postTemplate.put("code", template);

        return postTemplate;
    }

    private void updateResourceState() {

        // Create a dictionary to map the state of Livy to the ResourceState
        HashMap<String, ResourceState> stateMapping = new HashMap<>();
        stateMapping.put("not_started", ResourceState.READY);
        stateMapping.put("starting", ResourceState.RUNNING);
        stateMapping.put("idle", ResourceState.COMPLETE);
        stateMapping.put("busy", ResourceState.RUNNING);
        stateMapping.put("shutting_down", ResourceState.RUNNING);
        stateMapping.put("error", null);
        stateMapping.put("dead", null);
        stateMapping.put("success", ResourceState.COMPLETE);

        // Get the state of the session
        JsonNode request = restGET(this.resourceURL + "/sessions/" + sessionID);
        String requestState = request.get("state").asText();

        // Convert the Livy state to the one of IRCT and set the new resource state
        resourceState = stateMapping.get(requestState);
    }

    class PathElement {
        String pui;
    }

    class HailResponse {

        private String errorMessage = "";
        private String hailMessage = "";
        private String jobStatus = "";
        private Data hailData = null;

        public HailResponse(JsonNode rootNode) {
            logger.debug("HailResponse() constructor");

            if (rootNode.get("state") == null) {
                logger.error("HailResponse() 'status' field is mandatory, but it is missing.");
            } else {
                logger.debug("HailResponse() 'status' field has data in it.");
                // Start parsing a Hail response
                if (rootNode.get("state").textValue().equalsIgnoreCase("waiting") ||
                        rootNode.get("state").textValue().equalsIgnoreCase("running") ||
                                rootNode.get("state").textValue().equalsIgnoreCase("available")) {
                    logger.debug("HailResponse() Success message from Hail.");

                    // Success response from Hail
                    if (rootNode.get("output") != null) {
                        // If this is a job response
                        logger.debug("HailResponse() parse job details.");
                        this.jobUUID = rootNode.get("id").toString();
                        this.hailMessage = rootNode.get("output").textValue();
                        String state = rootNode.get("state").textValue();
                        mapResultState(state);
                    } else {
                        logger.debug("HailResponse() parse data details.");
                        this.jobUUID = rootNode.get("id").toString();
                        this.hailMessage = "Parsing Hail data";
                        String state = rootNode.get("state").textValue();
                        mapResultState(state);
                    }

                } else {
                    logger.debug("HailResponse() Error message from Hail" + rootNode.get("message").textValue());

                    // Error response from Hail
                    this.errorMessage = rootNode.get("message").textValue();
                }
            }
            logger.debug("HailResponse() finished");
        }

        public void mapResultState(String stateLivy) {

            // Create a dictionary to map the result state of Livy to the one of IRCT
            HashMap<String, String> resultStateMapping = new HashMap<>();
            resultStateMapping.put("waiting", "");
            resultStateMapping.put("running", "RUNNING");
            resultStateMapping.put("available", "AVAILABLE");
            resultStateMapping.put("error", "ERROR");
            resultStateMapping.put("cancelling", "ERROR");
            resultStateMapping.put("cancelled", "ERROR");

            this.jobStatus = resultStateMapping.get(stateLivy);
          }

        public String getJobUUID() {
            return jobUUID;
        }

        private String jobUUID = "";

        public boolean isError() {
            return !this.errorMessage.isEmpty();
        }

        public String getErrorMessage() {
            return this.errorMessage;
        }

        public void setError(String errorMsg) {
            this.errorMessage = errorMsg;
        }

        public Data getData() {
            return this.hailData;
        }

        public String getJobStatus() {
            return jobStatus;
        }

        public String getHailMessage() {
            return hailMessage;
        }

    }
}
