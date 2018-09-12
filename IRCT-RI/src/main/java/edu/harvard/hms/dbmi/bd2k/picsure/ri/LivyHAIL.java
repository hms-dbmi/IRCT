package edu.harvard.hms.dbmi.bd2k.picsure.ri;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.harvard.hms.dbmi.bd2k.irct.IRCTApplication;
import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.WhereClause;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ResourceState;
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
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * A resource implementation of a data source that communicates with a HAIL proxy via HTTP
 */
@SuppressWarnings("Duplicates")
public class LivyHAIL implements QueryResourceImplementationInterface {
    Logger logger = Logger.getLogger(this.getClass());

    protected String resourceName;
    protected String resourceURL;
    protected ResourceState resourceState;

    private String sessionID;
    private String inputFile;

    private String dataFileDir = "/app/data/";

    /**
     * Set up the resource parameters, initialize a PySpark session, start the
     * Hail library and load all available data files as Hail tables.
     *
     * @param parameters Map<String, String> of setup parameters
     * @throws ResourceInterfaceException if a set up parameter is missing
     */
    @Override
    public void setup(Map<String, String> parameters) throws ResourceInterfaceException {

        if (logger.isDebugEnabled())
            logger.debug("setup for Hail" +
                    " Starting...");

        // Check the resource name
        String errorString = "";
        this.resourceName = parameters.get("resourceName");
        if (this.resourceName == null) {
            logger.error("setup() `resourceName` parameter is missing.");
            errorString += " resourceName";
        }

        // Check the resource URL
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

        // Initialize a new PySpark session
        HashMap<String, String> kindSpecified = new HashMap<>();
        kindSpecified.put("kind" , "pyspark");
        JsonNode sessionResponse = restPOST(this.resourceURL + "/sessions", kindSpecified);
        sessionID = sessionResponse.get("id").toString();

        try {
            // Wait for the session to be either ready or fail
            while (resourceState != ResourceState.COMPLETE) {
                Thread.sleep(3000);
                updateResourceState();
                logger.debug(resourceName + " is in state" + resourceState);
            }
        } catch (InterruptedException | UnsupportedOperationException e) {
            logger.error("Session state is not ready");
        }

        // Initialize hail and load all datafiles as a hail table
        Map<java.lang.String, java.lang.String> dataTemplate = loadDataTemplate();
        restPOST(this.resourceURL + "/sessions/" + sessionID + "/statements", dataTemplate);

        logger.debug("setup() for " + resourceName +
                " Finished. " + resourceName +
                " is in COMPLETE state.");
    }

    /**
     * A string representation of the type of resource implementation this is.
     *
     * @return Type of resource
     */
    @Override
    public String getType() {
        return "Hail";
    }

    /**
     * Run the given query. It converts the predicate fields into variables on the Hail request,
     * create a POST request with the variables set in a Hail function using a PySpark template.
     *
     * @param user A basic user representation. It can be associated with a session in EE 7. The
     * userId, and name are the same in this implementation.
     * @param query A query against any individual or group of resources
     * @param result Execution that is run on the IRCT
     * @return Execution that is run on the IRCT with set parameters for receiving it
     * @throws ResourceInterfaceException when there is an error in the Resource Interface
     */
    @Override
    public Result runQuery(User user, Query query, Result result) throws ResourceInterfaceException {
        logger.debug("runQuery() *** STARTING ***");

        if (result == null)
            logger.error("runQuery() `result` object is null, still.");

        List<WhereClause> whereClauses = query.getClausesOfType(WhereClause.class);
        result.setResultStatus(ResultStatus.CREATED);
        result.setMessage("Started running the query.");

        // Convert the predicate fields into variables on the Hail request
        // These variables will be used when rendering the Hail template into an actual script.
        Map<String, String> hailVariables = new HashMap<>();
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

            // Add the desired input file to the haiLVariables, which is at the second position of the PUI
            inputFile = whereClause.getField().getPui().split("/")[2];
        }

        // hailVariables now contains at least 'template' and 'study' fields, but not necessarily with valid values
        // Send the JSON request to the remote datasource, as an HTTP POST, with `variables` as the body of the request.
        logger.debug("runQuery() starting hail job submission");
        Date starttime = new Date();

        // Read the PySpark template file and create a request with it
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

    /**
     * Perform GET Request on the Hail job, and parse the data as a JSON table.
     *
     * @param user A basic user representation. It can be associated with a session in EE 7. The
     * userId, and name are the same in this implementation.
     * @param result Execution that is run on the IRCT with set parameters for receiving it
     * @return Result of the Hail query
     * @throws ResourceInterfaceException when there is an error in the Resource Interface
     */
    @Override
    public Result getResults(User user, Result result) throws ResourceInterfaceException {
        logger.debug("getResults() starting");

        String hailJobUUID = result.getResourceActionId();
        logger.debug("getResults() getting result for " + hailJobUUID);

        // Create a GET request on the Hail job.
        JsonNode nd = restGET(resourceURL + "/sessions/" + sessionID + "/statements/" + hailJobUUID);

        HailResponse hailResponse = new HailResponse(nd);
        logger.debug("getResults() finished parsing Hail response.");

        // Check for errors
        if (hailResponse.isError()) {
            logger.debug("getResults() Hail error message:" + hailResponse.getErrorMessage());
            result.setResultStatus(ResultStatus.ERROR);
            result.setMessage(hailResponse.getErrorMessage());
        // Get the results when no errors are occurred
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
                    // Parse the output
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

    /**
     * Get the state of the resource.
     * Note that this function is not used, but part is of the
     * 'QueryResourceImplementationInterface'.
     *
     * @return the resource state
     */
    @Override
    public ResourceState getState() {
        return resourceState;
    }

    /**
     * Get the result data type.
     * Note that this function is not used, but part is of the
     * 'QueryResourceImplementationInterface'.
     *
     * @param query Query to run
     * @return Result data type
     */
    @Override
    public ResultDataType getQueryDataType(Query query) {
        return ResultDataType.TABULAR;
    }

    /**
     * HTTP POST with JSON body, constructed from `payload` Map, and parse the
     * returned stream into a JsonNode object for later parsing. Protocol errors
     * are captured as well.
     * Any protocol or parsing error will be thrown as RuntimeException.
     *
     * @param urlString String referring to the Resource URL
     * @param payload Map<String, String> of Livy parameters of the request body
     * @return JsonNode containing the elements inside a JSON stream
     */
    private JsonNode restPOST(String urlString, Map<String, String> payload) {
        logger.debug("restPOST() Starting ");
        JsonNode responseObject = null;

        ObjectMapper objectMapper = IRCTApplication.objectMapper;
        CloseableHttpClient restClient = IRCTApplication.CLOSEABLE_HTTP_CLIENT;

        // Create the HTTP post request
        HttpPost post = new HttpPost((urlString));
        post.addHeader("Content-Type", ContentType.APPLICATION_JSON.toString());

        // Set the request body as a string entity
        try {
            post.setEntity(
                    new StringEntity(objectMapper
                            .writeValueAsString(payload)));
        } catch (JsonProcessingException e) {
            throw new ResourceInterfaceException("Hail - restPOST() cannot parse payload map to json string for request body: " + payload + ", with message: " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            throw new ResourceInterfaceException("Hail - restPOST() the encoding is not supported by apache httppost: " + e.getMessage());
        }

        // Try to close the request and otherwise raise an error
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
     * HTTP GET, parse the returned stream into a JsonNode object for later parsing.
     *
     * @param urlString String referring to the Resource URL
     * @return JsonNode object of the JSON response
     * @throws ResourceInterfaceException when there is an error in the Resource Interface
     */
    private JsonNode restGET(String urlString) throws ResourceInterfaceException {
        logger.debug("restGET() Starting");
        JsonNode responseObject = null;

        CloseableHttpResponse restResponse = null;
        try {
            ObjectMapper objectMapper = IRCTApplication.objectMapper;
            CloseableHttpClient restClient = HttpClientBuilder.create().build();

            // Executes HTTP request using the given Resource URL
            HttpGet get = new HttpGet((urlString));
            restResponse = restClient.execute(get);

            // Check for errors
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

    /**
     * Parse the output element of the JsonNode into a JSON Table.
     *
     * @param result Execution that is run on the IRCT with set parameters for receiving it
     * @param responseJsonNode Output of GET request containing JsonNode elements
     * @throws PersistableException when a problem occours by the persistence provider
     * @throws ResultSetException when the data can not be converted to the result table
     */
    private void parseData(Result result, JsonNode responseJsonNode)
            throws PersistableException, ResultSetException {
        FileResultSet frs = (FileResultSet) result.getData();

        // Expected to be a string in TSV format.
        // If not, a ResultSetException will most likely occur while appending rows or columns
        JsonNode queryOutput = responseJsonNode.get("output").get("data").get("text/plain");
        // Remove the first and the last apostrophes to avoid json in string format
        String jsonString = queryOutput.textValue().replaceAll("^'|'$", "");

        JSONObject jsonObject = new JSONObject(jsonString);
        logger.debug("Parsed json object: " + jsonObject);

        // Get the header
        JSONArray fields = jsonObject.getJSONObject("schema").getJSONArray("fields");
        for (int i = 0; i < fields.length(); i++) {
            JSONObject field = fields.getJSONObject(i);
            // Remove the index column, which is automatically added by converting to JSON
            if (!field.get("name").equals("index")) {
                PrimitiveDataType dataType = PrimitiveDataType.valueOf(field.getString("type").toUpperCase());
                frs.appendColumn(new Column(field.getString("name"), dataType));
            }
        }
        // Get the data rows
        JSONArray rows = jsonObject.getJSONArray("data");
        for (int i = 0; i < rows.length(); i++) {
            JSONObject row = rows.getJSONObject(i);
            frs.appendRow();
            // Get for every row the data per column
            for (int j = 0; j < row.length(); j++) {
                String header = fields.getJSONObject(j).getString("name");
                if (!header.equals("index")) {
                    String value = row.get(header).toString();
                    frs.updateString(j-1, value);
                }
            }
        }

        result.setData(frs);
        result.setDataType(ResultDataType.TABULAR);
        result.setMessage("Data has been downloaded");
    }

    /**
     * Get the Hail variables and put them in the PySpark template with the correct Hail
     * function.
     *
     * @param variables <String, String> of Hail variables
     * @return Map<java.lang.String, java.lang.String> of the Livy request body 'code'
     */
    private java.util.Map<java.lang.String, java.lang.String> generateQuery(Map variables) {

        // Get the specified variables out of the HashMap hailVariables
        String gene = variables.get("gene").toString();
        String significance = variables.get("significance").toString();
        String subjectIds = variables.get("subject_id").toString();

        // Parse multiple variables into one string suitable for the Hail template
        String genesHailFormat = parseMultipleVariables(gene, "GENE");
        String significancesHailFormat = parseMultipleVariables(significance, "CLIN_SIG");
        String idsHailFormat = parseMultipleVariables(subjectIds, "ID");

        // Fill in the template with the desired variables
        String template =
                "new_data = all_data_files['" + inputFile + "'].filter((" + genesHailFormat + ") & " +
                                     "(" + significancesHailFormat + ") & " +
                                     "(" + idsHailFormat + "))\n" +
                "new_data.to_pandas().to_json(orient='table')";

        // Create a HashMap to specify where the data code can be found for the post request
        HashMap<java.lang.String, java.lang.String> postTemplate = new HashMap<>();
        postTemplate.put("code", template);

        return postTemplate;
    }

    /**
     * Check if a Hail variable consist of multiple values and parse the values to a
     * PySpark template that can be read by Hail.
     *
     * @param variable String of Hail variables separated by comma's
     * @param columnName String referring the to column name in the data file
     * @return String readable by Hail using PySpark
     */
    private String parseMultipleVariables(String variable, String columnName) {

        // The variables are strings that can consist of multiple elements separated by commas
        String[] splittedVariables = variable.split(",");

        StringBuilder hailFormat = new StringBuilder();
        int i = 0;
        // Create for every variable a string in where the column is set to the variable name
        for (String var : splittedVariables) {
            String part = "(all_data_files['" + inputFile + "']." + columnName + "=='" + var + "')";
            if (i++ == splittedVariables.length-1) {
                hailFormat.append(part);
            } else {
                // The string should be extended with the or operator until the last element
                hailFormat.append(part + " | ");
            }
        }

        return hailFormat.toString();
    }

    /**
     * Create a PySpark template that loads the Hail library and read all data files
     * as a Hail table.
     *
     * @return java.util.Map<java.lang.String, java.lang.String> of the Livy request
     * body 'code'
     */
    private java.util.Map<java.lang.String, java.lang.String> loadDataTemplate() {

        // Create a PySpark that imports hail and load all datafiles as a hail table
        String pysparkFormatTemplate =
                "import warnings\n" +
                "warnings.filterwarnings('ignore')\n" +
                "import hail as hl\n" +
                "hl.init(sc, quiet=True, idempotent=True)\n" +
                "import os\n" +
                "all_data_files = {}\n" +
                "for data_file in os.listdir('" + dataFileDir + "'):\n" +
                "\tif data_file.endswith('.maf') or data_file.endswith('.tsv'):\n" +
                "\t\tall_data_files[data_file] = hl.import_table('" + dataFileDir + "'+data_file)\n";

        // Create a HashMap to specify where the data code can be found for the post request
        HashMap<java.lang.String, java.lang.String> postDataTemplate = new HashMap<>();
        postDataTemplate.put("code", pysparkFormatTemplate);

        return postDataTemplate;
    }

    /**
     * Map the resource state of Livy to the one of IRCT.
     */
    private void updateResourceState() {

        // Create a dictionary to map the state of Livy to the IRCT ResourceState
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

    /**
     * Get the Hail response of the POST request.
     */
    class HailResponse {

        private String errorMessage = "";
        private String hailMessage = "";
        private String jobStatus = "";

        /**
         * Get JsonNode elements out of the response of the POST request.
         *
         * @param rootNode JsonNode containing the elements inside a JSON stream
         */
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
                        // Get JsonNode elements if this is a job response
                        logger.debug("HailResponse() parse job details.");
                        this.jobUUID = rootNode.get("id").toString();
                        this.hailMessage = rootNode.get("output").textValue();
                        String state = rootNode.get("state").textValue();
                        mapResultState(state);
                    } else {
                        // Else get the available elements out to get the output of te response later
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

        /**
         * Map the result State of Livy to the one of IRCT.
         *
         * @param stateLivy String that represent the result state of Livy
         */
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

        /**
         * Get the Hail job number.
         *
         * @return String representing the Hail job number
         */
        public String getJobUUID() {
            return jobUUID;
        }

        private String jobUUID = "";

        /**
         * Check if an error occurs.
         *
         * @return boolean that refers if there is an error message or not
         */
        public boolean isError() {
            return !this.errorMessage.isEmpty();
        }

        /**
         * Get the error message.
         *
         * @return String with the error message
         */
        public String getErrorMessage() {
            return this.errorMessage;
        }

        /**
         * Get the IRCT status of the Hail job.
         *
         * @return String that represents the state a result is in
         */
        public String getJobStatus() {
            return jobStatus;
        }

        /**
         * Get the JsonNode element 'output' out of the JSON stream.
         *
         * @return String of the JsonNode element 'output'
         */
        public String getHailMessage() {
            return hailMessage;
        }

    }
}
