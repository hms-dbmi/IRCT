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
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A resource implementation of a data source that communicates with a HAIL proxy via HTTP
 */
public class HAIL implements QueryResourceImplementationInterface,
        PathResourceImplementationInterface {

    Logger logger = Logger.getLogger(this.getClass());

    protected String resourceName;
    protected String resourceURL;

    protected ResourceState resourceState;

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

//		retrieveToken();

        resourceState = ResourceState.READY;
        logger.debug( "setup() for " + resourceName +
                " Finished. " + resourceName +
                " is in READY state.");
    }

    @Override
    public String getType() {
        return "hail";
    }

    @Override
    public List<Entity> getPathRelationship(Entity path, OntologyRelationship relationship, User user) {
        logger.debug("getPathRelationship() Starting");

        logger.debug("getPathRelationship() path:"+ path);

        List<Entity> entities = new ArrayList<Entity>();
        logger.debug("getPathRelationship() Finished");
        return entities;
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
                restCall(resourceURL + Utility.getURLFromPui(whereClause
                        .getField()
                        .getPui(),resourceName), result);
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

    private void restCall(String urlString, Result result) {

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
            logger.debug("restClient() released entity resource.");

        } catch (PersistableException ex) {
            result.setResultStatus(ResultStatus.ERROR);
            logger.error("Persistable error: " + ex.getMessage() );
        } catch (ResultSetException ex) {
            result.setResultStatus(ResultStatus.ERROR);
            logger.error("Cannot append row: " + ex.getMessage());
        } catch (JsonParseException ex){
            result.setResultStatus(ResultStatus.ERROR);
            result.setMessage("Cannot parse gnome response as a JsonNode");
            logger.error("Cannot parse response as a JsonNode: " + ex.getMessage());
        } catch (IOException ex ){
            result.setResultStatus(ResultStatus.ERROR);
            result.setMessage("Cannot execute Post request to gnome");
            logger.error("IOException: Cannot cannot execute POST with URL: " + urlString);
        } finally {
            try {
                if (restResponse != null)
                    restResponse.close();
            } catch (Exception ex) {
                logger.error("restCall() finallyException: " + ex.getMessage());
            }
        }
        logger.debug("restClient() finished.");
    }

    private void parseData(Result result, JsonNode responseJsonNode)
            throws PersistableException, ResultSetException{
        FileResultSet frs = (FileResultSet) result.getData();

        String responseStatus = responseJsonNode.get("status").textValue();

        JsonNode matrixNode = responseJsonNode.get("matrix");
        if (responseStatus.equalsIgnoreCase("ok")){
            if (!matrixNode.getNodeType().equals(JsonNodeType.ARRAY)
                    || !matrixNode.get(0).getNodeType().equals(JsonNodeType.ARRAY)){
                String errorMessage = "Cannot parse response JSON from gnome: expecting an 2D array";
                result.setMessage(errorMessage);
                throw new PersistableException(errorMessage);
            }

            // append columns
            for (JsonNode innerJsonNode : matrixNode.get(0)){
                if (!innerJsonNode.getNodeType().equals(JsonNodeType.STRING)){
                    String errorMessage = "Cannot parse response JSON from gnome: expecting a String in header array";
                    result.setMessage(errorMessage);
                    throw new PersistableException(errorMessage);
                }

                // how can I know what datatype it is for now?... just set it primitive string...
                frs.appendColumn(new Column(innerJsonNode.textValue(), PrimitiveDataType.STRING));
            }

            // append rows
            for (int i = 1; i < matrixNode.size(); i++){
                JsonNode jsonNode = matrixNode.get(i);
                if (!jsonNode.getNodeType().equals(JsonNodeType.ARRAY)){
                    String errorMessage = "Cannot parse response JSON from gnome: expecting an 2D array";
                    result.setMessage(errorMessage);
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
}
