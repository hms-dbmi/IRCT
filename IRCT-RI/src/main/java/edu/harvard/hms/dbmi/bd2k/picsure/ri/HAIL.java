package edu.harvard.hms.dbmi.bd2k.picsure.ri;

import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.find.FindInformationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Entity;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.model.process.IRCTProcess;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.PrimitiveDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ResourceState;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.PathResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.ProcessResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.QueryResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultStatus;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.Column;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.tabular.FileResultSet;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.User;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A resource implementation of a data source that communicates with a HAIL proxy via HTTP
 */
public class HAIL
        implements QueryResourceImplementationInterface, PathResourceImplementationInterface, ProcessResourceImplementationInterface {

    Logger logger = Logger.getLogger(this.getClass());

    protected String resourceName;
    protected String resourceURL;

    protected ResourceState resourceState;

    @Override
    public void setup(Map<String, String> parameters) throws ResourceInterfaceException {

        if (!parameters.keySet().contains("resourceName"))
            throw new ResourceInterfaceException("Missing mandatory `resourceName` parameter.");

        if (!parameters.keySet().contains("resourceURL"))
            throw new ResourceInterfaceException("Missing mandatory `resourceURL` parameter.");

        logger.debug("setup() finished setting up everything. Zoom-zoom...");
        resourceState = ResourceState.READY;
    }

    @Override
    public String getType() {
        return "hail";
    }

    @Override
    public List<Entity> getPathRelationship(Entity path, OntologyRelationship relationship, User user) {
        logger.debug("getPathRelationship() Starting");

        logger.debug("getPathRelationship() path:"+path.getName());
        logger.debug("getPathRelationship() path:"+relationship.getName());

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
        result.setResultStatus(ResultStatus.CREATED);
        result.setMessage("Started running the query.");

        try {
            // TODO: Make a remote HTTP call to the resource endpoint (HAIL master cluster IP address)
            String resultId = "stringResultId";
            String queryId = "stringQqueryId";

            // TODO: What the heck is THIS for?
            result.setResourceActionId("resourceactionid");
            result.setResultStatus(ResultStatus.RUNNING);

            restCall(this.resourceURL+"/spark", result);

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

        try {
            if (result == null) {
                logger.debug("getResults() result is null!");
            }

            result.setMessage("No result from HAIL, yet.");
            result.setResultStatus(ResultStatus.COMPLETE);

        } catch (Exception e) {
            logger.error(String.format("getResults() Exception:%s", e.getMessage()));

            result.setMessage(String.valueOf(e.getMessage()));
            result.setResultStatus(ResultStatus.ERROR);
        }

        logger.debug("getResults() finished");
        return result;
    }

    @Override
    public ResourceState getState() {
        return resourceState;
    }
    @Override
    public ResultDataType getProcessDataType(IRCTProcess pep) {
        return ResultDataType.JSON;
    }

    @Override
    public ResultDataType getQueryDataType(Query query) {
        return ResultDataType.JSON;
    }

    @Override
    public Result runProcess(User user, IRCTProcess process,
                             Result result) throws ResourceInterfaceException {
        logger.debug("runProcess() starting");

        try {

        } catch (Exception e) {
            logger.error(String.format("runProcess() Exception:%s", e.getMessage()));
            result.setResultStatus(ResultStatus.ERROR);
            result.setMessage(e.getMessage());
        }
        logger.debug("runProcess() starting");
        return result;
    }

    private void restCall(String urlString, Result result) {
        logger.debug("restCall() starting "+urlString);

        CloseableHttpClient restClient = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(urlString);
        CloseableHttpResponse restResponse = null;

        try {
            result.setResultStatus(ResultStatus.RUNNING);

            restResponse = restClient.execute(get);
            HttpEntity restEntity = restResponse.getEntity();

            // Stream processing of the response body
            String inputLine ;
            StringBuilder content = new StringBuilder();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(restEntity.getContent()))) {
                while ((inputLine = br.readLine()) != null) content.append(inputLine);
            } catch (Exception e) {
                logger.error("restCall() Exception reading HTTP response. "+String.valueOf(e.getMessage()));
            }
            logger.debug("restClient() Converted responseBody to String.");

            FileResultSet frs = (FileResultSet) result.getData();
            frs.appendColumn(newStringColumn("status"));
            frs.appendColumn(newStringColumn("message"));
            frs.appendColumn(newStringColumn("content"));

            frs.appendRow();
            frs.updateString("status", "OK");
            //responseJsonNode.get("message").textValue()
            frs.updateString("message", "message received");
            frs.updateString("content", content.toString());

            result.setData(frs);
            logger.debug("restClient() wrote data to `result` object.");
            result.setResultStatus(ResultStatus.COMPLETE);

            // https://stackoverflow.com/questions/15969037/why-did-the-author-use-entityutils-consumehttpentity#15970985
            EntityUtils.consume(restEntity);
            logger.debug("restClient() released entity resource.");

        } catch (Exception ex ){
            logger.error("restCall() Exception:"+ex.getMessage());

            result.setResultStatus(ResultStatus.ERROR);
            result.setMessage(String.valueOf(ex.getMessage()));

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

    private Column newStringColumn(String columnLabel) {
        Column c = new Column();
        c.setName(columnLabel);
        c.setDataType(PrimitiveDataType.STRING);
        return c;
    }

}
