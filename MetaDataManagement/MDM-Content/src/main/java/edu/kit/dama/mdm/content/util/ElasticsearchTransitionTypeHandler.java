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

import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.mdm.base.DigitalObjectTransition;
import edu.kit.dama.mdm.content.ElasticsearchTransition;
import edu.kit.dama.mdm.core.exception.EntityNotFoundException;
import edu.kit.dama.mdm.core.exception.PersistFailedException;
import edu.kit.dama.mdm.tools.AbstractTransitionTypeHandler;
import edu.kit.dama.util.CryptUtil;
import edu.kit.dama.util.DataManagerSettings;
import org.apache.commons.configuration.Configuration;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public final class ElasticsearchTransitionTypeHandler extends AbstractTransitionTypeHandler<JSONObject> {
    
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ElasticsearchTransitionTypeHandler.class);
    
    private String elasticHost = "localhost";
    private int elasticPort = 9300;
    private String elasticCluster = "KITDataManager";
    private String elasticIndex = "customtransitions";
    
    public final static String TRANSITION_TYPE_ID = "transition";
    
    @Override
    public boolean configure(Configuration pConfig) throws ConfigurationException {
        elasticHost = DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.ELASTIC_SEARCH_DEFAULT_HOST_ID, "localhost");
        elasticPort = DataManagerSettings.getSingleton().getIntProperty(DataManagerSettings.ELASTIC_SEARCH_DEFAULT_PORT_ID, 9300);
        elasticCluster = DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.ELASTIC_SEARCH_DEFAULT_CLUSTER_ID, "KITDataManager");
        elasticIndex = pConfig.getString("index", "customtransitions").toLowerCase();
        return true;
    }
    
    @Override
    public DigitalObjectTransition<JSONObject> factoryTransitionEntity() {
        return new ElasticsearchTransition();
    }
    
    @Override
    public String getTransitionEntityId(JSONObject pTransitionEntity) throws PersistFailedException {
        LOGGER.debug("Trying to index entity '{}'", pTransitionEntity.toString());
        try {
            Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", elasticCluster).build();
            Client client = new TransportClient(settings)
                    .addTransportAddress(new InetSocketTransportAddress(elasticHost, elasticPort));
            String id = CryptUtil.stringToSHA1(pTransitionEntity.toString());
            IndexResponse response = client.prepareIndex(elasticIndex, TRANSITION_TYPE_ID, id).
                    setSource(pTransitionEntity.toString())
                    .execute()
                    .actionGet();
            return response.getId();
        } catch (Throwable t) {
            throw new PersistFailedException("Failed to persist transition entity in elasticsearch index.", t);
        }
    }
    
    @Override
    public JSONObject handleTransitionEntityData(String pTransitionEntityData) {
        try {
            LOGGER.debug("Trying to handle transition entity data '{}' as JSONArray", pTransitionEntityData);
            return new JSONObject(pTransitionEntityData);
        } catch (JSONException ex) {
            throw new IllegalArgumentException("Invalid transition entity data '" + pTransitionEntityData + "' for elasticsearch handler", ex);
        }
    }
    
    @Override
    public JSONObject loadTransitionEntity(String pTransitionEntityId) throws EntityNotFoundException {
        Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", elasticCluster).build();
        Client client = new TransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(elasticHost, elasticPort));
        SearchRequestBuilder requestBuilder = client.prepareSearch(elasticIndex);
        requestBuilder.setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
        requestBuilder.setQuery(QueryBuilders.idsQuery(TRANSITION_TYPE_ID).addIds(pTransitionEntityId));
        SearchResponse response = requestBuilder
                .setFrom(0)
                .setSize(1)
                .setExplain(true)
                .execute()
                .actionGet();
        
        if (response.getHits().getTotalHits() == 1) {
            return new JSONObject(response.getHits().getAt(0).getSourceAsString());
        } else {
            throw new EntityNotFoundException(" Query for transitionEntityId '" + pTransitionEntityId + "' returned " + response.getHits().getTotalHits() + " result(s), but expecting exactly 1. Returning null.");
        }
    }
    
}
