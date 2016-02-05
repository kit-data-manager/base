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
package edu.kit.sharing.test;

import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.spi.container.TestContainerException;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.impl.Group;
import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.rest.sharing.client.impl.SharingRestClient;
import edu.kit.dama.rest.sharing.types.GrantWrapper;
import edu.kit.dama.rest.sharing.types.GroupIdWrapper;
import java.io.IOException;
import java.net.ServerSocket;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author dapp
 */
public class SharingServiceTest extends JerseyTest {

    private static SharingRestClient client;
    private final SimpleRESTContext ctx;

    public SharingServiceTest() throws TestContainerException {
        super("edu.kit.sharing.test");
        ctx = new SimpleRESTContext("secret", "secret");
        client = new SharingRestClient("http://localhost:9998/SharingTest", ctx);

        SharingTestService.factoryGrant();
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
    public void testGetGrantById() {
        GrantWrapper gw = client.getGrantById(200l, null, ctx);

        assertEquals(gw.getWrappedEntities().get(0).getId(), Long.valueOf(200l));
    }

    @Test
    public void testCreateGrant() {

        String testDom = "testDomain";
        String resId = "resourceId1";
        String testGroup = "testGroup";
        String uid = "testUser";
        Group kg;
        GroupIdWrapper kiw;

        GrantWrapper gw = client.createGrant(testDom, resId, uid, testGroup,
                Role.GUEST, ctx);

        assertEquals(1l, gw.getCount().longValue());
    }

    @Test
    public void testCreateAndGetGrant() {
        testCreateGrant();
        GrantWrapper gw = client.getGrantById(300l, null, ctx);

        assertEquals(Long.valueOf(300l), gw.getWrappedEntities().get(0).getId());
    }

    @Test
    public void test111() {
    }
}
