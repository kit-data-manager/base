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

import java.net.InetSocketAddress;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

/**
 *
 * @author jejkal
 */
public class ElasticHelper {

    public static TransportClient getTransportClient(String hostname, String cluster) {
        return getTransportClient(hostname, 9300, cluster);
    }

    public static TransportClient getTransportClient(String hostname, int port, String cluster) {
        Settings settings = Settings.builder().put("cluster.name", cluster).build();

        TransportClient client = new PreBuiltTransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress(hostname, port)));

        return client;
    }

}
