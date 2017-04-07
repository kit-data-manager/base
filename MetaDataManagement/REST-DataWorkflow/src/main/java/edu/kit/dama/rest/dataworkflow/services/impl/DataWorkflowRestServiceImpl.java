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
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.base.Investigation;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.rest.base.types.CheckServiceResponse;
import edu.kit.dama.rest.base.types.ServiceStatus;
import edu.kit.dama.rest.dataworkflow.services.interfaces.IDataWorkflowRestService;
import edu.kit.dama.rest.dataworkflow.types.ExecutionEnvironmentConfigurationWrapper;
import edu.kit.dama.rest.dataworkflow.types.DataWorkflowTaskWrapper;
import edu.kit.dama.rest.util.RestUtils;
import edu.kit.dama.util.PropertiesUtil;
import edu.kit.dama.mdm.dataworkflow.ExecutionEnvironmentConfiguration;
import edu.kit.dama.mdm.dataworkflow.DataWorkflowTask;
import edu.kit.dama.mdm.dataworkflow.DataWorkflowTaskConfiguration;
import edu.kit.dama.dataworkflow.impl.DataWorkflowPersistenceImpl;
import edu.kit.dama.mdm.core.jpa.MetaDataManagerJpa;
import edu.kit.dama.mdm.dataworkflow.interfaces.IDefaultDataWorkflowConfiguration;
import edu.kit.dama.mdm.dataworkflow.interfaces.IDefaultDataWorkflowTask;
import edu.kit.dama.mdm.dataworkflow.interfaces.IDefaultExecutionEnvironment;
import edu.kit.dama.mdm.dataworkflow.tools.DataWorkflowTaskSecureQueryHelper;
import edu.kit.dama.mdm.tools.DigitalObjectSecureQueryHelper;
import edu.kit.dama.rest.base.IEntityWrapper;
import edu.kit.dama.rest.dataworkflow.types.DataWorkflowTaskConfigurationWrapper;
import edu.kit.dama.util.Constants;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    public IEntityWrapper<? extends IDefaultDataWorkflowTask> getTaskById(String groupId, Long id, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(ctx);
        try {
            LOGGER.debug("Getting DataWorkflow task by id {}.", id);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DataWorkflowTask.default");

            DataWorkflowTask result = mdm.find(DataWorkflowTask.class, id);
            if (result == null) {
                LOGGER.error("No DataWorkflowTask found for id " + id + ".");
                throw new WebApplicationException(404);
            }
            return new DataWorkflowTaskWrapper(result);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Context " + ctx + " is not authorized to get a DataWorkflowTask for id " + id, ex);
            throw new WebApplicationException(401);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultDataWorkflowTask> getAllTasks(String groupId, Integer first, Integer results, HttpContext hc) {
        if (results > Constants.REST_MAX_PAGE_SIZE) {
            LOGGER.error("BAD_REQUEST. Result count {} is larger than max. page size {}", results, Constants.REST_MAX_PAGE_SIZE);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(ctx);
        try {
            LOGGER.debug("Getting accessible task list.");
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DataWorkflowTask.default");
            return new DataWorkflowTaskWrapper(mdm.findResultList("SELECT t FROM DataWorkflowTask t", DataWorkflowTask.class
            ));
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Context " + ctx + " is not authorized to get a list of DataWorkflowTasks.", ex);
            throw new WebApplicationException(401);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultDataWorkflowTask> getTaskCount(String groupId, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(ctx);
        try {
            LOGGER.debug("Getting accessible task count.");
            return new DataWorkflowTaskWrapper((int) mdm.findSingleResult("SELECT COUNT(t) FROM DataWorkflowTask t", Number.class).intValue());
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Context " + ctx + " is not authorized to get DataWorkflowTask count.", ex);
            throw new WebApplicationException(401);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultDataWorkflowTask> createTask(String pGroupId, Long pInvestigationId, Long pConfigurationId, Long pEnvironmentId, Long pPredecessorId,
            String pInputObjectMap, String pExecutionSettings, String pApplicationArguments, HttpContext hc) {
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
                mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "Investigation.simple");
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
                mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DataWorkflowTaskConfiguration.simple");
                configuration = mdm.find(DataWorkflowTaskConfiguration.class, pConfigurationId);
                if (configuration == null) {
                    LOGGER.error("No DataWorkflowTaskConfiguration found for id " + pConfigurationId + ". Unable to continue.");
                    throw new WebApplicationException(404);
                }
            }

            mdm.removeProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH);

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
                mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DataWorkflowTask.simple");
                predecessor = new DataWorkflowTaskSecureQueryHelper().getDataWorkflowTaskById(pPredecessorId, mdm, ctx);
            } else {
                LOGGER.debug("No predecessor task provided");
            }

            mdm.removeProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH);
            Properties inputObjectViewMapping = new Properties();
            LOGGER.debug("Obtaining input object-view mapping.");
            try {
                JSONArray array = new JSONArray(pInputObjectMap);
                LOGGER.debug("Parsing mappings from JSON array.");
                for (int i = 0; i < array.length(); i++) {
                    LOGGER.debug("Getting mapping '{}'", i);
                    JSONObject objectViewMapping = array.getJSONObject(i);
                    String objectId = (String) objectViewMapping.keys().next();
                    String viewName = objectViewMapping.getString(objectId);
                    LOGGER.debug("Adding mapping {}:{} to properties.", objectId, viewName);
                    inputObjectViewMapping.put(objectId, viewName);
                }
            } catch (JSONException ex) {
                LOGGER.debug("Parsing mapping from JSON failed. Parsing mapping from serialized properties.");
                inputObjectViewMapping = PropertiesUtil.propertiesFromString(pInputObjectMap);
            }

            //this is necessary to support baseId and digitalObjectIdentifier mappings. Later on, digitalObjectIdentifiers must be used, e.g. for download of the input data.
            LOGGER.debug("Transforming object-view mapping to digitalObjectId-based representation.");
            Properties fixedInputObjectViewMapping = new Properties();
            Enumeration<Object> keys = inputObjectViewMapping.keys();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                DigitalObjectId id = getObjectIdentifierForId(key, ctx);
                fixedInputObjectViewMapping.put(id.getStringRepresentation(), inputObjectViewMapping.get(key));
            }

            Properties executionSettingsMapping = new Properties();
            LOGGER.debug("Obtaining execution settings mapping.");
            try {
                JSONArray array = new JSONArray(pExecutionSettings);
                LOGGER.debug("Parsing mappings from JSON array.");
                for (int i = 0; i < array.length(); i++) {
                    LOGGER.debug("Getting mapping '{}'", i);
                    JSONObject objectViewMapping = array.getJSONObject(i);
                    String objectId = (String) objectViewMapping.keys().next();
                    String viewName = objectViewMapping.getString(objectId);
                    LOGGER.debug("Adding mapping {}:{} to properties.", objectId, viewName);
                    executionSettingsMapping.put(objectId, viewName);
                }
            } catch (JSONException ex) {
                LOGGER.debug("Parsing mapping from JSON failed. Parsing mapping from serialized properties.");
                executionSettingsMapping = PropertiesUtil.propertiesFromString(pExecutionSettings);
            }

            DataWorkflowTask task = DataWorkflowTask.factoryNewDataWorkflowTask();
            task.setInvestigationId(investigation.getInvestigationId());
            task.setConfiguration(configuration);
            task.setExecutionEnvironment(environment);
            task.setExecutorId(ctx.getUserId().getStringRepresentation());
            task.setExecutorGroupId(pGroupId);
            task.setPredecessor(predecessor);
            task.setObjectViewMapAsObject(inputObjectViewMapping);
            task.setExecutionSettingsAsObject(executionSettingsMapping);
            task.setApplicationArguments(pApplicationArguments);
            task.setLastUpdate(new Date());
            task.setStatus(DataWorkflowTask.TASK_STATUS.SCHEDULED);
            LOGGER.debug("Writing DataWorkflowTask to database.");
            DataWorkflowTask result = mdm.save(task);
            LOGGER.debug("DataWorkflowTask successfully saved.");
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DataWorkflowTask.default");

            return new DataWorkflowTaskWrapper(mdm.find(DataWorkflowTask.class, result.getId()));

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
    public IEntityWrapper<? extends IDefaultDataWorkflowConfiguration> getTaskConfigurationById(String groupId, Long id, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(ctx);
        try {
            LOGGER.debug("Getting DataWorkflowTaskConfiguration by id {}.", id);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DataWorkflowTaskConfiguration.default");
            DataWorkflowTaskConfiguration result = mdm.find(DataWorkflowTaskConfiguration.class, id);
            if (result == null) {
                LOGGER.error("No DataWorkflowTaskConfiguration found for id " + id + ".");
                throw new WebApplicationException(404);
            }

            return new DataWorkflowTaskConfigurationWrapper(result);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Context " + ctx + " is not authorized to get DataWorkflowTaskConfiguration with id " + id + ".", ex);
            throw new WebApplicationException(401);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultDataWorkflowConfiguration> getAllTaskConfigurations(String groupId, Integer first, Integer results, HttpContext hc) {
        if (results > Constants.REST_MAX_PAGE_SIZE) {
            LOGGER.error("BAD_REQUEST. Result count {} is larger than max. page size {}", results, Constants.REST_MAX_PAGE_SIZE);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(ctx);
        try {
            LOGGER.debug("Getting all DataWorkflowTaskConfigurations.");
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DataWorkflowTaskConfiguration.default");
            return new DataWorkflowTaskConfigurationWrapper(mdm.findResultList("SELECT c FROM DataWorkflowTaskConfiguration c", DataWorkflowTaskConfiguration.class, first, results));
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Context " + ctx + " is not authorized to list DataWorkflowTaskConfigurations.", ex);
            throw new WebApplicationException(401);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultDataWorkflowConfiguration> getTaskConfigurationCount(String groupId, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(ctx);
        try {
            LOGGER.debug("Getting DataWorkflowTaskConfigurations count.");
            return new DataWorkflowTaskConfigurationWrapper(((Number) mdm.findSingleResult("SELECT COUNT(c) FROM DataWorkflowTaskConfiguration c")).intValue());
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Context " + ctx + " is not authorized to get DataWorkflowTaskConfiguration count.", ex);
            throw new WebApplicationException(401);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultExecutionEnvironment> getExecutionEnvironmentConfigurationById(String groupId, Long id, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(ctx);
        try {
            LOGGER.debug("Getting ExecutionEnvironmentConfiguration by id {}.", id);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "ExecutionEnvironmentConfiguration.default");
            ExecutionEnvironmentConfiguration result = mdm.find(ExecutionEnvironmentConfiguration.class, id);
            if (result == null) {
                LOGGER.error("No ExecutionEnvironmentConfiguration found for id " + id + ".");
                throw new WebApplicationException(404);
            }

            return new ExecutionEnvironmentConfigurationWrapper(result);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Context " + ctx + " is not authorized to get ExecutionEnvironmentConfiguration with id " + id + ".", ex);
            throw new WebApplicationException(401);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultExecutionEnvironment> getAllExecutionEnvironmentConfigurations(String groupId, Integer first, Integer results, HttpContext hc) {
        if (results > Constants.REST_MAX_PAGE_SIZE) {
            LOGGER.error("BAD_REQUEST. Result count {} is larger than max. page size {}", results, Constants.REST_MAX_PAGE_SIZE);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(ctx);
        try {
            LOGGER.debug("Getting all ExecutionEnvironmentConfiguration.");
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "ExecutionEnvironmentConfiguration.default");
            return new ExecutionEnvironmentConfigurationWrapper(mdm.findResultList("SELECT e FROM ExecutionEnvironmentConfiguration e", ExecutionEnvironmentConfiguration.class, first, results));
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Context " + ctx + " is not authorized to list ExecutionEnvironmentConfigurations.", ex);
            throw new WebApplicationException(401);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultExecutionEnvironment> getExecutionEnvironmentConfigurationCount(String groupId, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(ctx);
        try {
            LOGGER.debug("Getting ExecutionEnvironmentConfiguration count.");
            int result = ((Number) mdm.findSingleResult("SELECT COUNT(e) FROM ExecutionEnvironmentConfiguration e")).intValue();
            return new ExecutionEnvironmentConfigurationWrapper(result);
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

    /**
     * Get a digital object for the provided id. The id might be the string
     * representation of the numeric id or the unique identifier of the digital
     * object. This helper method will take care of the transformation.
     */
    private DigitalObjectId getObjectIdentifierForId(String pId, IAuthorizationContext pCtx) {
        if (pId == null) {
            LOGGER.error("Argument pId must not be null.");
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        String s_id = pId.trim();
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(pCtx);
        try {
            String objectId = s_id;
            try {
                LOGGER.debug("Trying to parse provided object id {} as long value", s_id);
                long id = Long.parseLong(s_id);
                LOGGER.debug("Parsing to long succeeded. Obtaining string identifier for long id.");
                s_id = mdm.findSingleResult("SELECT o.digitalObjectIdentifier FROM DigitalObject o WHERE o.baseId=" + id, String.class);
            } catch (NumberFormatException ex) {
                LOGGER.debug("Parsing to long failed, expecting string identifier");
            }

            if (new DigitalObjectSecureQueryHelper().objectByIdentifierExists(objectId, mdm, pCtx)) {
                LOGGER.debug("Returning digital object id {}", objectId);
                return new DigitalObjectId(objectId);
            } else {
                LOGGER.error("No DigitalObject accessible by context " + pCtx + " found for provided id " + pId + ".");
                throw new WebApplicationException(404);
            }
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Context " + pCtx.toString() + " is not authorized to access object by id " + s_id);
            throw new WebApplicationException(401);
        } catch (EntityNotFoundException ex) {
            LOGGER.error("No DigitalObject accessible by context " + pCtx + " found for provided id " + pId + ".");
            throw new WebApplicationException(404);
        } finally {
            mdm.close();
        }
    }
}
