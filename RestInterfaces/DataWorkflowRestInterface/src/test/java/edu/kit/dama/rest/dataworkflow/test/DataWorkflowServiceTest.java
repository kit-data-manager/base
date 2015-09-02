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
package edu.kit.dama.rest.dataworkflow.test;

import com.sun.jersey.test.framework.JerseyTest;
import edu.kit.dama.mdm.base.Investigation;
import edu.kit.dama.rest.dataworkflow.types.DataWorkflowTaskWrapper;
import edu.kit.dama.rest.dataworkflow.client.impl.DataWorkflowRestClient;
import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.rest.dataworkflow.types.ExecutionEnvironmentConfigurationWrapper;
import edu.kit.dama.rest.dataworkflow.types.DataWorkflowTaskConfigurationWrapper;
import edu.kit.dama.mdm.dataworkflow.ExecutionEnvironmentConfiguration;
import edu.kit.dama.mdm.dataworkflow.DataWorkflowTask;
import edu.kit.dama.mdm.dataworkflow.DataWorkflowTaskConfiguration;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Properties;
import javax.xml.ws.WebServiceException;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author dapp
 */
public class DataWorkflowServiceTest extends JerseyTest {

  private static DataWorkflowRestClient client;
  private final SimpleRESTContext ctx;

  public DataWorkflowServiceTest() throws Exception {
    super("edu.kit.dama.rest.dataworkflow.test");
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
    client = new DataWorkflowRestClient(
            "http://localhost:9998/DataWorkflowTest/",
            new SimpleRESTContext("secret", "secret"));
  }

  @Test
  public void testGetConfigById() {
    DataWorkflowTaskConfigurationWrapper rid = client.getTaskConfigurationById("USERS", 1l, ctx);
    assertEquals((long) rid.getCount(), 1l);
    assertEquals((long) rid.getEntities().get(0).getId(), 1l);
  }

  @Test
  public void testGetAllConfigs() {
    DataWorkflowTaskConfigurationWrapper rid = client.getAllConfigurations("USERS", 0, Integer.MAX_VALUE, ctx);
    assertEquals((long) rid.getCount(), 1l);
    assertEquals((long) rid.getEntities().size(), 1l);
  }

  @Test
  public void testGetEnvById() {
    ExecutionEnvironmentConfigurationWrapper rid = client.getExecutionEnvironmentConfigurationById("USERS", 1l, ctx);
    assertEquals((long) rid.getCount(), 1l);
    assertEquals((long) rid.getEntities().get(0).getId(), 1l);
  }

  @Test
  public void testGetAllEnvs() {
    ExecutionEnvironmentConfigurationWrapper rid = client.getAllExecutionEnvironmentConfigurations("USERS", 0, Integer.MAX_VALUE, ctx);
    assertEquals((long) rid.getCount(), 1l);
    assertEquals((long) rid.getEntities().size(), 1l);
  }

  @Test
  public void testGetTaskId() {
    DataWorkflowTaskWrapper rid = client.getTaskById("USERS", 1l, ctx);
    assertEquals((long) rid.getCount(), 1l);
    assertEquals(rid.getEntities().get(0).getJobId(), "1");
    try {
      assertEquals(rid.getEntities().get(0).getObjectViewMapAsObject().size(), 1);
    } catch (IOException ex) {
      Assert.fail("Failed to obtain object-transfer map.");
    }
  }

  @Test
  public void testGetAllTasks() {
    DataWorkflowTaskWrapper rid = client.getAllTasks("USERS", 0, Integer.MAX_VALUE, ctx);
    assertEquals((long) rid.getCount(), 10l);
    assertEquals((long) rid.getEntities().size(), 10l);
  }

  @Test
  public void testCreateTask() {
    Investigation inv = new Investigation();
    inv.setInvestigationId(1l);
    DataWorkflowTaskConfiguration config = new DataWorkflowTaskConfiguration();
    config.setId(1l);
    ExecutionEnvironmentConfiguration env = new ExecutionEnvironmentConfiguration();
    env.setId(1l);
    Properties p = new Properties();
    p.put("1", "default");
    Properties conf = new Properties();
    conf.put("key", "value");
    DataWorkflowTask predecessor = client.getTaskById("USERS", 1l).getEntities().get(0);
    DataWorkflowTaskWrapper rid = client.createTask("USERS", inv, config, env, predecessor, p, conf, "", ctx);
    assertEquals((long) rid.getCount(), 1l);
    assertEquals((long) rid.getEntities().size(), 1l);
    assertEquals((long) rid.getEntities().get(0).getConfiguration().getId(), 1l);
    assertEquals((long) rid.getEntities().get(0).getExecutionEnvironment().getId(), 1l);
  }

  @Test(expected = WebServiceException.class)
  public void testGetInvalidTask() {
    client.getTaskById("USERS", 666l, ctx);
  }

  @Test(expected = WebServiceException.class)
  public void testGetInvalidConfig() {
    client.getTaskConfigurationById("USERS", 666l, ctx);
  }

  @Test(expected = WebServiceException.class)
  public void testGetInvalidEnv() {
    client.getExecutionEnvironmentConfigurationById("USERS", 666l, ctx);
  }
}
