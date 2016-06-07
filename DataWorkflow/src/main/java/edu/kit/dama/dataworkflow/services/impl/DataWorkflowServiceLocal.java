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
package edu.kit.dama.dataworkflow.services.impl;

import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.dataworkflow.exceptions.UnsupportedTaskException;
import edu.kit.dama.mdm.base.Investigation;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.authorization.SecureMetaDataManager;
import edu.kit.dama.mdm.core.jpa.MetaDataManagerJpa;
import edu.kit.dama.mdm.dataworkflow.DataWorkflowTask;
import edu.kit.dama.mdm.dataworkflow.DataWorkflowTaskConfiguration;
import edu.kit.dama.mdm.dataworkflow.ExecutionEnvironmentConfiguration;
import edu.kit.dama.mdm.dataworkflow.tools.DataWorkflowTaskSecureQueryHelper;
import edu.kit.dama.util.PropertiesUtil;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public final class DataWorkflowServiceLocal {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataWorkflowServiceLocal.class);
    private static DataWorkflowServiceLocal SINGLETON;

    public static synchronized DataWorkflowServiceLocal getSingleton() {
        if (SINGLETON == null) {
            SINGLETON = new DataWorkflowServiceLocal();
        }
        return SINGLETON;
    }

    public DataWorkflowTask getTaskById(Long id, IAuthorizationContext ctx) throws UnauthorizedAccessAttemptException {
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Getting DataWorkflow task by id {}.", id);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DataWorkflowTaskConfiguration.default");
            return new DataWorkflowTaskSecureQueryHelper().getDataWorkflowTaskById(id, mdm, ctx);
        } finally {
            mdm.close();
        }
    }

    public List<DataWorkflowTask> getAllTasks(String groupId, Integer first, Integer results, IAuthorizationContext ctx) throws UnauthorizedAccessAttemptException {
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        mdm.setAuthorizationContext(ctx);
        try {
            LOGGER.debug("Getting accessible task list.");
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DataWorkflowTaskConfiguration.default");
            if (groupId != null) {
                return new DataWorkflowTaskSecureQueryHelper().getAllTasks("o.executorGroupId='" + groupId + "'", mdm, first, results, ctx);
            } else {
                return new DataWorkflowTaskSecureQueryHelper().getAllTasks(null, mdm, first, results, ctx);
            }
        } finally {
            mdm.close();
        }
    }

    public Integer getTaskCount(String groupId, IAuthorizationContext ctx) throws UnauthorizedAccessAttemptException {
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Getting accessible task count.");
            if (groupId != null) {
                return new DataWorkflowTaskSecureQueryHelper().getReadableResourceCount(mdm, "o.executorGroupId='" + groupId + "'", ctx);
            } else {
                return new DataWorkflowTaskSecureQueryHelper().getReadableResourceCount(mdm, ctx);
            }
        } finally {
            mdm.close();
        }
    }

    public DataWorkflowTask createTask(Long pInvestigationId, Long pConfigurationId, Long pEnvironmentId, String pInputObjectMap, IAuthorizationContext ctx) throws UnauthorizedAccessAttemptException, EntityNotFoundException, UnsupportedTaskException {
        return createTask(pInvestigationId, pConfigurationId, pEnvironmentId, pInputObjectMap, null, ctx);
    }

    public DataWorkflowTask createTask(Long pInvestigationId, Long pConfigurationId, Long pEnvironmentId, String pInputObjectMap, String pApplicationArguments, IAuthorizationContext ctx) throws UnauthorizedAccessAttemptException, EntityNotFoundException, UnsupportedTaskException {
        return createTask(pInvestigationId, pConfigurationId, pEnvironmentId, pInputObjectMap, null, pApplicationArguments, ctx);
    }

    public DataWorkflowTask createTask(Long pInvestigationId, Long pConfigurationId, Long pEnvironmentId, String pInputObjectMap, String pExecutionSettings, String pApplicationArguments, IAuthorizationContext ctx) throws UnauthorizedAccessAttemptException, EntityNotFoundException, UnsupportedTaskException {
        return createTask(pInvestigationId, pConfigurationId, pEnvironmentId, null, pInputObjectMap, pExecutionSettings, pApplicationArguments, ctx);
    }

    public DataWorkflowTask createTask(Long pInvestigationId, Long pConfigurationId, Long pEnvironmentId, Long pPredecessorId, String pInputObjectMap, String pExecutionSettings, String pApplicationArguments, IAuthorizationContext ctx) throws UnauthorizedAccessAttemptException, EntityNotFoundException, UnsupportedTaskException {
        return createTask(null, pInvestigationId, pConfigurationId, pEnvironmentId, pPredecessorId, pInputObjectMap, pExecutionSettings, pApplicationArguments, ctx);
    }

    public DataWorkflowTask createTask(String pGroupId, Long pInvestigationId, Long pConfigurationId, Long pEnvironmentId, Long pPredecessorId,
            String pInputObjectMap, String pExecutionSettings, String pApplicationArguments, IAuthorizationContext ctx) throws UnauthorizedAccessAttemptException, EntityNotFoundException, UnsupportedTaskException {
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);

        if (pInvestigationId == null) {
            throw new IllegalArgumentException("Argument pInvestigationId must not be null.");
        }
        if (pConfigurationId == null) {
            throw new IllegalArgumentException("Argument pConfigurationId must not be null.");
        }
        if (pEnvironmentId == null) {
            throw new IllegalArgumentException("Argument pEnvironmentId must not be null.");
        }
        DataWorkflowTask task = DataWorkflowTask.factoryNewDataWorkflowTask();
        //handle all properties (de-)serialization here in order to avoid propagating the IOException
        try {
            task.setObjectViewMapAsObject(PropertiesUtil.propertiesFromString(pInputObjectMap));
        } catch (IOException ex) {
            throw new IllegalArgumentException("Argument pInputObjectMap cannot be deserialized.", ex);
        }
        try {
            task.setExecutionSettingsAsObject(PropertiesUtil.propertiesFromString(pExecutionSettings));
        } catch (IOException ex) {
            throw new IllegalArgumentException("Argument pExecutionSettings cannot be deserialized.", ex);
        }

        DataWorkflowTask predecessor = null;
        try {
            LOGGER.debug("Obtaining Investigation for id {}", pInvestigationId);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "Investigation.simple");
            Investigation investigation = mdm.find(Investigation.class, pInvestigationId);
            if (investigation == null) {
                throw new EntityNotFoundException("No Investigation found for id " + pInvestigationId + ". Unable to continue.");
            }

            LOGGER.debug("Obtaining DataWorkflowTaskConfiguration for id {}", pConfigurationId);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DataWorkflowTaskConfiguration.simple");
            DataWorkflowTaskConfiguration configuration = mdm.find(DataWorkflowTaskConfiguration.class, pConfigurationId);
            if (configuration == null) {
                throw new EntityNotFoundException("No DataWorkflowTaskConfiguration found for id " + pConfigurationId + ". Unable to continue.");
            }

            mdm.removeProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH);

            LOGGER.debug("Obtaining ExecutionEnvironmentConfiguration for id {}", pEnvironmentId);
            ExecutionEnvironmentConfiguration environment = mdm.find(ExecutionEnvironmentConfiguration.class, pEnvironmentId);
            if (environment == null) {
                throw new EntityNotFoundException("No ExecutionEnvironmentConfiguration found for id " + pEnvironmentId + ". Unable to continue.");
            }

            LOGGER.debug("Checking if environment with id {} is capable for executing task configuration with id {}", pEnvironmentId, pConfigurationId);
            if (!environment.canExecute(configuration)) {
                throw new UnsupportedTaskException("Execution ExecutionEnvironment with id " + pEnvironmentId + " is not capable for executing DataWorkflowTask for configuration with id " + pConfigurationId + ". EvironmentProperies do not fit task requirements.");
            }

            if (pPredecessorId != null) {
                LOGGER.debug("Checking predecessor task for id {}", pPredecessorId);
                mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DataWorkflowTask.simple");
                predecessor = new DataWorkflowTaskSecureQueryHelper().getDataWorkflowTaskById(pPredecessorId, mdm, ctx);
            } else {
                LOGGER.debug("No predecessor task provided");
            }
            mdm.removeProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH);

            task.setInvestigationId(investigation.getInvestigationId());
            task.setConfiguration(configuration);
            task.setExecutionEnvironment(environment);
            task.setExecutorId(ctx.getUserId().getStringRepresentation());
            if (pGroupId == null) {
                task.setExecutorGroupId(ctx.getGroupId().getStringRepresentation());
            } else {
                task.setExecutorGroupId(pGroupId);
            }
            task.setPredecessor(predecessor);
            task.setApplicationArguments(pApplicationArguments);
            task.setLastUpdate(new Date());
            task.setStatus(DataWorkflowTask.TASK_STATUS.SCHEDULED);
            LOGGER.debug("Writing DataWorkflowTask to database.");
            DataWorkflowTask result = mdm.save(task);
            LOGGER.debug("DataWorkflowTask successfully saved.");
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DataWorkflowTaskConfiguration.default");
            return mdm.find(DataWorkflowTask.class, result.getId());
        } finally {
            mdm.close();
        }
    }

    public DataWorkflowTaskConfiguration getTaskConfigurationById(Long id, IAuthorizationContext ctx) throws UnauthorizedAccessAttemptException {
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Getting DataWorkflowTaskConfiguration by id {}.", id);
            return mdm.find(DataWorkflowTaskConfiguration.class, id);
        } finally {
            mdm.close();
        }
    }

    public List<DataWorkflowTaskConfiguration> getAllTaskConfigurations(String pGroupId, Integer first, Integer results, IAuthorizationContext ctx) throws UnauthorizedAccessAttemptException {
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Getting all DataWorkflowTaskConfigurations.");
            if (pGroupId != null) {
                return mdm.findResultList("SELECT c FROM DataWorkflowTaskConfiguration c WHERE c.groupId='" + pGroupId + "'", DataWorkflowTaskConfiguration.class, first, results);
            } else {
                return mdm.findResultList("SELECT c FROM DataWorkflowTaskConfiguration c", DataWorkflowTaskConfiguration.class, first, results);
            }
        } finally {
            mdm.close();
        }
    }

    public Integer getTaskConfigurationCount(String pGroupId, IAuthorizationContext ctx) throws UnauthorizedAccessAttemptException {
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Getting DataWorkflowTaskConfigurations count.");
            if (pGroupId != null) {
                return ((Number) mdm.findSingleResult("SELECT COUNT(c) FROM DataWorkflowTaskConfiguration c WHERE c.groupId='" + pGroupId + "'")).intValue();
            } else {
                return ((Number) mdm.findSingleResult("SELECT COUNT(c) FROM DataWorkflowTaskConfiguration c")).intValue();
            }
        } finally {
            mdm.close();
        }
    }

    public ExecutionEnvironmentConfiguration getExecutionEnvironmentConfigurationById(Long id, IAuthorizationContext ctx) throws UnauthorizedAccessAttemptException {
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Getting ExecutionEnvironmentConfiguration by id {}.", id);
            return mdm.find(ExecutionEnvironmentConfiguration.class, id);
        } finally {
            mdm.close();
        }
    }

    public List<ExecutionEnvironmentConfiguration> getAllExecutionEnvironmentConfigurations(String groupId, Integer first, Integer results, IAuthorizationContext ctx) throws UnauthorizedAccessAttemptException {
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Getting all ExecutionEnvironmentConfiguration.");
            if (groupId != null) {
                return mdm.findResultList("SELECT e FROM ExecutionEnvironmentConfiguration e WHERE e.groupId='" + groupId + "'", ExecutionEnvironmentConfiguration.class, first, results);
            } else {
                return mdm.findResultList("SELECT e FROM ExecutionEnvironmentConfiguration e", ExecutionEnvironmentConfiguration.class, first, results);
            }
        } finally {
            mdm.close();
        }
    }

    public Integer getExecutionEnvironmentConfigurationCount(String groupId, IAuthorizationContext ctx) throws UnauthorizedAccessAttemptException {
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Getting ExecutionEnvironmentConfiguration count.");
            if (groupId != null) {
                return ((Number) mdm.findSingleResult("SELECT COUNT(e) FROM ExecutionEnvironmentConfiguration e WHERE e.groupId='" + groupId + "'")).intValue();

            } else {
                return ((Number) mdm.findSingleResult("SELECT COUNT(e) FROM ExecutionEnvironmentConfiguration e")).intValue();
            }
        } finally {
            mdm.close();
        }
    }
}
