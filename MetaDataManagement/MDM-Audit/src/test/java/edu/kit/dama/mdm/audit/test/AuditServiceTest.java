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
package edu.kit.dama.mdm.audit.test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.JerseyTest;
import edu.kit.dama.rest.util.RestClientUtils;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author mf6319
 */
@Ignore
public class AuditServiceTest extends JerseyTest {

    static WebResource webResource;

    public AuditServiceTest() throws Exception {
        super("edu.kit.dama.mdm.audit.test");
    }

    @Override
    protected int getPort(int defaultPort) {
        ServerSocket server = null;
        int port = -1;
        try {
            server = new ServerSocket(defaultPort);
            port = server.getLocalPort();
        } catch (IOException e) {
            // ignore
        } finally {
            if (server != null) {
                try {
                    server.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        if ((port != -1) || (defaultPort == 0)) {
            return port;
        }
        return getPort(0);
    }

    @BeforeClass
    public static void initClient() {

        Client client = Client.create();

        //  client = Client.create();
        URI resourceUri = null;
        try {
            resourceUri = new URL("http://localhost:9998/audit").toURI();
        } catch (MalformedURLException | URISyntaxException ex) {
            ex.printStackTrace();
        }
        webResource = client.resource(resourceUri);
    }

    @Test
    public void auditTest() {
      //  MultivaluedMap queryParam = new MultivaluedMapImpl();
       // queryParam.put("groupId", "USERS");
        RestClientUtils.performGet((Class)null, webResource.path(RestClientUtils.encodeUrl(
                "/digitalObjects/{0}", 1l)), null);
    }
}
