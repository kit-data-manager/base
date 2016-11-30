/*
 * Copyright 2015 Karlsruhe Institute of Technology.
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
package edu.kit.dama.audit.test;

import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.spi.container.TestContainerException;
import edu.kit.dama.mdm.audit.types.AuditEvent;
import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.rest.audit.client.impl.AuditRestClient;
import edu.kit.dama.rest.audit.types.AuditEventWrapper;
import edu.kit.dama.util.Constants;
import java.io.IOException;
import java.net.ServerSocket;
import javax.ws.rs.core.Response;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author dapp
 */
public class AuditServiceTest extends JerseyTest {

    private AuditRestClient client;
    private final SimpleRESTContext ctx;

    public AuditServiceTest() throws TestContainerException {
        super("edu.kit.dama.audit.test");
        ctx = new SimpleRESTContext("secret", "secret");
        client = new AuditRestClient("http://localhost:9998/AuditTest", ctx);
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

    @Test
    public void testGetEvents() {
        AuditEventWrapper wrapper = client.getEvents("test-1234", 0, Integer.MAX_VALUE, Constants.USERS_GROUP_ID);
        //one result
        assertEquals(1, wrapper.getCount().intValue());
        assertEquals(AuditEvent.TYPE.CREATION, wrapper.getEntities().get(0).getEventType());
        assertEquals(AuditEvent.TRIGGER.INTERNAL, wrapper.getEntities().get(0).getEventTrigger());
    }

    @Test
    public void testGetExternalEvents() {
        AuditEventWrapper wrapper = client.getEvents("test-1234", null, AuditEvent.TRIGGER.EXTERNAL, null, null, "audit.digitalObject", 0, Integer.MAX_VALUE, Constants.USERS_GROUP_ID);
        //one result
        assertEquals(1, wrapper.getCount().intValue());
        assertEquals(AuditEvent.TRIGGER.EXTERNAL, wrapper.getEntities().get(0).getEventTrigger());
        assertEquals("audit.digitalObject", wrapper.getEntities().get(0).getCategory());

    }

    @Test
    public void testGetEvent() {
        AuditEventWrapper wrapper = client.getEventById(1l, Constants.USERS_GROUP_ID);
        //one result
        assertEquals(1, wrapper.getCount().intValue());
        assertEquals(1l, wrapper.getEntities().get(0).getId().longValue());
    }

    @Test
    public void testAddEvent() {
        boolean result = client.createEvent("test-12345", AuditEvent.TYPE.CREATION, "audit.digitalObject", null, Constants.USERS_GROUP_ID);
        //one result
        Assert.assertTrue(result);
    }
}
