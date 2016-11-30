/*
 * Copyright 2014 Karlsruhe Institute of Technology.
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
package edu.kit.dama.mdm.content.search.impl;

import edu.kit.dama.mdm.content.search.AbstractSearchProvider;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.content.util.ElasticHelper;
import edu.kit.dama.util.DataManagerSettings;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mf6319
 */
public abstract class BaseElasticSearchProvider extends AbstractSearchProvider<DigitalObjectId> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseElasticSearchProvider.class);

    private final static int ELASTIC_PORT = 9300;
    private final static String DEFAULT_CLUSTER = "elasticsearch";
    private final static String DEFAULT_HOST = "localhost";

    private String cluster = DEFAULT_CLUSTER;
    private String hostname = DEFAULT_HOST;
    private int port = ELASTIC_PORT;
    private String index = null;
    private String objectType = null;
    private Client elasticClient;

    public BaseElasticSearchProvider() {
    }

    public BaseElasticSearchProvider(String pIndex, String pObjectType) {
        this(DEFAULT_CLUSTER, DEFAULT_HOST, pIndex, pObjectType);
    }

    public BaseElasticSearchProvider(String pHostName, String pIndex, String pObjectType) {
        this(DEFAULT_CLUSTER, pHostName, pIndex, pObjectType);
    }

    public BaseElasticSearchProvider(String pCluster, String pHostName, String pIndex, String pObjectType) {
        cluster = (pCluster != null) ? pCluster : DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.ELASTIC_SEARCH_DEFAULT_CLUSTER_ID, "KITDataManager");
        hostname = (pHostName != null) ? pHostName : DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.ELASTIC_SEARCH_DEFAULT_HOST_ID, "localhost");
        port = DataManagerSettings.getSingleton().getIntProperty(DataManagerSettings.ELASTIC_SEARCH_DEFAULT_PORT_ID, 9300);
        index = pIndex;
        objectType = pObjectType;
        LOGGER.debug("Setting up elasticsearch provider for cluster {} and {}@{}/{} (index@host/objectType)", cluster, index, hostname, objectType);
    }

    @Override
    public void initialize() {
    }

    @Override
    public List<DigitalObjectId> queryForResults(List<BaseSearchTerm> pTermValues) {
        BoolQueryBuilder builder = QueryBuilders.boolQuery();

        //BoolFilterBuilder filter = FilterBuilders.boolFilter();
        elasticClient = ElasticHelper.getTransportClient(hostname, port, cluster);

        SearchRequestBuilder requestBuilder = (index != null) ? elasticClient.prepareSearch(index) : elasticClient.prepareSearch();
        if (objectType != null) {
            requestBuilder.setTypes(objectType);
        }

        requestBuilder.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
//    requestBuilder.setHighlighterPreTags("<b>");
//    requestBuilder.setHighlighterPostTags("</b>");
//    requestBuilder.addHighlightedField("abstract");
//    requestBuilder.addHighlightedField("title");
//    requestBuilder.addHighlightedField("authors");
//    requestBuilder.addHighlightedField("keywords");
//    requestBuilder.addHighlightedField("url");

        //  boolean doFilter = false;
        for (BaseSearchTerm term : pTermValues) {
            if (term instanceof TextTerm) {
                if (term.getKey().equals(DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.ELASTIC_SEARCH_FULLTEXT_SEARCH_KEY_ID, "es.fulltext"))) {
                    //fulltext search...drop everything defined before and set only plain fulltext content
                    requestBuilder.setQuery(QueryBuilders.queryStringQuery(((TextTerm) term).getValue()));
                    builder = null;
                    //         doFilter = false;
                    break;
                } else {
                    //normal query term
                    builder = builder.must(QueryBuilders.queryStringQuery(term.getKey() + ":" + ((TextTerm) term).getValue()));
                }
            } else if (term instanceof DateRangeTerm) {
                long start = ((DateRangeTerm) term).getValue().getMinimumLong();
                long end = ((DateRangeTerm) term).getValue().getMaximumLong();
                builder = builder.must(QueryBuilders.rangeQuery("creation").from(new Date(start)).to(new Date(end)));
                /*    filter.must(FilterBuilders.rangeFilter("creation").from(new Date(start)).to(new Date(end)));*/
                //doFilter = true;
            }
        }

        //check if there were any text clauses
        if (builder != null) {
            if (!builder.hasClauses()) {
                //nothing in, select all
                builder.must(QueryBuilders.matchAllQuery());
            }
            requestBuilder.setQuery(builder);
        }
        //apply filter if necessary
        /* if (doFilter) {
            requestBuilder.setPostFilter(filter);
        }*/

        SearchResponse response = requestBuilder
                .setFrom(0)
                .setSize(getResultCacheSize())
                .setExplain(true)
                .execute()
                .actionGet();

        List<DigitalObjectId> results = new LinkedList<>();
        for (SearchHit hit : response.getHits()) {
            String id = hit.getId();
            int splitPosition = id.lastIndexOf("_");
            if (splitPosition > 0) {
                DigitalObjectId doi = new DigitalObjectId(id.substring(0, splitPosition));
                if (!results.contains(doi)) {
                    results.add(doi);
                }
            }// else {
            //invalid id
            //}
        }
        return results;
    }

    @Override
    public List<DigitalObjectId> filterResults(List<DigitalObjectId> pUnfilteredResults, IAuthorizationContext pContext) {
        List<DigitalObjectId> results = new LinkedList<>();
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(pContext);
        try {
            pUnfilteredResults.forEach((id) -> {
                DigitalObject o = new DigitalObject();
                o.setDigitalObjectId(id);
                try {
                    if (mdm.find(o, o) != null) {
                        results.add(id);

                    }// else {
                    //null!?
                    //}
                } catch (UnauthorizedAccessAttemptException ex) {
                    //not allowed
                }
            });
        } finally {
            mdm.close();
        }
        return results;
    }

//  public static void main(String[] args) throws Exception {
//
//    /*  MetadataIndexingTask task = new MetadataIndexingTask();
//     task.setId(1l);
//     for (int i = 1; i <= 4; i++) {
//     MetaDataSchema schema = new MetaDataSchema("post3", "http://datamanager.kit.edu/md/post3");
//     task.setSchemaReference(schema);
//     task.setDigitalObjectId(Integer.toString(i));
//     task.setMetadataDocumentUrl("file:///d:/metadata_" + Integer.toString(i) + ".xml");
//     String json = MetadataIndexingHelper.getSingleton().convertDocumentToJSON(new URL(task.getMetadataDocumentUrl()).toURI());
//     System.out.println(json);
//     MetadataIndexingHelper.getSingleton().indexJson(task, json, "postings");
//     }*/ Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", "KITDataManager").build();
//    Client client = new TransportClient(settings)
//            .addTransportAddress(new InetSocketTransportAddress("dama-virtualbox", 9300));
//
//    SearchResponse response = client.prepareSearch("kitdatamanager")
//            .setTypes("dc")
//            .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
//            .setQuery(QueryBuilders.queryString("dc.subject:SampleInvestigation"))
//            // Query
//            .setFrom(0).setSize(60).setExplain(true)
//            .execute()
//            .actionGet();
//
//    System.out.println(response.getHits().getTotalHits());
//    /* Document doc = JaxenUtil.getW3CDocument(new File("d:/metadata_bmd.xml"));
//     List<Node> nodes = JaxenUtil.getNodes(doc, "/bmd:bmd/csmd:csmd", new Namespace[]{Namespace.getNamespace("bmd", "http://ipelsdf1.lsdf.kit.edu/dama/basemetadata/2012-04"), Namespace.getNamespace("csmd", "http://ipelsdf1.lsdf.kit.edu/dama/metadata/2012-04")});
//     System.out.println(nodes);
//     Source source = new DOMSource(nodes.get(0).getFirstChild());
//     StringWriter stringWriter = new StringWriter();
//     Result result = new StreamResult(stringWriter);
//     // Java XML factories are not declared to be thread safe
//     TransformerFactory factory = TransformerFactory.newInstance();
//     Transformer transformer = factory.newTransformer();
//     transformer.transform(source, result);
//     System.out.println(stringWriter.getBuffer().toString());*/
//  }
}
