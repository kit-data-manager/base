/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology (support@kitdatamanager.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.kit.dama.rest.dataorganization.test;

import com.sun.jersey.test.framework.JerseyTest;
import edu.kit.dama.rest.dataorganization.types.DataOrganizationNodeWrapper;
import edu.kit.dama.rest.dataorganization.client.impl.DataOrganizationRestClient;
import edu.kit.dama.rest.dataorganization.types.DataOrganizationViewWrapper;
import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.staging.entities.DataOrganizationNodeImpl;
import edu.kit.dama.util.Constants;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author dapp
 */
public class DataOrganizationServiceTest extends JerseyTest {

  private static DataOrganizationRestClient client;
  private final SimpleRESTContext ctx;

  public DataOrganizationServiceTest() throws Exception {
    super("edu.kit.dama.rest.dataorganization.test");
    //super("edu.kit.dama.rest");
    ctx = new SimpleRESTContext("secret", "secret");
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
    client = new DataOrganizationRestClient(
            "http://localhost:9998/DataOrganizationTest/organization",
            new SimpleRESTContext("secret", "secret"));
  }

  @Test
  public void testRootNodeCount() {
    DataOrganizationNodeWrapper rid = client.getRootNodeCount(null, 1l,
            Constants.DEFAULT_VIEW, ctx);
    assertEquals((long) rid.getCount(), 1l);
  }

  @Test
  public void testRootNode() {
    DataOrganizationNodeWrapper nw = client.getRootNode(null, 1l, 0,
            Integer.MAX_VALUE, Constants.DEFAULT_VIEW, ctx);

    assertEquals(nw.getEntities().size(), 1);
    DataOrganizationNodeImpl ni = nw.getEntities().get(0);
    assertEquals(ni.getParent(), null);
    assertEquals(ni.getNodeId(), 100l);
  }

  @Test
  public void testRootChildCount() {
    DataOrganizationNodeWrapper nw = client.getRootNode(null, 1l, 0,
            Integer.MAX_VALUE, Constants.DEFAULT_VIEW, ctx);

    List<DataOrganizationNodeImpl> l = nw.getWrappedEntities();
    assertEquals(l.size(), 1);

    DataOrganizationNodeWrapper nw2 = client.getChildCount(null,
            1l, 100l, "default");

    assertEquals((long) nw2.getCount(), 2l);
  }

  @Test
  public void testNodeName() {

    DataOrganizationNodeWrapper nw1 = client.
            getNodeInformation(null, 1l, 1l,
                    null);

    assertEquals(nw1.getWrappedEntities().size(), 1);
    assertEquals(nw1.getWrappedEntities().get(0).getName(), "child 1");

    DataOrganizationNodeWrapper nw2 = client.
            getNodeInformation(null, 1l, 2l,
                    null);

    assertEquals(nw2.getWrappedEntities().size(), 1);
    assertEquals(nw2.getWrappedEntities().get(0).getName(), "child 2");
  }

  @Test
  public void testViewCount() {

    DataOrganizationViewWrapper vw = client.getViewCount(null, 1l);
    assertEquals((long) vw.getCount(), 3l);
  }

  @Test
  public void testViews() {

    DataOrganizationViewWrapper vw = client.getViews(null, 1l);
    assertEquals(vw.getWrappedEntities().contains("View 1"), true);
    assertEquals(vw.getWrappedEntities().contains("View 2"), true);
    assertEquals(vw.getWrappedEntities().contains("Default"), true);
  }

  @Test
  public void testChildrenCount() {
    DataOrganizationNodeWrapper nw = client.getChildCount(null, 1l, 100l,
            null);
    assertEquals((long) nw.getCount(), 2l);

    nw = client.getChildCount(null, 2l, 200l,
            null);
    assertEquals((long) nw.getCount(), 3l);

  }

  @Test
  public void testChildren() {
    DataOrganizationNodeWrapper nw = client.getChildren(null, 1l, 100l, 0,
            Integer.MAX_VALUE, null);

    DataOrganizationNodeWrapper nw2 = client.getChildCount(null, 1l, 100l,
            null);
    assertEquals((long) nw.getCount(), 2l);
    assertEquals((long) nw.getCount(), (long) nw2.getCount());

    List<String> nameList = new LinkedList<>();

    DataOrganizationNodeImpl nd = new DataOrganizationNodeImpl();
    for (DataOrganizationNodeImpl ni : nw.getWrappedEntities()) {
      nameList.add(ni.getName());
    }

    assertEquals(nameList.contains("child 1"), true);
    assertEquals(nameList.contains("child 2"), true);
  }

  @Test
  public void testNullArguments() {
    DataOrganizationNodeWrapper nw = client.getRootNode(null, 1l, null, null,
            null);
    List<DataOrganizationNodeImpl> l = nw.getWrappedEntities();
    assertEquals(l.size(), 1);

    nw = client.getChildren(null, 1l, 100l, null, null, null);
    assertEquals((long) nw.getCount(), 2l);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullId1() {
    DataOrganizationNodeWrapper nw = client.getRootNode(null, null, null, null,
            null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullId2() {
    DataOrganizationNodeWrapper nw = client.getChildren(null, null, null, null,
            null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullId3() {
    DataOrganizationNodeWrapper nw = client.getChildren(null, 1l, null, null,
            null, null);
  }
}
