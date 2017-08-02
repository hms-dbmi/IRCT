package edu.harvard.hms.dbmi.bd2k.irct.ri.elasticsearch;

import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;

import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.carrotsearch.hppc.cursors.ObjectObjectCursor;

import edu.harvard.hms.dbmi.bd2k.irct.exception.ResourceInterfaceException;
import edu.harvard.hms.dbmi.bd2k.irct.model.find.FindInformationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.Entity;
import edu.harvard.hms.dbmi.bd2k.irct.model.ontology.OntologyRelationship;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.ClauseAbstract;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.Query;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.SelectClause;
import edu.harvard.hms.dbmi.bd2k.irct.model.query.WhereClause;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.ResourceState;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.PathResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.resource.implementation.QueryResourceImplementationInterface;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.Result;
import edu.harvard.hms.dbmi.bd2k.irct.model.result.ResultDataType;
import edu.harvard.hms.dbmi.bd2k.irct.model.security.SecureSession;

public class ElasticSearchResourceImplementation implements
		QueryResourceImplementationInterface,
		PathResourceImplementationInterface {
	private String resourceName;
	private String resourceURL;
	private ResourceState resourceState;
	private String index;

	private Client client;

	@Override
	public void setup(Map<String, String> parameters)
			throws ResourceInterfaceException {
		String[] strArray = { "resourceName", "resourceURL" };
		if (!parameters.keySet().containsAll(Arrays.asList(strArray))) {
			throw new ResourceInterfaceException("Missing parameters");
		}
		this.resourceName = parameters.get("resourceName");
		this.resourceURL = parameters.get("resourceURL");
		this.index = parameters.get("index");

		String url = resourceURL.split(":")[0];
		String port = resourceURL.split(":")[1];

		try {
			client = TransportClient
					.builder()
					.build()
					.addTransportAddress(
							new InetSocketTransportAddress(InetAddress
									.getByName(url), Integer.parseInt(port)));
		} catch (UnknownHostException e) {
			throw new ResourceInterfaceException("Unknown host");
		}

		resourceState = ResourceState.READY;
	}

	@Override
	public List<Entity> getPathRelationship(Entity path,
			OntologyRelationship relationship, SecureSession session)
			throws ResourceInterfaceException {
		List<Entity> returns = new ArrayList<Entity>();

		String basePath = path.getPui();
		String[] pathComponents = basePath.split("/");

		if (pathComponents.length >= 5) {
			return returns;
		}

		// Get a list of all the indexes if the index is not set
		if ((this.index == null) && (pathComponents.length == 2)) {
			String[] response = client.admin().cluster().prepareState()
					.execute().actionGet().getState().getMetaData()
					.concreteAllIndices();
			for (String index : response) {
				Entity entity = new Entity();
				entity.setName(index);
				entity.setDisplayName(index);
				entity.setPui(basePath + "/" + index);
				returns.add(entity);
			}
			return returns;
		} else if (((this.index != null) && (pathComponents.length == 2))
				|| ((this.index == null) && (pathComponents.length == 3))) {
			String searchIndex = "";
			if (this.index == null) {
				searchIndex = pathComponents[2];
			} else {
				searchIndex = this.index;
			}
			ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> f = client
					.admin().indices()
					.getMappings(new GetMappingsRequest().indices(searchIndex))
					.actionGet().getMappings();

			ImmutableOpenMap<String, MappingMetaData> mapping = f
					.get(searchIndex);
			for (ObjectObjectCursor<String, MappingMetaData> c : mapping) {
				Entity entity = new Entity();
				entity.setName(c.key);
				entity.setDisplayName(c.key);
				entity.setPui(basePath + "/" + c.key);
				returns.add(entity);
			}
			return returns;
		} else if (((this.index != null) && (pathComponents.length == 3))
				|| ((this.index == null) && (pathComponents.length == 4))) {
			String searchIndex = "";
			String searchObject = "";
			if (this.index == null) {
				searchIndex = pathComponents[2];
				searchObject = pathComponents[3];
			} else {
				searchIndex = this.index;
				searchObject = pathComponents[2];
			}

			GetMappingsResponse res = client.admin().indices()
					.getMappings(new GetMappingsRequest().indices(searchIndex))
					.actionGet();
			MappingMetaData rawMap = res.mappings().get(searchIndex)
					.get(searchObject);

			JsonObject jsonObject = Json
					.createReader(new StringReader(rawMap.source().toString()))
					.readObject().getJsonObject(searchObject)
					.getJsonObject("properties");

			for (String key : jsonObject.keySet()) {
				Entity entity = new Entity();
				entity.setName(key);
				entity.setDisplayName(key);
				entity.setPui(basePath + "/" + key);
				returns.add(entity);
			}

		}

		return returns;
	}

	@Override
	public Result runQuery(SecureSession session, Query qep, Result result)
			throws ResourceInterfaceException {
		SearchRequestBuilder sb = new SearchRequestBuilder(client,
				SearchAction.INSTANCE);
		BoolQueryBuilder qb = boolQuery();

		ArrayList<String> indices = new ArrayList<String>();
		ArrayList<String> types = new ArrayList<String>();

		for (ClauseAbstract clause : qep.getClauses().values()) {
			if (clause instanceof SelectClause) {
				SelectClause sc = (SelectClause) clause;
				indices.add(getIndexNameFrom(sc.getParameter().getPui()));
				types.add(getTypeNameFrom(sc.getParameter().getPui()));

				if (sc.getParameter().getDataType() == ElasticSearchDataType.FILE) {
					sb.addHighlightedField(getFieldNameFrom(sc.getParameter()
							.getPui()) + ".content");
				} else {
					sb.addField(getFieldNameFrom(sc.getParameter().getPui()));
				}

			} else if (clause instanceof WhereClause) {
				WhereClause wc = (WhereClause) clause;

				indices.add(getIndexNameFrom(wc.getField().getPui()));
				types.add(getTypeNameFrom(wc.getField().getPui()));

				QueryBuilder predicate = createPredicateFromClause(wc);

				if (wc.getLogicalOperator().name().equalsIgnoreCase("MUST")) {
					qb.must(predicate);
				} else if (wc.getLogicalOperator().name()
						.equalsIgnoreCase("MUSTNOT")) {
					qb.mustNot(predicate);
				} else if (wc.getLogicalOperator().name()
						.equalsIgnoreCase("SHOULD")) {
					qb.should(predicate);
				}
			}

		}

		sb.setIndices(indices.toArray(new String[] {}));
		sb.setTypes(types.toArray(new String[] {}));

		sb.setQuery(qb);
		sb.setScroll(new TimeValue(60000));
		sb.setSearchType(SearchType.DEFAULT);
		SearchResponse response = sb.execute().actionGet();

		// GET FIELDS
		// GET HIGHLIGHTED FIELDS

		// TODO Auto-generated method stub

		return result;
	}

	

	private QueryBuilder createPredicateFromClause(WhereClause wc) {
		String predicateName = wc.getPredicateType().getName();

		if (predicateName.equals("MATCH")) {
			return matchQuery(getFieldNameFrom(wc.getStringValues()
					.get("FIELD")), wc.getStringValues().get("VALUE"));
		} else if (predicateName.equals("COMMONTERM")) {
			return commonTermsQuery(
					getFieldNameFrom(wc.getStringValues().get("FIELD")), wc
							.getStringValues().get("VALUE"));
		} else if (predicateName.equals("QUERYSTRING")) {
			return queryStringQuery(wc.getStringValues().get("QUERYSTRING"));
		} else if (predicateName.equals("SIMPLEQUERYSTRING")) {
			return simpleQueryStringQuery(wc.getStringValues().get(
					"QUERYSTRING"));
		} else if (predicateName.equals("TERMQUERY")) {
			return termQuery(
					getFieldNameFrom(wc.getStringValues().get("FIELD")), wc
							.getStringValues().get("VALUE"));
		} else if (predicateName.equals("TERMSQUERY")) {
			return termQuery(
					getFieldNameFrom(wc.getStringValues().get("FIELD")), wc
							.getStringValues().get("VALUE"));
		} else if (predicateName.equals("RANGEQUERY")) {

			boolean includeLower = wc.getStringValues().get("LOWER_INCLUSIVE")
					.equalsIgnoreCase("TRUE");
			boolean includeUpper = wc.getStringValues().get("UPPER_INCLUSIVE")
					.equalsIgnoreCase("TRUE");

			return rangeQuery(
					getFieldNameFrom(wc.getStringValues().get("FIELD")))
					.from(wc.getStringValues().get("FROM"))
					.to(wc.getStringValues().get("TO"))
					.includeLower(includeLower).includeUpper(includeUpper);
		}

		// GEO QUERIES

		return null;
	}
	
	private String getTypeNameFrom(String pui) {
		String[] pathComponents = pui.split("/");
		if ((this.index != null) && (pathComponents.length == 4)) {
			return pathComponents[3];
		} else if ((this.index == null) && (pathComponents.length == 5)) {
			return pathComponents[4];
		}

		return null;
	}

	private String getIndexNameFrom(String pui) {
		if (this.index != null) {
			return this.index;
		}
		String pathComponents[] = pui.split("/");
		return pathComponents[3];
	}

	private String getFieldNameFrom(String pui) {
		String[] pathComponents = pui.split("/");
		if ((this.index != null) && (pathComponents.length == 4)) {
			return pathComponents[4];
		} else if ((this.index == null) && (pathComponents.length == 5)) {
			return pathComponents[5];
		}

		return null;
	}

	@Override
	public List<Entity> find(Entity path,
			FindInformationInterface findInformation, SecureSession session)
			throws ResourceInterfaceException {
		return new ArrayList<Entity>();
	}

	@Override
	public Result getResults(SecureSession session, Result result)
			throws ResourceInterfaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getType() {
		return "elasticSearch";
	}

	@Override
	public ResourceState getState() {
		return resourceState;
	}

	@Override
	public ResultDataType getQueryDataType(Query query) {
		return ResultDataType.TABULAR;
	}

}
