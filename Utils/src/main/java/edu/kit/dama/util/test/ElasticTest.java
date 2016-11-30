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
package edu.kit.dama.util.test;

import edu.kit.dama.mdm.content.search.impl.BaseSearchTerm;
import edu.kit.dama.mdm.content.search.impl.FulltextElasticSearchProvider;
import edu.kit.dama.mdm.content.util.ElasticHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.fzk.grid.util.JWhich;

/**
 *
 * @author jejkal
 */
public class ElasticTest {

    public static void main(String[] args) throws Exception {
        FulltextElasticSearchProvider provider = new FulltextElasticSearchProvider("KITDM_Test", "localhost", "abc", "message");
        List<BaseSearchTerm> terms = provider.getSearchTerms();
        terms.get(0).setValue("*");
        System.out.println(provider.queryForResults(terms));

        if (true) {
            return;
        }

        List<TestThread> threads = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            TestThread t = new TestThread(i);
            t.start();
            threads.add(t);
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
        }

        int cnt = 0;
        while (true) {
            for (TestThread t : threads) {
                if (t.isRunning()) {
                    break;
                }
                return;
            }

            try {
                System.out.println("Wait for node");
                Thread.sleep(1000);
                cnt++;
            } catch (Exception e) {
            }
            if (cnt == 30) {
                break;
            }
        }
    }

}

class TestThread extends Thread {

    boolean running = true;
    int tid = 0;

    public TestThread(int ptid) {
        setDaemon(true);
        tid = ptid;
    }

    @Override
    public void run() {
        try {
            System.out.println(JWhich.which("org.apache.logging.log4j.Logger"));
            Client client = ElasticHelper.getTransportClient("localhost", 9300, "KITDM_Test");
            for (int i = 0; i < 10; i++) {
                System.out.println("Add element #" + i + " in thread " + tid);
                JSONObject o = new JSONObject();
                o.append("random.number", Math.random() * 1000);
                o.append("String", "Hello World!");
                o.append("Index", i);
                o.append("ThreadId", tid);
                int retry = 0;
                while (retry < 10) {
                    try {
                        IndexResponse response = client.prepareIndex("abc", "message", UUID.randomUUID().toString()).
                                setSource(o.toString())
                                .execute()
                                .actionGet();
                        System.out.println("Created: " + response.getId());
                        break;
                    } catch (NoNodeAvailableException ex) {
                        //retry???
                        System.out.println("RETRY!");
                        retry++;
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                        }
                    }
                }

            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        running = false;
    }

    public boolean isRunning() {
        return running;
    }
}
