package edu.harvard.hms.dbmi.bd2k.picsure.ri;

import com.fasterxml.jackson.core.JsonParseException;
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
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
        logger.debug(p);
        if (p.indexOf('/',2)==-1) {
            // This is a request for the root
            logger.debug("querying the root path");


            restCall(resourceURL + '/objects', , rslt);
        } else {
            String objectPath = p.substring(p.indexOf('/',2));
            logger.debug("substring "+objectPath);

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
                String hailEndpoint = whereClause.getPredicateType().getName();
                logger.debug("runQuery() hailEndpoint:"+hailEndpoint);

                for (String fieldName: whereClause.getStringValues().keySet()) {
                    logger.debug("runQuery() endpoint Payload :"+fieldName+"="+whereClause.getStringValues().get(fieldName));
                }
                whereClause.getStringValues().put("study", Utility.getURLFromPui(whereClause
                        .getField()
                        .getPui(),resourceName));

                logger.debug("runQuery() endpoint URL:"+resourceURL + '/' + hailEndpoint);

                restCall(resourceURL + '/' + hailEndpoint, whereClause.getStringValues(), result);
            }
            logger.debug("runQuery() made the HTTP call. ");

        } catch (Exception e) {
            logger.error(String.format("runQuery() Exception: %s", e.getMessage()));

            result.setResultStatus(ResultStatus.ERROR);
            result.setMessage(String.valueOf(e.getMessage()));
        }

        logger.debug("runQuery() finished");
        return result;
    }

    @Override
    public Result getResults(User user, Result result) {
        logger.debug("getResults() starting");
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

    private void restCall(String urlString, Map<String, String> payload, Result result) {
        logger.debug("restCall() Starting with query_string");

        ObjectMapper objectMapper = IRCTApplication.objectMapper;

        CloseableHttpClient restClient = HttpClientBuilder.create().build();
        URIBuilder builder = null;
        HttpGet get = null;
        try {
            builder = new URIBuilder(urlString);
            for (String fieldName: payload.keySet()) {
                builder.setParameter(fieldName, payload.get(fieldName));
            }
            get = new HttpGet(builder.build());
            get.addHeader("Content-Type", ContentType.APPLICATION_JSON.toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Invalid URL generated:"+urlString);
        }
        CloseableHttpResponse restResponse = null;

        try {
            result.setResultStatus(ResultStatus.RUNNING);
            restResponse = restClient.execute(get);
            HttpEntity restEntity = restResponse.getEntity();

            // parsing data
            parseData(result, objectMapper
                    .readTree(restEntity
                            .getContent()));

            result.setResultStatus(ResultStatus.COMPLETE);

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

            // parsing data
            parseData(result, objectMapper
                    .readTree(restEntity
                            .getContent()));

            result.setResultStatus(ResultStatus.COMPLETE);

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

        String responseStatus = responseJsonNode.get("status").textValue();

        JsonNode matrixNode = responseJsonNode.get("matrix");
        if (responseStatus.equalsIgnoreCase("ok")){
            if (!matrixNode.getNodeType().equals(JsonNodeType.ARRAY)
                    || !matrixNode.get(0).getNodeType().equals(JsonNodeType.ARRAY)){
                String errorMessage = "Cannot parse response JSON from Hail: expecting an 2D array";
                result.setMessage(errorMessage);
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


    class PathElement {
        String pui;

    }
}
