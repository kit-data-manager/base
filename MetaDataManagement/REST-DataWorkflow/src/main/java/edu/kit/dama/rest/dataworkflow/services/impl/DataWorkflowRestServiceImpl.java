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
package edu.kit.dama.rest.dataworkflow.services.impl;

import com.sun.jersey.api.core.HttpContext;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.base.Investigation;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.rest.base.types.CheckServiceResponse;
import edu.kit.dama.rest.base.types.ServiceStatus;
import edu.kit.dama.rest.dataworkflow.services.interfaces.IDataWorkflowRestService;
import edu.kit.dama.rest.dataworkflow.types.ExecutionEnvironmentConfigurationWrapper;
import edu.kit.dama.rest.dataworkflow.types.DataWorkflowTaskConfigurationWrapper;
import edu.kit.dama.rest.dataworkflow.types.DataWorkflowTaskWrapper;
import edu.kit.dama.rest.util.RestUtils;
import static edu.kit.dama.rest.util.RestUtils.createObjectGraphStream;
import edu.kit.dama.util.Constants;
import edu.kit.dama.util.PropertiesUtil;
import edu.kit.dama.mdm.dataworkflow.ExecutionEnvironmentConfiguration;
import edu.kit.dama.mdm.dataworkflow.DataWorkflowTask;
import edu.kit.dama.mdm.dataworkflow.DataWorkflowTaskConfiguration;
import edu.kit.dama.dataworkflow.impl.DataWorkflowPersistenceImpl;
import edu.kit.dama.mdm.dataworkflow.tools.DataWorkflowTaskSecureQueryHelper;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mf6319
 */
@Path("/")
public final class DataWorkflowRestServiceImpl implements IDataWorkflowRestService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataWorkflowRestServiceImpl.class);

  @Override
  public StreamingOutput getTaskById(String groupId, Long id, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
    mdm.setAuthorizationContext(ctx);
    try {
      LOGGER.debug("Getting DataWorkflow task by id {}.", id);
      DataWorkflowTask result = new DataWorkflowTaskSecureQueryHelper().getDataWorkflowTaskById(id, mdm, ctx);
      if (result == null) {
        LOGGER.error("No DataWorkflowTask found for id " + id + ".");
        throw new WebApplicationException(404);
      }
      return createObjectGraphStream(DataWorkflowTaskWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new DataWorkflowTaskWrapper(result));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Context " + ctx + " is not authorized to get a DataWorkflowTask for id " + id, ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput getAllTasks(String groupId, Integer first, Integer results, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
    mdm.setAuthorizationContext(ctx);
    try {
      LOGGER.debug("Getting accessible task list.");
      List<DataWorkflowTask> result = new DataWorkflowTaskSecureQueryHelper().getAllTasks(null, mdm, first, results, ctx);
      return createObjectGraphStream(DataWorkflowTaskWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new DataWorkflowTaskWrapper(result));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Context " + ctx + " is not authorized to get a list of DataWorkflowTasks.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput getTaskCount(String groupId, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
    mdm.setAuthorizationContext(ctx);
    try {
      LOGGER.debug("Getting accessible task count.");
      int result = new DataWorkflowTaskSecureQueryHelper().getReadableResourceCount(mdm, ctx);
      return createObjectGraphStream(DataWorkflowTaskWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new DataWorkflowTaskWrapper(result));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Context " + ctx + " is not authorized to get DataWorkflowTask count.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput createTask(String pGroupId, Long pInvestigationId, Long pConfigurationId, Long pEnvironmentId, Long pPredecessorId, String pInputObjectMap, String pExecutionSettings, String pApplicationArguments, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(pGroupId));
    IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
    mdm.setAuthorizationContext(ctx);

    DataWorkflowTask predecessor = null;

    try {
      LOGGER.debug("Obtaining Investigation for id {}", pInvestigationId);
      Investigation investigation;
      if (pInvestigationId == null) {
        LOGGER.error("Provided investigation id argument is null. Unable to continue.");
        throw new WebApplicationException(404);
      } else {
        investigation = mdm.find(Investigation.class, pInvestigationId);
        if (investigation == null) {
          LOGGER.error("No Investigation found for id " + pInvestigationId + ". Unable to continue.");
          throw new WebApplicationException(404);
        }
      }

      LOGGER.debug("Obtaining DataWorkflowTaskConfiguration for id {}", pConfigurationId);
      DataWorkflowTaskConfiguration configuration;
      if (pConfigurationId == null) {
        LOGGER.error("Provided configuration id argument is null. Unable to continue.");
        throw new WebApplicationException(404);
      } else {
        configuration = mdm.find(DataWorkflowTaskConfiguration.class, pConfigurationId);
        if (configuration == null) {
          LOGGER.error("No DataWorkflowTaskConfiguration found for id " + pConfigurationId + ". Unable to continue.");
          throw new WebApplicationException(404);
        }
      }

      LOGGER.debug("Obtaining ExecutionEnvironmentConfiguration for id {}", pEnvironmentId);
      ExecutionEnvironmentConfiguration environment;
      if (pEnvironmentId == null) {
        LOGGER.error("Provided ExecutionEnvironmentConfiguration id argument is null. Unable to continue.");
        throw new WebApplicationException(404);
      } else {
        environment = mdm.find(ExecutionEnvironmentConfiguration.class, pEnvironmentId);
        if (environment == null) {
          LOGGER.error("No ExecutionEnvironmentConfiguration found for id " + pEnvironmentId + ". Unable to continue.");
          throw new WebApplicationException(404);
        }
      }
      LOGGER.debug("Checking if environment with id {} is capable for executing task configuration with id {}", pEnvironmentId, pConfigurationId);
      if (!environment.canExecute(configuration)) {
        LOGGER.error("Execution ExecutionEnvironment with id " + pEnvironmentId + " is not capable for executing DataWorkflowTask for configuration with id " + pConfigurationId + ". EvironmentProperies do not fit task requirements.");
        throw new WebApplicationException(400);
      }

      if (pPredecessorId != null) {
        LOGGER.debug("Checking predecessor task for id {}", pPredecessorId);
        predecessor = new DataWorkflowTaskSecureQueryHelper().getDataWorkflowTaskById(pPredecessorId, mdm, ctx);
      } else {
        LOGGER.debug("No predecessor task provided");
      }

      DataWorkflowTask task = DataWorkflowTask.factoryNewDataWorkflowTask();
      task.setInvestigationId(investigation.getInvestigationId());
      task.setConfiguration(configuration);
      task.setExecutionEnvironment(environment);
      task.setExecutorId(ctx.getUserId().getStringRepresentation());
      task.setExecutorGroupId(pGroupId);
      task.setPredecessor(predecessor);
      task.setObjectTransferMapAsObject(PropertiesUtil.propertiesFromString(pInputObjectMap));
      task.setExecutionSettingsAsObject(PropertiesUtil.propertiesFromString(pExecutionSettings));
      task.setApplicationArguments(pApplicationArguments);
      task.setLastUpdate(new Date());
      task.setStatus(DataWorkflowTask.TASK_STATUS.SCHEDULED);
      LOGGER.debug("Writing DataWorkflowTask to database.");
      DataWorkflowTask result = mdm.save(task);
      LOGGER.debug("DataWorkflowTask successfully saved.");
      return createObjectGraphStream(DataWorkflowTaskWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new DataWorkflowTaskWrapper(result));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Unauthorized to create DataWorkflowTask or to obtain one or more configuration entities.", ex);
      throw new WebApplicationException(401);
    } catch (IOException ex) {
      LOGGER.error("Failed to deserialize either object-view or execution settings objects.", ex);
      throw new WebApplicationException(400);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput getTaskConfigurationById(String groupId, Long id, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
    mdm.setAuthorizationContext(ctx);
    try {
      LOGGER.debug("Getting DataWorkflowTaskConfiguration by id {}.", id);
      DataWorkflowTaskConfiguration result = mdm.find(DataWorkflowTaskConfiguration.class, id);
      if (result == null) {
        LOGGER.error("No DataWorkflowTaskConfiguration found for id " + id + ".");
        throw new WebApplicationException(404);
      }

      return createObjectGraphStream(DataWorkflowTaskConfigurationWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new DataWorkflowTaskConfigurationWrapper(result));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Context " + ctx + " is not authorized to get DataWorkflowTaskConfiguration with id " + id + ".", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput getAllTaskConfigurations(String groupId, Integer first, Integer results, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
    mdm.setAuthorizationContext(ctx);
    try {
      LOGGER.debug("Getting all DataWorkflowTaskConfigurations.");
      List<DataWorkflowTaskConfiguration> result = mdm.findResultList("SELECT c FROM DataWorkflowTaskConfiguration c", first, results);
      return createObjectGraphStream(DataWorkflowTaskConfigurationWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new DataWorkflowTaskConfigurationWrapper(result));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Context " + ctx + " is not authorized to list DataWorkflowTaskConfigurations.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput getTaskConfigurationCount(String groupId, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
    mdm.setAuthorizationContext(ctx);
    try {
      LOGGER.debug("Getting DataWorkflowTaskConfigurations count.");
      int result = ((Number) mdm.findSingleResult("SELECT COUNT(c) FROM DataWorkflowTaskConfiguration c")).intValue();
      return createObjectGraphStream(DataWorkflowTaskConfigurationWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new DataWorkflowTaskConfigurationWrapper(result));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Context " + ctx + " is not authorized to get DataWorkflowTaskConfiguration count.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput getExecutionEnvironmentConfigurationById(String groupId, Long id, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
    mdm.setAuthorizationContext(ctx);
    try {
      LOGGER.debug("Getting ExecutionEnvironmentConfiguration by id {}.", id);
      ExecutionEnvironmentConfiguration result = mdm.find(ExecutionEnvironmentConfiguration.class, id);
      if (result == null) {
        LOGGER.error("No ExecutionEnvironmentConfiguration found for id " + id + ".");
        throw new WebApplicationException(404);
      }

      return createObjectGraphStream(ExecutionEnvironmentConfigurationWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new ExecutionEnvironmentConfigurationWrapper(result));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Context " + ctx + " is not authorized to get ExecutionEnvironmentConfiguration with id " + id + ".", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput getAllExecutionEnvironmentConfigurations(String groupId, Integer first, Integer results, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
    mdm.setAuthorizationContext(ctx);
    try {
      LOGGER.debug("Getting all ExecutionEnvironmentConfiguration.");
      List<ExecutionEnvironmentConfiguration> result = mdm.findResultList("SELECT e FROM ExecutionEnvironmentConfiguration e", first, results);
      return createObjectGraphStream(ExecutionEnvironmentConfigurationWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new ExecutionEnvironmentConfigurationWrapper(result));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Context " + ctx + " is not authorized to list ExecutionEnvironmentConfigurations.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput getExecutionEnvironmentConfigurationCount(String groupId, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
    mdm.setAuthorizationContext(ctx);
    try {
      LOGGER.debug("Getting ExecutionEnvironmentConfiguration count.");
      int result = ((Number) mdm.findSingleResult("SELECT COUNT(e) FROM ExecutionEnvironmentConfiguration e")).intValue();
      return createObjectGraphStream(ExecutionEnvironmentConfigurationWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new ExecutionEnvironmentConfigurationWrapper(result));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Context " + ctx + " is not authorized to get ExecutionEnvironmentConfiguration count.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public Response checkService() {
    ServiceStatus status;
    try {
      LOGGER.debug("Doing service check by getting one task");
      List<DataWorkflowTask> tasks = DataWorkflowPersistenceImpl.getSingleton(AuthorizationContext.factorySystemContext()).getAllTasks(0, 1);
      LOGGER.debug("Service check using task count returned {} tasks.", tasks.size());
      status = ServiceStatus.OK;
    } catch (Throwable t) {
      LOGGER.error("Obtaining task count returned an error. Service status is set to ERROR", t);
      status = ServiceStatus.ERROR;
    }
    return Response.status(200).entity(new CheckServiceResponse("DataWorkflow", status)).build();
  }

}
