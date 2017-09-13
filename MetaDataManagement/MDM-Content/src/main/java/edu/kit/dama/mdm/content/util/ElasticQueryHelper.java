/*
 * Copyright 2016 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.dama.mdm.content.util;

import edu.kit.dama.util.DataManagerSettings;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class ElasticQueryHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticQueryHelper.class);

    private static TransportClient client = null;
    private static boolean INITIALIZED = false;

    public static void initialize() {
        LOGGER.debug("Initializing ElasticQueryHelper.");
        String elasticHost = DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.ELASTIC_SEARCH_DEFAULT_HOST_ID, "localhost");
        String elasticCluster = DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.ELASTIC_SEARCH_DEFAULT_CLUSTER_ID, "NFFA@localhost");
        int port = DataManagerSettings.getSingleton().getIntProperty(DataManagerSettings.ELASTIC_SEARCH_DEFAULT_PORT_ID, 9300);
        LOGGER.debug("Connecting client to host {}.", elasticHost);
        client = ElasticHelper.getTransportClient(elasticHost, port, elasticCluster);
        LOGGER.debug("Try getting elastic status.");
        client.admin().cluster().prepareNodesInfo().all().execute(new ActionListener<NodesInfoResponse>() {
            @Override
            public void onResponse(NodesInfoResponse rspns) {
                LOGGER.debug("Successfully received status for cluster {}. Setting ElasticQueryHelper to INITIALIZED.", rspns.getClusterName());
            }

            @Override
            public void onFailure(Exception excptn) {

            }
        });
        INITIALIZED = true;
    }

    public static Map<String, Collection<String>> performQuery(String searchIndex, String type, JSONObject queryString, int index, int results) throws IOException {
        return performQuery(searchIndex, new String[]{type}, queryString, index, results);
    }

    /**
     * Query for resource ids for one or more types. The result map will contain
     * a mapping of type and a list of matching document ids. The content of the
     * documents is not part of the result.
     *
     * @param searchIndex The search index.
     * @param types An array of types that are queried.
     * @param query The structure containing the query.
     * @param index The first result index.
     * @param results The max. number of retrieved results.
     *
     * @return A map of type names and a list of matching document ids per type.
     *
     * @throws IOException If the elastic service cannot be accessed.
     */
    public static Map<String, Collection<String>> performQuery(String searchIndex, String[] types, JSONObject query, int index, int results) throws IOException {
        if (!INITIALIZED) {
            initialize();
        }

        LOGGER.debug("Preparing query of index {} for types {} using query string:\n{}", searchIndex, Arrays.asList(types), query);
        SearchResponse response = client.
                prepareSearch(searchIndex).
                setTypes(types).
                setQuery(QueryBuilders.wrapperQuery(query.toString())).
                setFetchSource(false).
                setFrom(index).
                setSize(results).
                execute().
                actionGet();
        LOGGER.debug("Query has returned. Obtaining ids.");
        Map<String, Collection<String>> resultMap = new HashMap<>();

        for (SearchHit hit : response.getHits()) {
            Collection<String> idsForType = resultMap.get(hit.getType());
            if (idsForType == null) {
                idsForType = new HashSet<>();
                resultMap.put(hit.getType(), idsForType);
            }
            idsForType.add(hit.getId());
        }
        for (String type : types) {
            if (!resultMap.containsKey(type)) {
                resultMap.put(type, new HashSet<>());
            }
        }

        LOGGER.debug("Obtained {} id(s).", resultMap.size());
        return resultMap;
    }

    /**
     * Query for resource ids for one type. The result map will contain a
     * mapping of type and a list of matching document ids. The content of the
     * documents is not part of the result.
     *
     * @param type The types that is queried.
     * @param query The structure containing the query.
     * @param index The first result index.
     * @param results The max. number of retrieved results.
     *
     * @return A map of type names and a list of matching document ids per type.
     *
     * @throws IOException If the elastic service cannot be accessed.
     */
    public static Map<String, Collection<String>> performQuery(String type, JSONObject query, int index, int results) throws IOException {
        return performQuery(new String[]{type}, query, index, results);
    }

    /**
     * Query for resource ids for one or more types. The result map will contain
     * a mapping of type and a list of matching document ids. The content of the
     * documents is not part of the result.
     *
     * @param types An array of types that are queried.
     * @param query The structure containing the query.
     * @param index The first result index.
     * @param results The max. number of retrieved results.
     *
     * @return A map of type names and a list of matching document ids per type.
     *
     * @throws IOException If the elastic service cannot be accessed.
     */
    public static Map<String, Collection<String>> performQuery(String[] types, JSONObject query, int index, int results) throws IOException {
        String elasticIndex = DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.ELASTIC_SEARCH_DEFAULT_INDEX_ID, "idrp");
        return performQuery(elasticIndex, types, query, index, results);
    }

    /**
     * Query for results for one type. The result contains all search hits
     * containing the total number of results as well as the document ids and
     * the document sources.
     *
     * @param type The type that is queried.
     * @param query The structure containing the query.
     * @param index The first result index.
     * @param results The max. number of retrieved results.
     *
     * @return A SearchHits wrapper containing all result documents and the
     * total number of results.
     */
    public static SearchHits search(String type, JSONObject query, int index, int results) {
        return search(type, query, null, null, index, results);
    }

    /**
     * Query for results for one type. The result contains all search hits
     * containing the total number of results as well as the document ids and
     * the document sources.
     *
     * @param types The types that are queried.
     * @param query The structure containing the query.
     * @param aggregation A single aggregation builder.
     *
     *
     * @return A SearchHits wrapper containing all result documents and the
     * total number of results.
     */
    public static Aggregations aggregate(String[] types, JSONObject query, AggregationBuilder aggregation) {
        return aggregate(types, query, Arrays.asList(aggregation));
    }

    /**
     * Query for results for one type. The result contains all search hits
     * containing the total number of results as well as the document ids and
     * the document sources.
     *
     * @param types The types that are queried.
     * @param query The structure containing the query.
     * @param aggregations A list of aggregations builder.
     *
     *
     * @return A SearchHits wrapper containing all result documents and the
     * total number of results.
     */
    public static Aggregations aggregate(String[] types, JSONObject query, List<AggregationBuilder> aggregations) {
        if (!INITIALIZED) {
            initialize();
        }
        String elasticIndex = DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.ELASTIC_SEARCH_DEFAULT_INDEX_ID, "idrp");
        LOGGER.debug("Preparing query of index {} for types {} using query string:\n{}", elasticIndex, Arrays.asList(types), query);
        SearchRequestBuilder queryBuilder = client.
                prepareSearch(elasticIndex).
                setTypes(types).
                setQuery(QueryBuilders.wrapperQuery(query.toString()));

        for (AggregationBuilder aggregation : aggregations) {
            queryBuilder = queryBuilder.addAggregation(aggregation);
        }

        queryBuilder = queryBuilder.setFetchSource(true).
                setSize(0);

        SearchResponse rsponse = queryBuilder.execute().actionGet();
        System.out.println(rsponse.getHits().getTotalHits());
        return rsponse.getAggregations();
    }

    /**
     * Query for sorted results for one type. The result contains all search
     * hits containing the total number of results as well as the document ids
     * and the document sources.
     *
     * @param type The type that is queried.
     * @param query The structure containing the query.
     * @param sortField The field to sort.
     * @param index The first result index.
     * @param results The max. number of retrieved results.
     *
     * @return A SearchHits wrapper containing all result documents and the
     * total number of results sorted in ascending order by the provided field.
     */
    public static SearchHits search(String type, JSONObject query, String sortField, int index, int results) {
        return search(type, query, sortField, SortOrder.ASC, index, results);
    }

    /**
     * Query for sorted results for one type. The result contains all search
     * hits containing the total number of results as well as the document ids
     * and the document sources.
     *
     * @param type The type that is queried.
     * @param query The structure containing the query.
     * @param sortField The field to sort.
     * @param order The sort order.
     * @param index The first result index.
     * @param results The max. number of retrieved results.
     *
     * @return A SearchHits wrapper containing all result documents and the
     * total number of results sorted by the provided field and order.
     */
    public static SearchHits search(String type, JSONObject query, String sortField, SortOrder order, int index, int results) {
        return search(new String[]{type}, query, sortField, order, index, results);
    }

    /**
     * Query for sorted results for multiple types. The result contains all
     * search hits containing the total number of results as well as the
     * document ids and the document sources.
     *
     * @param types A list of types that is queried.
     * @param query The structure containing the query.
     * @param sortField The field to sort.
     * @param order The sort order.
     * @param index The first result index.
     * @param results The max. number of retrieved results.
     *
     * @return A SearchHits wrapper containing all result documents and the
     * total number of results sorted by the provided field and order.
     */
    public static SearchHits search(String[] types, JSONObject query, String sortField, SortOrder order, int index, int results) {
        if (!INITIALIZED) {
            initialize();
        }
        String elasticIndex = DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.ELASTIC_SEARCH_DEFAULT_INDEX_ID, "idrp");
        LOGGER.debug("Preparing query of index {} for types {} using query string:\n{}", elasticIndex, Arrays.asList(types), query);
        SearchRequestBuilder queryBuilder = client.
                prepareSearch(elasticIndex).
                setTypes(types).
                setQuery(QueryBuilders.wrapperQuery(query.toString())).
                setFetchSource(true).
                setFrom(index).
                setSize(results);
        if (sortField != null) {
            queryBuilder.addSort(sortField, order == null ? SortOrder.ASC : order);
        }

        return queryBuilder.execute().actionGet().getHits();
    }

    public static void performDelete(String type, String[] ids) throws IOException {
        if (!INITIALIZED) {
            initialize();
        }
        String elasticIndex = DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.ELASTIC_SEARCH_DEFAULT_INDEX_ID, "idrp");

        for (String id : ids) {
            LOGGER.debug("Preparing deletion of object {} for type {}.", id, type);
            try {
                DeleteResponse response = client.prepareDelete(elasticIndex, type, id).execute().get();
            } catch (InterruptedException | ExecutionException ex) {
                LOGGER.trace("Delete operation has been interrupted.", ex);
            }
        }
    }

    /**
     * Destroy
     */
    public static void destroy() {
        LOGGER.debug("Destroying elastic transport client.");
        try {
            client.close();
        } finally {
            client = null;
            INITIALIZED = false;
        }
    }

    public static void main(String[] args) throws Exception {

        BoolQueryBuilder builder = QueryBuilders.boolQuery().must(QueryBuilders.queryStringQuery("f9d8950c-e0ef-4dd2-afeb-b24d58b34da8").field("measurementId.keyword"));
        JSONObject query = new JSONObject(builder.toString());

        TermsAggregationBuilder aggBuilder = AggregationBuilders.terms("types").field("_type");

        Aggregations response = ElasticQueryHelper.aggregate(new String[]{"experiment", "measurement", "dataasset"}, query, aggBuilder);
        // Global agg = response.get("aggs");
        StringTerms terms = response.get("types");
        for (Bucket b : terms.getBuckets()) {
            System.out.println(b.getKey() + " = " + b.getDocCount());
        }
        //
        //System.out.println(URLCreator.getName(new URL("http://matcloud.org/archive/2017.0001/v1/files/Barcodes_CH4_MOF_ZIF.tar.bz2")));
        //System.out.println((Proposal) GenericPersistence.initialize("IDRP").getEntityById(Proposal.class, "bc154604-ca44-458b-998a-e7771a304968", AuthorizationContext.factorySystemContext()));
        // System.out.println(GenericPersistence.initialize().getEntitiesByIds(Measurement.class, "measurementId", Arrays.asList("e61e7dd1-e065-43e2-9f07-e167bc4aeb91"), AuthorizationContext.factorySystemContext()));
//        BoolQueryBuilder builder = QueryBuilders.boolQuery().
//                must(QueryBuilders.queryStringQuery("*").field("measurementId"));
//
//        LOGGER.trace("Filtering list using query {}", builder.toString());
//
//        JSONObject query = new JSONObject(builder.toString());
//        SearchHits response = ElasticQueryHelper.search("measurement", query, "measurementType", SortOrder.ASC, 0, 10);
//
//        for (SearchHit hit : response.getHits()) {
//            System.out.println(hit.getId());
//        }
        /*
        PUT idrp/_mapping/proposal
{
  "properties": {
    "proposalTitle": { 
      "type":     "text",
      "fielddata": true
    }
  }
}
         */
//                
//        IAuthorizationContext ctx = AuthorizationContext.factorySystemContext();
//        JSONObject queryObject = null;
//
//        try {
//            queryObject = new JSONObject("{\"match_all\" : {}}");
//        } catch (JSONException ex) {
//        }
//
//        Map<String, Collection<String>> ids = ElasticQueryHelper.performQuery(Project.class.getSimpleName(), queryObject, 0, 100);
//        System.out.println(ids);
    }
}
