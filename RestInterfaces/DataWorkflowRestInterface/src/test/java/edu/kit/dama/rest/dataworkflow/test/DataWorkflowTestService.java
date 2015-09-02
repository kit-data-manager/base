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

import com.sun.jersey.api.core.HttpContext;
import edu.kit.dama.rest.base.types.CheckServiceResponse;
import edu.kit.dama.rest.base.types.ServiceStatus;
import edu.kit.dama.rest.dataworkflow.services.interfaces.IDataWorkflowRestService;
import edu.kit.dama.rest.dataworkflow.types.ExecutionEnvironmentConfigurationWrapper;
import edu.kit.dama.rest.dataworkflow.types.DataWorkflowTaskConfigurationWrapper;
import edu.kit.dama.rest.dataworkflow.types.DataWorkflowTaskWrapper;
import edu.kit.dama.rest.util.RestUtils;
import edu.kit.dama.util.Constants;
import edu.kit.dama.util.PropertiesUtil;
import edu.kit.dama.mdm.dataworkflow.ExecutionEnvironmentConfiguration;
import edu.kit.dama.mdm.dataworkflow.DataWorkflowTask;
import edu.kit.dama.mdm.dataworkflow.DataWorkflowTaskConfiguration;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

/**
 *
 * @author dapp
 */
@Path("/DataWorkflowTest")
public class DataWorkflowTestService implements IDataWorkflowRestService {

  private DataWorkflowTaskConfiguration config;
  private ExecutionEnvironmentConfiguration environment;
  private List<DataWorkflowTask> tasks;

  public DataWorkflowTestService() {
    config = new DataWorkflowTaskConfiguration();
    config.setId(1l);
    config.setLabel("Config");
    config.setVersion(1);
    config.setCreationDate(new Date());
    config.setKeywords("test");
    config.setApplicationPackageUrl("<none>");
    config.setApplicationArguments("--help");
    config.setDescription("Test configuration");
    environment = new ExecutionEnvironmentConfiguration();
    environment.setId(1l);
    environment.setName("Environment");
    environment.setAccessPointLocalBasePath("/tmp");
    environment.setStagingAccessPointId("12345");
    environment.setMaxParallelTasks(5);
    environment.setHandlerImplementationClass("edu.kit.dama.test.TestHandler");

    tasks = new ArrayList<>();
    for (int i = 1; i <= 10; i++) {
      DataWorkflowTask t = DataWorkflowTask.factoryNewDataWorkflowTask();
      t.setId((long) i);
      t.setJobId(Integer.toString(i));
      t.setConfiguration(config);
      t.setExecutorGroupId("USERS");
      t.setExecutorId("admin");
      Properties p = new Properties();
      p.put("1", "default");
      try {
        t.setObjectViewMapAsObject(p);
      } catch (IOException ex) {
        throw new IllegalArgumentException("Failed to build up sample data strucure.", ex);
      }
      t.setExecutionEnvironment(environment);
      tasks.add(t);
    }
  }

  private DataWorkflowTask getTaskById(final Long id) {
    DataWorkflowTask result = (DataWorkflowTask) CollectionUtils.find(tasks, new Predicate() {

      @Override
      public boolean evaluate(Object o) {
        return Objects.equals(((DataWorkflowTask) o).getId(), id);
      }
    });

    if (result == null) {
      throw new WebApplicationException(404);
    }
    return result;
  }

  private DataWorkflowTaskConfiguration getTaskConfigById(Long id) {
    if (!Objects.equals(config.getId(), id)) {
      throw new WebApplicationException(404);
    }
    return config;
  }

  private ExecutionEnvironmentConfiguration getEnvById(Long id) {
    if (!Objects.equals(environment.getId(), id)) {
      throw new WebApplicationException(404);
    }
    return environment;
  }

  @Override
  public StreamingOutput getTaskById(String groupId, final Long id, HttpContext hc) {
    return RestUtils.createObjectGraphStream(DataWorkflowTaskWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new DataWorkflowTaskWrapper(getTaskById(id)));
  }

  @Override
  public StreamingOutput getAllTasks(String groupId, Integer first, Integer results, HttpContext hc) {
    return RestUtils.createObjectGraphStream(DataWorkflowTaskWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new DataWorkflowTaskWrapper(tasks));
  }

  @Override
  public StreamingOutput createTask(String groupId, Long investigationId, Long configurationId, Long environmentId, Long predecessorId, String inputObjectMap, String executionSettings, String applicationArguments, HttpContext hc) {
    DataWorkflowTask t = new DataWorkflowTask();
    t.setConfiguration(getTaskConfigById(configurationId));
    t.setExecutionEnvironment(getEnvById(environmentId));
    if (predecessorId != null) {
      t.setPredecessor(getTaskById(predecessorId));
    }
    try {
      Properties inputMap = PropertiesUtil.propertiesFromString(inputObjectMap);
      t.setObjectTransferMapAsObject(inputMap);
      Properties execSettings = PropertiesUtil.propertiesFromString(executionSettings);
      t.setExecutionSettingsAsObject(execSettings);
    } catch (IOException ex) {
      throw new WebApplicationException(500);
    }

    t.setApplicationArguments(applicationArguments);
    t.setStatus(DataWorkflowTask.TASK_STATUS.SCHEDULED);
    t.setInvestigationId(investigationId);
    return RestUtils.createObjectGraphStream(DataWorkflowTaskWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new DataWorkflowTaskWrapper(t));
  }

  @Override
  public StreamingOutput getTaskConfigurationById(String groupId, Long id, HttpContext hc) {
    return RestUtils.createObjectGraphStream(DataWorkflowTaskConfigurationWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new DataWorkflowTaskConfigurationWrapper(getTaskConfigById(id)));
  }

  @Override
  public StreamingOutput getAllTaskConfigurations(String groupId, Integer first, Integer results, HttpContext hc) {
    return RestUtils.createObjectGraphStream(DataWorkflowTaskConfigurationWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new DataWorkflowTaskConfigurationWrapper(config));
  }

  @Override
  public StreamingOutput getExecutionEnvironmentConfigurationById(String groupId, Long id, HttpContext hc) {
    return RestUtils.createObjectGraphStream(ExecutionEnvironmentConfigurationWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new ExecutionEnvironmentConfigurationWrapper(getEnvById(id)));
  }

  @Override
  public StreamingOutput getAllExecutionEnvironmentConfigurations(String groupId, Integer first, Integer results, HttpContext hc) {
    return RestUtils.createObjectGraphStream(ExecutionEnvironmentConfigurationWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new ExecutionEnvironmentConfigurationWrapper(environment));
  }

  @Override
  public StreamingOutput getTaskCount(String groupId, HttpContext hc) {
    return RestUtils.createObjectGraphStream(DataWorkflowTaskWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new DataWorkflowTaskWrapper(tasks.size()));
  }

  @Override
  public StreamingOutput getTaskConfigurationCount(String groupId, HttpContext hc) {
    return RestUtils.createObjectGraphStream(DataWorkflowTaskConfigurationWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new DataWorkflowTaskConfigurationWrapper(1));
  }

  @Override
  public StreamingOutput getExecutionEnvironmentConfigurationCount(String groupId, HttpContext hc) {
    return RestUtils.createObjectGraphStream(ExecutionEnvironmentConfigurationWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new ExecutionEnvironmentConfigurationWrapper(1));
  }

  @Override
  public Response checkService() {
    return Response.status(200).entity(new CheckServiceResponse("DataWorkflowTest", ServiceStatus.OK)).build();
  }
}
